package org.ymegane.android.approom.presentation.view.fragment.nfc;

import org.ymegane.android.approom.presentation.view.activity.DetailActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class AndroidBeamFragment extends Fragment {
    public static final String TAG = "AndroidBeamFragment";

    private NfcAdapter mNfcAdapter;
    private OnCreateNdefMessageListener mListener;

    public static AndroidBeamFragment newInstance() {
        return new AndroidBeamFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mListener = (OnCreateNdefMessageListener) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if(mNfcAdapter == null) {
            return;
        }
        mNfcAdapter.setNdefPushMessageCallback(null, getActivity());
        mNfcAdapter.setNdefPushMessageCallback(new CreateNdefMessageCallback() {
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                String uri = mListener.onRequestSendUri();
                if(TextUtils.isEmpty(uri)) return null; // 状態が不正

                NdefMessage msg = new NdefMessage( new NdefRecord[] {NdefRecord.createUri(uri)});
                return msg;
            }
        }, getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        DetailActivity activity = (DetailActivity) getActivity();
        if(mNfcAdapter != null && !activity.isDestroy()) {
            mNfcAdapter.setNdefPushMessageCallback(null, getActivity());
        }
        super.onDestroy();
    }

    public interface OnCreateNdefMessageListener {
        String onRequestSendUri();
    }
}
