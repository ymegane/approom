package org.ymegane.android.approom.data.repository;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;

import com.github.ymegane.android.dlog.DLog;

import org.ymegane.android.approom.R;
import org.ymegane.android.approom.domain.repository.InstalledAppRepository;
import org.ymegane.android.approomcommns.domain.model.AppModel;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

public class InstalledAppRepositoryImpl implements InstalledAppRepository {
    private Context mContext;
    private boolean loadPallet;

    public InstalledAppRepositoryImpl(Context context) {
        mContext = context;
    }

    public InstalledAppRepositoryImpl setLoadPallet(boolean loadPallet) {
        this.loadPallet = loadPallet;
        return this;
    }

    @Override
    public List<AppModel> getInstalledAppList() {
        return getAppInfo();
    }

    private List<AppModel> getAppInfo() {
        final PackageManager packageMng = mContext.getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ApplicationInfo> installedAppList =
                packageMng.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS);//.queryIntentActivities(mainIntent, 0);

        AppPrefs prefs = AppPrefs.newInstance(mContext);
        final boolean includeSystemApp = prefs.isIncludeSystemApp();
        final boolean includeDisableApp = prefs.isIncludeDisableApp();
        // インストール日時でソート
        final AppInstallComparator comparator = new AppInstallComparator().setMode(prefs.getSortType());

        return Observable.from(installedAppList)
                .filter(new Func1<ApplicationInfo, Boolean>() {
                    @Override
                    public Boolean call(ApplicationInfo applicationInfo) {
                        return isAdd(applicationInfo, includeSystemApp, includeDisableApp);
                    }
                })
                .map(new Func1<ApplicationInfo, AppModel>() {
                    @Override
                    public AppModel call(ApplicationInfo appInfo) {
                        boolean isStop = false;
                        long lastMod;

                        // 停止状態のアプリの場合
                        if ((appInfo.flags & ApplicationInfo.FLAG_STOPPED) == 0) {
                            // Non-system app
                            isStop = true;
                        }

                        AppModel appData = new AppModel();
                        if (!loadPallet) {
                            appData.appInfo = appInfo;
                        }
                        lastMod = -1;

                        // Nullの場合があるよう。細かくは分かってない
                        if (appInfo.publicSourceDir != null) {
                            File file = new File(appInfo.publicSourceDir);

                            try {
                                // 最終更新日時
                                lastMod = file.lastModified();
                            } catch (SecurityException e) {
                                DLog.e("Access Error", e);
                            }
                        }
                        appData.packageName = appInfo.packageName;
                        appData.lastModify = lastMod;
                        appData.appName = (String) appInfo.loadLabel(packageMng);
                        appData.isStoped = isStop;
                        appData.iconUrl = Uri.parse("android.resource://" + appInfo.packageName + "/" + appInfo.icon);

                        if (loadPallet) {
                            Drawable icon = appInfo.loadIcon(packageMng);
                            Bitmap iconBitmap = ((BitmapDrawable) icon).getBitmap();
                            Palette palette = Palette.from(iconBitmap).generate();
                            appData.palette = palette.getVibrantColor(ContextCompat.getColor(mContext, R.color.primary));
                        }
                        return appData;
                    }
                })
                .toSortedList(new Func2<AppModel, AppModel, Integer>() {
                    @Override
                    public Integer call(AppModel appModel, AppModel appModel2) {
                        return comparator.compare(appModel, appModel2);
                    }
                }).toBlocking().single();
    }

    private static boolean isAdd(ApplicationInfo appInfo, boolean includeSystemApp, boolean includeDisableApp) {
        // ユーザーダウンロードアプリの場合
        if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                || (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            // Updated system app or Non-system app
            return true;
        }
        if (includeSystemApp) {
            if (!includeDisableApp && !appInfo.enabled) {
                return false;
            }
            return true;
        }
        return false;
    }
}
