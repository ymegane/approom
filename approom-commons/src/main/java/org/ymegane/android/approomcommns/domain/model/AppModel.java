package org.ymegane.android.approomcommns.domain.model;

import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * アプリ情報
 * @author y
 */
public class AppModel implements Parcelable {

    private ApplicationInfo appInfo;
    @SerializedName("lastModify")
    private long lastModify;
    @SerializedName("appName")
    private String appName;
    @SerializedName("packageName")
    private String packageName;
    @SerializedName("isStoped")
    private boolean isStoped;
    @SerializedName("palette")
    private int palette;

    private Uri iconUrl;

    @Override
    public String toString() {
        return appName;
    }

    public AppModel() {
    }

    public String getAppName() {
        return appName;
    }

    public ApplicationInfo getAppInfo() {
        return appInfo;
    }

    public int getPalette() {
        return palette;
    }

    public long getLastModify() {
        return lastModify;
    }

    public String getPackageName() {
        return packageName;
    }

    public Uri getIconUrl() {
        return iconUrl;
    }

    public void setAppInfo(ApplicationInfo appInfo) {
        this.appInfo = appInfo;
    }

    public void setLastModify(long lastModify) {
        this.lastModify = lastModify;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setStoped(boolean stoped) {
        isStoped = stoped;
    }

    public void setPalette(int palette) {
        this.palette = palette;
    }

    public void setIconUrl(Uri iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppModel)) return false;

        AppModel appModel1 = (AppModel) o;

        if (isStoped != appModel1.isStoped) return false;
        if (lastModify != appModel1.lastModify) return false;
        if (appInfo != null ? !appInfo.equals(appModel1.appInfo) : appModel1.appInfo != null)
            return false;
        if (appName != null ? !appName.equals(appModel1.appName) : appModel1.appName != null)
            return false;
        if (packageName != null ? !packageName.equals(appModel1.packageName) : appModel1.packageName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appInfo != null ? appInfo.hashCode() : 0;
        result = 31 * result + (int) (lastModify ^ (lastModify >>> 32));
        result = 31 * result + (appName != null ? appName.hashCode() : 0);
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        result = 31 * result + (isStoped ? 1 : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.appInfo, 0);
        dest.writeLong(this.lastModify);
        dest.writeString(this.appName);
        dest.writeString(this.packageName);
        dest.writeByte(isStoped ? (byte) 1 : (byte) 0);
        dest.writeInt(this.palette);
        if (this.iconUrl != null) {
            dest.writeParcelable(this.iconUrl, 0);
        }
    }

    private AppModel(Parcel in) {
        this.appInfo = in.readParcelable(ApplicationInfo.class.getClassLoader());
        this.lastModify = in.readLong();
        this.appName = in.readString();
        this.packageName = in.readString();
        this.isStoped = in.readByte() != 0;
        this.palette = in.readInt();
        this.iconUrl = in.readParcelable(ApplicationInfo.class.getClassLoader());
    }

    public static final Creator<AppModel> CREATOR = new Creator<AppModel>() {
        public AppModel createFromParcel(Parcel source) {
            return new AppModel(source);
        }

        public AppModel[] newArray(int size) {
            return new AppModel[size];
        }
    };
}