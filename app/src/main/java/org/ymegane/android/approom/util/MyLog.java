package org.ymegane.android.approom.util;

import android.text.TextUtils;
import android.util.Log;

public class MyLog {
    public static boolean IS_LOG_OUTPUT = false;

    public MyLog() {
    }

    public static void setOutputMode(boolean isOutput){
        IS_LOG_OUTPUT = isOutput;
    }

    public static void d(String tag, String message){
        if(IS_LOG_OUTPUT && !TextUtils.isEmpty(message)){
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message){
        if(IS_LOG_OUTPUT && !TextUtils.isEmpty(message)){
            Log.i(tag, message);
        }
    }

    public static void e(String tag, String message, Exception e){
        if(IS_LOG_OUTPUT && !TextUtils.isEmpty(message)){
            Log.e(tag, message, e);
        }
    }

    public static void w(String tag, Exception e){
        if(IS_LOG_OUTPUT){
            Log.w(tag, e);
        }
    }

    public static void w(String tag, String message, Exception e){
        if(IS_LOG_OUTPUT && !TextUtils.isEmpty(message)){
            Log.w(tag, message, e);
        }
    }

    public static void w(String tag, String message){
        if(IS_LOG_OUTPUT && !TextUtils.isEmpty(message)){
            Log.w(tag, message);
        }
    }
}
