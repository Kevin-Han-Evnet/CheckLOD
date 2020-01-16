package com.netmania.checklod.general.service;

/**
 * Created by Kevin Han on 2017-10-24.
 */

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.activities.IBaseJobActivity;
import com.netmania.checklod.general.data.AppSharedData;
import com.netmania.checklod.general.data.DBHelper;
import com.netmania.checklod.general.data.DataProviderUtil;
import com.netmania.checklod.general.dto.AuthorizedDeviceItemDto;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.TemperatureTrackingDto;
import com.netmania.checklod.general.manage.Constants;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.manage.FlagBox;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.general.view.InfoToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import lib.netmania.ble.BleController;
import lib.netmania.ble.callback.NetManiaBleCallbackListener;
import lib.netmania.ble.model.BeaconDataModel;
import lib.netmania.ble.model.BeaconDataModelNew;
import lib.netmania.ble.model.BeaconDeviceModel;

/**
 * Created by mjs on 2017-03-04.
 */
public class SensingProcessService extends Service implements NetManiaBleCallbackListener {


    private final String TAG = "SensingProcessService";


    public static Thread mThread;
    private boolean serviceRunning = false;
    private ComponentName recentComponentName;
    private ActivityManager mActivityManager;

    private DBHelper mDBHelper;

    private TelephonyManager telephonyManager;
    private TPhoneStateListener psListener;




