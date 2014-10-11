package org.ymegane.android.approom.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Vibrator;

public class CommonUtil {

    /**
     * アプリバージョンを取得する
     * @param context
     * @return
     */
    public static String getAppVersion(Context context){
        String ver;
        try {
            ver = context.getPackageManager().getPackageInfo( context.getPackageName(), 1 ).versionName;
        } catch (NameNotFoundException e) {
            ver = "";
        }
        return ver;
    }

    /**
     * バイブレーションを実行
     * @param context
     * @param time
     */
    public static void doViblate(Context context, int time){

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator != null) {
            vibrator.cancel();

            vibrator.vibrate(time);
        }
    }

    /**
     * マニフェストファイルからデバッグモードかどうかを取得する
     * @param context
     * @return
     */
    public static boolean isDebuggable(Context context) {
        PackageManager manager = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = manager.getApplicationInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE)
            return true;
        return false;
    }

    public static Intent createShareIntent(String string) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, string);
        return intent;
    }
}
