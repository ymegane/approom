package org.ymegane.android.approom;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import org.ymegane.android.approom.events.BusProvider;
import org.ymegane.android.approom.events.FailureAppInfoRequestEvent;
import org.ymegane.android.approom.events.SuccessAppInfoRequestEvent;
import org.ymegane.android.approomcommns.util.MyLog;

import java.util.concurrent.TimeUnit;

/**
 * mobileにアプリ情報をリクエストするService
 */
public class AppInfoRequestService extends IntentService {
    private static final String TAG = AppInfoRequestService.class.getSimpleName();

    private static final int CONNECT_TIMEOUT_MS = 100;

    public AppInfoRequestService() {
        super(TAG);
    }

    public static final String REQUEST_APP_INFO = "request/app_info";
    public static final String RESET_PATH = "request/reset";

    @Override
    protected void onHandleIntent(Intent intent) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        ConnectionResult result = googleApiClient.blockingConnect(CONNECT_TIMEOUT_MS,
                TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            MyLog.w(TAG, "Failed to connect to GoogleApiClient.");
            BusProvider.getInstance().post(new FailureAppInfoRequestEvent());
            return;
        }
        Wearable.MessageApi.sendMessage(googleApiClient, REQUEST_APP_INFO, RESET_PATH, null);
        BusProvider.getInstance().post(new SuccessAppInfoRequestEvent());
    }
}
