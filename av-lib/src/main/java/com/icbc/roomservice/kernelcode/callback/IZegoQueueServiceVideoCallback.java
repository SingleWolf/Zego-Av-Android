package com.icbc.roomservice.kernelcode.callback;

import com.icbc.roomservice.kernelcode.ResultCode;
import com.icbc.roomservice.kernelcode.block.OptionalBlock;

/**
 * 连麦服务相关回调
 */
public interface IZegoQueueServiceVideoCallback {

    /**
     * 连麦过程发生错误
     * @param resultCode 错误结果对象
     * @param retryBlock 重新连接操作，如果用户需尝试重新连接，执行retryBlock.retry(true);
     */
    void onVideoError(ResultCode resultCode, OptionalBlock retryBlock);

    /**
     * 坐席方主动结束连麦
     * @param finish 坐席结束连麦时，业务是否办理完成。false，坐席中途退出，业务还没办理完成；true，业务完成，坐席主动请求结束连麦
     */
    void onVideoFinish(boolean finish);

    /**
     * SDK音视频引擎停止工作
     * 当手动调用quitVideo()时，SDK会解除对设备的摄像头、麦克风的占用
     */
    void onAVEngineStop();
}
