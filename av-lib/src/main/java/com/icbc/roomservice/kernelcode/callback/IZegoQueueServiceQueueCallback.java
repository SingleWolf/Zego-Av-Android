package com.icbc.roomservice.kernelcode.callback;

import android.support.annotation.Nullable;

import com.icbc.roomservice.kernelcode.ResultCode;
import com.icbc.roomservice.kernelcode.block.OptionalBlock;

/**
 * 队列服务相关回调
 */
public interface IZegoQueueServiceQueueCallback {

    /**
     * 加入队列结果回调
     * @param resultCode 加入队列回调结果对象
     * @param optionalBlock 重试代码块，当resultCode.isSuccess() = true 的时候，optionalBlock = null
     *                                当resultCode.isSuccess() = true 的时候，即为异常情况，重试可以执行optionalBlock.execute(true)
     */
    void onEnqueue(ResultCode resultCode, OptionalBlock optionalBlock);

    /**
     * 当前用户在队列位置 更新回调 PS:该回调为定时回调，而非队列状态改变后才进行回调
     * @param index 当前在队列中的Index（最小为1）
     */
    void onQueueIndexUpdate(int index);

    /**
     * 坐席成功匹配用户，等候用户回复是否同意开始服务
     * @param timeout 等待超时时间
     * @param replyBlock 答复操作， 如果答应，需执行replyBlock.reply(true)
     *                   否则，执行replyBlock.reply(false)
     */
    void onReceiveServiceShouldStart(int timeout, OptionalBlock replyBlock);

    /**
     * 接受或者拒绝服务的响应回调
     * @param resultCode 响应回调结果对象
     * @param retryBlock 再次尝试操作，用户按需执行retryBlock.retry(true)执行再次尝试操作，
     *                   执行retryBlock.retry(false)不执行尝试操作
     *                   当resultCode.isSuccess() == true 的时候，retryBlock将为null；
     */
    void onReplyServiceComplete(ResultCode resultCode, @Nullable OptionalBlock retryBlock);

    /**
     * 当没有登录的座席
     */
    void onNoStaff();

    /**
     * 坐席已接通的回调
     */
    void onStaffCatch();
}