    @Override
    public void onCreate() {
        super.onCreate();
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        serviceRunning = true;
        mDBHelper = new DBHelper(BaseApplication.getInstance());
        LogUtil.I(DebugTags.TAG_SERVICE, "[BaseService] 서비스가 생성되었어요");


        psListener = new TPhoneStateListener ();
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(psListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        startForeground(FlagBox.SENSING_SERVICE_PI_RQ, new Notification());

        LogUtil.I(DebugTags.TAG_SERVICE, "서비스 시작했시오.. 센싱하는거 거시기.. ");

        startListenBle ();

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


    /** 시작해 봅시다.
     *
     */
    private void startListenBle () {
        //ble 테스트 고고싱
        createNewBleController ();
        mBleCheckTimer.schedule(mBleCheckTask, BLE_CHECK_CYCLE, BLE_CHECK_CYCLE); //-> 되살리지 마라...
    }


    /** 콜백 리스너
     *
     */
    @Override
    public void onInitialized() {
        //nothing yet;
    }

    private int fuck = 1;
    @Override
    public void onDataReceived(BeaconDataModel data) {


        LogUtil.I(BleController.TAG, data.idMacAddr + " ----> onDataReceived 수신!!");


        BeaconItemDto tItem = DataProviderUtil.getInstance(BaseApplication.getInstance()).getTrackingDevice (data.idMacAddr);

        if (tItem == null) return;

        LogUtil.I(BleController.TAG, data.idMacAddr + " [" + data.beaconSeq + "] --> " + data.dTemp + " / " + data.dTempOnChip);

        if (tItem.last_seq <= Integer.valueOf(data.beaconSeq)) {

            tItem.last_seq = Integer.valueOf(data.beaconSeq);
            tItem.temp_probe = String.valueOf(data.dTemp);

            tItem.timestamp = data.timestamp;
            tItem.rtc = data.RTC_YEAR + "-" + data.RTC_MONTH + "-" + data.RTC_DATE + data.RTC_HOUR + ":" + data.RTC_MIN + ":" + data.RTC_SEC;
            tItem.temp_chipset = String.valueOf (data.dTempOnChip);
            tItem.bcn_status = Constants.ALAWAYS_RUN_MODE ? BeaconDataModel.BCN_STATUS_RUN : data.bcnReserved;
            tItem.rssi = data.rssi;
            tItem.bcn_battery = data.dvBat;
            tItem.hum_probe = String.valueOf(data.dHum);
            tItem.hum_chipset = String.valueOf(data.dHumOnChip);
            tItem.sensor_disconnected = data.sensor_disconnected;

            /** 냉동건 테스트 */
            if (Constants.IS_FROZEN_TEST) {
                int tIdx = Constants.isFrozenTestDevice (data.idMacAddr);
                if (tIdx >= 0) {
                    tItem.temp_probe = String.valueOf((double)Math.round((data.dTemp + Constants.frozenTestDevicesTempAdaust[tIdx]) * 100000d) / 100000d);
                }
            }

            String qry = "UPDATE " + tItem.getTblName() + " SET "
                    + "last_seq=" + tItem.last_seq + ", "
                    + "temp_probe='" + tItem.temp_probe + "', "
                    + "hum_probe='" + tItem.hum_probe + "', "
                    + "timestamp=" + tItem.timestamp + ", "
                    + "rtc='" + tItem.rtc + "', "
                    + "temp_chipset='" + tItem.temp_chipset + "', "
                    + "hum_chipset='" + tItem.hum_chipset + "', "
                    + "rssi=" + data.rssi + ", "
                    + "bcn_status=" + tItem.bcn_status + ", "
                    + "bcn_battery='" + tItem.bcn_battery + "', "
                    + "sensor_disconnected=" + tItem.sensor_disconnected
                    + " WHERE MAC='" + tItem.MAC + "';";


            LogUtil.I(BleController.TAG, "QRY --> " + qry);

            boolean mResult = mDBHelper.update(qry);
            LogUtil.I(BleController.TAG, data.idMacAddr + "[" + data.beaconSeq + "] ----> onDataReceived 기록됨. ---> " + mResult);
        } else {
            LogUtil.I(BleController.TAG, data.idMacAddr + "[" + data.beaconSeq + "] ----> onDataReceived seq 가 이미 기록된 녀석이라 까임.");
        }



        //배송중인 녀석이라면
        if (tItem.islive == BeaconItemDto.INT_TRUE
                && tItem.delivery_step == BeaconItemDto.DELEVERY_STEP_DELIVERY
                && tItem.start_seq <= Integer.valueOf(data.beaconSeq)) {


            //처음 저장된 데이타를 토대로 계산식 생성
            TemperatureTrackingDto myFirst = DataProviderUtil.getInstance(BaseApplication.getInstance()).getSavedFirstData(data.idMacAddr);
            if (Integer.valueOf(data.beaconSeq) <= myFirst.seq) return;


            TemperatureTrackingDto tData = TemperatureTrackingDto.getInstance();
            tData.seq = Integer.valueOf(data.beaconSeq);
            tData.MAC = data.idMacAddr;
            tData.temp = String.valueOf(data.dTemp);
            tData.outside_temp = String.valueOf(data.dTempOnChip);
            tData.hum = String.valueOf(data.dHum);
            tData.outside_hum = String.valueOf(data.dHumOnChip);
            tData.sensor_disconnected = data.sensor_disconnected;
            tData.is_adjusted = TemperatureTrackingDto.LIVE_DATA;


            if (tItem.ble_signal_cycle > 0) {
                tData.timestamp =  myFirst.timestamp + Long.valueOf (tItem.ble_signal_cycle * ((Integer.valueOf(data.beaconSeq) - myFirst.seq)));
            } else {
                tData.timestamp = data.timestamp;
            }

            tData.timestamp = Calendar.getInstance().getTimeInMillis();

            tData.rtc = data.RTC_YEAR + "-" + data.RTC_MONTH + "-" + data.RTC_DATE + data.RTC_HOUR + ":" + data.RTC_MIN + ":" + data.RTC_SEC; //RTC_STR;
            tData.sent = TemperatureTrackingDto.NOT_REPORTED;
            tData.ble_status = Constants.ALAWAYS_RUN_MODE ? BeaconDataModel.BCN_STATUS_RUN : data.bcnReserved;

            if (tData.timestamp > 0) mDBHelper.insertWithAsset(TemperatureTrackingDto.getInstance().getTblName(), tData.getInsertAsset());

            try {
                DataProviderUtil.getInstance(BaseApplication.getInstance()).updateLostData(data.idMacAddr, 2); //--> 데이타 보정
            } catch (Exception e) {
                e.printStackTrace();
            }


            //시간차 구하기
            long gab = DataProviderUtil.getInstance(BaseApplication.getInstance()).getRtcGab(tData);

            if ((tItem.ble_signal_cycle > gab && Constants.BASE_SIGNAL_CYCLE <= gab) || tItem.ble_signal_cycle <= 0) {

                tItem.ble_signal_cycle = Constants.BASE_SIGNAL_CYCLE;//gab;
                String qry2 = "UPDATE " + tItem.getTblName() + " SET "
                        + "ble_signal_cycle=" + tItem.ble_signal_cycle
                        + " WHERE MAC='" + tItem.MAC + "';";


                mDBHelper.update(qry2);
            }

        }

    }

    @Override
    public void onDataReceived(BeaconDataModelNew data) {

        TemperatureTrackingDto first = DataProviderUtil.getInstance(BaseApplication.getInstance()).getSavedFirstData(data.idMacAddr);
        LogUtil.I("이런 니미 ----> first.seq = " + first.seq);
        int dataNum = (first.seq > 0) ? Integer.valueOf(data.beaconSeq) - first.seq : 0;

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dataNum * Constants.NEW_LOGGER_CYCLE);

        BeaconDataModel t = new BeaconDataModel();
        t.idMacAddr = data.idMacAddr;
        t.beaconSeq = data.beaconSeq;
        t.bcnReserved = data.bcnReserved;
        t.dTemp = data.dTemp;
        t.dHum = data.dHum;
        t.dTempOnChip = data.dTempOnChip;
        t.timestamp = data.timestamp;
        t.dvBat = data.dvBatVol;
        t.rssi = data.rssi;
        t.sensor_disconnected = data.sensor_disconnected;
        //00-01-0100:07:22
        t.RTC_YEAR = "00";//String.valueOf(c.get(Calendar.YEAR)).substring(2);
        t.RTC_MONTH = String.format("%02d", c.get(Calendar.MONTH) + 1);
        t.RTC_DATE = String.format("%02d", c.get(Calendar.DATE)); //String.valueOf(c.get(Calendar.DATE));
        t.RTC_HOUR = String.format("%02d", c.get(Calendar.HOUR_OF_DAY)); //String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        t.RTC_MIN = String.format("%02d", c.get(Calendar.MINUTE)); //String.valueOf(c.get(Calendar.MINUTE));
        t.RTC_SEC = String.format("%02d", c.get(Calendar.SECOND)); //String.valueOf(c.get(Calendar.SECOND));

        LogUtil.I(BleController.TAG, t.idMacAddr + " -- beaconSeq = " + data.beaconSeq);
        LogUtil.I(BleController.TAG, t.idMacAddr + " -- dvBat = " + data.dvBat);
        LogUtil.I(BleController.TAG, t.idMacAddr + " -- sensor_disconnected = " + data.sensor_disconnected);
        LogUtil.I(BleController.TAG, t.idMacAddr + " -- hasNext = " + data.hasNext);

        onDataReceived(t);
    }

    @Override
    public void onDeviceAdd(BeaconDeviceModel deviceModel) {
        //nothing yet;
    }

    @Override
    public void onDeviceRemove(String macAddress) {
        //nothing yet;
    }

    @Override
    public void onBluetoothFailed() {
        //nothing yet;
    }


    /** 음... */
    private BleController mBleController;
    public void createNewBleController () {

        String[] mScanTargets = new String[]{};
        if (mBleController != null) mBleController.releaseBleController();
        mBleController = BleController.getInstance(BaseApplication.getInstance(), mScanTargets, this);
    }


    /** 서브 리스너
     *
     */
    public class TPhoneStateListener extends PhoneStateListener {
        public int signalStrengthValue;

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99)
                    signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
                else
                    signalStrengthValue = signalStrength.getGsmSignalStrength();
            } else {
                signalStrengthValue = signalStrength.getCdmaDbm();
            }

