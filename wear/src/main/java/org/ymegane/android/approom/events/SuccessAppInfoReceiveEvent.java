package org.ymegane.android.approom.events;

import org.ymegane.android.approomcommns.domain.model.AppModel;

import java.util.List;

public class SuccessAppInfoReceiveEvent {
    List<AppModel> mAppModels;

    public SuccessAppInfoReceiveEvent(List<AppModel> appModels) {
        this.mAppModels = appModels;
    }

    public List<AppModel> getAppModels() {
        return mAppModels;
    }
}
