package com.icbc.roomservice.kernelcode.callback;

import com.icbc.roomservice.kernelcode.block.FetchTokenBlock;

/**
 * 获取token回调类
 */
public interface IZegoQueueServiceFetchTokenCallback {
    /**
     * 获取token回调方法
     * @param fetchTokenBlock 刷新token后执行的代码块，刷新获取token后，执行fetchTokenBlock.fetchTokenBlock([token])
     */
    void fetchLoginQueueToke(FetchTokenBlock fetchTokenBlock);
}
