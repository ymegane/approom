package org.ymegane.android.approomcommns.util;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

public class CommonUtil {

    /**
     * バイブレーションを実行
     * @param context
     * @param time
     */
    public static void doVibrate(Context context, int time){

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator != null) {
            vibrator.cancel();

            vibrator.vibrate(time);
        }
    }

    public static Intent createShareIntent(String string) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, string);
        return intent;
    }
}
