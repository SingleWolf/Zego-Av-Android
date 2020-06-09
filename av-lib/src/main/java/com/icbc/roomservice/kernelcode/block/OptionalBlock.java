package com.icbc.roomservice.kernelcode.block;

/**
 * 可选择执行代码块
 */
public interface OptionalBlock {

    /**
     * 执行已定义操作
     * @param option 是否执行尝试操作 true 为尝试执行操作，false 放弃执行尝试操作
     */
    void execute(boolean option);
}
