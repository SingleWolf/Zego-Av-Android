package com.icbc.roomservice.kernelcode.block;

import com.icbc.roomservice.kernelcode.ResultCode;

public interface LoginBlock {
    /**
     * 登录后逻辑执行代码块
     * @param resultCode 执行错误码
     */
    void onLogin(ResultCode resultCode);
}
