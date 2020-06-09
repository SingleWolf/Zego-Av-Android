package com.icbc.roomservice.base.data;

import com.icbc.roomservice.kernelcode.utils.LogToFileUtils;
import com.icbc.roomservice.kernelcode.block.FetchTokenBlock;
import com.icbc.roomservice.kernelcode.utils.ThreadUtils;
import com.zego.zegoliveroom.constants.ZegoConstants;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * token 获取代码实例
 */
public class TokenHelper {

    public static void fetchQueueLoginToken(final FetchTokenBlock fetchTokenBlock, final long APP_ID, final String userID, final String userName, final String TOKEN_URL) {
        LogToFileUtils.write( "fetchTokenAndLoginQueue jsonObject: ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int QUEUE_ROLE_CUSTOMER = 10;
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("timestamp", System.currentTimeMillis());
                    jsonObject.put("app_id", APP_ID);
                    jsonObject.put("user_id", userID);
                    jsonObject.put("user_name", userName);
                    jsonObject.put("queue_role", QUEUE_ROLE_CUSTOMER);
                    jsonObject.put("room_role", ZegoConstants.RoomRole.Audience);
                } catch (Exception ignore) {
                }
                LogToFileUtils.write("fetchTokenAndLoginQueue jsonObject: " + jsonObject.toString());
                byte[] data = jsonObject.toString().getBytes();
                try {
                    URL url = new URL(TOKEN_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    outputStream.write(data);
                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] temp = new byte[1024];
                        int len;
                        while ((len = inputStream.read(temp)) != -1) {
                            out.write(temp, 0, len);
                        }
                        JSONObject responseJson = new JSONObject(out.toString());
                        final String token = responseJson.get("login_token").toString();
                        LogToFileUtils.write("fetchTokenAndLoginQueue token: " + token);
                        // 拉取token 成功
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fetchTokenBlock.fetchTokenBlock((token));
                            }
                        });
                        return;
                    } else {
                        LogToFileUtils.write( "fetchTokenAndLoginQueue != 200");
                    }
                } catch (Exception exception) {
                    LogToFileUtils.write("fetchTokenAndLoginQueue exception: " + exception.getMessage());
                }
                // 拉取token 出错，以空字符串登录触发登录异常
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fetchTokenBlock.fetchTokenBlock("");
                    }
                });
            }
        }).start();
    }
}

