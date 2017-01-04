package org.ymegane.android.approom.presentation.presenter;

import android.app.Activity;
import android.view.View;

import com.github.ymegane.android.dlog.DLog;
import com.trello.rxlifecycle.components.RxActivity;

import org.ymegane.android.approom.data.repository.InstalledAppRepositoryImpl;
import org.ymegane.android.approom.databinding.FragmentAppListBinding;
import org.ymegane.android.approom.domain.usecase.GetInstalledAppListUseCase;
import org.ymegane.android.approom.presentation.view.adapter.GridAppsAdapter;
import org.ymegane.android.approomcommns.domain.model.AppModel;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AppModelsPresenter implements Presenter {
    private RxActivity mActivity;
    private FragmentAppListBinding mBinding;
    private final GetInstalledAppListUseCase mGetInstalledAppListUseCase;

    private List<AppModel> mAppModelList;
    private GridAppsAdapter mGridAppsAdapter;
    private int lastGridPosition;

    public AppModelsPresenter(GetInstalledAppListUseCase getInstalledAppListUseCase) {
        mGetInstalledAppListUseCase = getInstalledAppListUseCase;
    }

    public void setActivity(RxActivity activity) {
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

    public void setMashroomListenerIfNeeded() {
        
    }

    public void loadAppModels() {
        showViewLoading();

        Observable.create(new Observable.OnSubscribe<List<AppModel>>(){
            @Override
            public void call(Subscriber<? super List<AppModel>> subscriber) {
                subscriber.onNext(mGetInstalledAppListUseCase.getAppModels());
                subscriber.onCompleted();
            }
        })
                .compose(mActivity.bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<AppModel>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<AppModel> appModels) {
                        DLog.printMethod();

                        hideViewLoading();

                        if(appModels != null) {
                            mAppModelList = appModels;
                            setGridAdapter(mAppModelList);
                        }
                    }
                });
    }

    private void showViewLoading() {
        mBinding.layoutProgress.setVisibility(View.VISIBLE);
    }

    private void hideViewLoading() {
        mBinding.layoutProgress.setVisibility(View.GONE);
    }

    private void setGridAdapter(List<AppModel> appModelList) {
        mGridAppsAdapter = new GridAppsAdapter(mActivity, appModelList);
        mBinding.gridAppIcon.setAdapter(mGridAppsAdapter);
        // スクロール位置を復元
        mBinding.gridAppIcon.setSelection(lastGridPosition);
    }

}
