package org.ymegane.android.approom;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DismissOverlayView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.ymegane.android.approomcommns.AppInfo;
import org.ymegane.android.approomcommns.AppLinkBase;
import org.ymegane.android.approomcommns.QRCodeLoader;
import org.ymegane.android.approomcommns.util.MyLog;

/**
 * 共有画面
 */
public class ShareActivity extends WearableActivity implements LoaderManager.LoaderCallbacks<Bitmap> {
    private static final String TAG = ShareActivity.class.getSimpleName();

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

        setAmbientEnabled();

        mAppInfo = getIntent().getParcelableExtra("appInfo");
        mImageView = (ImageView) findViewById(R.id.imageQR);
        mDismissOverlayView = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mGestureDetector = new GestureDetectorCompat(ShareActivity.this, new LongPressListener());

        getLoaderManager().initLoader(0, null, ShareActivity.this);
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        return new QRCodeLoader(getApplicationContext(), AppLinkBase.LINK_HTTP_DETAIL + mAppInfo.packageName, getResources().getDimensionPixelSize(R.dimen.qr_size));
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

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        MyLog.d(TAG, "[onEnterAmbient]");

        // TODO 画像を反転させる？
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        MyLog.d(TAG, "[onExitAmbient]");
    }

    private class LongPressListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            mDismissOverlayView.show();
        }
    }
}
