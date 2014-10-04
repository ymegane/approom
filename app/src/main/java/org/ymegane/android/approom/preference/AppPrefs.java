package org.ymegane.android.approom.preference;

import org.ymegane.android.approom.AppInstallComparator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPrefs {
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
    
    public final int getIntegerPref(String key){
        return mDefSharedPref.getInt(key, getintDefValue(key));
    }

    public final void setIntPref(String key, int value){
        mDefSharedPref.edit().putInt(key, value).commit();
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
