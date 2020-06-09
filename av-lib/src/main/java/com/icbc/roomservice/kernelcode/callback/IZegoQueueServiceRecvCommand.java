package com.icbc.roomservice.kernelcode.callback;

/**
 * 收到其他指令的处理
 */
public interface IZegoQueueServiceRecvCommand {

    void onRecvCommand(String userID, String userName, String content, String roomID);
}
