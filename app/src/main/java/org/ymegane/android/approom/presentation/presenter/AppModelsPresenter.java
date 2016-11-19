package org.ymegane.android.approom.presentation.presenter;

import android.app.Activity;

import org.ymegane.android.approom.databinding.FragmentAppDetailBinding;
import org.ymegane.android.approom.databinding.FragmentAppListBinding;
import org.ymegane.android.approom.domain.usecase.GetInstalledAppListUseCase;
import org.ymegane.android.approomcommns.domain.model.AppModel;

import java.util.List;

public class AppModelsPresenter implements Presenter {
    private Activity mActivity;
    private FragmentAppListBinding mBinding;
    private final GetInstalledAppListUseCase mGetInstalledAppListUseCase;

    public AppModelsPresenter(GetInstalledAppListUseCase getInstalledAppListUseCase) {
        mGetInstalledAppListUseCase = getInstalledAppListUseCase;
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public void setView(FragmentAppListBinding binding) {
        mBinding = binding;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    public void loadAppModels() {
        List<AppModel> list = mGetInstalledAppListUseCase.getAppModels();

    }

    private void showViewLoading() {

    }

    private void hideViewLoading() {

    }
}
