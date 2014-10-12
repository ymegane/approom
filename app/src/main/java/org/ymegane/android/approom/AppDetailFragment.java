package org.ymegane.android.approom;

import java.util.ArrayList;

import org.ymegane.android.approom.preference.AppPrefs;
import org.ymegane.android.approomcommns.QRCodeLoader;
import org.ymegane.android.approomcommns.util.CommonUtil;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;


/**
 * アプリ詳細表示
 */
public class AppDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Bitmap> {
    public static final String TAG = "AppDetailFragment";

    public static final String KEY_APPINFO = "key_appinfo";
    public static final String KEY_TOUCH_ENABLE = "key_touch";

    private TextView textLinkUri;
    private ImageView imageQr;

    private ShareActionProvider shareActionProvider;
    private PackageManager packageMng;
    private ApplicationInfo appInfo;

    public interface OnAppDetailEventObserver {
        void onDetailDestory();
    }

    private OnAppDetailEventObserver eventListener;
    private String packageName;
    private String appName;
    private String currentUri;

    public static AppDetailFragment newInsance(ApplicationInfo appInfo, boolean isTouch) {
        Bundle args = new Bundle();
        args.putParcelable(AppDetailFragment.KEY_APPINFO, appInfo);
        args.putBoolean(KEY_TOUCH_ENABLE, isTouch);
        AppDetailFragment fragment = new AppDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        eventListener = (OnAppDetailEventObserver) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        packageMng = getActivity().getPackageManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_appdetail, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        appInfo = args.getParcelable(KEY_APPINFO);
        appName = appInfo.loadLabel(packageMng).toString();
        packageName = appInfo.packageName;

        View view = getView();
        TextView textAppName = (TextView) view.findViewById(R.id.textAppName);
        textAppName.setText(appName);
        textAppName.requestFocus();

        Drawable icon = appInfo.loadIcon(packageMng);
        if(icon != null) {
            ImageView imageIcon = (ImageView) view.findViewById(R.id.imageIcon);
            imageIcon.setImageDrawable(icon);
        }

        textLinkUri = (TextView) view.findViewById(R.id.textUri);
        imageQr = (ImageView) view.findViewById(R.id.imageQr);

        initActionBar();

        if(args.getBoolean(KEY_TOUCH_ENABLE)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showTouchEnable();
                }
            }, 500);
        }
    }

    private void showTouchEnable() {
        View view = getView();
        if(view == null) return;

        View touchInfo = view.findViewById(R.id.textTouchInfo);
        touchInfo.setVisibility(View.VISIBLE);
        touchInfo.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_top));
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
        eventListener.onDetailDestory();
        super.onDestroy();
    }

    private void initActionBar() {
        final ArrayList<String> linkList = createLinkArray();

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);

        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, android.R.id.text1, getResources().getStringArray(R.array.linktype));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                if(itemPosition < linkList.size()) {
                    AppPrefs.newInstance(getActivity()).saveLinkType(itemPosition);
                    currentUri = linkList.get(itemPosition);
                    shareActionProvider.setShareIntent(CommonUtil.createShareIntent(appName + ":" + currentUri));
                    textLinkUri.setText(currentUri);
                    Bundle arg = new Bundle();
                    arg.putString("currentUri", currentUri);
                    getLoaderManager().restartLoader(0, arg, AppDetailFragment.this);
                    return true;
                }
                return false;
            }
        });
        actionBar.setSelectedNavigationItem(AppPrefs.newInstance(getActivity()).getLinkType());

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);
        shareActionProvider = (ShareActionProvider) menu.findItem(R.id.item_share).getActionProvider();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
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

    private ArrayList<String> createLinkArray() {
        ArrayList<String> array = new ArrayList<String>(4);

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
        return new QRCodeLoader(getActivity(), uri, getResources().getDimensionPixelSize(R.dimen.qr_size));
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> arg0, Bitmap arg1) {
        imageQr.setImageBitmap(arg1);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> arg0) {
    }
}
