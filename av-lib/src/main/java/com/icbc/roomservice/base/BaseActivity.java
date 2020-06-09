package com.icbc.roomservice.base;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.icbc.roomservice.kernelcode.ZegoQueueService;
import com.icbc.roomservice.kernelcode.block.FetchTokenBlock;
import com.icbc.roomservice.kernelcode.callback.IZegoQueueServiceFetchTokenCallback;
import com.icbc.roomservice.base.data.TokenHelper;
import com.icbc.roomservice.base.data.ZegoDataCenter;
import com.icbc.roomservice.base.utils.UiUtils;
import com.icbc.roomservice.base.view.TipDialog;
import com.zego.zegoliveroom.constants.ZegoAvConfig;

/**
 * Activity 抽象基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    /**
     * 分辨率设置
     */
    public static int CaptureResolutionWidth=450;
    public static int CaptureResolutionHeight=600;

    /**
     * 申请权限 code
     */
    private static final int PERMISSIONS_REQUEST_CODE = 1002;

    /**
     * 队列服务类
     */
    protected static ZegoQueueService mZegoQueueService;

    /**
     * 提示Dialog
     */
    private TipDialog mTipDialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        if (mZegoQueueService == null) {
            // 新建 推拉流码率配置
            // 码率为600kbps，帧率15
            ZegoAvConfig avConfig = new ZegoAvConfig(ZegoAvConfig.Level.Generic);
            // 视频采集分辨率
            avConfig.setVideoCaptureResolution(CaptureResolutionWidth, CaptureResolutionHeight);
            // 视频编码分辨率
            avConfig.setVideoEncodeResolution(CaptureResolutionWidth, CaptureResolutionHeight);
            mZegoQueueService = ZegoQueueService.serviceWithUser(ZegoDataCenter.ZEGO_USER, avConfig);
            // 设置FetchToken
            mZegoQueueService.setFetchTokenCallback(new IZegoQueueServiceFetchTokenCallback() {
                @Override
                public void fetchLoginQueueToke (FetchTokenBlock fetchTokenBlock){
                    TokenHelper.fetchQueueLoginToken(fetchTokenBlock,ZegoDataCenter.APP_ID,ZegoDataCenter.ZEGO_USER.userID,ZegoDataCenter.ZEGO_USER.userName,ZegoDataCenter.getTokenUrl());
                }
            });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置沉浸式布局
        UiUtils.setImmersedWindow(getWindow(), true);

        // 申请权限
        checkOrRequestPermission(PERMISSIONS_REQUEST_CODE);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 设置沉浸式布局
        if (Build.VERSION.SDK_INT > 18) {
            getContentView().setPadding(0, UiUtils.getStatusBarHeight(this), 0, 0);
        }
    }

    /**
     * 获取contentView
     *
     * @return 返回contentView
     */
    protected View getContentView() {
        ViewGroup contentLayout = getWindow().getDecorView().findViewById(android.R.id.content);
        return contentLayout != null && contentLayout.getChildCount() != 0 ? contentLayout.getChildAt(0) : null;
    }

    /**
     * 懒加载TipDialog
     *
     * @return 返回页面公用的TipDialog
     */
    public TipDialog getTipDialog() {
        if (mTipDialog == null) {
            mTipDialog = new TipDialog(this);
        }
        return mTipDialog;
    }

    // 相机存储音频权限申请
    private static String[] PERMISSIONS_REQUEST = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    /**
     * 检查并申请权限
     *
     * @param requestCode requestCode
     * @return 权限是否已经允许
     */
    protected boolean checkOrRequestPermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(PERMISSIONS_REQUEST, requestCode);
                return false;
            }
        }
        return true;
    }
}
