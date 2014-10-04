package org.ymegane.android.approom;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;

/**
 * グリッド表示時のアダプター
 * @author y
 *
 */
public class GridAppsAdapter extends ArrayAdapter<AppInfo> implements Filterable {
    private static final String TAG = "GridAppsAdapter";

    private LayoutInflater inflater;
    private PackageManager packageMng;
    private Bitmap loadingBitmap;

    public GridAppsAdapter(Context context, List<AppInfo> objects) {
        super(context, -1, objects);
        packageMng = context.getPackageManager();
        inflater = LayoutInflater.from(context);

        loadingBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_gray);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GridAppItemView itemView = (GridAppItemView) convertView;
        if(convertView == null) {
            itemView = (GridAppItemView) inflater.inflate(R.layout.adapter_grid_app_item, null);
        }

        AppInfo appData = getItem(position);

        itemView.textAppName.setText(appData.appName);
        loadIcon(getContext(), appData, itemView.imageIcon, loadingBitmap);

        if (itemView.isChecked()) {
            itemView.setBackgroundColor(R.drawable.grid_item_selected);
        } else {
            itemView.setBackgroundResource(0);
        }

        return itemView;
    }

    private class IconImageTask extends AsyncTask<AppInfo, Void, Drawable> {
        private final WeakReference<ImageView> mImageViewReference;
        AppInfo mAppInfo;

        public IconImageTask(ImageView imageView) {
            mImageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Drawable doInBackground(AppInfo... params) {
            AppInfo info = params[0];
            mAppInfo = info;

            Drawable icon = params[0].appInfo.loadIcon(packageMng);
            return icon;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if(result != null) {
                ImageView imageVIew = mImageViewReference.get();
                if(imageVIew != null) {
                    IconImageTask task = getIconImageTask(imageVIew);
                    if(this == task) {
                        imageVIew.setImageDrawable(result);
                    }
                }
            }
        }
    }

    private static IconImageTask getIconImageTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public static boolean cancelPotentialWork(AppInfo info, ImageView imageView) {
        final IconImageTask iconLoadTask = getIconImageTask(imageView);

        if (iconLoadTask != null) {
            final AppInfo iconInfo = iconLoadTask.mAppInfo;
            if (iconInfo != null && !iconInfo.equals(info)) {
                // 以前のタスクをキャンセル
                iconLoadTask.cancel(true);
            } else {
                // 同じタスクがすでに走っているので、このタスクは実行しない
                return false;
            }
        }
        // この ImageView に関連する新しいタスクを実行する
        return true;
    }


    public void loadIcon(Context context, AppInfo appINfo, ImageView imageView, Bitmap loadingBitmap) {
        // 同じタスクが走っていないか、同じ ImageView で古いタスクが走っていないかチェック
        if (cancelPotentialWork(appINfo, imageView)) {
            final IconImageTask task = new IconImageTask(imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), loadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(appINfo);
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<IconImageTask> iconImageTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, IconImageTask bitmapWorkerTask) {
            super(res, bitmap);
            iconImageTaskReference = new WeakReference<IconImageTask>(bitmapWorkerTask);
        }

        public IconImageTask getBitmapWorkerTask() {
            return iconImageTaskReference.get();
        }
    }
}
