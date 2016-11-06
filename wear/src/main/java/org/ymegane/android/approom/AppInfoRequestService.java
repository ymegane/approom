package org.ymegane.android.approom;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.github.ymegane.android.dlog.DLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.ymegane.android.approom.events.BusProvider;
import org.ymegane.android.approom.events.FailureAppInfoRequestEvent;
import org.ymegane.android.approom.events.SuccessAppInfoRequestEvent;

import java.util.concurrent.TimeUnit;

/**
 * mobileにアプリ情報をリクエストするService
 */
public class AppInfoRequestService extends IntentService {
    private static final String TAG = AppInfoRequestService.class.getSimpleName();

    private static final int CONNECT_TIMEOUT_MS = 10000;

    public AppInfoRequestService() {
        super(TAG);
    }

    public static final String REQUEST_APP_INFO = "/request/app_info";
    public static final String RESET_PATH = "/request/reset";

    public static void startAppInfoRequestService(Context context) {
        Intent intent = new Intent(context, AppInfoRequestService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        ConnectionResult result = googleApiClient.blockingConnect(CONNECT_TIMEOUT_MS,
                TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            DLog.w("Failed to connect to GoogleApiClient.");
            BusProvider.getInstance().post(new FailureAppInfoRequestEvent());
            return;
        }

        final NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        if (nodes.getNodes().isEmpty()) {
            DLog.w("Failed to connect to GoogleApiClient.");
            BusProvider.getInstance().post(new FailureAppInfoRequestEvent());
            return;
        }

        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result1 = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), REQUEST_APP_INFO, null).await();
        }
        BusProvider.getInstance().post(new SuccessAppInfoRequestEvent());
    }
}
