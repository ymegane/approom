package org.ymegane.android.approom.nfc;

import java.nio.ByteBuffer;

import com.felicanetworks.mfc.PushSegment;
import com.felicanetworks.mfc.PushStartBrowserSegment;

import net.kazzz.felica.FeliCaException;
import net.kazzz.felica.lib.FeliCaLib;
import net.kazzz.felica.lib.FeliCaLib.CommandPacket;
import net.kazzz.felica.lib.FeliCaLib.IDm;

/**
 * FeliCa PushコマンドのNFC用実装
 * @see {@link "http://d.hatena.ne.jp/esmasui/20110130/1296339520"}
 */
public class PushCommand extends CommandPacket {

    public static final byte PUSH = (byte) 0xb0;

    static {
        FeliCaLib.commandMap.put(PUSH, "Push");
    }

    public PushCommand(IDm idm, PushSegment segment) throws FeliCaException {
        super(PUSH, idm, packContent(packSegment(buildData(segment))));
    }

    private static byte[] packContent(byte[] segments) {
        byte[] buffer = new byte[segments.length + 1];
        buffer[0] = (byte) segments.length;
        System.arraycopy(segments, 0, buffer, 1, segments.length);
        return buffer;
    }

    private static byte[] packSegment(byte[]... segments) {

        int bytes = 3; // 個別部数(1byte) + チェックサム(2bytes)
        for (int i = 0; i < segments.length; ++i)
            bytes += segments[i].length;

        ByteBuffer buffer = ByteBuffer.allocate(bytes);

        // 個別部数
        buffer.put((byte) segments.length);

        // 個別部
        for (int i = 0; i < segments.length; ++i)
            buffer.put(segments[i]);

        // チェックサム
        int sum = segments.length;
        for (int i = 0; i < segments.length; ++i) {
            byte[] e = segments[i];
            for (int j = 0; j < e.length; ++j)
                sum += e[j];
        }
        int checksum = -sum & 0xffff;

        putAsBigEndian(checksum, buffer);

        return buffer.array();
    }

    private static byte[][] buildData(PushSegment segment)
            throws FeliCaException {
        if (segment instanceof PushStartBrowserSegment)
            return buildPushStartBrowserSegment((PushStartBrowserSegment) segment);
        throw new IllegalArgumentException("not supported " + segment);
    }

    private static byte[][] buildPushStartBrowserSegment(
            PushStartBrowserSegment segment) {

        final int type = segment.getType();
        final String url = segment.getURL();
        final String browserStartupParam = segment.getBrowserStartupParam();

        byte[] urlByte = url.getBytes();
        byte[] browserStartupParamByte = browserStartupParam == null ? new byte[0]
                : browserStartupParam.getBytes();

        int capacity = urlByte.length + browserStartupParamByte.length + 5;

        ByteBuffer buffer = ByteBuffer.allocate(capacity);

        // 個別部ヘッダ

        // 起動制御情報
        buffer.put((byte) type);
        // 個別部パラメータサイズ
        int paramSize = urlByte.length + browserStartupParamByte.length + 2; // urlLength(2bytes)
        putAsLittleEndian(paramSize, buffer);

        // 個別部パラメータ

        // URLサイズ
        putAsLittleEndian(urlByte.length, buffer);
        // URL
        buffer.put(urlByte);
        // (ブラウザスタートアップパラメータ)
        buffer.put(browserStartupParamByte);

        return new byte[][] { buffer.array() };
    }

    private static void putAsLittleEndian(int i, ByteBuffer buffer) {
        buffer.put((byte) ((i >> 0) & 0xff));
        buffer.put((byte) ((i >> 8) & 0xff));
    }

    private static void putAsBigEndian(int i, ByteBuffer buffer){
        buffer.put((byte) ((i >> 8) & 0xff));
        buffer.put((byte) ((i >> 0) & 0xff));
    }
}
