package com.icbc.roomservice.kernelcode.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2019/2/25.
 */

public class DateUtil {
    //起止时间
    public static String getoldTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = format.format(new Date(System.currentTimeMillis()));
        return date;
    }
//结束时间
    public static Date currentTime(){
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date enddata = new Date(System.currentTimeMillis());
        return enddata;
    }
    public static String getDateEN() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;
    }

}
