package org.ymegane.android.approom.presentation.view.fragment;

import java.util.ArrayList;

import org.ymegane.android.approom.R;
import org.ymegane.android.approom.data.repository.PreferenceSettingsRepository;
import org.ymegane.android.approom.databinding.FragmentAppDetailBinding;
import org.ymegane.android.approom.domain.repository.SettingsRepository;
import org.ymegane.android.approom.presentation.view.activity.DetailActivity;
import org.ymegane.android.approomcommns.domain.model.AppModel;
import org.ymegane.android.approomcommns.AppLinkBase;
import org.ymegane.android.approomcommns.data.repository.QRCodeLoaderSupport;
import org.ymegane.android.approomcommns.util.CommonUtil;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.github.ymegane.android.dlog.DLog;

/**
 * アプリ詳細表示
 */
public class AppDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Bitmap> {
    public static final String TAG = "AppDetailFragment";

    public static final String KEY_APPINFO = "key_appinfo";
    public static final String KEY_TOUCH_ENABLE = "key_touch";

    private FragmentAppDetailBinding mBinding;
    private SettingsRepository mSettingsRepository;

    public interface OnAppDetailEventObserver {
        void onDetailDestroy();
    }

    private OnAppDetailEventObserver eventListener;
    private String packageName;
    private String appName;
    private String currentUri;

    public static AppDetailFragment newInstance(AppModel appModel, boolean isTouch) {
        Bundle args = new Bundle();
        args.putParcelable(AppDetailFragment.KEY_APPINFO, appModel);
        args.putBoolean(KEY_TOUCH_ENABLE, isTouch);
        AppDetailFragment fragment = new AppDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        eventListener = (OnAppDetailEventObserver) context;

        mSettingsRepository = new PreferenceSettingsRepository(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_detail, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        AppModel appModel = args.getParcelable(KEY_APPINFO);
        appName = appModel.getAppName();
        packageName = appModel.getPackageName();

        mBinding = DataBindingUtil.bind(getView());
        mBinding.setAppModel(appModel);
        mBinding.textAppName.requestFocus();
        mBinding.fab.setOnClickListener(view ->
            onClickFab()
        );

        ViewCompat.setTransitionName(mBinding.imageIcon, DetailActivity.TRANSITION_ICON);
        ViewCompat.setTransitionName(mBinding.textAppName, DetailActivity.TRANSITION_LABEL);

        initActionBar();

        if(args.getBoolean(KEY_TOUCH_ENABLE)) {
            new Handler().postDelayed(this::showTouchEnable, 500);
        }
    }

    private void showTouchEnable() {
        View view = getView();
        if(view == null) return;
        Snackbar.make(view, R.string.touch_enable, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        eventListener.onDetailDestroy();
        super.onDestroy();
    }

    private void initActionBar() {
        final ArrayList<String> linkList = createLinkArray();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);

        ArrayAdapter adapter
                = new ArrayAdapter<>(actionBar.getThemedContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1, getResources().getStringArray(R.array.linktype));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(adapter, (itemPosition, itemId) -> {
            if (getActivity() == null) {
                return false;
            }
            if(itemPosition < linkList.size()) {
                mSettingsRepository.setLinkType(itemPosition);
                currentUri = linkList.get(itemPosition);
                mBinding.textUri.setText(currentUri);
                Bundle arg = new Bundle();
                arg.putString("currentUri", currentUri);
                getLoaderManager().restartLoader(0, arg, AppDetailFragment.this);
                return true;
            }
            return false;
        });
        actionBar.setSelectedNavigationItem(mSettingsRepository.getLinkType());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                getActivity().supportFinishAfterTransition();
                return true;
            case R.id.item_detail:
                Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+packageName));
                settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                settingsIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.applications.InstalledAppDetails"));
                try {
                    startActivity(settingsIntent);
                }catch(ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.error_opne_failed, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.item_store:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppLinkBase.LINK_MARKET_DETAIL+packageName));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                }catch(ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.error_store_uninstalled, Toast.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickFab() {
        try {
            startActivity(Intent.createChooser(CommonUtil.createShareIntent(appName + ":" + currentUri), getString(R.string.action_mode_title_share)));
        } catch (ActivityNotFoundException e) {
            DLog.w(e);
        }
    }

    private ArrayList<String> createLinkArray() {
        ArrayList<String> array = new ArrayList<>(4);

        array.add(AppLinkBase.LINK_HTTP_DETAIL + packageName);
        array.add(AppLinkBase.LINK_MARKET_DETAIL + packageName);

        return array;
    }

    public String getCurrentUri() {
        return currentUri;
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int arg0, Bundle arg1) {
        String uri = arg1.getString("currentUri");
        return new QRCodeLoaderSupport(getActivity(), uri, getResources().getDimensionPixelSize(R.dimen.qr_size));
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> arg0, Bitmap arg1) {
        mBinding.imageQr.setImageBitmap(arg1);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> arg0) {
    }
}
