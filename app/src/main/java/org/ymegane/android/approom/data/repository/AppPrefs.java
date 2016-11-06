package org.ymegane.android.approom.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class AppPrefs {
    private AppPrefs(Context context){
        mDefSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    };

    public static AppPrefs newInstance(Context context) {
        return new AppPrefs(context);
    }
    private final SharedPreferences mDefSharedPref;

    /** @deprecated 旧バージョンのため廃止 */
    public static final String KEY_PREFS_LINKTYPE_v1 = "key_def_type";

    public static final String KEY_PREFS_LINKTYPE_v2 = "key_def_type2";

    public static final String KEY_PREFS_SORTTYPE = "key_sort_type";

    public static final String KEY_PREFS_INCLUDE_SYSTEM_APP = "key_include_system_app";

    public static final String KEY_PREFS_INCLUDE_DISABLE_APP = "key_include_disable_app";

    public void saveSortType(int type) {
        setIntPref(KEY_PREFS_SORTTYPE, type);
    }

    public int getSortType() {
        return getIntegerPref(KEY_PREFS_SORTTYPE);
    }

    public void saveLinkType(int type) {
        setIntPref(KEY_PREFS_LINKTYPE_v2, type);
    }
    
    public int getLinkType() {
        return getIntegerPref(KEY_PREFS_LINKTYPE_v2);
    }

    public boolean isIncludeSystemApp() {
        return mDefSharedPref.getBoolean(KEY_PREFS_INCLUDE_SYSTEM_APP, false);
    }

    public boolean isIncludeDisableApp() {
        return mDefSharedPref.getBoolean(KEY_PREFS_INCLUDE_DISABLE_APP, false);
    }

    public final int getIntegerPref(String key){
        return mDefSharedPref.getInt(key, getintDefValue(key));
    }

    public final void setIntPref(String key, int value){
        mDefSharedPref.edit().putInt(key, value).apply();
    }

    private static int getintDefValue(String key){
        int ret = 0;

        if(key.contentEquals(KEY_PREFS_LINKTYPE_v2)){
            ret = 0;
        }else if(key.contentEquals(KEY_PREFS_SORTTYPE)){
            ret = AppInstallComparator.MODE_INSTALL;
        }

        return ret;
    }
}
