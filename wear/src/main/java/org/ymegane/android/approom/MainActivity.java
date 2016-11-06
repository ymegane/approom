package org.ymegane.android.approom;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WearableListView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.ymegane.android.dlog.DLog;
import com.squareup.otto.Subscribe;

import org.ymegane.android.approom.events.BusProvider;
import org.ymegane.android.approom.events.FailureAppInfoReceiveEvent;
import org.ymegane.android.approom.events.FailureAppInfoRequestEvent;
import org.ymegane.android.approom.events.SuccessAppInfoReceiveEvent;
import org.ymegane.android.approom.events.SuccessAppInfoRequestEvent;
import org.ymegane.android.approomcommns.domain.model.AppModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity implements WearableListView.ClickListener {

    private ProgressBar mProgress;
    private WearableListView mListView;

    private GestureDetectorCompat mGestureDetector;
    private DismissOverlayView mDismissOverlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mListView = (WearableListView) findViewById(R.id.list);
        mListView.setAdapter(new Adapter(getApplicationContext(), new ArrayList<AppModel>(1)));
        mListView.setClickListener(MainActivity.this);
        mDismissOverlayView = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mGestureDetector = new GestureDetectorCompat(MainActivity.this, new LongPressListener());

        BusProvider.getInstance().register(this);

        AppInfoRequestService.startAppInfoRequestService(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);

        super.onDestroy();
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        if (mDismissOverlayView.isShown()) {
            return;
        }
        AppModel appModel = (AppModel) viewHolder.itemView.getTag();
        ShareActivity.startShareActivity(this, appModel);
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    @Subscribe
    public void onSuccessAppInfoReceive(final SuccessAppInfoReceiveEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress.setVisibility(View.GONE);
                mListView.setAdapter(new Adapter(MainActivity.this, event.getAppModels()));
            }
        });
    }

    @Subscribe
    public void onFailureAppInfoReceive(FailureAppInfoReceiveEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, R.string.error_failed_get_appinfo, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Subscribe
    public void onSuccessAppInfoRequest(SuccessAppInfoRequestEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(MainActivity.this, "アプリ情報を取得中", Toast.LENGTH_SHORT).show();
                DLog.d("アプリ情報を取得中");
            }
        });
    }

    @Subscribe
    public void onFailureAppInfoRequest(FailureAppInfoRequestEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, R.string.error_failed_connect_device, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return false;
        }
        return super.dispatchTouchEvent(event);
    }

    private class LongPressListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            mDismissOverlayView.show();
        }
    }

    private static final class Adapter extends WearableListView.Adapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final List<AppModel> mAppModel;

        private Adapter(Context context, List<AppModel> appModelList) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mAppModel = appModelList;
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WearableListView.ViewHolder(
                    mInflater.inflate(R.layout.app_list_item, null));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            WearableListItemLayout itemView = (WearableListItemLayout) holder.itemView;

            AppModel appModel = mAppModel.get(position);
            itemView.bindView(appModel);
            holder.itemView.setTag(appModel);
        }

        @Override
        public int getItemCount() {
            return mAppModel.size();
        }
    }
}
