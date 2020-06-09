package com.icbc.roomservice.demo.application;

import android.app.Application;
import android.content.Context;

import com.icbc.roomservice.base.data.ZegoDataCenter;
import com.icbc.roomservice.kernelcode.ZegoApiManager;
import com.icbc.roomservice.kernelcode.utils.LogToFileUtils;

public class DemoApplication extends Application{

    public static Context sApplication;

    private static Context mApplication;
    public static Context getMyApplication(){
        return mApplication;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sApplication = base;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication=this;
        //初始化日志输出
        LogToFileUtils.init(this);

        // 初始化ZegoSDK,获取是否成功的返回值
        ZegoDataCenter.initUser("helloworld_3","helloworld_3");
        LogToFileUtils.write("use 114.255.225.35:15000");
        ZegoDataCenter.setTokenUrl("http://114.255.225.35:16000/logintoken");
        ZegoApiManager.setDomainName("114.255.225.35:15000");
        ZegoApiManager.getInstance().initContext(this);
        ZegoApiManager.getInstance().initZegoSDK(ZegoDataCenter.ZEGO_USER, ZegoDataCenter.getAPP_ID(), ZegoDataCenter.getAPP_SIGN());
    }
}
