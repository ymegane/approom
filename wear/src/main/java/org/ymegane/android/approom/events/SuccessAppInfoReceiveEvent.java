package org.ymegane.android.approom.events;

import org.ymegane.android.approomcommns.domain.model.AppInfo;

import java.util.List;

public class SuccessAppInfoReceiveEvent {
    List<AppInfo> appInfos;

    public SuccessAppInfoReceiveEvent(List<AppInfo> appInfos) {
        this.appInfos = appInfos;
    }

    public List<AppInfo> getAppInfos() {
        return appInfos;
    }
}
