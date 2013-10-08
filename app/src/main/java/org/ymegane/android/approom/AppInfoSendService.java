package org.ymegane.android.approom;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import org.ymegane.android.approomcommns.AppInfo;
import org.ymegane.android.approomcommns.util.MyLog;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * アプリ情報の送信Service
 */
public class AppInfoSendService extends IntentService {
    private static final String TAG = AppInfoSendService.class.getSimpleName();

    public AppInfoSendService() {
        super(TAG);
    }

    private static final int CONNECT_TIMEOUT_MS = 10000;
    public static final String REQUEST_APP_INFO = "/request/app_info";
    public static final String RESET_PATH = "/request/reset";

    public static void startAppInfoService(Context context) {
        Intent intent = new Intent(context, AppInfoSendService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        ConnectionResult result = googleApiClient.blockingConnect(CONNECT_TIMEOUT_MS,
                TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            MyLog.w(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }
        List<AppInfo> appInfoList = AppInfoLoader.getAppInfo(getApplicationContext(), true);
        if (appInfoList.size() > 20) {
            //appInfoList = appInfoList.subList(0, 20);
        }
        String jsonStr = new Gson().toJson(appInfoList);
        MyLog.d(TAG, "appListJson " + jsonStr);

        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        if (nodes.getNodes().isEmpty()) {
            MyLog.w(TAG, "Failed to connect.");
            return;
        }
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result1 = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), REQUEST_APP_INFO, jsonStr.getBytes()).await();
            if (!result1.getStatus().isSuccess()) {
                MyLog.w(TAG, "Failed to send.");
            }
        }
    }
}
