package com.icbc.roomservice.kernelcode.callback;

import com.icbc.roomservice.kernelcode.ResultCode;
import com.icbc.roomservice.kernelcode.event.ZegoQueueConnectEvent;

public interface IZegoQueueServiceConnectEventCallback {

    /**
     * 收到队列服务连接事件通知
     * @param connectEvent {@link ZegoQueueConnectEvent} 队列服务连接事件
     * @param resultCode 具体结果对象
     */
    void onReceiveConnectEvent(int connectEvent, ResultCode resultCode);


}
