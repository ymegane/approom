package org.ymegane.android.approom;

import android.app.Application;

import com.github.ymegane.android.dlog.DLog;

public class Approom extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DLog.init(this);
    }
}
