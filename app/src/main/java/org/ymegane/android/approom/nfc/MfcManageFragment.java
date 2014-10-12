package org.ymegane.android.approom.nfc;

import java.util.Map;
import java.util.WeakHashMap;

import com.felicanetworks.mfc.AppInfo;
import com.felicanetworks.mfc.Felica;
import com.felicanetworks.mfc.FelicaEventListener;
import com.felicanetworks.mfc.FelicaException;
import com.felicanetworks.mfc.PushIntentSegment;
import com.felicanetworks.mfc.PushStartBrowserSegment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import org.ymegane.android.approomcommns.util.MyLog;

/**
 * Felica Libraryを利用してPush送信を行うFragment
 * @author y
 *
 */
public class MfcManageFragment extends Fragment implements ServiceConnection, FelicaEventListener {
    public static final String TAG = "MfcManageFragment";

    public interface OnPushRequestEventObserver {
        String onPushRequest();
    }
    private OnPushRequestEventObserver eventObserver;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            // 端末が近接したことを検知しようとしたけど無理だった
            // 通常状態の端末では搬送波は出してないっぽい
            //if(!getRFSState()) {
            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(eventObserver.onPushRequest()));
            //pushToSendIntent(intent);
            //pushToSendBrowser(eventObserver.onPushRequest());
            //}
            new FelicaPushTask().execute(eventObserver.onPushRequest());
            handler.sendEmptyMessageDelayed(0, 1000);
        };
    };

    private class FelicaPushTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];
            if(url != null) {
                pushToSendBrowser(url);
            }
            return null;
        }
    }

    private Felica felica;
    private boolean connected;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        eventObserver = (OnPushRequestEventObserver) activity;
        connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isFelicaOpen) {
            open();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        close();
        isFelicaOpen = false;
        handler.removeMessages(0);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        inactivateFelica();

        disconnect();
    }

    public static boolean isEnalbeFelica(Context context) {
        boolean ret = false;

        try {
            String verson = Felica.getMFCVersion(context);
            MyLog.d(TAG, "MFCVersion = " + verson);
            ret = true;
        } catch (FelicaException e) {
            if(e.getType() == FelicaException.TYPE_MFC_NOT_FOUND) {
                MyLog.d(TAG, "MFC_NOT_FOUND");
            }
        }

        return ret;
    }

    private void connect() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), Felica.class);
        if (!getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE)) {
            return;
        }
        // 接続状態変更はonServiceConnected()が呼び出されたタイミングで実施
    }

    private void disconnect() {
        if(connected) {
            getActivity().unbindService(this);
        }
    }

    /**
     * Felicaの利用開始処理を実行します。
     */
    private void activateFelica() {

        // まず前処理の情報をクリア
        clear();

        // 状態チェック
        try {
            checkFelica();

        } catch (IllegalArgumentException e) {
            MyLog.w(TAG, "Felica#activateFelica() failed.", e);
            //requestToDisplay("Felica#activateFelica() failed." + e.getMessage());
            return;
        }

        try {
            // FeliCaチップの利用開始処理
            // 第1引数には、フェリカネットワークスより払い出された許可証
            // (複数ある場合は許可証配列)を、第2引数には、
            // 利用開始処理結果通知用リスナFelicaEventListenerを指定。
            felica.activateFelica(null, this);
            //requestToDisplay("Felica#activateFelica() succeeded. waiting...");
        } catch (IllegalArgumentException e) {
            // 不正な引数が指定された場合
            //requestToDisplay(e.getMessage());
        } catch (FelicaException e) {
            // FelicaExceptionをキャッチした場合
            handleFelicaException(e);
        } catch (Exception e) {
            // 予期せぬ例外をキャッチした場合
            handleUnexpectedException(e);
        }
    }

    /**
     * Felicaの利用終了処理を実行します。
     */
    private void inactivateFelica() {

        // 前処理の情報をクリア
        clear();

        // 状態チェック
        try {
            checkFelica();

        } catch (IllegalArgumentException e) {
            MyLog.w(TAG, "Felica#inactivateFelica() failed", e);
            return;
        }

        try {
            // FeliCaチップの利用終了処理
            // (Felica#activateFelica()の処理結果待ちの場合、利用開始処理をキャンセルする)
            felica.inactivateFelica();
            MyLog.d(TAG, "Felica#inactivateFelica() succeeded!");
        } catch (FelicaException e) {
            // FelicaExceptionをキャッチした場合
            handleFelicaException(e);
        } catch (Exception e) {
            // 予期せぬ例外をキャッチした場合
            handleUnexpectedException(e);
        }
    }

    private boolean isFelicaOpen = false;
    /**
     * Felicaチップをオープンします。
     */
    private void open() {

        // 前処理の情報をクリア
        clear();

        // 状態チェック
        try {
            checkFelica();

        } catch (IllegalArgumentException e) {
            MyLog.w(TAG, "Felica#open() failed.", e);
            return;
        }

        try {
            // FeliCaチップのオープン
            felica.open();
            isFelicaOpen = true;
            handler.sendEmptyMessageDelayed(0, 300);
            MyLog.d(TAG, "Felica#open() succeeded!");
        } catch (FelicaException e) {
            // FelicaExceptionをキャッチした場合
            handleFelicaException(e);
        } catch (Exception e) {
            // 予期せぬ例外をキャッチした場合
            handleUnexpectedException(e);
        }
    }

    /**
     * Felicaチップをクローズします。
     */
    private void close() {

        // 前処理の情報をクリア
        clear();

        // 状態チェック
        try {
            checkFelica();

        } catch (IllegalArgumentException e) {
            MyLog.w(TAG, "close", e);
            return;
        }

        try {
            // FeliCaチップのクローズ
            felica.close();
            MyLog.d(TAG, "Felica#close() succeeded!");
        } catch (FelicaException e) {
            // FelicaExceptionをキャッチした場合
            handleFelicaException(e);
        } catch (Exception e) {
            // 予期せぬ例外をキャッチした場合
            handleUnexpectedException(e);
        }
    }

    /**
     * Push送信によって、Androidインテントを送信します。
     */
    public void pushToSendIntent(Intent intent) {

        // 状態チェック
        try {
            checkFelica();

        } catch (IllegalArgumentException e) {
            MyLog.w(TAG, "Felica#push() (send Intent) failed.", e);
            return;
        }

        try {
            // Androidインテント実行パラメータの生成
            PushIntentSegment pushSegment;
            pushSegment = new PushIntentSegment(intent);

            // Push送信
            felica.push(pushSegment);

            MyLog.d(TAG, "Felica#push() (send Intent) succeeded!");
        } catch (IllegalArgumentException e) {
            // 不正な引数が指定された場合
        } catch (FelicaException e) {
            // FelicaExceptionをキャッチした場合
            handleFelicaException(e);
        } catch (Exception e) {
            // 予期せぬ例外をキャッチした場合
            handleUnexpectedException(e);
        }
    }

    /**
     * Push送信によって、Androidインテントを送信します。
     */
    public void pushToSendBrowser(String uri) {

        // 状態チェック
        try {
            checkFelica();

        } catch (IllegalArgumentException e) {
            MyLog.w(TAG, "Felica#push() (send Intent) failed.", e);
            return;
        }

        try {
            // Androidインテント実行パラメータの生成
            PushStartBrowserSegment pushSegment;
            pushSegment = new PushStartBrowserSegment(uri, null);

            // Push送信
            felica.push(pushSegment);

            MyLog.d(TAG, "Felica#push() (send Browser) succeeded!");
        } catch (IllegalArgumentException e) {
            // 不正な引数が指定された場合
        } catch (FelicaException e) {
            // FelicaExceptionをキャッチした場合
            handleFelicaException(e);
        } catch (Exception e) {
            // 予期せぬ例外をキャッチした場合
            handleUnexpectedException(e);
        }
    }

    /**
     * RFSの状態を取得します。
     */
    private boolean getRFSState() {

        // 状態チェック
        try {
            checkFelica();

        } catch (IllegalArgumentException e) {
            MyLog.w(TAG, "Felica#getRFSState() failed.", e);
            return false;
        }

        try {

            // 搬送波の受信状況の取得
            boolean state = felica.getRFSState();
            MyLog.d(TAG, "Felica#getRFSState() result:" + state);
            return state;

        } catch (FelicaException e) {
            // FelicaExceptionをキャッチした場合
            handleFelicaException(e);
        } catch (Exception e) {
            // 予期せぬ例外をキャッチした場合
            handleUnexpectedException(e);
        }
        return false;
    }

    private void clear() {
        if(felica != null) {
            try {
                felica.close();
            } catch (FelicaException e) {
                MyLog.w(TAG, e.getMessage());
            }
        }
    }

    /**
     * Felicaが設定されていることをチェックします
     */
    private void checkFelica() throws IllegalArgumentException {

        if (felica == null) {
            throw new IllegalArgumentException("Felica is not set");
        }
    }

    /**
     * FelicaExceptionの種別を文字列に変換するためのマップ。
     */
    private static final Map<Integer, String> FELICA_EXCEPTION_ID_CONVERSION_MAP = new WeakHashMap<Integer, String>() {

        {
            put(FelicaException.ID_UNKNOWN_ERROR, "ID_UNKNOWN_ERROR");
            put(FelicaException.ID_ILLEGAL_STATE_ERROR, "ID_ILLEGAL_STATE_ERROR");
            put(FelicaException.ID_IO_ERROR, "ID_IO_ERROR");
            put(FelicaException.ID_GET_KEY_VERSION_ERROR, "ID_GET_KEY_VERSION_ERROR");
            put(FelicaException.ID_READ_ERROR, "ID_READ_ERROR");
            put(FelicaException.ID_WRITE_ERROR, "ID_WRITE_ERROR");
            put(FelicaException.ID_SET_NODECODESIZE_ERROR, "ID_SET_NODECODESIZE_ERROR");
            put(FelicaException.ID_OPEN_ERROR, "ID_OPEN_ERROR");
            put(FelicaException.ID_GET_NODE_INFORMATION_ERROR, "ID_GET_NODE_INFORMATION_ERROR");
            put(FelicaException.ID_GET_PRIVACY_NODE_INFORMATION_ERROR,
                    "ID_GET_PRIVACY_NODE_INFORMATION_ERROR");
            put(FelicaException.ID_SET_PRIVACY_ERROR, "ID_SET_PRIVACY_ERROR");
            put(FelicaException.ID_PERMISSION_ERROR, "ID_PERMISSION_ERROR");
            put(FelicaException.ID_GET_BLOCK_COUNT_INFORMATION_ERROR,
                    "ID_GET_BLOCK_COUNT_INFORMATION_ERROR");
        }
    };

    /**
     * FelicaExceptionのタイプを文字列に変換するためのマップ。
     */
    private static final Map<Integer, String> FELICA_EXCEPTION_TYPE_CONVERSION_MAP = new WeakHashMap<Integer, String>() {

        {
            put(FelicaException.TYPE_NOT_OPENED, "TYPE_NOT_OPENED");
            put(FelicaException.TYPE_CURRENTLY_ONLINE, "TYPE_CURRENTLY_ONLINE");
            put(FelicaException.TYPE_NOT_SELECTED, "TYPE_NOT_SELECTED");
            put(FelicaException.TYPE_NOT_ACTIVATED, "TYPE_NOT_ACTIVATED");
            put(FelicaException.TYPE_INVALID_RESPONSE, "TYPE_INVALID_RESPONSE");
            put(FelicaException.TYPE_TIMEOUT_OCCURRED, "TYPE_TIMEOUT_OCCURRED");
            put(FelicaException.TYPE_OPEN_FAILED, "TYPE_OPEN_FAILED");
            put(FelicaException.TYPE_SELECT_FAILED, "TYPE_SELECT_FAILED");
            put(FelicaException.TYPE_GET_KEY_VERSION_FAILED, "TYPE_GET_KEY_VERSION_FAILED");
            put(FelicaException.TYPE_SERVICE_NOT_FOUND, "TYPE_SERVICE_NOT_FOUND");
            put(FelicaException.TYPE_BLOCK_NOT_FOUND, "TYPE_BLOCK_NOT_FOUND");
            put(FelicaException.TYPE_PIN_NOT_CHECKED, "TYPE_PIN_NOT_CHECKED");
            put(FelicaException.TYPE_READ_FAILED, "TYPE_READ_FAILED");
            put(FelicaException.TYPE_PURSE_FAILED, "TYPE_PURSE_FAILED");
            put(FelicaException.TYPE_CASH_BACK_FAILED, "TYPE_CASH_BACK_FAILED");
            put(FelicaException.TYPE_INVALID_PIN, "TYPE_INVALID_PIN");
            put(FelicaException.TYPE_CHECK_PIN_LIMIT, "TYPE_CHECK_PIN_LIMIT");
            put(FelicaException.TYPE_CHECK_PIN_OVERRUN, "TYPE_CHECK_PIN_OVERRUN");
            put(FelicaException.TYPE_WRITE_FAILED, "TYPE_WRITE_FAILED");
            put(FelicaException.TYPE_ENABLE_PIN_FAILED, "TYPE_ENABLE_PIN_FAILED");
            put(FelicaException.TYPE_FELICA_NOT_SET, "TYPE_FELICA_NOT_SET");
            put(FelicaException.TYPE_DEVICELIST_NOT_SET, "TYPE_DEVICELIST_NOT_SET");
            put(FelicaException.TYPE_LISTENER_NOT_SET, "TYPE_LISTENER_NOT_SET");
            put(FelicaException.TYPE_COMMUNICATION_START_FAILED, "TYPE_COMMUNICATION_START_FAILED");
            put(FelicaException.TYPE_SET_NODECODESIZE_FAILED, "TYPE_SET_NODECODESIZE_FAILED");
            put(FelicaException.TYPE_GET_CONTAINER_ISSUE_INFORMATION_FAILED,
                    "TYPE_GET_CONTAINER_ISSUE_INFORMATION_FAILED");
            put(FelicaException.TYPE_NOT_IC_CHIP_FORMATTING, "TYPE_NOT_IC_CHIP_FORMATTING");
            put(FelicaException.TYPE_ILLEGAL_NODECODE, "TYPE_ILLEGAL_NODECODE");
            put(FelicaException.TYPE_GET_NODE_INFORMATION_FAILED,
                    "TYPE_GET_NODE_INFORMATION_FAILED");
            put(FelicaException.TYPE_GET_PRIVACY_NODE_INFORMATION_FAILED,
                    "TYPE_GET_PRIVACY_NODE_INFORMATION_FAILED");
            put(FelicaException.TYPE_SET_PRIVACY_FAILED, "TYPE_SET_PRIVACY_FAILED");
            put(FelicaException.TYPE_NOT_CLOSED, "TYPE_NOT_CLOSED");
            put(FelicaException.TYPE_ILLEGAL_METHOD_CALL, "TYPE_ILLEGAL_METHOD_CALL");
            put(FelicaException.TYPE_PUSH_FAILED, "TYPE_PUSH_FAILED");
            put(FelicaException.TYPE_ALREADY_ACTIVATED, "TYPE_ALREADY_ACTIVATED");
            put(FelicaException.TYPE_GET_BLOCK_COUNT_INFORMATION_FAILED,
                    "TYPE_GET_BLOCK_COUNT_INFORMATION_FAILED");
            put(FelicaException.TYPE_RESET_FAILED, "TYPE_RESET_FAILED");
            put(FelicaException.TYPE_GET_SYSTEM_CODE_LIST_FAILED,
                    "TYPE_GET_SYSTEM_CODE_LIST_FAILED");
            put(FelicaException.TYPE_GET_CONTAINER_ID_FAILED, "TYPE_GET_CONTAINER_ID_FAILED");
            put(FelicaException.TYPE_REMOTE_ACCESS_FAILED, "TYPE_REMOTE_ACCESS_FAILED");
            put(FelicaException.TYPE_CURRENTLY_ACTIVATING, "TYPE_CURRENTLY_ACTIVATING");
            put(FelicaException.TYPE_ILLEGAL_SYSTEMCODE, "TYPE_ILLEGAL_SYSTEMCODE");
            put(FelicaException.TYPE_GET_RFS_STATE_FAILED, "TYPE_GET_RFS_STATE_FAILED");
            put(FelicaException.TYPE_INVALID_SELECTED_INTERFACE, "TYPE_INVALID_SELECTED_INTERFACE");
            put(FelicaException.TYPE_FELICA_NOT_AVAILABLE, "TYPE_FELICA_NOT_AVAILABLE");
        }
    };

    /**
     * FelicaExceptionの内容に応じて、エラー処理を実施します。 処理の内容は以下の通り。
     * ・ID_UNKNOWN_ERRORが発生した場合に、FeliCaチップのクローズ、およびFeliCaチップの利用終了処理を実施
     * ・(ID_OPEN_ERROR, TYPE_NOT_IC_CHIP_FORMATTING)が発生した場合に、FeliCaチップの利用終了処理を実施
     * ・エラー内容を画面に表示
     *
     * @param e 処理対象のFelicaException
     */
    private void handleFelicaException(FelicaException e) {

        String errMsg;
        String iDString = FELICA_EXCEPTION_ID_CONVERSION_MAP.get(e.getID());
        String typeString = FELICA_EXCEPTION_TYPE_CONVERSION_MAP.get(e.getType());
        String additionalMsg = null;

        switch (e.getID()) {
            case FelicaException.ID_UNKNOWN_ERROR:
                // 未知のエラーが発生
                // 復帰不能なのでFelica#close()、#inactivateFelica()を実行
                try {
                    felica.close();
                    felica.inactivateFelica();
                } catch (FelicaException e1) {
                    // 強制終了処理に失敗した場合、キャッチした例外を無視
                }
                break;
            case FelicaException.ID_OPEN_ERROR:
                if (e.getType() == FelicaException.TYPE_NOT_IC_CHIP_FORMATTING) {
                    Toast.makeText(getActivity(), "「おサイフケータイアプリ」を起動してFeliCaの初期化を行ってください", Toast.LENGTH_LONG).show();

                    // Felica#inactivateFelica()を実行し、FeliCaチップの利用終了処理を実施
                    try {
                        felica.inactivateFelica();
                    } catch (FelicaException e1) {
                        // 利用終了処理に失敗した場合、キャッチした例外を無視
                    }
                }
                break;
            default:
                break;
        }

        // エラー内容をViewに表示

        // ベースメッセージ生成
        errMsg = "caught FelicaException\n(ID, TYPE)=(" + iDString + ", " + typeString + ")";

        // 追加メッセージ作成
        switch (e.getID()) {
            case FelicaException.ID_UNKNOWN_ERROR:
                // 復帰困難なエラーが発生した旨を表示
                additionalMsg = "(Non-recoverable error)";
                break;
            case FelicaException.ID_READ_ERROR:
            case FelicaException.ID_WRITE_ERROR:
            case FelicaException.ID_GET_NODE_INFORMATION_ERROR:
            case FelicaException.ID_GET_PRIVACY_NODE_INFORMATION_ERROR:
            case FelicaException.ID_GET_BLOCK_COUNT_INFORMATION_ERROR:
            case FelicaException.ID_SET_PRIVACY_ERROR:
                // ステータスフラグ取得
                additionalMsg = "Status Flag1:" + e.getStatusFlag1() + ", StatusFlag2:"
                        + e.getStatusFlag2();
                break;
            default:
                break;
        }

        if (additionalMsg != null) {
            errMsg += "\n";
            errMsg += additionalMsg;
        }
        //requestToDisplay(errMsg);
        MyLog.w(TAG, errMsg);

    }

    /**
     * 予期しないエラーへの処理を実施します(予期しないエラーか否かは呼出元で判定済)。 処理の内容は以下の通り。
     * ・FeliCaチップのクローズ、およびFeliCaチップの利用終了処理を実施 ・エラー内容を画面に表示
     *
     * @param e 処理対象のException
     */
    private void handleUnexpectedException(Exception e) {

        // 復帰不能なのでFelica#close()、#inactivateFelica()を実行
        try {
            felica.close();
            felica.inactivateFelica();
        } catch (FelicaException e1) {
            // 強制終了処理に失敗した場合、キャッチした例外を無視
        }

        // エラー内容をViewに表示
        //requestToDisplay(e.getMessage());
        MyLog.w(TAG, "handleUnexpectedException", e);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Felicaとの接続が確立されたので、Felicaインスタンスを取得する
        felica = ((Felica.LocalBinder)service).getInstance();
        //MFCSampleUse.getInstance().setFelica(felica);
        connected = true;
        // Activateを実行
        activateFelica();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        connected = false;
    }

    @Override
    public void errorOccurred(int id, String msg, AppInfo otherAppInfo) {
        String result = "Felica#activateFelica() failed\nError:";

        switch (id) {
            case FelicaEventListener.TYPE_USED_BY_OTHER_APP:
                // 他アプリケーションによってFeliCaチップが使用中の場合
                result += "FeliCa Chip is used by other application(PID:" + otherAppInfo.getPid() + ")";
                Toast.makeText(getActivity(), "他のアプリケーションでFeliCaが利用中です", Toast.LENGTH_LONG).show();
                break;
            case FelicaEventListener.TYPE_NOT_FOUND_ERROR:
                // 利用可能な許可証がみつからなかった場合
                result += "valid permits not found";
                break;
            case FelicaEventListener.TYPE_HTTP_ERROR:
                // 許可証検証中にHTTPエラーが発生した場合
                result += "HTTP error";
                break;
            case FelicaEventListener.TYPE_MFC_VERSION_ERROR:
                // MFCのバージョンアップが必要な場合
                result += "MFC version is too old";
                break;
            case FelicaEventListener.TYPE_UTILITY_VERSION_ERROR:
                // MFCユーティリティのバージョンアップが必要な場合
                result += "MFC utility version is too old";
                break;
            case FelicaEventListener.TYPE_UNKNOWN_ERROR:
                // その他のエラー
                result += "unknown error";
                break;
            default:
                // MFC仕様上、想定外のパス
                result += "unexpected error";
                break;
        }
        if (msg != null) {
            result += "\nerror detail:" + msg;
        }
        MyLog.d(TAG, "errorOccurred#" + result);
    }

    @Override
    public void finished() {
        MyLog.d(TAG, "Felica#activateFelica() succeeded!");
        open();
    }
}
