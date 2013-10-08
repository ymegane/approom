package org.ymegane.android.approom;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * アプリ情報の取得リクエストService
 */
public class AppInfoRequestReceiveService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        if (messageEvent.getPath().equals(AppInfoSendService.REQUEST_APP_INFO)) {
            AppInfoSendService.startAppInfoService(getApplicationContext());
        }
    }
}
