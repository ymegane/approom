package org.ymegane.android.approom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.support.wearable.view.DismissOverlayView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.ymegane.android.approomcommns.AppInfo;
import org.ymegane.android.approomcommns.QRCodeLoader;

/**
 * 共有画面
 */
public class ShareActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Bitmap> {

    public static void startShareActivity(Context context, AppInfo appInfo) {
        Intent intent = new Intent(context, ShareActivity.class);
        intent.putExtra("appInfo", appInfo);
        context.startActivity(intent);
    }

    private ImageView mImageView;
    private AppInfo mAppInfo;

    private GestureDetectorCompat mGestureDetector;
    private DismissOverlayView mDismissOverlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        mAppInfo = getIntent().getParcelableExtra("appInfo");
        mImageView = (ImageView) findViewById(R.id.imageQR);
        mDismissOverlayView = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mGestureDetector = new GestureDetectorCompat(ShareActivity.this, new LongPressListener());

        getSupportLoaderManager().initLoader(0, null, ShareActivity.this);
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

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        return mGestureDetector.onTouchEvent(event) || super.dispatchTouchEvent(event);
    }

    private class LongPressListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            mDismissOverlayView.show();
        }
    }
}
