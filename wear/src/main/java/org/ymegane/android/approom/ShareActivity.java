package org.ymegane.android.approom;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.ImageView;

import org.ymegane.android.approomcommns.AppInfo;
import org.ymegane.android.approomcommns.QRCodeLoader;

/**
 * Created by y on 2014/10/13.
 */
public class ShareActivity extends Activity implements LoaderManager.LoaderCallbacks<Bitmap> {

    public static void startShareActivity(Context context, AppInfo appInfo) {
        Intent intent = new Intent(context, ShareActivity.class);
        intent.putExtra("appInfo", appInfo);
        context.startActivity(intent);
    }

    private ImageView mImageView;
    private AppInfo mAppInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mImageView = (ImageView) findViewById(R.id.imageQR);

                getLoaderManager().initLoader(0, null, ShareActivity.this);
            }
        });
        mAppInfo = getIntent().getParcelableExtra("appInfo");
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        return new QRCodeLoader(getApplicationContext(), mAppInfo.packageName, getResources().getDimensionPixelSize(R.dimen.qr_size));
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
        getLoaderManager().destroyLoader(0);
        mImageView.setImageBitmap(data);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
    }
}
