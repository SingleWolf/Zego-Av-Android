package com.icbc.roomservice.kernelcode.utils;

import android.os.Handler;
import android.os.Looper;

public class ThreadUtils {
    private static Handler sUiHandler = new Handler(Looper.getMainLooper());

    /**
     * 在UI线程中执行操作
     * @param action 要执行的操作
     */
    public static void runOnUiThread(Runnable action) {
        if (Thread.currentThread() == sUiHandler.getLooper().getThread())  {
            action.run();
        } else {
            sUiHandler.post(action);
        }
    }
}
