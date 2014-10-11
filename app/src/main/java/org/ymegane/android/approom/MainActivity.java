package org.ymegane.android.approom;

import net.kazz.felica.NfcFeliCaTagFragment;
import net.kazzz.AbstractNfcTagFragment;
import net.kazzz.AbstractNfcTagFragment.INfcTagListener;
import net.kazzz.felica.FeliCaException;
import net.kazzz.felica.lib.FeliCaLib;

import org.ymegane.android.approom.AppDisplayFragment.OnAppInfoClickListener;
import org.ymegane.android.approom.nfc.AndroidBeamFragment;
import org.ymegane.android.approom.nfc.PushCommand;
import org.ymegane.android.approom.nfc.AndroidBeamFragment.OnCreateNdefMessageListener;
import org.ymegane.android.approom.nfc.MfcManageFragment;
import org.ymegane.android.approom.util.CommonUtil;
import org.ymegane.android.approom.util.IconCache;
import org.ymegane.android.approom.util.MyLog;

import com.felicanetworks.mfc.PushStartBrowserSegment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

public class MainActivity extends Activity implements OnAppInfoClickListener, INfcTagListener, OnCreateNdefMessageListener, AppDetailFragment.OnAppDetailEventObserver, MfcManageFragment.OnPushRequestEventObserver {
    private static final String TAG = "MainActivity";

    private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";

    private AppDetailFragment appDetailFragment;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        WallpaperManager wallpaper = WallpaperManager.getInstance(this);
        Drawable d = wallpaper.getFastDrawable().mutate();
        getWindow().setBackgroundDrawable(d);

        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
        if(savedInstanceState == null) {
            AppDisplayFragment fragment = new AppDisplayFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.layout_maincontent, fragment, AppDisplayFragment.TAG);
            ft.commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MyLog.d(TAG, "onNewIntent");
        if(nfcFelicaFragment != null) {
            nfcFelicaFragment.onNewIntent(intent);
        }
    }

    boolean isDestory = false;
    @Override
    protected void onDestroy() {
        isDestory = true;
        IconCache.getInstance().clear();
        super.onDestroy();
    }

    public boolean isDestory() {
        return isDestory;
    }

    @Override
    public void onItemClick(AppInfo info) {
        String action = getIntent().getAction();

        if(action != null && action.contains(ACTION_INTERCEPT)){
            Bundle args = new Bundle();
            args.putString("packageName", info.appInfo.packageName);
            LinkSelectDialog dialog = new LinkSelectDialog();
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), "LinkSelectDialog");
        }else {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            // NFC/Felicaのイベント管理Fragment
            boolean isTouch = addIcCardFragments(ft);
            appDetailFragment = AppDetailFragment.newInsance(info.appInfo, isTouch);

            ft.replace(R.id.layout_maincontent, appDetailFragment, AppDetailFragment.TAG);
            ft.addToBackStack(AppDetailFragment.TAG);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            ft.commit();
        }
    }

    private NfcFeliCaTagFragment nfcFelicaFragment;
    private AndroidBeamFragment beamFragment;
    private MfcManageFragment mfcFragment;

    private boolean addIcCardFragments(FragmentTransaction ft) {
        // NFCをサポートしてるぞぉぉぉ
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                nfcFelicaFragment = new NfcFeliCaTagFragment();
                ft.add(nfcFelicaFragment, "NfcFeliCaTagFragment");
                //インテントから起動された際の処理
                Intent intent = getIntent();
                this.onNewIntent(intent);
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                beamFragment = AndroidBeamFragment.newInstance();
                ft.add(beamFragment, "AndroidBeamFragment");
            }
            return true;
        } else if(MfcManageFragment.isEnalbeFelica(this)) {
            mfcFragment = new MfcManageFragment();
            ft.add(mfcFragment, MfcManageFragment.TAG);
            return true;
        }

        return false;
    }

    public static class LinkSelectDialog extends DialogFragment implements OnClickListener {
        private String[] linkItems;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String packageName = getArguments().getString("packageName");
            linkItems = new String[]{
                    AppLinkBase.LINK_HTTP_DETAIL + packageName,
                    AppLinkBase.LINK_MARKET_DETAIL + packageName};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.select_type);
            builder.setItems(linkItems, this);
            return builder.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent data = new Intent();
            data.putExtra("replace_key", linkItems[which]);
            getActivity().setResult(RESULT_OK, data);
            getActivity().finish();
        }
    }

    @Override
    public void onTagDiscovered(Intent intent, Parcelable nfcTag, AbstractNfcTagFragment fragment) {
        MyLog.d(TAG, "onTagDiscovered");
        if(fragment != null && fragment instanceof NfcFeliCaTagFragment) {
            FeliCaLib.IDm idm =
                    new FeliCaLib.IDm(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
            PushStartBrowserSegment segment = new PushStartBrowserSegment(appDetailFragment.getCurrentUri(), null);
            try {
                FeliCaLib.execute((Tag)nfcTag, new PushCommand(idm, segment));
                CommonUtil.doViblate(getApplicationContext(), 300);
            } catch (FeliCaException e) {
                MyLog.w(TAG, e);
            }
        }
    }

    @Override
    public String onRequestSendUri() {
        if(appDetailFragment != null) {
            return appDetailFragment.getCurrentUri();
        }
        return null;
    }

    @Override
    public void onDetailDestory() {
        if(nfcFelicaFragment != null) {
            nfcFelicaFragment = null;
        }
        if(beamFragment != null) {
            beamFragment = null;
        }
        if(mfcFragment != null) {
            mfcFragment = null;
        }
    }

    @Override
    public String onPushRequest() {
        if(appDetailFragment != null) {
            return appDetailFragment.getCurrentUri();
        }
        return null;
    }
}