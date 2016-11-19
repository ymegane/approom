package org.ymegane.android.approom.presentation.view.fragment;

import java.util.List;

import org.ymegane.android.approom.data.repository.AppInstallComparator;
import org.ymegane.android.approom.BuildConfig;
import org.ymegane.android.approom.data.repository.InstalledAppRepositoryImpl;
import org.ymegane.android.approom.data.repository.PreferenceSettingsRepository;
import org.ymegane.android.approom.databinding.FragmentApplistBinding;
import org.ymegane.android.approom.domain.repository.SettingsRepository;
import org.ymegane.android.approom.presentation.view.adapter.GridAppsAdapter;
import org.ymegane.android.approom.R;
import org.ymegane.android.approomcommns.domain.model.AppModel;
import org.ymegane.android.approomcommns.AppLinkBase;
import org.ymegane.android.approomcommns.util.CommonUtil;
import org.ymegane.android.approomcommns.util.HtmlUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.github.ymegane.android.dlog.DLog;
import com.trello.rxlifecycle.components.support.RxFragment;

import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * アプリ一覧表示Fragment
 */
public class AppDisplayFragment extends RxFragment implements Toolbar.OnMenuItemClickListener {
    public static final String TAG = "AppDisplayFragment";

    FragmentApplistBinding mBinding;

    private GridAppsAdapter adapter;
    //private ListView listAppView;
    private OnAppInfoClickListener clickListener;

    private List<AppModel> mAppModelList;

