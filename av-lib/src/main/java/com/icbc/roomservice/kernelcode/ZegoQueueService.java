package com.icbc.roomservice.kernelcode;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.icbc.roomservice.base.BaseActivity;
import com.zego.queue.CustomerDelegate;
import com.zego.queue.CustomerEnqueueInfo;
import com.zego.queue.Queue;
import com.zego.queue.QueueDelegate;
import com.zego.queue.QueueInfo;
import com.zego.queue.UserType;
import com.icbc.roomservice.kernelcode.utils.LogToFileUtils;
import com.icbc.roomservice.kernelcode.block.FetchTokenBlock;
import com.icbc.roomservice.kernelcode.block.LoginBlock;
import com.icbc.roomservice.kernelcode.block.OptionalBlock;
import com.icbc.roomservice.kernelcode.callback.IZegoQueueServiceConnectEventCallback;
import com.icbc.roomservice.kernelcode.callback.IZegoQueueServiceFetchTokenCallback;
import com.icbc.roomservice.kernelcode.callback.IZegoQueueServiceQueueCallback;
import com.icbc.roomservice.kernelcode.callback.IZegoQueueServiceQueueListCallback;
import com.icbc.roomservice.kernelcode.callback.IZegoQueueServiceRecvCommand;
import com.icbc.roomservice.kernelcode.callback.IZegoQueueServiceVideoCallback;
import com.icbc.roomservice.kernelcode.event.ZegoQueueConnectEvent;
import com.icbc.roomservice.kernelcode.utils.ThreadUtils;
import com.icbc.roomservice.kernelcode.utils.ZegoHelper;
import com.zego.zegoavkit2.soundlevel.IZegoSoundLevelCallback;
import com.zego.zegoavkit2.soundlevel.ZegoSoundLevelMonitor;
import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.callback.IZegoAVEngineCallback;
import com.zego.zegoliveroom.callback.IZegoCustomCommandCallback;
import com.zego.zegoliveroom.callback.IZegoLivePlayerCallback;
import com.zego.zegoliveroom.callback.IZegoLivePublisherCallback;
import com.zego.zegoliveroom.callback.IZegoLoginCompletionCallback;
import com.zego.zegoliveroom.callback.IZegoRoomCallback;
import com.zego.zegoliveroom.callback.im.IZegoIMCallback;
import com.zego.zegoliveroom.callback.im.IZegoRoomMessageCallback;
import com.zego.zegoliveroom.constants.ZegoAvConfig;
import com.zego.zegoliveroom.constants.ZegoConstants;
import com.zego.zegoliveroom.constants.ZegoVideoViewMode;
import com.zego.zegoliveroom.entity.AuxData;
import com.zego.zegoliveroom.entity.ZegoPlayStreamQuality;
import com.zego.zegoliveroom.entity.ZegoPublishStreamQuality;
import com.zego.zegoliveroom.entity.ZegoRoomMessage;
import com.zego.zegoliveroom.entity.ZegoStreamInfo;
import com.zego.zegoliveroom.entity.ZegoUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class ZegoQueueService implements IZegoRoomCallback, IZegoLivePlayerCallback,IZegoLoginCompletionCallback,
        IZegoLivePublisherCallback, ZegoApiManager.ActiveChangedCallback, IZegoAVEngineCallback, IZegoCustomCommandCallback{
    // 客户无效number
    private static final int ZEGO_QUEUE_CUSTOMER_NO_INVALID = -1;
    private static final int QUIT_VIDEO_CMD_SEND_COUNT = 3;

    //-----------------设备错误常量------------------------//
    private final static int DEVICE_ERROR_MIC_DENIED = 1;
    private final static int DEVICE_ERROR_CAMERA_DENIED = 2;
    private final static int DEVICE_ERROR_IN_CALL = 3;

    //房间内消息
    private ZegoRoomMessage[] roomMessages;

    /**
     * 坐席队列信息
     */
    private ArrayList<QueueInfo> mqueueInfos;

    /**
     * 视频加载错误码
     */
    private int VideoErrorCode=0;
    public int getVideoErrorCode(){return VideoErrorCode;}

    /**
     * 用户对象
     */
    private ZegoUser mZegoUser;

    /**
     * 坐席用户对象
     */
    private ZegoUser mStaffZegoUser;

    /**
     * ZegoLiveRoom 对象
     */
    private ZegoLiveRoom mZegoLiveRoom;

    /**
     * 排队队列
     */
    private Queue mQueue;

    /**
     * 排队的队列ID
     */
    private String mQueueID;

    /**
     * 用户额外信息，由于业务方根据情况传输用户信息，json字符串
     */
    private String mUserExtraInfo;

    /**
     * 客户vip优先级，0-普通客户，1-9为vip等级客户
     */
    private int mPriority;

    /**
     * 服务端返回的房间ID，用于之后连麦使用
     */
    private String mRoomID;

    /**
     * 当前排队的位置
     */
    private int mCustomerIndex;

    /**
     * 预览View
     */
    private View mPreviewView;
    /**
     * 播放View
     */
    private View mPlayView;

    /**
     * 服务器返回的房间流
     */
    private ZegoStreamInfo mStuffStreamInfo;

    /**
     * 信令发送失败后还需重试都次数
     */
    private int mCmdRetryCount;

    //-----------------相关回调值------------------//

    /**
     * 刷新token 回调
     */
    private IZegoQueueServiceFetchTokenCallback mFetchTokenCallback;

    /**
     * 列表连接状态改变回调
     */
    private IZegoQueueServiceConnectEventCallback mConnectEventCallback;

    /**
     * 队列列表回调
     */
    private IZegoQueueServiceQueueListCallback mQueueListCallback;

    /**
     * 队列状态 回调
     */
    private IZegoQueueServiceQueueCallback mQueueCallback;

    /**
     * 视频相关回调
     */
    private IZegoQueueServiceVideoCallback mVideoCallback;

    //-----------------相关状态值------------------//


    //--------------------登录异常重试代码块-----------------//
    private LinkedList<LoginBlock> mLoginBlocks;

    /**
     * 是否登录队列服务，不在reset方法中重置，因为reset仅仅是重置除登录队列服务状态外的其他状态
     * 仅仅在onConnectState状态改变到情况下回调
     */
    private boolean isLoginQueue;

    /**
     * 是否在排队
     */
    private boolean isEnqueue;

    /**
     * 是否答应执行视频见证
     */
    private boolean isAcceptService;

    /**
     * 是否已经被坐席选中
     */
    private boolean isCatch;

    /**
     * 是否登录视频房间
     */
    private boolean isLoginRoom;

    /**
     * 是否在预览中
     */
    private boolean isPreview;

    /**
     * 是否在推流中
     */
    private boolean isPublish;

    /**
     * ZegoSoundLevelMonitor
     */
    private ZegoSoundLevelMonitor mZegoSoundLevelMonitor;

    /**
     * 是否在播放
     */
    private boolean isPlay;

    /**
     * 候机回调和接收消息回调
     */
    private IZegoQueueServiceRecvCommand mIZegoQueueServiceRecvCommand;

    /**
     * 是否断开连接状态
     */
    private boolean isDisconnect;

    /**
     * 坐席主动关闭，业务已经办理成功
     */
    private boolean isVideoFinish;

    /**
     * 是否执行退出逻辑，主要出现在退出信令发送过程中，避免重复调用导致信令重复操作和状态混乱等问题
     */
    private boolean isVideoQuitting;

    private long appid ;
    public String userId ;
    public  String userName;
    public  String tokenUrl;

    private boolean isCcis;
    public  String queueID ;
    public  String userExtraInfo;
    public  int priority;

    private ZegoQueueService() {
    }

    /**
     * @param zegoUser 当前用户
     * @param avConfig 推拉流码率配置
     * @return 初始化 服务模块
     */
    public static ZegoQueueService serviceWithUser(ZegoUser zegoUser, ZegoAvConfig avConfig) {
        ZegoQueueService zegoQueueService = new ZegoQueueService();
        zegoQueueService.mZegoUser = zegoUser;
        zegoQueueService.setup(avConfig);
        return zegoQueueService;
    }

    /**
     * 登录队列服务，现在不用主动执行LoginQueue，所以需要LoginQueue都操作，都主动检查是否LoginQueue，然后执行
     */
    private void loginQueue(LoginBlock loginBlock) {
        LogToFileUtils.write("loginQueue:");
        mLoginBlocks.offer(loginBlock);
        fetchTokenAndLoginQueue();
    }


    public void login(LoginBlock loginBlock, long app_id, String user_dd, String user_name, String token_url){
        appid=app_id;
        userId=user_dd;
        userName=user_name;
        tokenUrl=token_url;
        loginQueue(loginBlock);
    }

 public boolean queueInfosCheck(String queueID){
     LogToFileUtils.write("queueInfosCheck ;"+queueID);
        if(mqueueInfos.size()==0){
            LogToFileUtils.write("queueInfosCheck :has no queueInfo" );
            return false;
        }
        for(QueueInfo queue:mqueueInfos){
            if(queue.getQueueId().equals(queueID)&&queue.getStaffCount()>=1){
                LogToFileUtils.write("queueInfosCheck ,QueueID:"+queueID+" get "+queue.getStaffCount()+" Staff ");
                return true;
            }
        }
     LogToFileUtils.write("queueInfosCheck :has no queueInfo that QueueID like"+queueID );
        return false;
    }

    /**
     * 登出队列服务
     */
    public  boolean logoutQueue() {
        LogToFileUtils.write("logoutQueue");
        try{
            mQueue.userLogout();
            return true;
        }catch(Exception e){
            LogToFileUtils.write("logoutQueue error: " + e.getMessage());
            return false;
        }
    }


    /**
     * 刷新队列列表
     */
    public void fetchQueueList() {
        LogToFileUtils.write("fetchQueueList ;");
        // 如果没有登录，则先登录，后执行刷新队列
        if (!isLoginQueue) {
            loginQueue(new LoginBlock() {
                @Override
                public void onLogin(ResultCode resultCode) {
                    final ResultCode mRsultCode=resultCode;
                    if (resultCode.isSuccess()) {
                        ZegoQueueService.this.fetchQueueList();
                    } else {
                        if (mQueueListCallback != null) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mQueueListCallback.onQueueList(mRsultCode, null);
                                }
                            });
                        }
                    }
                }
            });
        } else {
            fetchQueueListInner();
        }
    }

    /**
     * 加入指定队列（正常队列）
     *
     * @param queueID       队列ID
     * @param userExtraInfo 用户额外信息，由于业务方根据情况传输用户信息，json字符串
     * @return 是否成功调用， 参数传入格式问题
     */
    public boolean enqueue(String queueID, String userExtraInfo) {
        //fetchQueueListInner();
        if(!queueInfosCheck(queueID)){
            if (mQueueCallback != null) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mQueueCallback.onNoStaff();
                    }
                });
            }
            return false;
        }
        return enqueue(queueID, userExtraInfo, 0);
    }


      public void fetchQueueAndEnQueue(String queueID, String userExtraInfo, int priority,boolean isCcis){
          this.queueID=queueID;
          this.userExtraInfo=userExtraInfo;
          this.priority=priority;
          this.isCcis=isCcis;
          fetchQueueListInner();
      }


    /**
     * 加入指定队列（正常队列或者优先队列）
     *
     * @param queueID       队列ID
     * @param userExtraInfo 用户额外信息，由于业务方根据情况传输用户信息，json字符串
     * @param priority      客户vip信息，0-普通客户，1-9为vip等级客户
     * @return 是否成功调用， 参数传入格式问题
     */
    public boolean enqueue(String queueID, String userExtraInfo, int priority) {
//        Log.d(TAG, "enqueue queueID: " + queueID + " userExtraInfo: " + userExtraInfo + " priority: " + priority);//含用户信息，不能打印输出
        LogToFileUtils.write("enqueue queueID ;"+queueID);
        //fetchQueueListInner();
        // 队列ID必须不为null或者空字符串    如果在队列里面
        if (TextUtils.isEmpty(queueID)) {
            return false;
        }
        if(!queueInfosCheck(queueID)){
            if (mQueueCallback != null) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mQueueCallback.onNoStaff();
                    }
                });
            }
            return false;
        }
        mQueueID = queueID;
        mUserExtraInfo = userExtraInfo;
        mPriority = priority;
        enqueue();
        return true;
    }

    /**
     * 进入队列，进入上次尝试进入的队列
     * 这个方法主要是在重试登录执行的，不需在传输queueID
     * 如果没有登录，则按顺序执行 登录 -> 进队
     */
    private void enqueue() {
        LogToFileUtils.write("enqueue isLoginQueue: " + isLoginQueue);
        if (!isLoginQueue) {
            loginQueue(new LoginBlock() {
                @Override
                public void onLogin(ResultCode resultCode) {
                    final ResultCode mRsultCode=resultCode;
                    if (resultCode.isSuccess()) {
                        ZegoQueueService.this.enqueueInner();
                    } else {
                        if (mQueueCallback != null) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mQueueCallback.onEnqueue(mRsultCode, new OptionalBlock(){
                                        @Override
                                        public void execute(boolean option) {
                                            if(option){
                                                ZegoQueueService.this.enqueue();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            });
        } else {
            enqueueInner();
        }
    }

    /**
     * 进入队列，进入上次尝试进入的队列（内部实现）
     */
    private void enqueueInner() {
        LogToFileUtils.write("enqueueInner isEnqueue: " + isEnqueue);
        // 加入排队队列
        mQueue.customerEnter(mQueueID, mUserExtraInfo, mPriority);
    }

    /**
     * 退出当前队列
     */
    public boolean dequeue() {
        LogToFileUtils.write("dequeue");
        try{
            mQueue.customerQuit();
            // 重置状态值
            isEnqueue = false;
            isAcceptService = false;
            isCatch = false;
            mQueueID = null;
            mCustomerIndex = ZEGO_QUEUE_CUSTOMER_NO_INVALID;
            return true;
        }catch(Exception e){
            LogToFileUtils.write("dequeue error: " + e.getMessage());
            return false;
        }
    }

    /**
     * 设置用于视频见证的房间ID
     * <h2>使用注意事项：</h2>
     * 该方法仅用于不使用队列服务方案的场景。
     *
     * @param roomID 用于视频见证的房间ID，只支持数字、字母、下划线，长度为1~127字节，需全局唯一
     */
    public void setRoomID(String roomID) {
        mRoomID = roomID;
        // 该状态指示当前用户是否被坐席抓取，只有被抓取的用户，才可以视频见证。
        isCatch = true;
    }

    /**
     * 开始视频的计时器，超时后直接退出视频通话
     */
    private Timer Videotimer;
    private int VideodelayTime=40000;
    public void setPreviewTimeOutTime(int mVideodelayTime){VideodelayTime=mVideodelayTime;}
    class onVideoTask extends TimerTask {
        @Override
        public void run() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mVideoCallback.onVideoFinish(false);
                }
            });
        }
    }
    /**
     * 开始办理业务（连麦），如果没有ZegoLiveRoom.setUser，则执行 登录 -》 开始办理业务（连麦）
     *
     * @return 是否成功调用，false 由于房间ID为空，或者不是catch状态
     */
    public boolean startVideo() {
        LogToFileUtils.write("startVideo mRoomID: " + mRoomID + " isCatch: " + isCatch);
        if (TextUtils.isEmpty(mRoomID) || !isCatch) {
            return false;
        }
        Videotimer=new Timer();
        Videotimer.schedule(new onVideoTask(),VideodelayTime);

        if (!isLoginQueue) {
            loginQueue(new LoginBlock() {
                @Override
                public void onLogin(ResultCode resultCode) {
                    final ResultCode mRsultCode=resultCode;
                    LogToFileUtils.write("onLogin resultCode:"+resultCode);
                    if (resultCode.isSuccess()) {
                        startVideoInner();
                    } else {
                        if (mVideoCallback != null) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mQueueCallback.onEnqueue(mRsultCode, new OptionalBlock(){
                                        @Override
                                        public void execute(boolean option) {
                                            if(option){
                                                ZegoQueueService.this.startVideo();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            });
        } else {
            startVideoInner();
        }
        return true;
    }

    /**
     * 开始办理业务（连麦），内部实现
     */
    private void startVideoInner() {
        final int deviceError;
        VideoErrorCode=0;
        Context context = ZegoApiManager.getInstance().getApplication();
        //  如果在电话中（听电话，或者电话来了）
        if (ZegoHelper.isInCall(context)) {
            deviceError = DEVICE_ERROR_IN_CALL;
        } else if (!ZegoHelper.checkCameraPermission(context)) {
            // 没有授予摄像头权限
            deviceError = DEVICE_ERROR_CAMERA_DENIED;
        } else if (!ZegoHelper.checkMicrophonePermission(context)) {
            // 没有授予麦克风权限
            deviceError = DEVICE_ERROR_MIC_DENIED;
        } else {
            deviceError = -1;
        }
        LogToFileUtils.write("startVideo deviceError: " + deviceError);
        // 出现相关设备问题
        if (deviceError != -1) {
            if (mVideoCallback != null) {
                VideoErrorCode=101;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoCallback.onVideoError(ResultCode.createResultCodeByDeviceError(deviceError),new OptionalBlock(){
                            @Override
                            public void execute(boolean option) {
                                if(option){
                                    startVideo();
                                }
                            }
                        });
                    }
                });
            }
            return;
        }
        loginRoom();
    }

    /**
     * 结束办理业务
     * 在退出时务必调用该方法，解除对设备的占用
     */
    public boolean quitVideo() {
        LogToFileUtils.write("quitVideo isCatch: " + isCatch);
        try{
            if (!isCatch) {
                return false;
            }
            quitVideoInner(true);
            return true;
        }catch(Exception e){
            LogToFileUtils.write("quitVideo error: " + e.getMessage());
            return false;
        }
    }

    /**
     * 设置视频展示视图
     * 该方法可以在任何时机调用
     *
     * @param previewView 预览展示的视图
     * @param playView    坐席远端视频播放视图
     */
    public void setPreviewView(View previewView, View playView) {
        LogToFileUtils.write("setPreviewView" );
        mPreviewView = previewView;
        mPlayView = playView;

        mZegoLiveRoom.setPreviewView(previewView);
        if (isPlay) {
            mZegoLiveRoom.updatePlayView(mStuffStreamInfo.streamID, playView);
        }
    }

    /**
     * 设置采集音频
     * @param voice
     */
    public void setCaptureVolume(int voice){
        LogToFileUtils.write("setCaptureVolume");
        mZegoLiveRoom.setCaptureVolume(voice);
    }

    /**
     * 摄像头
     */
    public void setCamera(boolean cam){
        LogToFileUtils.write("setCamera");
        mZegoLiveRoom.enableCamera(cam);
    }

    /**
     * 获取坐席ID
     */
    public String getStaffUserID(){
        return mStaffZegoUser.userID;
    }

    /**
     * 获取坐席Name
     */
    public String getStaffUserName(){
        return mStaffZegoUser.userName;
    }

    /**
     * 切换摄像头
     */
    public void ChangeCamera(boolean cam){ mZegoLiveRoom.setFrontCam(cam);}

    /**
     * 本地静音
     */
    public void setMute(boolean cam){ mZegoLiveRoom.enableSpeaker(cam);}

    /**
     * 获取当前坐席音量
     */
    public float getSoundLevelOfStream(){
        return mZegoLiveRoom.getSoundLevelOfStream(mStuffStreamInfo.streamID);
}

    /**
     * 获取当前用户音量
     */
    public float getSoundLevelOfUserStream(){
        return mZegoLiveRoom.getSoundLevelOfStream(ZegoHelper.getStreamIDWithUser(mZegoUser));
    }

    /**
     * 释放相关变量
     */
    public void releaseQS() {
        quitVideo();
        dequeue();
        logoutQueue();
        mFetchTokenCallback = null;
        mConnectEventCallback = null;
        mQueueListCallback = null;
        mQueueCallback = null;
        mVideoCallback = null;
        mLoginBlocks = null;
        mIZegoQueueServiceRecvCommand=null;
        mZegoSoundLevelMonitor=null;
    }

    /**
     * @return 是否已经登录队列服务
     */
    public boolean isLoginQueue() {
        return isLoginQueue;
    }

    /**
     * @return 是否已经在队列
     */
    public boolean isEnqueue() {
        return isEnqueue;
    }

    /**
     * @return 返回客户当前排队位置
     */
    public int getCustomerIndex() {
        return mCustomerIndex;
    }

    //----------------------设置相关回调-------------------//

    /**
     * 设置获取Token的回调函数
     *
     * @param fetchTokenCallback 获取Token的回调函数
     */
    public void setFetchTokenCallback(IZegoQueueServiceFetchTokenCallback fetchTokenCallback) {
        this.mFetchTokenCallback = fetchTokenCallback;
    }

    /**
     * 设置列表服务登录回调
     *
     * @param connectEventCallback 列表服务登录回调对象
     */
    public void setConnectEventCallback(IZegoQueueServiceConnectEventCallback connectEventCallback) {
        this.mConnectEventCallback = connectEventCallback;
    }

    /**
    *IM回调
     */
    public void setZegoIMCallback(IZegoIMCallback imCallback){
        mZegoLiveRoom.setZegoIMCallback(imCallback);
    }
    /**
     * 发送消息,结果通知
     * messageType:1文本
     * messageCategory：1聊天，2系统,3点赞，4礼物
     * content：消息内容
     */
    public boolean sendRoomMessage(int messageType,int messageCategory,String content){
        return mZegoLiveRoom.sendRoomMessage(messageType,messageCategory,content,new IZegoRoomMessageCallback(){
            @Override
            public  void onSendRoomMessage(int var1, String var2, long var3){
                LogToFileUtils.write("onSendRoomMessage roomID:"+var2+"errorCode:"+var1);
            }
        });
    }
    /**
     * 接收消息
     */
    public void onRecvRoomMessage(){
        mZegoLiveRoom.onRecvRoomMessage(mRoomID,roomMessages);
    }


    /**
     * 设置获取队列列表回调
     *
     * @param queueListCallback 获取队列列表回调对象
     */
    public void setQueueListCallback(IZegoQueueServiceQueueListCallback queueListCallback) {
        mQueueListCallback = queueListCallback;
    }

    /**
     * 设置队列状态等相关回调
     *
     * @param queueCallback 队列状态等相关回调对象
     */
    public void setQueueCallback(IZegoQueueServiceQueueCallback queueCallback) {
        mQueueCallback = queueCallback;
    }

    /**
     * 设置 视频相关回调
     *
     * @param videoCallback 视频相关回调对象
     */
    public void setVideoCallback(IZegoQueueServiceVideoCallback videoCallback) {
        mVideoCallback = videoCallback;
    }

    /**
     * 刷新列表内部实现
     */
    public void fetchQueueListInner() {
        LogToFileUtils.write("fetchQueueListInner" );
        mQueue.getQueueList();
    }

    /**
     * 结束办理业务内部实现
     *
     * @param shouldSendCmd 是否需要发信令到坐席端
     */
    private void quitVideoInner(boolean shouldSendCmd) {
        LogToFileUtils.write("quitVideoInner shouldSendCmd: " + shouldSendCmd + " isVideoQuitting:" + isVideoQuitting);
        // 如果正在退出
        if (isVideoQuitting) {
            return;
        }

        isVideoQuitting = true;
        isCatch = false;

        // 登出房间之前，停止预览
        if (isPreview) {
            stopPreview();
        }
        // 登出房间之前，停止推流
        if (isPublish) {
            stopPublish();
        }
        // 登出房间之前，停止播放流
        if (isPlay) {
            stopPlayStuffStream();
        }
        // 在这 isLoginQueue 的判断是为了保证信令可以正常发送（信令能正常发送的前提保证是isLoginQueue=true）
        // 如果是isLoginRoom，才有需要发信令，否则直接调logoutRoom();
        if (shouldSendCmd && isLoginQueue && isLoginRoom) {
            mCmdRetryCount = QUIT_VIDEO_CMD_SEND_COUNT;
            sendQuitVideoCmd();
        } else {
            // 如果不需发信令，直接执行房间登出和状态重置
            logoutRoom();
        }
    }

    /**
    *发送自定义信令
     */
    public boolean sendCustomCommand(String str){
        LogToFileUtils.write("sendCustomCommand"+str);
        return mZegoLiveRoom.sendCustomCommand(new ZegoUser[]{mStaffZegoUser}, str,new IZegoCustomCommandCallback(){
            @Override
            public void onSendCustomCommand(int errorCode,String roomID){
                LogToFileUtils.write("onSendCustomCommand errorCode: " + errorCode + " roomID: " + roomID);
            }
        } );
    }

    /**
     * 发送退出房间信令
     */
    private void sendQuitVideoCmd() {
        LogToFileUtils.write("sendQuitVideoCmd mCmdRetryCount: " + mCmdRetryCount + " mStaffZegoUser: " + mStaffZegoUser);
        // 如果重试超过次数了
        if (mCmdRetryCount == 0 || mStaffZegoUser == null) {
            logoutRoom();
            return;
        }
        mCmdRetryCount--;
        JSONObject Msg = new JSONObject();
        try {
            Msg.put("msg_content","hangup_request");
            Msg.put("msg_type","system");
        } catch (Exception ignore) {
            LogToFileUtils.write("sendQuitVideoCmd error");
        }
        // 如果参数出现问题，如staffZegoUser信息有误
        if (!mZegoLiveRoom.sendCustomCommand(new ZegoUser[]{mStaffZegoUser}, Msg.toString(), this)) {
            logoutRoom();
        }
    }

    /**
     * 初始化 ZegoLiveRoom 回调，相关参数 和 队列的回调，相关参数
     *
     * @param avConfig 推拉流码率配置
     */
    private void setup(ZegoAvConfig avConfig) {
        mZegoLiveRoom = ZegoApiManager.getInstance().getZegoLiveRoom();

        // 设置相关回调
        ZegoApiManager.getInstance().registerForeAndBackgroundCallback(this);  // 绑定前台后台回调
        mZegoLiveRoom.setZegoRoomCallback(this);
        mZegoLiveRoom.setZegoLivePlayerCallback(this);
        mZegoLiveRoom.setZegoLivePublisherCallback(this);

        // 初始化相关值
        mLoginBlocks = new LinkedList<>();

        // 设置AV config
        mZegoLiveRoom.setAVConfig(avConfig);
        mZegoLiveRoom.setLatencyMode(ZegoConstants.LatencyMode.Low3);
        ZegoLiveRoom.setAudioDeviceMode(ZegoConstants.AudioDeviceMode.Communication);
        // 等比缩放填充整View，可能有部分被裁减。
        mZegoLiveRoom.setPreviewViewMode(ZegoVideoViewMode.ScaleAspectFill);
        //设置音频前处理
        // 回声消除
        mZegoLiveRoom.enableAEC(true);
        // 音频采集自动增益
        mZegoLiveRoom.enableAGC(true);
        // 音频采集噪声抑制
        mZegoLiveRoom.enableNoiseSuppress(true);
        // 开启流量控制
        mZegoLiveRoom.enableTrafficControl(ZegoConstants.ZegoTrafficControlProperty.ZEGOAPI_TRAFFIC_CONTROL_ADAPTIVE_FPS, true);

        // 初始化队列信息
        mQueue = Queue.sharedQueue();
        mQueue.start(UserType.USER_TYPE_CUSTOMER);
        mQueue.setQueueDelegate(mQueueDelegate);
        mQueue.setCustomerDelegate(mCustomerDelegate);
    }

    /**
     * 重置状态值
     */
    private void reset() {
        // 重置状态值
        mQueueID = null;
        mRoomID = null;
        mStaffZegoUser = null;
//        mStuffStreamInfo = null;
        isEnqueue = false;
        isAcceptService = false;
        isCatch = false;
        isLoginRoom = false;
        isDisconnect = false;
        isPreview = false;
        isPublish = false;
        isPlay = false;
        isVideoFinish = false;
        isVideoQuitting = false;
        mCustomerIndex = ZEGO_QUEUE_CUSTOMER_NO_INVALID;
    }

    /**
     * 登录视频见证房间
     */
    private void loginRoom() {
        LogToFileUtils.write("loginRoom mRoomID: " + mRoomID + " isLoginRoom: " + isLoginRoom);
        mZegoLiveRoom.loginRoom(mRoomID, ZegoConstants.RoomRole.Audience, this);
    }

    /**
     * 登出房间
     */
    private void logoutRoom() {
        LogToFileUtils.write("logoutRoom isLoginRoom: " + isLoginRoom);
        mZegoLiveRoom.logoutRoom();
        reset();
    }

    /**
     * 重连房间
     */
    private void reconnectToLiveRoom() {
        startVideo();
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        // 如果已经开启
        if (isPreview) {
            return;
        }
        mZegoLiveRoom.setPreviewView(mPreviewView);
        boolean result = mZegoLiveRoom.startPreview();

        if (result) {
            isPreview = true;
            setCamera(true);
            LogToFileUtils.write("zegoLiveRoom start preview success");
        } else {
            LogToFileUtils.write("zegoLiveRoom start preview failed");
        }
    }

    /**
     * 停止预览
     */
    private void stopPreview() {
        mZegoLiveRoom.stopPreview();
        mZegoLiveRoom.setPreviewView(null);
        isPreview = false;
    }

    public void startScreenPublish(){
        boolean result=mZegoLiveRoom.startPublishing2(ZegoHelper.getStreamIDWithUser(mZegoUser)+"snc_","", ZegoConstants.PublishFlag.JoinPublish,ZegoConstants.PublishChannelIndex.AUX);
        if (result) {
            LogToFileUtils.write("startScreenPublish success");
        } else {
            LogToFileUtils.write("startScreenPublish failed");
        }
    }

    public void stopScreenPublish(){
        boolean result= mZegoLiveRoom.stopPublishing( ZegoConstants.PublishChannelIndex.AUX);//停止录屏推流
        if (result) {
            LogToFileUtils.write("stopScreenPublish success");
        } else {
            LogToFileUtils.write("stopScreenPublish failed");
        }
    }


    /**
     * 开始推流
     */
    private void startPublish() {
        if (isPublish) {
            return;
        }
        boolean result = mZegoLiveRoom.startPublishing(ZegoHelper.getStreamIDWithUser(mZegoUser), "", ZegoConstants.PublishFlag.JoinPublish);
        if (result) {
            isPublish = true;
            LogToFileUtils.write("zegoLiveRoom start publishing success");
        } else {
            LogToFileUtils.write("zegoLiveRoom start publishing failed");
        }
    }

    /**
     * 停止推流
     */
    private void stopPublish() {
        mZegoLiveRoom.stopPublishing();
        isPublish = false;
    }

    /**
     * 开始拉流播放
     *
     * @param zegoStreamInfo 坐席的流
     */
    private void startPlayStuffStream(ZegoStreamInfo zegoStreamInfo) {
        if (zegoStreamInfo == null || isPlay) {
            LogToFileUtils.write("startPlayStuffStream zegoStreamInfo: " + zegoStreamInfo + " isPlay:" + isPlay);
            return;
        }

        mStuffStreamInfo = zegoStreamInfo;
        boolean result = mZegoLiveRoom.startPlayingStream(zegoStreamInfo.streamID, mPlayView);
        if (result) {
            isPlay = true;
            LogToFileUtils.write("zegoLiveRoom start play stream success");
        } else {
            LogToFileUtils.write("zegoLiveRoom start play stream failed");
        }
    }

    /**
     * 停止拉流播放
     */
    private void stopPlayStuffStream() {
        mZegoLiveRoom.stopPlayingStream(mStuffStreamInfo.streamID);
        mZegoLiveRoom.updatePlayView(mStuffStreamInfo.streamID, null);
        isPlay = false;
    }

    /**
     * 获取填充的流
     *
     * @param listStream 登录房间时获取的 房间流列表
     * @return 获取坐席的流
     */
    private ZegoStreamInfo getStuffStreamFromStreamList(ZegoStreamInfo[] listStream) {
        if (listStream != null) {
            for (ZegoStreamInfo streamInfo : listStream) {
                if (ZegoHelper.isStuffStream(streamInfo.streamID)) {
                    return streamInfo;
                }
            }
        }
        return null;
    }


    private void onLoginRoomComplete(int errorCode, ZegoStreamInfo[] listStream) {
        LogToFileUtils.write("onLoginRoomComplete errorCode: " + errorCode + " listStream.length:" + listStream.length);
        // 登录房间错误码处理
        final int mErrorCode =errorCode;
        if (errorCode == 0) {
            isLoginRoom = true;
            isDisconnect = false;
            // 此处将下面两个值重置
            // 保证可以正常退出房间
            isVideoQuitting = false;
            // 保证进入新房间之后，信令就不再发送
            mCmdRetryCount = 0;

            // 登录房间成功
            // 开始预览
            startPreview();
            // 开始推流
            startPublish();

            if (listStream.length != 0) {
                // 开始拉流
                startPlayStuffStream(getStuffStreamFromStreamList(listStream));
            }
        } else {
            // 统一回调播放视频错误
            if (mVideoCallback != null) {
                VideoErrorCode=101;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoCallback.onVideoError(ResultCode.createResultCodeByRoomResult(mErrorCode),new OptionalBlock(){
                            @Override
                            public void execute(boolean option) {
                                if(option){
                                    logoutRoom();
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    /**
     * 拉取token 并且登录
     */
    private void fetchTokenAndLoginQueue() {
        if (mFetchTokenCallback != null) {
            mFetchTokenCallback.fetchLoginQueueToke(new FetchTokenBlock() {
                @Override
                public void fetchTokenBlock(String token) {
                    final String mToken =token;
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mQueue.userLogin(mToken);
                        }
                    });
                }}
            );
        } else {
            mQueue.userLogin("");
        }
    }

    //----------------- 排队队列相关回调 ---------------------//
    private QueueDelegate mQueueDelegate = new QueueDelegate() {
        /**
         * 登录队列服务回调
         *
         * @param errorCode 错误结果码
         */
        @Override
        public void onUserLogin(int errorCode) {
            LogToFileUtils.write("onUserLogin errorCode: " + errorCode);
            // 登录队列服务成功，尝试加入排队队列
            final int mErrorCode =errorCode;
            if (errorCode == 0) {
                isLoginQueue = true;
            }
            if (mConnectEventCallback != null) {
                // 登录成功失败回调
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogToFileUtils.write("run() {" );
                        LoginBlock loginBlock = mLoginBlocks.poll();
                        if (loginBlock != null) {
                            loginBlock.onLogin(ResultCode.createResultCodeByLoginQueueResult(mErrorCode));
                        } else {
                            // 这种情况基本不会出现
                            LogToFileUtils.write("异常情况: onUserLogin loginBlock == null");
                        }
                    }
                });
            }
        }

        @Override
        public void onConnectState(int connectEvent, int errorCode) {
            final int mErrorCode =errorCode;
            final int mConnectEvent =connectEvent;
            switch (connectEvent) {
                case ZegoQueueConnectEvent.RECONNECT:
                    break;
                case ZegoQueueConnectEvent.TEMP_BROKEN:
                    break;
                case ZegoQueueConnectEvent.DISCONNECT:
                case ZegoQueueConnectEvent.KICK_OUT:
                    // 重置 已登录和 在队列状态
                    isLoginQueue = false;
                    isEnqueue = false;
                    break;
                case ZegoQueueConnectEvent.REENTER_QUEUE_FAILED:
                    // 登录队列服务成功
                    isLoginQueue = true;
                    // 进入队列失败
                    isEnqueue = false;
                    break;
            }
            if (mConnectEventCallback != null) {
                // 队列服务登录连接状态改变回调
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectEventCallback.onReceiveConnectEvent(mConnectEvent, ResultCode.createResultCodeByQueueResult(mErrorCode));
                    }
                });
            }
        }

        /*
         * @param queueInfo 当前队列的信息，包含队列中的人数，队列头的客户序号customerNo
         */
        @Override
        public void onQueueUpdated(int errorCode, int index, QueueInfo queueInfo) {
            final  int mindex=index;
            LogToFileUtils.write("onQueueUpdated errorCode: " + errorCode + " index: " + index + " queueInfo: " + queueInfo);
            // 已经跳转到视频见证页面了，忽略队列相关到状态回调
            // 如果 errorCode 不为0，不执行回调
            if (isCatch || errorCode != 0) {
                return;
            }
            mCustomerIndex = index + 1;
            // 回调当前用户队列位置
            if (mQueueCallback != null) {
                // index 是从0开始，所以需在这里+1
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mQueueCallback.onQueueIndexUpdate(mindex + 1);
                    }
                });

            }
        }

        /**
         * 队列列表回调
         *
         * @param errorCode  错误码
         * @param queueInfos 队列列表
         */
        @Override
        public void onQueueList(int errorCode, ArrayList<QueueInfo> queueInfos) {
            mqueueInfos=queueInfos; //test
            final ArrayList<QueueInfo> mqueueInfos=queueInfos;
            final int merrorCode=errorCode;
            LogToFileUtils.write("onQueueList errorCode: " + errorCode + " queueInfos: " + queueInfos.toString());
            if(isCcis){
                enqueue(queueID, userExtraInfo,priority);
            }
            if (mQueueListCallback != null) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mQueueListCallback.onQueueList(ResultCode.createResultCodeByQueueResult(merrorCode), mqueueInfos);
                    }
                });
            }
        }
    };


    private CustomerDelegate mCustomerDelegate = new CustomerDelegate() {

        /**
         * 设置 UserExtraInfo 回调
         * @param errorCode 错误码
         */
        public void onSetUserExtraInfo(int errorCode) {
            LogToFileUtils.write("onSetUserExtraInfo errorCode: " + errorCode + " resultCode: " + ResultCode.createResultCodeByQueueResult(errorCode));
        }

        /**
         * 客户进入队列回调，
         *
         * @param errorCode           错误结果码
         * @param customerEnqueueInfo 客户进队信息
         */
        @Override
        public void onCustomerEnter(int errorCode, CustomerEnqueueInfo customerEnqueueInfo) {
            LogToFileUtils.write("onCustomerEnter errorCode: " + errorCode + " customerEnqueueInfo: " + customerEnqueueInfo.toString());
            // 进入队列成功
            final CustomerEnqueueInfo mcustomerEnqueueInfo=customerEnqueueInfo;
            final int merrorCode=errorCode;
            if (errorCode == 0) {
                // 记录相关数值
                isEnqueue = true;
                mCustomerIndex = customerEnqueueInfo.getCountInFront() + 1;
                // 回调排队位置
                if (mQueueCallback != null) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mQueueCallback.onQueueIndexUpdate(mcustomerEnqueueInfo.getCountInFront() + 1);
                        }
                    });
                }
            }
            // 进入队列回调
            if (mQueueCallback != null) {
                // 重新执行加入队列操作
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mQueueCallback.onEnqueue(ResultCode.createResultCodeByQueueResult(merrorCode),new OptionalBlock(){
                            @Override
                            public void execute(boolean option) {
                                if(option){
                                    enqueue();
                                }
                            }
                        });
                    }
                });
            }
        }

        /**
         * 客户退出队列回调
         *
         * @param errorCode 错误结果码
         */
        @Override
        public void onCustomerQuit(int errorCode) {
            LogToFileUtils.write("onCustomerQuit errorCode: " + errorCode);
            // 空实现，因为是统一编译多个平台版本，所以没有删除该方法
        }

        /**
         * 服务端响应视频见证请求回调
         *
         * @param errorCode 结果码
         */
        @Override
        public void onCustomerAcceptService(int errorCode) {
            LogToFileUtils.write("onCustomerCatch errorCode: " + errorCode);
          final int MerrorCode =errorCode;
            // 已经跳转到视频见证页面了，忽略之前的状态回调
            if (isCatch) {
                return;
            }
            // 服务端catch客户成功
            if (errorCode == 0 && isAcceptService) {
                isCatch = true;
            }
            // 服务端响应视频见证请求回调
            if (mQueueCallback != null) {
                // 重新执行请求视频见证
               final OptionalBlock retryBlock =  new OptionalBlock(){

                    @Override
                    public void execute(boolean option) {
                        if(option){
                            mQueue.customerAcceptService(isAcceptService);
                        }
                    }
                };
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mQueueCallback.onReplyServiceComplete(ResultCode.createResultCodeByQueueResult(MerrorCode), MerrorCode == 0 ? null : retryBlock);
                    }
                });

            }
        }

        /**
         * @param roomID  房间ID，
         * @param timeout 超时时间
         * @param staffId 坐席ID，由于信令传输，由于SendCustomCommand将消息传输给坐席
         * @param staffName 坐席名字，由于SendCustomCommand将消息传输给坐席
         */
        @Override
        public void onServiceAvailable(String roomID, int timeout, String staffId, String staffName) {
            LogToFileUtils.write("onServiceAvailable roomID: " + roomID + " timeout: " + timeout + " staffId: " + staffId + " staffName: " + staffName + " isCatch: " + isCatch + " mQueueCallback: " + mQueueCallback);
            // 已经跳转到视频见证页面了，忽略队列相关到状态回调
            final int mtimeout=timeout;
            if (isCatch) {
                return;
            }
            if (mQueueCallback != null) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mQueueCallback.onStaffCatch();
                    }
                });
            }
            // 设置房间信息
            mRoomID = roomID;
            // 设置坐席信息
            mStaffZegoUser = new ZegoUser();
            mStaffZegoUser.userID = staffId;
            mStaffZegoUser.userName = staffName;
            // 此时已经不在队列里面
            isEnqueue = false;
            // 退出队列回调
            if (mQueueCallback != null) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mQueueCallback.onReceiveServiceShouldStart(mtimeout, new OptionalBlock() {
                            @Override
                            public void execute(boolean option) {
                                if (option) {
                                    isAcceptService = option;
                                    // 向服务器请求视频见证
                                    mQueue.customerAcceptService(option);
                                }
                            }
                        });
                    }
                });

            }
        }
    };

    @Override
    public void onAVEngineStart() {
        // DO NOTHING
    }

    /**
     * SDK音视频引擎停止工作
     */
    @Override
    public void onAVEngineStop() {
        if (mVideoCallback != null) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVideoCallback.onAVEngineStop();
                }
            });
        }
    }

    //-------------------- 视频相关回调 ---------------------//

    /**
     * 视频过程中被踢出房间回调
     *
     * @param reason 原因码
     * @param roomID 房间ID
     */
    @Override
    public void onKickOut(int reason, String roomID) {
        LogToFileUtils.write("onKickOut reason: " + reason + " roomID: +" + roomID);
        // 在这需规避一种逻辑，当业务办理完成后，客户可能一直不退出房间，此时坐席主动关闭房间，
        // 导致会再次接受到这个方法，reason为16777220，这种是错误到情况，在这里进行规避
        final  int mreason=reason;
        if (isVideoFinish) {
            isLoginRoom = false;
            return;
        }

        // reason = 16777220 表示该账户是被手动踢出
        if (reason == 16777220) {
            // 坐席主动退出
            // 执行回调
            if (mVideoCallback != null) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoCallback.onVideoFinish(false);
                    }
                });
            }
        } else {
            isLoginRoom = false;
            isDisconnect = true;
            isPlay = false;
            isPublish = false;
            // 统一回调播放视频错误
            if (mVideoCallback != null) {
                VideoErrorCode=102;
                ThreadUtils.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         mVideoCallback.onVideoError(ResultCode.createResultCodeByKickOutReason(mreason),
                                 new OptionalBlock(){
                             @Override
                             public void execute(boolean option) {
                                 if(option){
                                     reconnectToLiveRoom();
                                 }
                             }
                         });
                     }
                });
            }
        }
    }

    /**
     * 跟服务器断开连接回调
     *
     * @param errorCode 错误码
     * @param roomID    房间ID
     */
    @Override
    public void onDisconnect(int errorCode, String roomID) {
        LogToFileUtils.write("onDisconnect errorCode: " + errorCode + " roomID: +" + roomID);
        isLoginRoom = false;
        isDisconnect = true;
        isPlay = false;
        isPublish = false;
        final int merrorCode=errorCode;
        // 统一回调播放视频错误
        if (mVideoCallback != null) {
            VideoErrorCode=102;
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVideoCallback.onVideoError(ResultCode.createResultCodeByRoomDisconnectReason(merrorCode), new OptionalBlock() {
                        @Override
                        public void execute(boolean option) {
                            if(option){
                                reconnectToLiveRoom();
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * 当房间流信息更新
     *
     * @param type            ZegoConstants.StreamUpdateType
     * @param zegoStreamInfos 更新的流列表，只有更新的流才会在里面
     * @param roomID          房间ID
     */
    @Override
    public void onStreamUpdated(int type, ZegoStreamInfo[] zegoStreamInfos, String roomID) {
        LogToFileUtils.write("onStreamUpdated type: " + type + " zegoStreamInfos.length: " + zegoStreamInfos.length + " roomID: +" + roomID);
        ZegoStreamInfo zegoStreamInfo = getStuffStreamFromStreamList(zegoStreamInfos);
        // 不是stuff流改变，不需进行处理
        if (zegoStreamInfo == null) {
            return;
        }
        // 根据情况，进行处理
        if (type == ZegoConstants.StreamUpdateType.Added) {
            startPlayStuffStream(zegoStreamInfo);
        } else if (type == ZegoConstants.StreamUpdateType.Deleted) {
            stopPlayStuffStream();
        }
    }

    /**
     * 推流状态更新
     *
     * @param stateCode  状态码, 0:成功, 其它:失败
     * @param streamID   推流ID
     * @param streamInfo 推流信息
     */
    @Override
    public void onPublishStateUpdate(int stateCode, String streamID, HashMap<String, Object> streamInfo) {
        LogToFileUtils.write("onPublishStateUpdate stateCode: " + stateCode + " streamID: " + streamID + " streamInfo: " + streamInfo);
        // 如果是断开连接状态，重置是否推流状态，等重连后重新执行
       final int mstateCode =stateCode;
        if (isDisconnect) {
            isPublish = false;
            return;
        }
        // 推流失败
        if (stateCode != 0) {
            isPublish = false;
            if (mVideoCallback != null) {
                VideoErrorCode=102;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoCallback.onVideoError(ResultCode.createResultCodeByPublishState(mstateCode), new OptionalBlock() {
                            @Override
                            public void execute(boolean option) {
                                if(option){
                                    startPublish();
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    /**
     * 播放状态更新
     *
     * @param stateCode 状态码, 0:成功, 其它:失败
     * @param streamID  流ID
     */
    @Override
    public void onPlayStateUpdate(int stateCode, String streamID) {
        LogToFileUtils.write("onPlayStateUpdate stateCode: " + stateCode + " streamID: " + streamID);
        if(Videotimer!=null){
            LogToFileUtils.write("Videotimer cancel: " );
            Videotimer.cancel();
        }
        // 如果是断开连接状态，重置是否播放状态，等重连后重新执行
        final int mstateCode =stateCode;
        if (isDisconnect) {
            isPlay = false;
            return;
        }
        // 播放失败
        if (stateCode != 0) {
            isPlay = false;
            if (mVideoCallback != null) {
                VideoErrorCode=102;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoCallback.onVideoError(ResultCode.createResultCodeByPlayState(mstateCode), new OptionalBlock() {
                            @Override
                            public void execute(boolean option) {
                                if(option){
                                    startPlayStuffStream(mStuffStreamInfo);
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public void onSendCustomCommand(int errorCode, String roomID) {
        LogToFileUtils.write("onSendCustomCommand errorCode: " + errorCode + " roomID: " + roomID);
        // 保证内部逻辑都是在一个线程中执行
        final int merrorCode= errorCode;
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (merrorCode == 0) {
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        LogToFileUtils.write("onSendCustomCommand error ");
                    }
                    logoutRoom();
                } else {
                    sendQuitVideoCmd();
                }
            }
        });
        // 信令发送成功
    }

    //----------------------前台后台状态改变回调------------------------//

    @Override
    public void onApplicationDidBecomeActive() {
        LogToFileUtils.write("onApplicationDidBecomeActive");
        // 只有在登录房间状态下，才触发相关逻辑
        if (isLoginRoom) {
            this.setCamera(true);
        }
    }

    @Override
    public void onApplicationWillResignActive() {
        LogToFileUtils.write("onApplicationWillResignActive");
        if (isLoginRoom) {
            this.setCamera(false);
        }
    }

    //---------------------- 不需实现的回调 ---------------------------//

    @Override
    public void onPlayQualityUpdate(String streamID, ZegoPlayStreamQuality zegoPlayStreamQuality) {
        LogToFileUtils.write("onPlayQualityUpdate streamID: " + streamID + " zegoPlayStreamQuality: " + zegoPlayStreamQuality);
        // DO NOTHING
    }

    @Override
    public void onInviteJoinLiveRequest(int seq, String fromUserID, String fromUserName, String roomID) {
        LogToFileUtils.write("onInviteJoinLiveRequest seq: " + seq + " fromUserID: " + fromUserID + " fromUserName: " + fromUserName + " roomID: " + roomID);
        // DO NOTHING
    }

    @Override
    public void onRecvEndJoinLiveCommand(String fromUserID, String fromUserName, String roomID) {
        LogToFileUtils.write("onRecvEndJoinLiveCommand fromUserID: " + fromUserID + " fromUserName: " + fromUserName + " roomID: " + roomID);
        // DO NOTHING
    }

    @Override
    public void onVideoSizeChangedTo(String streamID, int width, int height) {
        LogToFileUtils.write("onVideoSizeChangedTo streamID: " + streamID + " width: " + width + " i1: " + height);
        // DO NOTHING
    }

    @Override
    public void onJoinLiveRequest(int seq, String fromUserID, String fromUserName, String roomID) {
        LogToFileUtils.write("onJoinLiveRequest seq: " + seq + " fromUserID: " + fromUserID + " fromUserName: " + fromUserName + " roomID: " + roomID);
        // DO NOTHING
    }

    @Override
    public void onPublishQualityUpdate(String streamID, ZegoPublishStreamQuality zegoPublishStreamQuality) {
        LogToFileUtils.write("onPublishQualityUpdate streamID: " + streamID + " pktLostRate: " + zegoPublishStreamQuality.pktLostRate+ " quality: " + zegoPublishStreamQuality.quality);
        // DO NOTHING
    }
    @Override
    public AuxData onAuxCallback(int expectDataLength) {
        LogToFileUtils.write("onAuxCallback expectDataLength: " + expectDataLength);
        // DO NOTHING
        return null;
    }

    @Override
    public void onCaptureVideoSizeChangedTo(int width, int height) {
        LogToFileUtils.write("onCaptureVideoSizeChangedTo width: " + width + " height: " + height);
        // DO NOTHING
    }

    @Override
    public void onCaptureVideoFirstFrame() {
        LogToFileUtils.write("onCaptureVideoFirstFrame");
        // DO NOTHING
    }

    @Override
    public void onMixStreamConfigUpdate(int stateCode, String mixStreamID, HashMap<String, Object> streamInfo) {
        LogToFileUtils.write("onMixStreamConfigUpdate stateCode: " + stateCode + " mixStreamID: " + mixStreamID + " streamInfo: " + streamInfo);
        // DO NOTHING
    }

    @Override
    public void onReconnect(int errorCode, String roomID) {
        LogToFileUtils.write("onReconnect errorCode: " + errorCode + " s: " + roomID);
        final int merrorCode=errorCode;
        if(mConnectEventCallback!=null&&merrorCode==0){
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConnectEventCallback.onReceiveConnectEvent(ZegoQueueConnectEvent.RECONNECT,ResultCode.createResultCodeByQueueResult(0));
                }
            });
        }
    }

    @Override
    public void onTempBroken(int errorCode, String roomID) {
        LogToFileUtils.write( "onTempBroken errorCode: " + errorCode + " roomID: " + roomID);
        // DO NOTHING
    }

    @Override
    public void onStreamExtraInfoUpdated(ZegoStreamInfo[] zegoStreamInfos, String roomID) {
        LogToFileUtils.write("onStreamExtraInfoUpdated zegoStreamInfos.length: " + zegoStreamInfos.length + " roomID: " + roomID);
        // DO NOTHING
    }

    @Override
    public void onRecvCustomCommand(String userID, String userName, String content, String roomID) {
        LogToFileUtils.write("onRecvCustomCommand userID: " + userID + " userName: " + userName + " content: " + content + " s3: " + roomID);
       final String muserID=userID;
       final String muserName=userName;
       final String mcontent=content;
       final String mroomID=roomID;
       JSONObject jsonObject=new JSONObject();
       String Content="";
       String Type="";

       try{
           jsonObject=new JSONObject(content);
           Content=jsonObject.get("msg_content").toString();
           Type=jsonObject.get("msg_type").toString();
       }catch(Exception e){
           LogToFileUtils.write("onRecvCustomCommand userID: "+e.getMessage());
       }
       //处理挂断指令
        if (!TextUtils.isEmpty(content)&&"hangup_request".equals(Content)&&"system".equals(Type)) {
            isVideoFinish = true;
            LogToFileUtils.write("onRecvCustomCommand:hangup_request");
            if (mVideoCallback != null) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoCallback.onVideoFinish(true);
                    }
                });
            }
            return;
        }
        //处理坐席端异常指令
        if (!TextUtils.isEmpty(content)&&"staff_logout_room".equals(Content)&&"system".equals(Type)) {
            isVideoFinish = true;
            LogToFileUtils.write("onRecvCustomCommand:staff_logout_room");
            if (mVideoCallback != null) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoCallback.onVideoFinish(false);
                    }
                });
            }
            return;
        }
        if(mIZegoQueueServiceRecvCommand!=null){
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogToFileUtils.write("onRecvCustomCommand for other Command");
                    mIZegoQueueServiceRecvCommand.onRecvCommand(muserID,muserName,mcontent,mroomID);
                }
            });
        }
    }

    @Override
    public void onLoginCompletion(int i, ZegoStreamInfo[] zegoStreamInfos) {
        onLoginRoomComplete(i,zegoStreamInfos);
    }

    /**
     * 设置IZegoQueueServiceRecvCommand相关回调
     *
     */
    public void setIZegoQueueServiceRecvCommand(IZegoQueueServiceRecvCommand recvCommand) {
        this.mIZegoQueueServiceRecvCommand = recvCommand;
    }

    /**
     * 设置IZegoSoundLevelCallback相关回调
     */
    public void setIZegoSoundLevelCallback(IZegoSoundLevelCallback mIZegoSoundLevelCallback){
        if(this.mZegoSoundLevelMonitor==null){
            this.mZegoSoundLevelMonitor=ZegoSoundLevelMonitor.getInstance();
            //设置刷新时间
            this.mZegoSoundLevelMonitor.setCycle(100);
        }
        this.mZegoSoundLevelMonitor.setCallback(mIZegoSoundLevelCallback);
        this.mZegoSoundLevelMonitor.start();
    }

    public void setCaptureResolution(int mCaptureResolutionWidth,int mCaptureResolutionHeight){
        LogToFileUtils.write("setCaptureResolution");
        BaseActivity.CaptureResolutionWidth=mCaptureResolutionWidth;
        BaseActivity.CaptureResolutionHeight=mCaptureResolutionHeight;
    }

}
