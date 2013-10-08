package org.ymegane.android.approom;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.felicanetworks.mfc.PushStartBrowserSegment;

import net.kazz.felica.NfcFeliCaTagFragment;
import net.kazzz.AbstractNfcTagFragment;
import net.kazzz.felica.FeliCaException;
import net.kazzz.felica.lib.FeliCaLib;

import org.ymegane.android.approom.nfc.AndroidBeamFragment;
import org.ymegane.android.approom.nfc.MfcManageFragment;
import org.ymegane.android.approom.nfc.PushCommand;
import org.ymegane.android.approomcommns.util.CommonUtil;
import org.ymegane.android.approomcommns.util.MyLog;

public class DetailActivity extends AppCompatActivity implements AppDetailFragment.OnAppDetailEventObserver, AbstractNfcTagFragment.INfcTagListener, AndroidBeamFragment.OnCreateNdefMessageListener,  MfcManageFragment.OnPushRequestEventObserver  {
    private static final String TAG = DetailActivity.class.getSimpleName();

    public static final String APP_INFO = "applicationInfo";

    private AppDetailFragment appDetailFragment;

    public static void launch(Activity activity, View transitionView, ApplicationInfo info) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, transitionView, APP_INFO);
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(APP_INFO, info);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        WallpaperManager wallpaper = WallpaperManager.getInstance(this);
        Drawable d = wallpaper.getFastDrawable().mutate();
        ImageView imageView = (ImageView) findViewById(R.id.imageWallpaper);
        imageView.setImageDrawable(d);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            ApplicationInfo info = getIntent().getParcelableExtra(APP_INFO);
            appDetailFragment = AppDetailFragment.newInstance(info, addIcCardFragments(ft));

            ft.replace(R.id.container, appDetailFragment, AppDetailFragment.TAG);
            ft.commit();
        }
    }

    boolean isDestroy = false;
    @Override
    protected void onDestroy() {
        isDestroy = true;
        super.onDestroy();
    }

    public boolean isDestroy() {
        return isDestroy;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MyLog.d(TAG, "onNewIntent");
        if(nfcFelicaFragment != null) {
            nfcFelicaFragment.onNewIntent(intent);
        }
    }

    private NfcFeliCaTagFragment nfcFelicaFragment;
    private AndroidBeamFragment beamFragment;
    private MfcManageFragment mfcFragment;

    private boolean addIcCardFragments(FragmentTransaction ft) {
        // NFCをサポートしてるぞぉぉぉ
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
            if (adapter != null && adapter.isEnabled()) { // 有効な場合
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    nfcFelicaFragment = new NfcFeliCaTagFragment();
                    ft.add(nfcFelicaFragment, "NfcFeliCaTagFragment");
                    //インテントから起動された際の処理
                    Intent intent = getIntent();
                    this.onNewIntent(intent);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    beamFragment = AndroidBeamFragment.newInstance();
                    ft.add(beamFragment, "AndroidBeamFragment");
                }
                return true;
            }
        }
        if(MfcManageFragment.isEnalbeFelica(this)) {
            mfcFragment = new MfcManageFragment();
            ft.add(mfcFragment, MfcManageFragment.TAG);
            return true;
        }

        return false;
    }


    @Override
    public void onDetailDestroy() {
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
    public String onPushRequest() {
        if(appDetailFragment != null) {
            return appDetailFragment.getCurrentUri();
        }
        return null;
    }
}
