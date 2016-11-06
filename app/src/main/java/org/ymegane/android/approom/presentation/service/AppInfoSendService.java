package org.ymegane.android.approom.presentation.service;

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
import com.google.gson.Gson;

import org.ymegane.android.approom.data.repository.InstalledAppRepositoryImpl;
import org.ymegane.android.approom.domain.exception.ErrorBundle;
import org.ymegane.android.approom.domain.repository.InstalledAppRepository;
import org.ymegane.android.approomcommns.domain.model.AppModel;

import java.util.Collection;
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
        InstalledAppRepositoryImpl repository = new InstalledAppRepositoryImpl(getApplicationContext())
                .setLoadPallet(true);

        handleAppModels(repository.getInstalledAppList());
    }

    private void handleAppModels(Collection<AppModel> appModels) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        ConnectionResult result = googleApiClient.blockingConnect(CONNECT_TIMEOUT_MS,
                TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            DLog.w("Failed to connect to GoogleApiClient.");
            return;
        }

        if (appModels.size() > 20) {
            //appModelList = appModelList.subList(0, 20);
        }
        String jsonStr = new Gson().toJson(appModels);
        DLog.d("appListJson " + jsonStr);

        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        if (nodes.getNodes().isEmpty()) {
            DLog.w("Failed to connect.");
            return;
        }
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result1 = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), REQUEST_APP_INFO, jsonStr.getBytes()).await();
            if (!result1.getStatus().isSuccess()) {
                DLog.w("Failed to send.");
            }
        }
    }
}
