package com.icbc.roomservice.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.icbc.roomservice.kernelcode.ResultCode;
import com.icbc.roomservice.kernelcode.block.OptionalBlock;
import com.icbc.roomservice.kernelcode.callback.IZegoQueueServiceConnectEventCallback;
import com.icbc.roomservice.kernelcode.callback.IZegoQueueServiceQueueCallback;
import com.icbc.roomservice.kernelcode.callback.IZegoQueueServiceVideoCallback;
import com.icbc.roomservice.base.view.TipDialog;
import com.icbc.roomservice.kernelcode.event.ZegoQueueConnectEvent;
import com.icbc.roomservice.kernelcode.utils.LogToFileUtils;

public class VideoServiceActivity extends BaseActivity implements IZegoQueueServiceQueueCallback,IZegoQueueServiceVideoCallback, IZegoQueueServiceConnectEventCallback ,View.OnClickListener{

    private final static String TAG = VideoServiceActivity.class.getSimpleName();

    public final static String EXTRA_KEY_ROOM_ID = "room_id";


    //默认使用前置摄像头
    private boolean camera=true;
    //设置采集音量
    private int voice=100;
    TextView quietView;
    TextView handupView;
    TextView cameraView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_queue_service);

        mZegoQueueService.setPreviewView(findViewById(R.id.preview_tv), findViewById(R.id.play_tv));
        mZegoQueueService.setVideoCallback(this);
        mZegoQueueService.setConnectEventCallback(this);
        mZegoQueueService.setQueueCallback(this);
        startVideo();

        quietView=findViewById(R.id.tv_call_quiet);
        handupView=findViewById(R.id.tv_handup_call);
        cameraView=findViewById(R.id.tv_change_camera);
        quietView=findViewById(R.id.tv_call_quiet);
        handupView=findViewById(R.id.tv_handup_call);
        cameraView=findViewById(R.id.tv_change_camera);
        quietView.setOnClickListener(this);
        handupView.setOnClickListener(this);
        cameraView.setOnClickListener(this);

    }

    private void startVideo() {
        String roomID = getIntent().getStringExtra(EXTRA_KEY_ROOM_ID);
        if (TextUtils.isEmpty(roomID)) {
            Toast.makeText(this, "房间ID 为null！", Toast.LENGTH_SHORT).show();
            return;
        }
        mZegoQueueService.setRoomID(roomID);
        // 开始连麦
        mZegoQueueService.startVideo();
    }


    @Override
    public void onReceiveConnectEvent(int connectEvent, ResultCode resultCode) {
        LogToFileUtils.write("onReceiveConnectEvent connectEvent: " + connectEvent);
        switch (connectEvent) {

            case ZegoQueueConnectEvent.RECONNECT:
                Toast.makeText(this, "重连服务器成功", Toast.LENGTH_SHORT).show();
                break;
            case ZegoQueueConnectEvent.TEMP_BROKEN:
                Toast.makeText(this, "和服务器断开连接，正在重连", Toast.LENGTH_SHORT).show();
                break;
            case ZegoQueueConnectEvent.DISCONNECT:
            case ZegoQueueConnectEvent.KICK_OUT:
                // 界面不作明显显示，只有等到用户主动刷新才显示
                break;
            case ZegoQueueConnectEvent.REENTER_QUEUE_FAILED:
                // 不该出现这种情况
                break;
        }
    }

    @Override
    public void onVideoError(ResultCode resultCode, final OptionalBlock retryBlock) {
        Log.d(TAG, "onVideoError resultCode: " + resultCode);
        if (!resultCode.isSuccess()) {
            // 视频开始失败
            final TipDialog tipDialog = getTipDialog();
            tipDialog.reset();
            tipDialog.setCancelable(false);
            tipDialog.setCanceledOnTouchOutside(false);
            tipDialog.mCloseIv.setVisibility(View.INVISIBLE);
            tipDialog.mTitleTv.setText("视频见证过程出错");
            tipDialog.mDescTv.setText("是否进行重试？");
            tipDialog.mButton1.setText("取消");
            tipDialog.mButtonOk.setText("重试");
            tipDialog.mButton1.setVisibility(View.VISIBLE);
            tipDialog.mButtonOk.setVisibility(View.VISIBLE);
            tipDialog.mButton1.setOnClickListener((v) -> {
                tipDialog.dismiss();
                mZegoQueueService.quitVideo();
                finish();

            });
            tipDialog.mButtonOk.setOnClickListener((v) -> {
                tipDialog.dismiss();
                retryBlock.execute(true);
            });
            tipDialog.show();
        }
    }

    @Override
    public void onVideoFinish(boolean finish) {
        if (finish) {
            // 业务办理完成，坐席请求关闭房间
            final TipDialog tipDialog = getTipDialog();
            tipDialog.reset();
            tipDialog.setCancelable(false);
            tipDialog.setCanceledOnTouchOutside(false);
            tipDialog.mCloseIv.setVisibility(View.INVISIBLE);
            tipDialog.mTitleTv.setText("本次业务已经办理完成");
            tipDialog.mDescTv.setVisibility(View.GONE);
            tipDialog.mButtonOk.setText("离开");
            tipDialog.mButton1.setVisibility(View.GONE);
            tipDialog.mButtonOk.setVisibility(View.VISIBLE);
            tipDialog.mButtonOk.setOnClickListener((v) -> {
                tipDialog.dismiss();
                mZegoQueueService.quitVideo();
                finish();
            });
            tipDialog.show();
        } else {
            // 代表业务还未办理完成，坐席就主动关闭了
            final TipDialog tipDialog = getTipDialog();
            tipDialog.reset();
            tipDialog.setCancelable(false);
            tipDialog.setCanceledOnTouchOutside(false);
            tipDialog.mCloseIv.setVisibility(View.GONE);
            tipDialog.mTitleTv.setText("业务办理失败");
            tipDialog.mDescTv.setText("由于业务员中途结束业务，本次业务办理失败");
            tipDialog.mButtonOk.setText("离开");
            tipDialog.mButton1.setVisibility(View.GONE);
            tipDialog.mButtonOk.setVisibility(View.VISIBLE);
            tipDialog.mButtonOk.setOnClickListener((v) -> {
                tipDialog.dismiss();
                mZegoQueueService.quitVideo();
                finish();
            });
            tipDialog.show();
        }
    }

    @Override
    public void onAVEngineStop() {
        // 此时解除对摄像头、麦克风等设备等占用
        // 按需执行操作
    }

    @Override
    public void onBackPressed() {
        final TipDialog tipDialog = getTipDialog();
        tipDialog.reset();
        tipDialog.setCancelable(false);
        tipDialog.setCanceledOnTouchOutside(false);
        tipDialog.mCloseIv.setVisibility(View.INVISIBLE);
        tipDialog.mTitleTv.setText("退出房间");
        tipDialog.mDescTv.setText("本次业务还未结束，退出房间将视为取消本次业务，是否确定退出房间？");
        tipDialog.mButtonOk.setText("确定");
        tipDialog.mButton1.setText("取消");
        tipDialog.mButton1.setVisibility(View.VISIBLE);
        tipDialog.mButtonOk.setVisibility(View.VISIBLE);
        tipDialog.mButtonOk.setOnClickListener((v) -> {
            mZegoQueueService.quitVideo();
            tipDialog.dismiss();
            finish();
        });
        // 取消按钮
        tipDialog.mButton1.setOnClickListener((v) -> tipDialog.dismiss());
        tipDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mZegoQueueService.releaseQS();
        mZegoQueueService = null;
    }

    @Override
    public void onClick(View v){
        if(v.getId()==R.id.preview_tv){
            mZegoQueueService.setPreviewView(findViewById(R.id.preview_tv), findViewById(R.id.play_tv));return;
        }
        if(v.getId()==R.id.play_tv){
            mZegoQueueService.setPreviewView(findViewById(R.id.play_tv), findViewById(R.id.preview_tv));return;
        }
        if(v.getId()==R.id.tv_call_quiet){
            if(voice==100){
                voice=0;
                quietView.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.chat_voice_off) , null, null);
                mZegoQueueService.setCaptureVolume(voice);return;
            }else if(voice==0){
                voice=100;
                quietView.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.chat_voice_on) , null, null);
                mZegoQueueService.setCaptureVolume(voice);return;
            }
        }
        if(v.getId()==R.id.tv_handup_call){
            mZegoQueueService.quitVideo();onBackPressed();return;
        }
        if(v.getId()==R.id.tv_change_camera){
            if(camera==true){
                camera=false;
                cameraView.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.chat_camera_off) , null, null);
                mZegoQueueService.ChangeCamera(camera);return;
            }else if(camera==false){
                camera=true;
                cameraView.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.chat_camera_on) , null, null);
                mZegoQueueService.ChangeCamera(camera);return;
            }
        }
    }

    @Override
    public void onEnqueue(ResultCode resultCode, OptionalBlock optionalBlock) {

    }

    @Override
    public void onQueueIndexUpdate(int index) {

    }

    @Override
    public void onReceiveServiceShouldStart(int timeout, OptionalBlock replyBlock) {

    }

    @Override
    public void onReplyServiceComplete(ResultCode resultCode, @Nullable OptionalBlock retryBlock) {

    }

    @Override
    public void onNoStaff() {

    }

    @Override
    public void onStaffCatch() {

    }
}
