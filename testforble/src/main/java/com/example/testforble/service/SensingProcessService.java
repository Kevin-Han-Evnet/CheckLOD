package com.example.testforble.service;

/**
 * Created by Kevin Han on 2017-10-24.
 */

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.testforble.BaseApplication;
import com.example.testforble.R;
import com.example.testforble.data.AppSharedData;
import com.example.testforble.data.DBHelper;
import com.example.testforble.data.DataProviderUtil;
import com.example.testforble.dto.TemperatureTrackingDto;
import com.example.testforble.manage.DebugTags;
import com.example.testforble.manage.FlagBox;
import com.example.testforble.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lib.netmania.ble.BleController;
import lib.netmania.ble.model.BeaconDataModel;
import lib.netmania.ble.model.BeaconDataModelNew;

/**
 * Created by mjs on 2017-03-04.
 */
public class SensingProcessService extends Service {


    private final String TAG = "SensingProcessService";


    public static Thread mThread;
    private boolean serviceRunning = false;
    private ComponentName recentComponentName;
    private ActivityManager mActivityManager;

    private DBHelper mDBHelper;





    @Override
    public void onCreate() {
        super.onCreate();
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        serviceRunning = true;
        mDBHelper = new DBHelper(BaseApplication.getInstance());
        LogUtil.I(DebugTags.TAG_SERVICE, "[BaseService] 서비스가 생성되었어요");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        startForeground(FlagBox.SENSING_SERVICE_PI_RQ, new Notification());

        LogUtil.I(DebugTags.TAG_SERVICE, "서비스 시작했시오.. 센싱하는거 거시기.. ");

        //ble 테스트 고고싱
        startBleScan ();

        try {

            if (mThread == null) {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (serviceRunning) {
                            List<ActivityManager.RecentTaskInfo> info = mActivityManager.getRecentTasks(1, Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (info != null && info.size() > 0) {
                                ActivityManager.RecentTaskInfo recent = info.get(0);
                                Intent mIntent = recent.baseIntent;
                                ComponentName name = mIntent.getComponent();

                                if (name.equals(recentComponentName)) {
                                    LogUtil.D(DebugTags.TAG_SERVICE, "== pre App, recent App is same App");
                                } else {
                                    recentComponentName = name;
                                    LogUtil.D(DebugTags.TAG_SERVICE, "== Application is catched: " + name);
                                }
                            }
                            SystemClock.sleep(2000);
                        }
                    }
                });

                mThread.start();
            } else if (mThread.isAlive() == false) {
                //mThread.start();
                mThread = null;
                onStartCommand(intent, flags, startId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        serviceRunning = false;
        LogUtil.I (DebugTags.TAG_SERVICE, "[BaseService] 서비스가 죽었어요");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    //BLE --------------------------------------------------------------------------------------------------------------------------------------------
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;
    private String[] mScanTargets;
    private ArrayList<String> mTargetList;
    private static final long REPORT_DELAY = 0;

    private void startBleScan () {
        initBle ();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startScan();
        }
    }



    /** BLE initialize
     *
     */
    private void initBle () {

        mTargetList = AppSharedData.getTrackingDeviceList();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }


    /** 스캔 스타트 합시당.
     *
     */
    private BluetoothLeScanner mBluetoothLeScanner;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startScan() {
        if (mScanning) {
            return;
        }


        ScanSettings.Builder sb = new ScanSettings.Builder();
        sb.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        sb.setReportDelay(REPORT_DELAY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            sb.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            sb.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
        }


        ScanSettings scanSettings = sb.build();

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(Collections.singletonList(new ScanFilter.Builder().build()), scanSettings, mScanCallback);

        mScanning = true;

    }




    /** BLE 스캔 콜백
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            /* boolean isIt = false;
            for (int i = 0; i < mTargets.length; i++) {
                if (mTargets[i].equals(result.getDevice().getAddress())) {
                    isIt = true;
                    break;
                }
            }*/


            mTargetList = AppSharedData.getTrackingDeviceList();

            if (!mTargetList.contains(result.getDevice().getAddress())) return;
            //LogUtil.I (BleController.TAG, result.getDevice().getAddress() + " :: " + result.getDevice().getName() + " ---> received!!");

            boolean isLogger = BeaconDataModel.isBeaconBleLogger(result.getScanRecord().getBytes());

            if (isLogger) {
                handleNewBeaconDiscoverd (result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
            } else {
                handleNewBeaconDiscoverd2 (result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            mTargetList = AppSharedData.getTrackingDeviceList();
            boolean isLogger;

            for (int i = 0; i < results.size(); i++) {
                if (mTargetList.contains(results.get(i).getDevice().getAddress()))  {
                    //LogUtil.I (BleController.TAG, results.get(i).getDevice().getAddress() + " :: " + results.get(i).getDevice().getName() + " (list) ---> received!!");

                    isLogger = BeaconDataModel.isBeaconBleLogger(results.get(i).getScanRecord().getBytes());
                    if (isLogger) {
                        handleNewBeaconDiscoverd(results.get(i).getDevice(), results.get(i).getRssi(), results.get(i).getScanRecord().getBytes());
                    } else {
                        handleNewBeaconDiscoverd2(results.get(i).getDevice(), results.get(i).getRssi(), results.get(i).getScanRecord().getBytes());
                    }
                }
            }

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i(TAG, "실패 !!!!! --> " + errorCode);
        }
    };



    /** 음....
     *
     * @param bleDevice
     * @param rssi
     * @param advertisement
     */
    public void handleNewBeaconDiscoverd (final BluetoothDevice bleDevice, final int rssi, final byte[] advertisement)  {

        BeaconDataModel tmpBeacon = new BeaconDataModel();

        try {
            tmpBeacon.addFrom (bleDevice, rssi, advertisement);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (AppSharedData.getBoolean(AppSharedData.DNAME_PREFERENCE, tmpBeacon.idMacAddr)) {

            TemperatureTrackingDto tData = TemperatureTrackingDto.getInstance();
            tData.seq = Integer.valueOf(tmpBeacon.beaconSeq);
            tData.MAC = tmpBeacon.idMacAddr;
            tData.temp = String.valueOf(tmpBeacon.dTemp);
            tData.outside_temp = String.valueOf(tmpBeacon.dTempOnChip);
            tData.hum = String.valueOf(tmpBeacon.dHum);
            tData.outside_hum = String.valueOf(tmpBeacon.dHumOnChip);
            tData.is_adjusted = TemperatureTrackingDto.LIVE_DATA;
            tData.timestamp = tmpBeacon.timestamp;
            //tData.timestamp = Calendar.getInstance().getTimeInMillis();

            tData.rtc = ""; //tmpBeacon.RTC_YEAR + "-" + tmpBeacon.RTC_MONTH + "-" + tmpBeacon.RTC_DATE + tmpBeacon.RTC_HOUR + ":" + tmpBeacon.RTC_MIN + ":" + tmpBeacon.RTC_SEC;

            tData.sent = TemperatureTrackingDto.NOT_REPORTED;
            tData.ble_status = tmpBeacon.bcnReserved;

            tData.battery = (int) tmpBeacon.dvBat;
            tData.rssi = tmpBeacon.rssi;
            tData.is_adjusted = TemperatureTrackingDto.LIVE_DATA;

            TemperatureTrackingDto t = DataProviderUtil.getInstance(BaseApplication.getInstance()).getSavedDataWithSeq(tData.MAC, String.valueOf(tData.seq));
            if (tData.timestamp > 0 && t == null) mDBHelper.insertWithAsset(TemperatureTrackingDto.getInstance().getTblName(), tData.getInsertAsset());

            try {
                DataProviderUtil.getInstance(BaseApplication.getInstance()).updateLostData(tmpBeacon.idMacAddr, 2); //--> 데이타 보정
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tmpBeacon = null;

    }



    /** 음....
     *
     * @param bleDevice
     * @param rssi
     * @param advertisement
     */
    public void handleNewBeaconDiscoverd2 (final BluetoothDevice bleDevice, final int rssi, final byte[] advertisement)  {

        BeaconDataModelNew tmpBeacon = new BeaconDataModelNew();

        try {
            tmpBeacon.addFrom (bleDevice, rssi, advertisement);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (AppSharedData.getBoolean(AppSharedData.DNAME_PREFERENCE, tmpBeacon.idMacAddr)) {

            TemperatureTrackingDto tData = TemperatureTrackingDto.getInstance();
            tData.seq = Integer.valueOf(tmpBeacon.beaconSeq);
            tData.MAC = tmpBeacon.idMacAddr;
            tData.temp = String.valueOf(tmpBeacon.dTemp);
            tData.outside_temp = String.valueOf(tmpBeacon.dTempOnChip);
            tData.hum = String.valueOf(tmpBeacon.dHum);
            tData.outside_hum = String.valueOf(tmpBeacon.dHumOnChip);
            tData.is_adjusted = TemperatureTrackingDto.LIVE_DATA;
            tData.timestamp = tmpBeacon.timestamp;
            //tData.timestamp = Calendar.getInstance().getTimeInMillis();

            tData.rtc = ""; //tmpBeacon.RTC_YEAR + "-" + tmpBeacon.RTC_MONTH + "-" + tmpBeacon.RTC_DATE + tmpBeacon.RTC_HOUR + ":" + tmpBeacon.RTC_MIN + ":" + tmpBeacon.RTC_SEC;

            tData.sent = TemperatureTrackingDto.NOT_REPORTED;
            tData.ble_status = tmpBeacon.bcnReserved;

            tData.battery = tmpBeacon.dvBat;
            tData.rssi = tmpBeacon.rssi;
            tData.is_adjusted = TemperatureTrackingDto.LIVE_DATA;

            TemperatureTrackingDto t = DataProviderUtil.getInstance(BaseApplication.getInstance()).getSavedDataWithSeq(tData.MAC, String.valueOf(tData.seq));
            if (tData.timestamp > 0 && t == null) mDBHelper.insertWithAsset(TemperatureTrackingDto.getInstance().getTblName(), tData.getInsertAsset());

            try {
                DataProviderUtil.getInstance(BaseApplication.getInstance()).updateLostData(tmpBeacon.idMacAddr, 2); //--> 데이타 보정
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tmpBeacon = null;

    }

}
