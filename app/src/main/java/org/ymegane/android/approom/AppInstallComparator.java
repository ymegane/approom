package org.ymegane.android.approom;

import java.io.Serializable;
import java.util.Comparator;

/**
 * アプリのインストール日時Comparator
 * @author y
 *
 */
public class AppInstallComparator implements Comparator<AppInfo>, Serializable {
	private static final long serialVersionUID = 1L;
	public static final int MODE_INSTALL = 0;
	public static final int MODE_NAME = 1;

	private int mode = MODE_INSTALL;

	@Override
	public int compare(AppInfo object1, AppInfo object2) {

		switch(mode){
		case MODE_INSTALL:
			if(object1.lastModify < object2.lastModify){
				return 1;
			}else if(object1.lastModify > object2.lastModify){
				return -1;
			}else{
				return 0;
			}
		case MODE_NAME:
		default:
			return object1.appName.compareTo(object2.appName);
		}
	}

	public AppInstallComparator setMode(int mode) {
		this.mode = mode;
		return this;
	}
}
