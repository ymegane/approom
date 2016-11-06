package org.ymegane.android.approom.data.repository;

import org.ymegane.android.approomcommns.domain.model.AppModel;

import java.io.Serializable;
import java.util.Comparator;

/**
 * アプリのインストール日時Comparator
 * @author y
 *
 */
public class AppInstallComparator implements Comparator<AppModel>, Serializable {
    private static final long serialVersionUID = 1L;
    public static final int MODE_INSTALL = 0;
    public static final int MODE_NAME = 1;

    private int mode = MODE_INSTALL;

    @Override
    public int compare(AppModel object1, AppModel object2) {

        switch(mode){
            case MODE_INSTALL:
                if(object1.getLastModify() < object2.getLastModify()){
                    return 1;
                }else if(object1.getLastModify() > object2.getLastModify()){
                    return -1;
                }else{
                    return 0;
                }
            case MODE_NAME:
            default:
                return object1.getAppName().compareTo(object2.getAppName());
        }
    }

    public AppInstallComparator setMode(int mode) {
        this.mode = mode;
        return this;
    }
}
