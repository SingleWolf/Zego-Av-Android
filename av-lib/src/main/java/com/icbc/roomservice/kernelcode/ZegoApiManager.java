package com.icbc.roomservice.kernelcode;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.icbc.roomservice.kernelcode.utils.LogToFileUtils;
import com.zego.queue.QueueHelper;
import com.icbc.roomservice.kernelcode.callback.IZegoQueueServiceVideoCallback;
import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.entity.ZegoUser;


/**
 * des: zego api管理器.
 */
public class ZegoApiManager {

    private static final String TAG = ZegoApiManager.class.getSimpleName();

    private ZegoLiveRoom mZegoLiveRoom;

    private ActiveChangedCallback mActiveChangedCallback;

    //服务器域名
    private static String DomainName = "114.255.225.35:15000";
    //是否https
    private static boolean UseHttps = false;

    //是否使用内网
    private static boolean useLocalNetwork = false;

    private Application mApplication;

    private ZegoApiManager() {
        mZegoLiveRoom = new ZegoLiveRoom();
    }

    private final static class ZegoApiManagerHolder {
        private static ZegoApiManager sInstance = new ZegoApiManager();
    }

    //DomainName set方法
    public static void setDomainName(String domainName) {
        DomainName = domainName;
    }

    public static String getDomainName() {
        return DomainName;
    }

    //DomainName set方法
    public static void setUseLocalNetwork(boolean UseLocalNetwork) {
        LogToFileUtils.write("setUseLocalNetwork:"+UseLocalNetwork);
        useLocalNetwork = UseLocalNetwork;
    }

    //useHttps set方法
    public static void setUseHttps(boolean useHttps) {
        LogToFileUtils.write("setUseHttps:"+useHttps);
        UseHttps = useHttps;
    }

    public static ZegoApiManager getInstance() {
        return ZegoApiManagerHolder.sInstance;
    }


    /**
     * 初始化sdk.(##新增初始化返回值)
     *
     * @param context  ApplicationContext 上下文
     * @param zegoUser ZegoUser, 通过new ZegoUser()创建
     * @param appID    应用ID，请到官网或者联系相关人员申请
     * @param appSign  应用签名，请到官网或者联系相关人员申请
     */
    public boolean initSDK(Application context, ZegoUser zegoUser, long appID, String appSign) {
        //设置参数
        return init(context, zegoUser, appID, appSign);
    }

    public boolean unInitSDK() {
        return mZegoLiveRoom.unInitSDK();
    }

    private boolean init(final Application context, ZegoUser zegoUser, long appID, String appSign) {
        mApplication = context;
        ZegoLiveRoom.SDKContext sdkContext = new ZegoLiveRoom.SDKContext() {
            @Nullable
            @Override
            public String getSoFullPath() {
                return null;
            }

            @Nullable
            @Override
            public String getLogPath() {
                return null;
            }

            @NonNull
            @Override
            public Application getAppContext() {
                return context;
            }
        };

        // 设置sdk context 对象，必须要在使用 ZegoLiveRoom 相关方法之前调用，因为里面有so库的加载
        ZegoLiveRoom.setSDKContext(sdkContext);
        // 同上，这个必须要在
        QueueHelper.init(sdkContext);

        ZegoLiveRoom.setDomainName(DomainName, UseHttps, useLocalNetwork);         // TODO 测试数据
        ZegoLiveRoom.setUser(zegoUser.userID, zegoUser.userName);
        // 初始化sdk
        boolean ret = mZegoLiveRoom.initSDK(appID, appSign);

        if (!ret) {
            // sdk初始化失败
            Toast.makeText(context, "Zego SDK初始化失败!", Toast.LENGTH_LONG).show();
            return ret;
        }
        // 注册 监听 Activity 生命周期
        context.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // DO NOTHING
            }

