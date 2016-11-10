package com.bupt.indooranalysis;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import com.bupt.indoorPosition.bean.Beacon;
import com.bupt.indoorPosition.bean.IndoorSignalRecord;
import com.bupt.indoorPosition.bean.Neighbor;
import com.bupt.indoorPosition.bean.Speed;
import com.bupt.indoorPosition.bean.UserSetting;
import com.bupt.indoorPosition.model.ModelService;
import com.bupt.indoorPosition.model.UserService;
import com.bupt.indoorPosition.uti.SignalUtil;
import com.bupt.indooranalysis.fragment.InspectFragment;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by delove on 2016/11/8.
 */

public class ScanSignalService extends Service {

    private String LOG_TAG = "SCANSIGNALSERVICE";
    private TelephonyManager telephonyManager;
    private ConnectivityManager connectivityManager;
    private MyPhonestateListener myPhonestateListener;
    public static List<Neighbor> neighbors = new ArrayList<Neighbor>();
    public static IndoorSignalRecord cellState = new IndoorSignalRecord();
    private Timestamp startTime;
    private Timer positionTimer;
    private int speedCount = 0;
    Map<String, Integer> map = new HashMap<String, Integer>();
    public static Speed speed = new Speed();

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // handler = new MainHandler();
        // init phonestate
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        myPhonestateListener = new MyPhonestateListener();
        telephonyManager.listen(myPhonestateListener,
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                        | PhoneStateListener.LISTEN_CALL_STATE
                        | PhoneStateListener.LISTEN_CELL_INFO
                        | PhoneStateListener.LISTEN_CELL_LOCATION
                        | PhoneStateListener.LISTEN_DATA_ACTIVITY
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                        | PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                        | PhoneStateListener.LISTEN_SERVICE_STATE);

//        beaconSet = new HashSet<Beacon>();
        // 打开蓝牙
//        if (!getPackageManager().hasSystemFeature(
//                PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Toast.makeText(ScanService.this, R.string.ble_not_supported,
//                    Toast.LENGTH_SHORT).show();
//            // finish();
//        }
//        Log.d("bluetooth", "ok");
//        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        bAdapter = bluetoothManager.getAdapter();
//        bAdapter.enable();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (bAdapter != null) {
//            if (!bAdapter.isEnabled())
//                bAdapter.enable();
//            Log.d("bluetooth", "start scaning");
//            bAdapter.startLeScan(mLeScanCallback);
            // test Intent
            // Intent mIntent = new
            // Intent(this,com.bupt.indoorPosition.show.ShowBeacon.class);
            // Bundle mBundle = new Bundle();
            //
            // for(Beacon b:beaconSet){
            // mbeaconSet.add(b);
            // }
            // test Intent
            // 退避算法
            // 计时开始
            UserSetting us = UserService.getUserSetting(ScanSignalService.this);
            final boolean isSpeedTest = us.isTestSpeed();
            Log.i("ScanService 用户设置是否测速", "" + us.isTestSpeed());
            startTime = new Timestamp(System.currentTimeMillis());
            //
            positionTimer = new Timer();
            positionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // speedCount();
                    speedCount = (speedCount + 1) % 6;
                    // 记录信号强度，beacon和邻区
                    // 当没有检测到beacon或者非法信号，不做记录
                    Log.i("上传下载速度测试计数器", "" + speedCount);
                    if (speedCount == 5) {
                        boolean tobeornottobe = ModelService.retreatSignal(map,
                                InspectFragment.isInArea);

                        if (tobeornottobe && isSpeedTest) {

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    ModelService.speedTest(ScanSignalService.this,
                                            speed, cellState);
                                }
                            }).start();
                        }
                    }
        }
    }, 1000, 2000);
        return START_STICKY;
    }

    @Override
	public void onDestroy() {
		if (positionTimer != null)
			positionTimer.cancel();
		Log.i("scan service bluetooth", "end");
		Timestamp end = new Timestamp(System.currentTimeMillis());
		int min = (int) Math.ceil((end.getTime() - startTime.getTime())
				/ (1000 * 60));
		if (InspectFragment.isInArea) {
			ModelService.insertInspection(this, startTime, end, InspectFragment.buildingNumber);
			Toast.makeText(this, "巡检总共进行了 " + min + " min, 成功记录", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "巡检总共进行了 " + min + " min, 写入记录失败\n可能是没有SIM卡",
					Toast.LENGTH_LONG).show();
		}
		telephonyManager.listen(myPhonestateListener,
				PhoneStateListener.LISTEN_NONE);
		super.onDestroy();
	}


    class MyPhonestateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            SignalUtil.updateCellLocation(connectivityManager,
                    telephonyManager, cellState, neighbors);
            // Toast.makeText(ScanService.this,
            // "network " + cellState.getNetworkType(), 500).show();
            Log.d("ScanSignalService","network " + cellState.getNetworkType());
            SignalUtil.updateWireless(signalStrength, cellState);

            // Toast.makeText(
            // ScanService.this,
            // "可能是XG信号强度" + cellState.getSignalStrength() + " "
            // + cellState.getRsrq() + " "
            // + cellState.getNetworkType() + " "
            // + cellState.getSinr(), 500).show();
            // SignalUtil.classInspector(signalStrength);
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            Log.i(LOG_TAG, "current voice state"
                    + serviceState.getState());

        }

        @Override
        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            super.onMessageWaitingIndicatorChanged(mwi);
            Log.i(LOG_TAG,
                    " message-waiting indicator " + mwi);
        }

        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {

            super.onCallForwardingIndicatorChanged(cfi);
            Log.i(LOG_TAG,
                    " call-forwarding indicator  " + cfi);
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {

            super.onCellLocationChanged(location);
            Log.i(LOG_TAG, " current celllocation cid  "
                    + ((GsmCellLocation) location).getCid());
            Log.i(LOG_TAG, " neighbor cell number "
                    + telephonyManager.getNeighboringCellInfo().size());
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            super.onCallStateChanged(state, incomingNumber);
            Log.i(LOG_TAG, " state " + state
                    + " incomingNumber " + incomingNumber);
        }

        @Override
        public void onDataConnectionStateChanged(int state) {

            super.onDataConnectionStateChanged(state);
            Log.i(LOG_TAG, " state " + state);

        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {

            super.onDataConnectionStateChanged(state, networkType);
            Log.i(LOG_TAG, " state " + state
                    + " networkType " + networkType);
        }

        @Override
        public void onDataActivity(int direction) {

            super.onDataActivity(direction);
            Log.i(LOG_TAG, " direction " + direction);
        }

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            super.onCellInfoChanged(cellInfo);
            Log.i(LOG_TAG, " cellInfo size "
                    + (cellInfo == null ? "null" : cellInfo.size()));
        }

    }
}
