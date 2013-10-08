# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:/android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#Otto
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

#Gson
-keep class com.google.gson.** {
    *;
}

-keepattributes Signature

-keep class org.ymegane.android.approomcommons.** {
    *;
}

#GPS
-keep class android.support.wearable.view.WearableListView {
  private void setScrollAnimator(int);
  private void setScrollVertically(int);
}

-keep class android.support.wearable.view.WearableListView.ViewHolder {
  private void setFocusPaddingTop(int);
  private void setFocusPaddingBottom(int);
}

# GmsCore Proguard rules.
# See: https://developer.android.com/google/play-services/setup.html
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

# Keep SafeParcelable value, needed for reflection. This is required to support backwards
# compatibility of some classes.
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

# Keep the names of classes/members we need for client functionality.
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

# Needed for Parcelable/SafeParcelable Creators to not get stripped
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}