package com.icbc.roomservice.base.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;


/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class UiUtils {
    @TargetApi(19)
    public static boolean setImmersedWindow(Window window, boolean immersive) {
        boolean result = false;
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();

            if (Build.VERSION.SDK_INT < 19) {
                try {
                    int trans_status = 64;
                    Field flags = lp.getClass().getDeclaredField("meizuFlags");
                    flags.setAccessible(true);
                    int value = flags.getInt(lp);
                    if (immersive) {
                        value |= trans_status;
                    } else {
                        value &= ~trans_status;
                    }

                    flags.setInt(lp, value);
                    result = true;
                } catch (Exception var7) {
                    Log.e("StatusBar", "setImmersedWindow: failed");
                }
            } else {
                lp.flags |= 67108864;
                window.setAttributes(lp);
                result = true;
            }
        }

        return result;
    }

    // 沉浸式状态栏
    public static int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int height = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(height);
        } catch (Exception var5) {
            var5.printStackTrace();
            return 75;
        }
    }
}
