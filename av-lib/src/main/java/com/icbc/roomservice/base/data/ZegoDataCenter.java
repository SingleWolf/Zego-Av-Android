package com.icbc.roomservice.base.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.icbc.roomservice.kernelcode.utils.LogToFileUtils;
import com.zego.zegoliveroom.entity.ZegoUser;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * Zego App ID 和 Sign Data，需从Zego主页申请。 //TODO
 */
public class ZegoDataCenter {

    private static final String SP_NAME = "sp_name_base";
    private static final String SP_KEY_USER_ID = "sp_key_user_id";
    private static final String SP_KEY_USER_NAME = "sp_key_user_name";
    private static String TOKEN_URL = "http://114.255.225.35:16000/logintoken";
//    private static String TOKEN_URL="http://rtc1.dccnet.com.cn:16000/logintoken";

    public static final long APP_ID = 1234590;   // TODO 请联系技术支持获取APP_ID

    public static final String APP_SIGN = "12345678901234567890123456798012";   // TODO 请联系技术支持获取APP_SIGN

    public static final ZegoUser ZEGO_USER = new ZegoUser(); // 根据自己情况初始化唯一识别USER

    static {
        ZEGO_USER.userID = getUserID(); // 使用 SERIAL 作为用户的唯一识别
        ZEGO_USER.userName = getUserName();
    }

    public static String getTokenUrl() {
        return TOKEN_URL;
    }

    public static long getAPP_ID() {
        return APP_ID;
    }

    public static String getAPP_SIGN() {
        return APP_SIGN;
    }

    public static void setTokenUrl(String TokenUrl) {
        LogToFileUtils.write("setTokenUrl:" + TokenUrl);
        TOKEN_URL = TokenUrl;
    }

    public static void initUser(String UserID, String UserName) {
        LogToFileUtils.write("initUser:" + UserID + "," + UserName);
        ZEGO_USER.userID = UserID;
        ZEGO_USER.userName = UserName;
    }

    /**
     * 获取保存的UserName，如果没有，则新建
     */
    public static String getUserID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取保存的UserName，如果没有，则新建
     */
    public static String getUserName() {
        String monthAndDay = new SimpleDateFormat("MMdd", Locale.CHINA).format(new Date());
        // 以设备名称 + 时间日期 + 一位随机数  作为用户名
        String userName = Build.MODEL + monthAndDay + new Random().nextInt(10);
        return userName;
    }

    /**
     * 获取用户的额外信息 json字符串 示例代码
     *
     * @return 用户的额外信息 json字符串表示
     */
    public static String getUserExtraInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            // 根据情况生产用户信息对象
            jsonObject.put("phone_number", "12345678901");
        } catch (Exception ignore) {
        }
        return jsonObject.toString();
    }
}
