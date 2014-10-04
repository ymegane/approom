package org.ymegane.android.approom;

import android.content.pm.ApplicationInfo;

/**
 * アプリ情報
 * @author y
 */
public class AppInfo {

    ApplicationInfo appInfo;
    long lastModify;
    String appName;
    String packageName;
    int iconResId;
    boolean isStoped;

    @Override
    public String toString() {
        return appName;
    }
}
