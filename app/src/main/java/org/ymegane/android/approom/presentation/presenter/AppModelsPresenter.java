package org.ymegane.android.approom.presentation.presenter;

import android.app.Activity;

import org.ymegane.android.approom.domain.usecase.GetInstalledAppListUseCase;

public class AppModelsPresenter implements Presenter {
    private final GetInstalledAppListUseCase mGetInstalledAppListUseCase;
    private Activity mActivity;

    public AppModelsPresenter(GetInstalledAppListUseCase getInstalledAppListUseCase) {
        mGetInstalledAppListUseCase = getInstalledAppListUseCase;
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    public void loadAppModels() {

    }

    private void showViewLoading() {

    }

    private void hideViewLoading() {

    }
}
