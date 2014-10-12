/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kazz.felica;


import net.kazzz.AbstractNfcTagFragment;
import net.kazzz.felica.FeliCaException;
import net.kazzz.felica.FeliCaLiteTag;
import net.kazzz.felica.FeliCaTag;
import net.kazzz.felica.command.ReadResponse;
import net.kazzz.felica.lib.FeliCaLib;
import net.kazzz.felica.lib.FeliCaLib.IDm;
import net.kazzz.felica.lib.FeliCaLib.MemoryConfigurationBlock;
import net.kazzz.felica.lib.FeliCaLib.ServiceCode;
import net.kazzz.felica.lib.FeliCaLib.SystemCode;
import net.kazzz.nfc.NfcTag;
import android.annotation.TargetApi;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.support.v4.app.FragmentActivity;

import org.ymegane.android.approomcommns.util.MyLog;

/**
 * NfcでFeliCa(FeliCa Lite)Tagを読み込むためのフラグメントを提供します
 * 
 * @author Kazzz.
 * @date 2011/06/24
 * @since Android API Level 9
 *
 */

@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class NfcFeliCaTagFragment extends AbstractNfcTagFragment {
    public static final String TAG = "NfcFeliCaTagFragment";

    public NfcFeliCaTagFragment() {
        super();
        mTechList = new String[][]{ new String[] { NfcF.class.getName() }};
    }

    /**
     * コンストラクタ 
     * @param activity アクティビティをセット
     */
    public NfcFeliCaTagFragment(FragmentActivity activity) {
        super(activity, NfcFeliCaTagFragment.TAG);
        
        // FeliCa及びFeliCaLiteは NFC-F のみ
        mTechList = new String[][]{ new String[] { NfcF.class.getName() }};
    }
    /**
     * FeliCa Liteデバイスか否かを検査します
     * @return boolean 読み込み対象がFeliCa Liteの場合trueが戻ります
     * @throws FeliCaException
     */
    public boolean isFeliCaLite()  {
        FeliCaTag f = new FeliCaTag(mNfcTag); 
        //polling は IDm、PMmを取得するのに必要
        IDm idm;
        try {
            idm = f.pollingAndGetIDm(FeliCaLib.SYSTEMCODE_FELICA_LITE);
        } catch (FeliCaException e) {
            return false;
        }
        return idm != null;
    }
    /**
     * FeliCatTagクラスのインスタンスを生成します
     * @return FeliCaTag 生成したFeliCaTagクラスのインスタンスが戻ります
     */
    public FeliCaTag createFeliCaTag() {
        return new FeliCaTag(mNfcTag);
    }
    /**
     * FeliCaLiteTagクラスのインスタンスを生成します
     * @return FeliCaLiteTag 生成したFeliCaLiteTagクラスのインスタンスが戻ります
     */
    public FeliCaLiteTag createFeliCaLiteTag() {
        return new FeliCaLiteTag(mNfcTag);
    }
    /* (non-Javadoc)
     * @see net.kazzz.AbstractNfcTagFragment#createNfcTag()
     */
    @Override
    public NfcTag createNfcTag() {
        return this.isFeliCaLite()
                ? this.createFeliCaLiteTag()
                : this.createFeliCaTag();
    }
    /* (non-Javadoc)
     * @see net.kazzz.NfcTagFragment#dumpTagData()
     */
    @Override
    public String dumpTagData() {
        StringBuilder sb = new StringBuilder();
        try {
            if ( this.isFeliCaLite() ) {
                sb.append("\n");
                sb.append("FeliCa Lite デバイスです ");
                sb.append("\n----------------------------------------");
                sb.append("\n");
                // FeliCa Lite 読み込み
                FeliCaLiteTag ft =  this.createFeliCaLiteTag();
                ft.polling();
                sb.append("  " + ft.toString());
                sb.append("\n----------------------------------------");
                sb.append("\n");
                
                //0ブロック目読み込み
                ReadResponse rr = ft.readWithoutEncryption((byte)0);
                sb.append("  " + rr.toString());
                sb.append("\n----------------------------------------");
                sb.append("\n");
                
                //MemoryConfig 読み込み
                MemoryConfigurationBlock mb = ft.getMemoryConfigBlock(); 
                sb.append("  " + mb.toString());
                sb.append("\n----------------------------------------");
                sb.append("\n");
                
                String result = sb.toString();
                MyLog.d(TAG, result);
                return result;
            }
            
            // FeliCa 
            FeliCaTag ft = this.createFeliCaTag();
            IDm idm = ft.pollingAndGetIDm(FeliCaLib.SYSTEMCODE_ANY);
            if ( idm != null ) {
                sb.append("\n");
                sb.append("FeliCa デバイスです");
                sb.append("\n-----------------------------------------");
                sb.append("\n");
                sb.append(ft.toString());

                // enum systemCode
                sb.append("\n");
                sb.append("  システムコード一覧");
                sb.append("\n  -----------------------------------------");
                sb.append("\n");
                SystemCode[] scs = ft.getSystemCodeList();
                for ( SystemCode sc : scs ) {
                    sb.append("  ").append(sc.toString()).append("\n");
                }

                // enum serviceCode
                sb.append("\n");
                sb.append("  サービスコード一覧");
                sb.append("\n-  ----------------------------------------");
                sb.append("\n");
                ServiceCode[] svs = ft.getServiceCodeList();
                for ( ServiceCode sc : svs ) {
                    sb.append("  ").append(sc.toString()).append("\n");
                }
            } else {
                sb.append("デバイスの読み込みに失敗しました");
            }
            
        } catch (Exception e) {
            String result = sb.toString();
            MyLog.d(TAG, result);
            e.printStackTrace();
            return result;
        }
        String result = sb.toString();
        MyLog.d(TAG, result);
        return result;
    }
    /**
     * FeliCa 使用履歴をダンプします
     * @return
     */
    public String dumpFeliCaHistoryData() throws Exception {
        try {
            if ( this.isFeliCaLite() ) {
                throw new FeliCaException("Tag is not FeliCa (maybe FeliCaLite)");
            }
            FeliCaTag f = this.createFeliCaTag();

            //polling は IDm、PMmを取得するのに必要
            f.polling(FeliCaLib.SYSTEMCODE_PASMO);

            //read
            ServiceCode sc = new ServiceCode(FeliCaLib.SERVICE_SUICA_HISTORY);
            byte addr = 0;
            ReadResponse result = f.readWithoutEncryption(sc, addr);

            StringBuilder sb = new StringBuilder();
            while ( result != null && result.getStatusFlag1() == 0  ) {
                sb.append("履歴 No.  " + (addr + 1) + "\n");
                sb.append("---------\n");
                sb.append("\n");
                //Suica.History s = new Suica.History(result.getBlockData(), this.getActivity());
                //sb.append(s.toString());
                sb.append("---------------------------------------\n");
                sb.append("\n");

                addr++;
                //Log.d(TAG, "addr = " + addr);
                result = f.readWithoutEncryption(sc, addr);
            }

            String str = sb.toString();
            MyLog.d(TAG, str);
            return str;
        } catch (FeliCaException e) {
            e.printStackTrace();
            MyLog.e(TAG, "readHistoryData", e);
            throw e;
        }
    }
    
}
