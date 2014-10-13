package org.ymegane.android.approomcommns;

import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * アプリ情報
 * @author y
 */
public class AppInfo implements Parcelable {

    public ApplicationInfo appInfo;
    @SerializedName("lastModify")
    public long lastModify;
    @SerializedName("appName")
    public String appName;
    @SerializedName("packageName")
    public String packageName;
    @SerializedName("isStoped")
    public boolean isStoped;

    @Override
    public String toString() {
        return appName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.lastModify);
        dest.writeString(this.appName);
        dest.writeString(this.packageName);
        dest.writeByte(isStoped ? (byte) 1 : (byte) 0);
    }

    public AppInfo() {
    }

    private AppInfo(Parcel in) {
        this.lastModify = in.readLong();
        this.appName = in.readString();
        this.packageName = in.readString();
        this.isStoped = in.readByte() != 0;
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        public AppInfo createFromParcel(Parcel source) {
            return new AppInfo(source);
        }

        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
}
