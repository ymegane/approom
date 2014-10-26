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
package net.kazzz;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import net.kazzz.nfc.NfcTag;
import net.kazzz.util.ArrayUtil;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.ymegane.android.approomcommns.util.MyLog;

/**
 * NFCTagにアクセスするView無しフラグメントクラスのルートを提供します
 * 
 * @author Kazzz.
 * @date 2011/07/09
 * @since Android API Level 10
 *
 */

@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public abstract class AbstractNfcTagFragment extends Fragment {
    public static final String TAG = "AbstractNfcFeliCaTagFragment";
    public static CopyOnWriteArrayList<String[]> sTechList; //
    
    protected String[][] mTechList;
    protected Tag mNfcTag;
    protected ArrayList<INfcTagListener> mListnerList = new ArrayList<INfcTagListener>();
    /**
     *  NFCタグの振舞いを監視するリスナを提供します
     * 
     * @author Copyright c 2011-2012 All Rights Reserved.
     * @date 2011/06/24
     * @since Android API Level 9
     *
     */
    public static interface INfcTagListener {
        void onTagDiscovered(Intent intent
                , Parcelable nfcTag, AbstractNfcTagFragment fragment);
    }
    
    /**
     * NfcAdapterに渡すTechListを登録します (配列のマージを行います)
     * @param techList タグのテクノロジの一覧をセットした文字列配列をセットします
     * @return String[][] 生成したテクノロジ一覧の文字列配列が戻ります
     */
    private static synchronized String[][] registerTechList(String[]... techList) {
        if ( sTechList == null ) {
            sTechList = new CopyOnWriteArrayList<String[]>();
        }
        
        for (String[] filterTech : techList) {
            boolean containTechList = false;
            if ( sTechList.size() > 0 ) {
                for ( String[] source : sTechList) {
                    if ( ArrayUtil.containArray(source, filterTech) ) {
                        containTechList = true;
                        break;
                    }
                }
            }
            if ( !containTechList ) {
                sTechList.add(filterTech);
            }
        }
        
        return sTechList.toArray(new String[sTechList.size()][]);
    }
    
    public AbstractNfcTagFragment() {
        super();
    }

    /**
     * デフォルトコンストラクタ
     * @param activity このフラグメントを管理するアクティビティをセット
     * @param tag このフラグメントを一意の識別するタグ名をセット
     */
    public AbstractNfcTagFragment(Activity activity, String tag) {
        super();
        this.registerFragment(activity, tag);
    }
    /**
     * フラグメントを生成してActivityに登録します
     * @param activity このフラグメントを管理するアクティビティをセット
     * @param tag フラグメントを識別するタグをセット
     */
    protected void registerFragment(Activity activity, String tag) {
        this.unRegisterFragment(activity, tag);
        FragmentManager fm = ((FragmentActivity)activity).getSupportFragmentManager();
        FragmentTransaction trans = fm.beginTransaction();
        {
            trans.add(this, tag);
        }
        trans.commit();
        //fm.executePendingTransactions();
    }
    /**
     * フラグメントActivityから除去します
     * @param activity このフラグメントを管理するアクティビティをセット
     * @param tag フラグメントを識別するタグをセット
     */
    protected void unRegisterFragment(Activity activity, String tag) {
        FragmentManager fm = ((FragmentActivity)activity).getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);
        if ( fragment != null ) {
            FragmentTransaction  trans = fm.beginTransaction();
            {
                trans.remove(fragment);
            }
            trans.commit();
            //fm.executePendingTransactions();
        }
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof INfcTagListener) {
            mListnerList.add((INfcTagListener)activity);
        }
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        MyLog.d(TAG, "*** AbstractNfcFeliCaTagFragment go Pause");
        if ( this.getActivity().isFinishing() ) {
            MyLog.d(TAG, "*** AbstractNfcFeliCaTagFragment will finishing");
            NfcAdapter adapter = 
                    NfcAdapter.getDefaultAdapter(this.getActivity());
            adapter.disableForegroundDispatch(this.getActivity());
            sTechList = null;
        }
        super.onPause();
    }
    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        MyLog.d(TAG, "*** AbstractNfcFeliCaTagFragment go Resume");
        
        //foregrandDispathch
        Activity a = this.getActivity();
        IntentFilter ndef = 
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter tag = 
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter tech = 
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        IntentFilter[] filters = new IntentFilter[] {ndef, tag, tech};
    
        PendingIntent pendingIntent = PendingIntent.getActivity(a, 0,
                new Intent(a, a.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this.getActivity());
        
        adapter.enableForegroundDispatch(this.getActivity()
                , pendingIntent, filters, registerTechList(mTechList) );
        
        super.onResume();
    }
    /**
     * インテントを捕捉する
     * @param intent アクティビティで捕捉したインテントがセットされます
     */
    public void onNewIntent(Intent intent) {
        String action = intent.getAction();
        MyLog.d(TAG, "incomming intent action = " + action );

        //TECHDISCOVERED
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) 
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ) {
            mNfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if ( mNfcTag != null )  {
                String[] tagTechs = mNfcTag.getTechList();
                for (String[] filterTechs : mTechList) {
                    if (ArrayUtil.containArray(tagTechs, filterTechs)) {
                        MyLog.d(TAG, "** nfcTag = " + mNfcTag.toString() );
                        for ( INfcTagListener listener : mListnerList ) {
                            //リスナに通知
                            listener.onTagDiscovered(intent, mNfcTag, this);
                        }
                    }
                }
            }
        }    
    }   
    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onDestroy()
     */
    @Override
    public void onDestroy() {
        //this.unRegisterFragment(this.getActivity(), this.getTag());
        super.onDestroy();
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        MyLog.d(TAG, "onDetach");
        mListnerList.clear();
    }

    /**
     * NfcTagListerを追加します
     * @param listener リスナをセット
     */
    public  void addNfcTagListener (INfcTagListener listener) {
        mListnerList.add(listener);
    }

    /**
     * NfcTagListenerを除去します
     * @param listener リスナをセット
     */
    public void removeNfcTagListener(INfcTagListener listener) {
        mListnerList.remove(listener);
    }

    /**
     * nfcTagを取得します
     * @return Parcelable nfcTagが戻ります
     */
    public Tag getNfcTag() {
        return mNfcTag;
    }

    /**
     * _nfcTagを設定します
     * @param nfcTag nfcTagをセットします
     */
    public void setNfcTag(Tag nfcTag) {
        mNfcTag = nfcTag;
    }
    
    /**
     * techListを取得します
     * @return String[][] techListが戻ります
     */
    public String[][] getTechList() {
        return mTechList;
    }
    /**
     * techListを設定します
     * @param techList techListをセットします
     */
    public void setTechList(String[]... techList) {
        mTechList = techList;
    }
    /**
     * FeliCa/FeliCaLiteタグデータをダンプします
     * @return String 読み込んだデータのダンプ結果が文字列で戻ります
     * @throws Exception
     */
    public abstract String dumpTagData();
    
    /**
     * 適切なNfcTagを生成します
     * @return NfcTag 生成したNfcTagが戻る
     */
    public abstract NfcTag createNfcTag();

}