            @Override
            public void onActivityStarted(Activity activity) {
                // 保证只有视频播放Activity才会执行前后台切换回调
                if (mActiveChangedCallback != null && isVideoActivity(activity)) {
                    mActiveChangedCallback.onApplicationDidBecomeActive();
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                // DO NOTHING
            }

            @Override
            public void onActivityPaused(Activity activity) {
                // DO NOTHING
            }

            @Override
            public void onActivityStopped(Activity activity) {
                // 保证只有视频播放Activity才会执行前后台切换回调
                if (mActiveChangedCallback != null && isVideoActivity(activity)) {
                    mActiveChangedCallback.onApplicationWillResignActive();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                // DO NOTHING
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                // DO NOTHING
            }
        });
        return ret;
    }

    /**
     * @return 返回 ZegoLiveRoom 实例
     */
    ZegoLiveRoom getZegoLiveRoom() {
        return mZegoLiveRoom;
    }

    /**
     * @return 返回注册时传入的Application
     */
    Application getApplication() {
        return mApplication;
    }

    /**
     * 绑定 前台后台状态回调
     */
    void registerForeAndBackgroundCallback(ActiveChangedCallback activeChangedCallback) {
        this.mActiveChangedCallback = activeChangedCallback;
    }

    /**
     * 是否视频播放Activity
     *
     * @param activity 检查到Activity
     * @return 是否视频播放Activity
     */
    private boolean isVideoActivity(Activity activity) {
        return activity instanceof IZegoQueueServiceVideoCallback;
    }

    /**
     * 前台后台状态回调
     */
    interface ActiveChangedCallback {
        // 当应用到了后台，当用户主动跳转到后台，或者电话音频视频通话是触发
        void onApplicationWillResignActive();

        void onApplicationDidBecomeActive();
    }

    public void initContext(final Application context) {
        LogToFileUtils.write("initContext ; 初始化Context");
        mApplication = context;
        ZegoLiveRoom.SDKContext sdkContext = new ZegoLiveRoom.SDKContext() {
            @Nullable
            @Override
            public String getSoFullPath() {
                return null;
            }

            @Nullable
            @Override
            public String getLogPath() {
                return null;
            }

            @NonNull
            @Override
            public Application getAppContext() {
                return context;
            }
        };

        // 设置sdk context 对象，必须要在使用 ZegoLiveRoom 相关方法之前调用，因为里面有so库的加载
        ZegoLiveRoom.setSDKContext(sdkContext);
        // 同上，这个必须要在
        QueueHelper.init(sdkContext);
    }

    public boolean initZegoSDK(ZegoUser zegoUser, long appID, String appSign){
        LogToFileUtils.write("initZegoSDK ; ZegoSDK");
        ZegoLiveRoom.setDomainName(DomainName, UseHttps, useLocalNetwork);         // TODO 测试数据
        ZegoLiveRoom.setUser(zegoUser.userID, zegoUser.userName);
        // 初始化sdk
        boolean ret = mZegoLiveRoom.initSDK(appID, appSign);

        if (!ret) {
            // sdk初始化失败
            Toast.makeText(mApplication, "Zego SDK初始化失败!", Toast.LENGTH_LONG).show();
            return ret;
        }
        // 注册 监听 Activity 生命周期
        mApplication.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // DO NOTHING
            }

            @Override
            public void onActivityStarted(Activity activity) {
                // 保证只有视频播放Activity才会执行前后台切换回调
                if (mActiveChangedCallback != null && isVideoActivity(activity)) {
                    mActiveChangedCallback.onApplicationDidBecomeActive();
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                // DO NOTHING
            }

            @Override
            public void onActivityPaused(Activity activity) {
                // DO NOTHING
            }

            @Override
            public void onActivityStopped(Activity activity) {
                // 保证只有视频播放Activity才会执行前后台切换回调
                if (mActiveChangedCallback != null && isVideoActivity(activity)) {
                    mActiveChangedCallback.onApplicationWillResignActive();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                // DO NOTHING
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                // DO NOTHING
            }
        });
        return ret;
    }
}
