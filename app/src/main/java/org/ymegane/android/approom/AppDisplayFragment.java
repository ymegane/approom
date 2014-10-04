package org.ymegane.android.approom;

import java.util.List;

import org.ymegane.android.approom.preference.AppPrefs;
import org.ymegane.android.approom.util.CommonUtil;
import org.ymegane.android.approom.util.MyLog;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * アプリ一覧表示Fragment
 */
public class AppDisplayFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AppInfo>> {
    public static final String TAG = "AppListFragment";

    private GridView gridAppView;
    private GridAppsAdapter adapter;
    //private ListView listAppView;
    private OnAppInfoClickListener clickListener;

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
        View v = inflater.inflate(R.layout.fragment_applist, null);
        gridAppView = (GridView) v.findViewById(R.id.gridAppIcon);
        gridAppView.setOnItemClickListener(new ItemClickListener());
        gridAppView.setOnItemLongClickListener(new ItemLongClickListener());
        //listAppView = (ListView) v.findViewById(R.id.listAppIcon);
        //listAppView.setOnItemClickListener(new ItemClickListener());

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initActoinBar();

        // 読み込み開始
        LoaderManager loaderMng = getLoaderManager();
        loaderMng.initLoader(0, null, this);
    }

    private void initActoinBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        setHasOptionsMenu(true);
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
        getView().findViewById(R.id.linearLayout1).setVisibility(View.VISIBLE);
        return new AppInfoLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<AppInfo>> listLoader, List<AppInfo> appInfos) {
        if(getView() != null) {
            getView().findViewById(R.id.linearLayout1).setVisibility(View.GONE);
        }
        if(appInfos != null && !appInfos.isEmpty()) {
            adapter = new GridAppsAdapter(getActivity(), appInfos);
            gridAppView.setAdapter(adapter);
        }
        //listAppView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<List<AppInfo>> arg0) {
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

    private class ItemLongClickListener implements OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            AppInfo info = (AppInfo) gridAppView.getItemAtPosition(arg2);
            Toast.makeText(getActivity(), info.appName, Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    /**
     * アプリ情報選択リスナー
     * @author y
     *
     */
    public interface OnAppInfoClickListener {
        void onItemClick(AppInfo info);
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

            TextView textAbs = (TextView) view.findViewById(R.id.textABS);
            textAbs.setText(Html.fromHtml(getString(R.string.abs_name)));
            textAbs.setMovementMethod(movementmethod);

            TextView textAbsLicense = (TextView) view.findViewById(R.id.textAbsLicense);
            textAbsLicense.setText(Html.fromHtml(getString(R.string.apache_license)));
            textAbsLicense.setMovementMethod(movementmethod);

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
