package org.ymegane.android.approomcommns.util;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

public class HtmlUtil {
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            return Html.fromHtml(source);
        } else {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        }
    }
}
