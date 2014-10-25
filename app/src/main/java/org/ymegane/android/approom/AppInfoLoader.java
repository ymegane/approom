package org.ymegane.android.approom;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ymegane.android.approom.preference.AppPrefs;
import org.ymegane.android.approomcommns.AppInfo;
import org.ymegane.android.approomcommns.util.MyLog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.AsyncTaskLoader;

/**
 * アプリ情報読み込みLoader
 * @author y
 *
 */
public class AppInfoLoader extends AsyncTaskLoader<List<AppInfo>> {
    private static final String TAG = "AppInfoLoader";

    public AppInfoLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        // 読み込み開始
        forceLoad();
    }

    @Override
    public List<AppInfo> loadInBackground() {
        return getAppInfo(getContext());
    }

    static List<AppInfo> getAppInfo(Context context) {
        PackageManager packageMng = context.getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ApplicationInfo> installedAppList =
                packageMng.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS);//.queryIntentActivities(mainIntent, 0);

        List<AppInfo> appsList = new ArrayList<AppInfo> ();

        boolean isStop;
        long lastMod;

        AppPrefs prefs = AppPrefs.newInstance(context);
        boolean includeSystemApp = prefs.isIncludeSystemApp();
        boolean includeDisableApp = prefs.isIncludeDisableApp();

        for (ApplicationInfo appInfo : installedAppList) {
            isStop = false;

            // 停止状態のアプリの場合
            if ((appInfo.flags & ApplicationInfo.FLAG_STOPPED) == 0) {
                // Non-system app
                isStop = true;
            }

            // システムアプリ以外
            if (isAdd(appInfo, includeSystemApp, includeDisableApp)) {
                AppInfo appData = new AppInfo();
                appData.appInfo = appInfo;

                lastMod = -1;

                // Nullの場合があるよう。細かくは分かってない
                if(appInfo.publicSourceDir != null){
                    File file = new File(appInfo.publicSourceDir);

                    try{
                        // 最終更新日時
                        lastMod = file.lastModified();
                    }catch (SecurityException e) {
                        MyLog.e(TAG, "Access Error", e);
                    }
                }
                appData.packageName = appInfo.packageName;
                appData.lastModify = lastMod;
                appData.appName = (String) appInfo.loadLabel(packageMng);
                appData.isStoped = isStop;

                appsList.add(appData);
            }
        }

        int sortType = AppPrefs.newInstance(context).getSortType();
        // インストール日時でソート
        Collections.sort(appsList, new AppInstallComparator().setMode(sortType));
        return appsList;
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
