package org.ymegane.android.approomcommns.util;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by y on 2014/10/11.
 */
public class IconCache {
    private static LruCache<String, Bitmap> iconLruCache;

    private static IconCache instance;

    private IconCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;       // 最大メモリに依存した実装

        iconLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 使用キャッシュサイズ(ここではKB単位)
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                // または bitmap.getByteCount() / 1024を利用
            }
        };
    }

    public static synchronized IconCache getInstance() {
        if (instance == null) {
            instance = new IconCache();
        }
        return instance;
    }

    // Cacheのインターフェイス実装
    public Bitmap getBitmap(String packageName) {
        return iconLruCache.get(packageName);
    }

    public void putBitmap(String packageName, Bitmap bitmap) {
        iconLruCache.put(packageName, bitmap);
    }

    public void clear() {
        iconLruCache.evictAll();
    }
}