    private SettingsRepository mSettingsRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            clickListener = (OnAppInfoClickListener) context;
        }catch(ClassCastException e) {
            DLog.e("unimplements Listener!!", e);
        }
        mSettingsRepository = new PreferenceSettingsRepository(context);
    }

    private ActionMode actionMode;
    private ShareActionProvider shareActionProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_applist, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding = DataBindingUtil.bind(getView());

        initActionBar();

        mBinding.gridAppIcon.setTextFilterEnabled(true);
        mBinding.gridAppIcon.setOnItemClickListener(new ItemClickListener());
        if (!clickListener.isMashroom()) {
            mBinding.gridAppIcon.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    ((AppCompatActivity)getActivity()).startSupportActionMode(new android.support.v7.view.ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
                            AppDisplayFragment.this.actionMode = actionMode;

                            actionMode.setTitle(R.string.action_mode_title_share);

                            shareActionProvider = new ShareActionProvider(getActivity());
                            MenuItem item = menu.add(getString(R.string.share)).setIcon(R.drawable.ic_share_24dp);
                            MenuItemCompat.setActionProvider(item, shareActionProvider);
                            MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
                            updateMultipleChoiceState(position);
                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(android.support.v7.view.ActionMode actionMode, MenuItem menuItem) {
                            return false;
                        }

                        @Override
                        public void onDestroyActionMode(android.support.v7.view.ActionMode actionMode) {
                            AppDisplayFragment.this.actionMode = null;
                            shareActionProvider = null;
                            adapter.resetCheckedState();
                            adapter.notifyDataSetChanged();
                        }
                    });
                    return true;
                }
            });
        }

        // 読み込み開始
        if (mAppModelList != null) {
            mBinding.layoutProgress.setVisibility(View.GONE);
            setGridAdapter(mAppModelList);
        } else {
            mBinding.layoutProgress.setVisibility(View.VISIBLE);

            Observable.create(new Observable.OnSubscribe<List<AppModel>>(){
                @Override
                public void call(Subscriber<? super List<AppModel>> subscriber) {
                    subscriber.onNext(new InstalledAppRepositoryImpl(getContext()).getInstalledAppList());
                    subscriber.onCompleted();
                }

            }).compose(this.<List<AppModel>>bindToLifecycle())
            .subscribeOn(Schedulers.newThread())
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

                    View view = getView();
                    if (view == null) {
                        return;
                    }
                    mBinding.layoutProgress.setVisibility(View.GONE);
                    if(appModels != null) {
                        mAppModelList = appModels;
                        setGridAdapter(mAppModelList);
                    }
                }
            });
        }
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.desplay_menu);

        // SearchViewの初期化
        initSearchView(toolbar.getMenu().findItem(R.id.item_search));

        toolbar.setOnMenuItemClickListener(this);
    }

    private int lastGridPosition;
    @Override
    public void onPause() {
        super.onPause();
        lastGridPosition = mBinding.gridAppIcon.getFirstVisiblePosition();

        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mBinding.gridAppIcon.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void initSearchView(MenuItem item) {
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                updateSearchResult(s);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                updateSearchResult(null);
                return false;
            }
        });
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
    }

    private void updateSearchResult(String word) {
        Filter filter = ((Filterable) mBinding.gridAppIcon.getAdapter()).getFilter();
        if (TextUtils.isEmpty(word)) {
            filter.filter("");
        } else {
            filter.filter(word);
        }
    }

    private void setGridAdapter(List<AppModel> appModelList) {
        adapter = new GridAppsAdapter(getActivity(), appModelList);
        mBinding.gridAppIcon.setAdapter(adapter);
        // スクロール位置を復元
        mBinding.gridAppIcon.setSelection(lastGridPosition);
    }

    /**
     * 表示するViewを切り替える
     */
    public void switchViewVisibility() {
        if(mBinding.gridAppIcon.getVisibility() == View.VISIBLE) {
            mBinding.gridAppIcon.setVisibility(View.GONE);
            //listAppView.setVisibility(View.VISIBLE);
        }else {
            mBinding.gridAppIcon.setVisibility(View.VISIBLE);
            //listAppView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item_sort_install:
                mSettingsRepository.setSortType(AppInstallComparator.MODE_INSTALL);
                adapter.setNotifyOnChange(true);
                adapter.sort(new AppInstallComparator().setMode(AppInstallComparator.MODE_INSTALL));
                return true;
            case R.id.item_sort_alpabet:
                mSettingsRepository.setSortType(AppInstallComparator.MODE_NAME);
                adapter.setNotifyOnChange(true);
                adapter.sort(new AppInstallComparator().setMode(AppInstallComparator.MODE_NAME));
                return true;
            case R.id.item_appinfo:
                new AppInfoDialog().show(getFragmentManager(), "AppInfoDialog");
                return true;
            case R.id.item_setting:
                if (clickListener != null) {
                    clickListener.onOpenSetting();
                }
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * グリッド/リストアイテムのクリックリスナー
     * @author y
     *
     */
    private class ItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if (actionMode != null) { // 複数選択モード
                updateMultipleChoiceState(position);
            } else {
                updateSearchResult(null);

                AppModel info = (AppModel) mBinding.gridAppIcon.getItemAtPosition(position);
                clickListener.onItemClick(view, info);
            }
        }
    }

    private void updateMultipleChoiceState(int position) {
        adapter.toggleCheckState(position);
        adapter.notifyDataSetChanged();
        if (adapter.getCheckedStates().size() <= 0) {
            actionMode.finish();
            return;
        }
        SparseBooleanArray checkedItems = adapter.getCheckedStates();

        actionMode.setSubtitle(getString(R.string.action_mode_subtitle_share, checkedItems.size()));

        StringBuilder appListStr = new StringBuilder();
        for (int idx=0; idx<checkedItems.size(); idx++) {
            int pos = checkedItems.keyAt(idx);
            AppModel appModel = (AppModel) mBinding.gridAppIcon.getItemAtPosition(pos);
            appListStr.append(appModel.getAppName()).append(":").append(AppLinkBase.LINK_HTTP_DETAIL).append(appModel.getPackageName()).append("\n\n");
        }

        shareActionProvider.setShareIntent(CommonUtil.createShareIntent(appListStr.toString()));
    }

    /**
     * アプリ情報選択リスナー
     * @author y
     *
     */
    public interface OnAppInfoClickListener {
        void onItemClick(View view, AppModel info);
        boolean isMashroom();
        void onOpenSetting();
    }

    public static class AppInfoDialog extends DialogFragment implements OnClickListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable  Bundle savedInstanceState) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_appinfo, null, false);

            // LinkMovementMethod のインスタンスを取得します
            MovementMethod movementmethod = LinkMovementMethod.getInstance();

            TextView textVersion = (TextView) view.findViewById(R.id.textVersion);
            textVersion.setText(getString(R.string.version, BuildConfig.VERSION_NAME));

            TextView textDev = (TextView) view.findViewById(R.id.textDeveloperName);
            textDev.setText(HtmlUtil.fromHtml(getString(R.string.creater)));
            textDev.setMovementMethod(movementmethod);

            setLicence(view, movementmethod, R.id.textZxing, R.string.zxing_name, R.id.textZxingLicense, R.string.apache_license);
            setLicence(view, movementmethod, R.id.textNfcFelica, R.string.nfcfelica_name, R.id.textNfcFelicaLicense, R.string.apache_license);
            setLicence(view, movementmethod, R.id.textGson, R.string.gson_name, R.id.textGsonLicense, R.string.apache_license);
            setLicence(view, movementmethod, R.id.textOtto, R.string.otto_name, R.id.textOttoLicense, R.string.apache_license);
            setLicence(view, movementmethod, R.id.textBtkn, R.string.butterknife_name, R.id.textBtknLicense, R.string.apache_license);
            setLicence(view, movementmethod, R.id.textPicasso, R.string.picasso_name, R.id.textPicassoLicense, R.string.apache_license);

            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setIcon(R.drawable.ic_info_24dp);
            dialog.setTitle(R.string.appinfo);
            dialog.setView(view);
            dialog.setPositiveButton(android.R.string.ok, this);
            return dialog.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // 何もしない
        }

        private static void setLicence(View view, MovementMethod movementMethod, @IdRes int textId, @StringRes int textRes, @IdRes int licenseId, @StringRes int licenseRes) {
            Resources res = view.getResources();
            TextView text = ButterKnife.findById(view, textId);
            text.setText(HtmlUtil.fromHtml(res.getString(textRes)));
            text.setMovementMethod(movementMethod);

            TextView textL = ButterKnife.findById(view, licenseId);
            textL.setText(HtmlUtil.fromHtml(res.getString(licenseRes)));
            textL.setMovementMethod(movementMethod);
        }
    }
}