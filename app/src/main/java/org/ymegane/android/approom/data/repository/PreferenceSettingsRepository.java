package org.ymegane.android.approom.data.repository;

import android.content.Context;

import org.ymegane.android.approom.domain.repository.SettingsRepository;

public class PreferenceSettingsRepository implements SettingsRepository {
    private AppPrefs mPrefs;

    public PreferenceSettingsRepository(Context context) {
       mPrefs = AppPrefs.newInstance(context);
    }

    @Override
    public int getSortType() {
        return mPrefs.getSortType();
    }

    @Override
    public void setSortType(int type) {
        mPrefs.saveSortType(type);
    }

    @Override
    public int getLinkType() {
        return mPrefs.getLinkType();
    }

    @Override
    public void setLinkType(int type) {
        mPrefs.saveLinkType(type);
    }

    @Override
    public boolean includeSystemApp() {
        return mPrefs.isIncludeSystemApp();
    }

    @Override
    public boolean includeDisabledApp() {
        return mPrefs.isIncludeDisableApp();
    }
}
