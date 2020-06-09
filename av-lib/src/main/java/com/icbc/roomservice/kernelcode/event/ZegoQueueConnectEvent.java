package com.icbc.roomservice.kernelcode.event;

public final class ZegoQueueConnectEvent {

    /**
     * 多端登录被踢出队列服务
     */
    public final static int KICK_OUT = -1;
    /**
     * 与 server 重连成功，会自动 重新登录队列服务 和 重新进入队列
     */
    public final static int RECONNECT = 1;
    /**
     * 与 server 中断，SDK会尝试自动重连
     */
    public final static int TEMP_BROKEN = 2;
    /**
     * 与 server 断开
     */
    public final static int DISCONNECT = 3;

    /**
     * 执行 RECONNECT 重连后 重新登录队列服务成功，但进入队列失败
     */
    public final static int REENTER_QUEUE_FAILED = 11;
}