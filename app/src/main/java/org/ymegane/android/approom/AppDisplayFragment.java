package org.ymegane.android.approom;

import java.util.List;

import org.ymegane.android.approom.preference.AppPrefs;
import org.ymegane.android.approomcommns.AppInfo;
import org.ymegane.android.approomcommns.AppLinkBase;
import org.ymegane.android.approomcommns.util.CommonUtil;
import org.ymegane.android.approomcommns.util.HtmlUtil;
import org.ymegane.android.approomcommns.util.MyLog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import android.widget.GridView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * アプリ一覧表示Fragment
 */
public class AppDisplayFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AppInfo>>, Toolbar.OnMenuItemClickListener {
    public static final String TAG = "AppDisplayFragment";

    @Bind(R.id.layoutProgress)
    protected View layoutProgress;
    @Bind(R.id.gridAppIcon)
    protected GridView gridAppView;

    private GridAppsAdapter adapter;
    //private ListView listAppView;
    private OnAppInfoClickListener clickListener;

    private List<AppInfo> appInfoList;

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
            MyLog.e(TAG, "unimplements Listener!!", e);
        }
    }

    private ActionMode actionMode;
    private ShareActionProvider shareActionProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applist, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initActionBar();

        gridAppView.setTextFilterEnabled(true);
        gridAppView.setOnItemClickListener(new ItemClickListener());
        if (!clickListener.isMashroom()) {
            gridAppView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
        if (appInfoList != null) {
            layoutProgress.setVisibility(View.GONE);
            setGridAdapter(appInfoList);
        } else {
            LoaderManager loaderMng = getLoaderManager();
            loaderMng.initLoader(0, null, this);
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
        lastGridPosition = gridAppView.getFirstVisiblePosition();

        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(gridAppView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
        Filter filter = ((Filterable) gridAppView.getAdapter()).getFilter();
        if (TextUtils.isEmpty(word)) {
            filter.filter("");
        } else {
            filter.filter(word);
        }
    }

    @Override
    public Loader<List<AppInfo>> onCreateLoader(int arg0, Bundle arg1) {
        // アプリ一覧の読み込み
        layoutProgress.setVisibility(View.VISIBLE);
        return new AppInfoLoader(getActivity(), false);
    }

    @Override
    public void onLoadFinished(Loader<List<AppInfo>> listLoader, List<AppInfo> appInfos) {
        MyLog.d(TAG, "onLoadFinished");

        View view = getView();
        getLoaderManager().destroyLoader(0);
        if(view != null) {
            layoutProgress.setVisibility(View.GONE);
        }
        if(appInfos != null) {
            appInfoList = appInfos;
            setGridAdapter(appInfoList);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<AppInfo>> arg0) {
    }

    private void setGridAdapter(List<AppInfo> appInfoList) {
        adapter = new GridAppsAdapter(getActivity(), appInfoList);
        gridAppView.setAdapter(adapter);
        // スクロール位置を復元
        gridAppView.setSelection(lastGridPosition);
    }

    /**
     * 表示するViewを切り替える
     */
    public void switchViewVisibility() {
        if(gridAppView.getVisibility() == View.VISIBLE) {
            gridAppView.setVisibility(View.GONE);
            //listAppView.setVisibility(View.VISIBLE);
        }else {
            gridAppView.setVisibility(View.VISIBLE);
            //listAppView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item_sort_install:
                AppPrefs.newInstance(getActivity()).saveSortType(AppInstallComparator.MODE_INSTALL);
                adapter.setNotifyOnChange(true);
                adapter.sort(new AppInstallComparator().setMode(AppInstallComparator.MODE_INSTALL));
                return true;
            case R.id.item_sort_alpabet:
                AppPrefs.newInstance(getActivity()).saveSortType(AppInstallComparator.MODE_NAME);
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

                AppInfo info = (AppInfo) gridAppView.getItemAtPosition(position);
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
            AppInfo appInfo = (AppInfo) gridAppView.getItemAtPosition(pos);
            appListStr.append(appInfo.appName).append(":").append(AppLinkBase.LINK_HTTP_DETAIL).append(appInfo.packageName).append("\n\n");
        }

        shareActionProvider.setShareIntent(CommonUtil.createShareIntent(appListStr.toString()));
    }

    /**
     * アプリ情報選択リスナー
     * @author y
     *
     */
    public interface OnAppInfoClickListener {
        void onItemClick(View view, AppInfo info);
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

            TextView textZxing = (TextView) view.findViewById(R.id.textZxing);
            textZxing.setText(HtmlUtil.fromHtml(getString(R.string.zxing_name)));
            textZxing.setMovementMethod(movementmethod);

            TextView textZxingLicense = (TextView) view.findViewById(R.id.textZxingLicense);
            textZxingLicense.setText(HtmlUtil.fromHtml(getString(R.string.apache_license)));
            textZxingLicense.setMovementMethod(movementmethod);

            TextView textNfcFelica = (TextView) view.findViewById(R.id.textNfcFelica);
            textNfcFelica.setText(HtmlUtil.fromHtml(getString(R.string.nfcfelica_name)));
            textNfcFelica.setMovementMethod(movementmethod);

            TextView textNfcFelicaLicense = (TextView) view.findViewById(R.id.textNfcFelicaLicense);
            textNfcFelicaLicense.setText(HtmlUtil.fromHtml(getString(R.string.apache_license)));
            textNfcFelicaLicense.setMovementMethod(movementmethod);

            TextView textGson = (TextView) view.findViewById(R.id.textGson);
            textGson.setText(HtmlUtil.fromHtml(getString(R.string.gson_name)));
            textGson.setMovementMethod(movementmethod);

            TextView textGsonLicense = (TextView) view.findViewById(R.id.textGsonLicense);
            textGsonLicense.setText(HtmlUtil.fromHtml(getString(R.string.apache_license)));
            textGsonLicense.setMovementMethod(movementmethod);

            TextView textOtto = (TextView) view.findViewById(R.id.textOtto);
            textOtto.setText(HtmlUtil.fromHtml(getString(R.string.otto_name)));
            textOtto.setMovementMethod(movementmethod);

            TextView textOttoL = (TextView) view.findViewById(R.id.textOttoLicense);
            textOttoL.setText(HtmlUtil.fromHtml(getString(R.string.apache_license)));
            textOttoL.setMovementMethod(movementmethod);

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
    }
}
