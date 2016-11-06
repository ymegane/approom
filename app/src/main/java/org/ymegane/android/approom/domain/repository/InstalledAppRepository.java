package org.ymegane.android.approom.domain.repository;

import org.ymegane.android.approomcommns.domain.model.AppModel;

import java.util.List;

public interface InstalledAppRepository {
    List<AppModel> getInstalledAppList();
}