            AppSharedData.put(
                    AppSharedData.DNAME_USER_INFO,
                    AppSharedData.KEY_LTE_RSSI,
                    signalStrengthValue
            );
        }
    }







    //체크체크체크 플리즈 ------------------------------------------------------------------------
    private long BLE_CHECK_CYCLE = 1000 * 60;
    private Timer mBleCheckTimer = new Timer();
    private DataProviderUtil mDataProviderUtil = DataProviderUtil.getInstance(BaseApplication.getInstance());
    private long MAX_ALLOWED_GAB = 1000 * 60 * 2;
    private TimerTask mBleCheckTask = new TimerTask() {
        @Override
        public void run() {

            boolean isDisconnected = false;
            long current = Calendar.getInstance().getTimeInMillis();
            ArrayList<BeaconItemDto> mBeacons = mDataProviderUtil.getTrackingDeviceList();
            for (int i = 0; i < mBeacons.size(); i++) {
                if (current - mDataProviderUtil.getSavedFinalData(mBeacons.get(i).MAC).timestamp > MAX_ALLOWED_GAB) {
                    isDisconnected = true;
                    break;
                }
            }

            LogUtil.I(BleController.TAG, "하나라도 끊겼는가??? --> " + isDisconnected);

            if (isDisconnected) BaseApplication.getInstance().killSensingService(); //자살인가?ㅋㅋㅋㅋㅋ

        }
    };

}
