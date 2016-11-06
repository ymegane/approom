package org.ymegane.android.approom.domain.usecase;

import android.content.Context;
import android.support.annotation.VisibleForTesting;

import org.ymegane.android.approom.data.repository.InstalledAppRepositoryImpl;
import org.ymegane.android.approom.data.repository.PreferenceSettingsRepository;
import org.ymegane.android.approom.domain.repository.InstalledAppRepository;
import org.ymegane.android.approom.domain.repository.SettingsRepository;
import org.ymegane.android.approomcommns.domain.model.AppModel;

import java.util.List;

public class GetInstalledAppListUseCaseImpl implements GetInstalledAppListUseCase {

    private SettingsRepository mSettingsRepository;
    private InstalledAppRepository mInstalledAppRepository;

    public GetInstalledAppListUseCaseImpl(Context context) {
        this(new InstalledAppRepositoryImpl(context), new PreferenceSettingsRepository(context));
    }

    @VisibleForTesting
    public GetInstalledAppListUseCaseImpl(InstalledAppRepository repository, SettingsRepository settingsRepository) {
        mInstalledAppRepository = repository;
        mSettingsRepository = settingsRepository;
    }

    @Override
    public List<AppModel> getAppModels() {
        return mInstalledAppRepository.getInstalledAppList();
    }
}
