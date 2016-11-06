package org.ymegane.android.approom.domain.repository;

public interface SettingsRepository {
    int getSortType();
    void setSortType(int type);

    int getLinkType();
    void setLinkType(int type);

    boolean includeSystemApp();
    boolean includeDisabledApp();
}
