package org.ymegane.android.approomcommns.data.repository;

import android.annotation.TargetApi;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

@TargetApi(11)
public class QRCodeLoader extends AsyncTaskLoader<Bitmap> {
    private String target;
    private int size;

    /** エンコード設定 */
    private static final String ENCORD_NAME = "ISO-8859-1";

    public QRCodeLoader(Context context, String target, int size) {
        super(context);
        this.target = target;
        this.size = size;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Bitmap loadInBackground() {
        Bitmap ret = null;
        try{
            int displaySize = size;
            // QRコードを生成
            QRCodeWriter writer = new QRCodeWriter();
            Hashtable<EncodeHintType, Object> encodeHint = new Hashtable<EncodeHintType, Object>();
            encodeHint.put(EncodeHintType.CHARACTER_SET, ENCORD_NAME);
            encodeHint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            BitMatrix bitData = writer.encode(target, BarcodeFormat.QR_CODE, displaySize, displaySize, encodeHint);
            int width = bitData.getWidth();
            int height = bitData.getHeight();
            int[] pixels = new int[width * height];
            // All are 0, or black, by default
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitData.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }
            // Bitmapに変換
            ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            ret.setPixels(pixels, 0, width, 0, 0, width, height);
            return ret;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
