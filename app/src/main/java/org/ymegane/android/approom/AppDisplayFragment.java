package org.ymegane.android.approom;

import java.util.List;

import org.ymegane.android.approom.preference.AppPrefs;
import org.ymegane.android.approomcommns.AppInfo;
import org.ymegane.android.approomcommns.util.CommonUtil;
import org.ymegane.android.approomcommns.util.MyLog;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Loader;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

/**
 * アプリ一覧表示Fragment
 */
public class AppDisplayFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AppInfo>> {
    public static final String TAG = "AppListFragment";

    private ViewGroup rootView;

    private View layoutProgress;
    private GridView gridAppView;
    private GridAppsAdapter adapter;
    //private ListView listAppView;
    private OnAppInfoClickListener clickListener;

    private List<AppInfo> appInfoList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            clickListener = (OnAppInfoClickListener) activity;
        }catch(ClassCastException e) {
            MyLog.e(TAG, "unimplements Listener!!", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_applist, null);
        layoutProgress = rootView.findViewById(R.id.layoutProgress);
        gridAppView = (GridView) rootView.findViewById(R.id.gridAppIcon);
        gridAppView.setOnItemClickListener(new ItemClickListener());
        //listAppView = (ListView) v.findViewById(R.id.listAppIcon);
        //listAppView.setOnItemClickListener(new ItemClickListener());
        if (!clickListener.isMashroom()) {
            gridAppView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
            gridAppView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                ShareActionProvider shareActionProvider;
                @Override
                public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                    int n = gridAppView.getCheckedItemCount();

                    if (n <= 0) {
                        return;
                    }

                    actionMode.setSubtitle(getString(R.string.action_mode_subtitle_share, n));

                    SparseBooleanArray checkedItems = gridAppView.getCheckedItemPositions();
                    StringBuilder appListStr = new StringBuilder();
                    for (int idx=0; idx<checkedItems.size(); idx++) {
                        int pos = checkedItems.keyAt(idx);
                        AppInfo appInfo = (AppInfo) gridAppView.getItemAtPosition(pos);
                        appListStr.append(appInfo.appName).append(":").append(AppLinkBase.LINK_HTTP_DETAIL).append(appInfo.packageName).append("\n\n");
                    }

                    shareActionProvider.setShareIntent(CommonUtil.createShareIntent(appListStr.toString()));
                }

                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    actionMode.setTitle(R.string.action_mode_title_share);

                    shareActionProvider = new ShareActionProvider(getActivity());
                    menu.add(getString(R.string.share)).setIcon(android.R.drawable.ic_menu_share)
                            .setActionProvider(shareActionProvider)
                            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
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

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initActionBar();
    }

    private void initActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        setHasOptionsMenu(true);
    }

    private int lastGridPosition;
    @Override
    public void onPause() {
        super.onPause();
        lastGridPosition = gridAppView.getFirstVisiblePosition();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.desplay_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item_sort_install:
                adapter.sort(new AppInstallComparator().setMode(AppInstallComparator.MODE_INSTALL));
                AppPrefs.newInstance(getActivity()).saveSortType(AppInstallComparator.MODE_INSTALL);
                return true;
            case R.id.item_sort_alpabet:
                adapter.sort(new AppInstallComparator().setMode(AppInstallComparator.MODE_NAME));
                AppPrefs.newInstance(getActivity()).saveSortType(AppInstallComparator.MODE_NAME);
                return true;
            case R.id.item_appinfo:
                new AppInfoDialog().show(getFragmentManager(), "AppInfoDialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<AppInfo>> onCreateLoader(int arg0, Bundle arg1) {
        // アプリ一覧の読み込み
        layoutProgress.setVisibility(View.VISIBLE);
        return new AppInfoLoader(getActivity());
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

    /**
     * グリッド/リストアイテムのクリックリスナー
     * @author y
     *
     */
    private class ItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            AppInfo info = (AppInfo) gridAppView.getItemAtPosition(position);
            clickListener.onItemClick(info);
        }
    }

    /**
     * アプリ情報選択リスナー
     * @author y
     *
     */
    public interface OnAppInfoClickListener {
        void onItemClick(AppInfo info);
        boolean isMashroom();
    }

    public static class AppInfoDialog extends DialogFragment implements OnClickListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_appinfo, null);

            // LinkMovementMethod のインスタンスを取得します
            MovementMethod movementmethod = LinkMovementMethod.getInstance();

            TextView textVersion = (TextView) view.findViewById(R.id.textVersion);
            textVersion.setText(getString(R.string.version, CommonUtil.getAppVersion(getActivity())));

            TextView textDev = (TextView) view.findViewById(R.id.textDeveloperName);
            textDev.setText(Html.fromHtml(getString(R.string.creater)));
            textDev.setMovementMethod(movementmethod);

            TextView textZxing = (TextView) view.findViewById(R.id.textZxing);
            textZxing.setText(Html.fromHtml(getString(R.string.zxing_name)));
            textZxing.setMovementMethod(movementmethod);

            TextView textZxingLicense = (TextView) view.findViewById(R.id.textZxingLicense);
            textZxingLicense.setText(Html.fromHtml(getString(R.string.apache_license)));
            textZxingLicense.setMovementMethod(movementmethod);

            TextView textNfcFelica = (TextView) view.findViewById(R.id.textNfcFelica);
            textNfcFelica.setText(Html.fromHtml(getString(R.string.nfcfelica_name)));
            textNfcFelica.setMovementMethod(movementmethod);

            TextView textNfcFelicaLicense = (TextView) view.findViewById(R.id.textNfcFelicaLicense);
            textNfcFelicaLicense.setText(Html.fromHtml(getString(R.string.apache_license)));
            textNfcFelicaLicense.setMovementMethod(movementmethod);

            TextView textGson = (TextView) view.findViewById(R.id.textGson);
            textGson.setText(Html.fromHtml(getString(R.string.gson_name)));
            textGson.setMovementMethod(movementmethod);

            TextView textGsonLicense = (TextView) view.findViewById(R.id.textGsonLicense);
            textGsonLicense.setText(Html.fromHtml(getString(R.string.apache_license)));
            textGsonLicense.setMovementMethod(movementmethod);

            TextView textOtto = (TextView) view.findViewById(R.id.textOtto);
            textOtto.setText(Html.fromHtml(getString(R.string.otto_name)));
            textOtto.setMovementMethod(movementmethod);

            TextView textOttoL = (TextView) view.findViewById(R.id.textOttoLicense);
            textOttoL.setText(Html.fromHtml(getString(R.string.apache_license)));
            textOttoL.setMovementMethod(movementmethod);

            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setIcon(android.R.drawable.ic_dialog_info);
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
