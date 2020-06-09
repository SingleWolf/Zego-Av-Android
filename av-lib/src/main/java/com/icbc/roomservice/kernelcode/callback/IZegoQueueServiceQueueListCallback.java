package com.icbc.roomservice.kernelcode.callback;

import com.icbc.roomservice.kernelcode.ResultCode;
import com.zego.queue.QueueInfo;
import java.util.List;

/**
 *  获取队列列表信息回调
 */
public interface IZegoQueueServiceQueueListCallback {

    /**
     * 获取队列列表信息回调
     * @param resultCode 结果对象， resultCode.isSuccess() 判断是否出现异常
     * @param queueInfos 队列列表信息，如果!resultCode.isSuccess()，则队列列表信息为空
     */
    void onQueueList(ResultCode resultCode, List<QueueInfo> queueInfos);
}
