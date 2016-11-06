package org.ymegane.android.approom.domain.usecase;

import org.ymegane.android.approomcommns.domain.model.AppModel;

import java.util.List;

public interface GetInstalledAppListUseCase {
    List<AppModel> getAppModels();
}
