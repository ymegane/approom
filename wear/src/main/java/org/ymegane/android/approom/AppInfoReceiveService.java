package org.ymegane.android.approom;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ymegane.android.approom.events.BusProvider;
import org.ymegane.android.approom.events.FailureAppInfoReceiveEvent;
import org.ymegane.android.approom.events.SuccessAppInfoReceiveEvent;
import org.ymegane.android.approomcommns.AppInfo;

import java.lang.reflect.Type;
import java.util.List;

public class AppInfoReceiveService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(AppInfoRequestService.REQUEST_APP_INFO)) {
            if (messageEvent.getData() == null) {
                BusProvider.getInstance().post(new FailureAppInfoReceiveEvent());
                return;
            }
            String appJson = new String(messageEvent.getData());
            Type listType = new TypeToken<List<AppInfo>>() {}.getType();
            List<AppInfo> appInfoList = new Gson().fromJson(appJson, listType);
            BusProvider.getInstance().post(new SuccessAppInfoReceiveEvent(appInfoList));
        }
    }
}
