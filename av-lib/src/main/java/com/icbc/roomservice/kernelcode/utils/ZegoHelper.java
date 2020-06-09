package com.icbc.roomservice.kernelcode.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.telephony.TelephonyManager;

import com.zego.zegoliveroom.entity.ZegoUser;

public class ZegoHelper {

    /**
     * 根据用户生成流ID
     *
     * @param user 用户
     * @return 生成的流ID
     */
    public static String getStreamIDWithUser(ZegoUser user) {
        return "c-" + user.userID;
    }

    /**
     * 检查流是否是坐席的流
     *
     * @param streamID 检查的流ID
     * @return 返回是否是坐席的流
     */
    public static boolean isStuffStream(String streamID) {
        return true; //修改位置
    }

    /**
     * @param context context对象
     * @return 是否在通话或者响铃中
     */
    public static boolean isInCall(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int callState = tm.getCallState();
        return callState != TelephonyManager.CALL_STATE_IDLE;
    }

    /**
     * @param context context对象
     * @return 是否授予了摄像头权限
     */
    public static boolean checkCameraPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            return (context.checkPermission(Manifest.permission.CAMERA, Process.myPid(), Process.myUid())
                    == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    /**
     * @param context context对象
     * @return 是否授予了麦克风权限
     */
    public static boolean checkMicrophonePermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            return (context.checkPermission(Manifest.permission.RECORD_AUDIO, Process.myPid(), Process.myUid())
                    == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }
}
