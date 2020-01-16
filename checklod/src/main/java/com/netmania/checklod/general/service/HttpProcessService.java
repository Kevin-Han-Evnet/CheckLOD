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
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.androidquery.callback.AjaxStatus;
import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.data.AppSharedData;
import com.netmania.checklod.general.data.DBHelper;
import com.netmania.checklod.general.data.DataProviderUtil;
import com.netmania.checklod.general.data.DataSenderUtil;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.DriverInfoDto;
import com.netmania.checklod.general.dto.LocationDto;
import com.netmania.checklod.general.dto.TemperatureTrackingDto;
import com.netmania.checklod.general.hash.Base64EncUtil;
import com.netmania.checklod.general.http.BaseAPI;
import com.netmania.checklod.general.http.GeneralAPI;
import com.netmania.checklod.general.http.MonitoringAPI;
import com.netmania.checklod.general.manage.Constants;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.manage.FlagBox;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.general.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lib.netmania.location.LocationChecker;
import lib.netmania.location.callback.LocationCallback;

/**
 * Created by mjs on 2017-03-04.
 */



public class HttpProcessService extends Service {


    private final String TAG = "HttpProcessService";

    private ArrayList<Messenger> mClientCallbacks = new ArrayList<Messenger>();
    final Messenger mMessenger = new Messenger( new CallbackHandler());
    int mValue = 0;


    public static Thread mThread;
    private boolean serviceRunning = false;
    private ComponentName recentComponentName;
    private ActivityManager mActivityManager;

    private LocationChecker locationChecker;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        serviceRunning = true;

        LogUtil.I(DebugTags.TAG_SERVICE, "[BaseService] 서비스가 생성되었어요");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startForeground(FlagBox.HTTP_SERVICE_PI_RQ, new Notification());
        LogUtil.I(DebugTags.TAG_SERVICE, "서비스 시작했시오.. 데이타 센드하는거 ");
        startSendData ();

        //테스트 데이타 저장
        /*startTestSaveData ();*/

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


    /** 보낼거 보냅시다.
     *
     */
    private void checkAndSendInfoToServer () {

        ArrayList<BeaconItemDto> beacons = DataProviderUtil.getInstance(BaseApplication.getInstance()).getTrackingDeviceList();
        BeaconItemDto bcn;
        for (int i = 0; i < beacons.size(); i++) {
            bcn = beacons.get(i);

            if (bcn.islive == BeaconItemDto.INT_TRUE
                    && bcn.delivery_step == BeaconItemDto.DELEVERY_STEP_DELIVERY
                    && bcn.is_data_checked_in == BeaconItemDto.INT_FALSE) {
                DataSenderUtil.getInstance(BaseApplication.getInstance(), mCallback).doCheckIn(beacons.get(i));
                return;
            }
        }


        //위 체크인이 다되면 아래 실행됨.....
        DataSenderUtil.getInstance(BaseApplication.getInstance(), mCallback).sendQueDatas();
    }



    /** 데이타 전송 개시
     *
     */
    private void startSendData () {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(mTimerTask, 0, Constants.REPORT_CYCLE);
        }



        if (locationChecker == null) locationChecker = new LocationChecker(BaseApplication.getInstance(), mLocationListener, Constants.SEND_DEBUG_INFO_TERM);
        locationChecker.onResume();
    }


    @Override
    public void onDestroy(){
        serviceRunning = false;
        LogUtil.I (DebugTags.TAG_SERVICE, "[BaseService] 서비스가 죽었어요");
        super.onDestroy();
    }




    private class CallbackHandler  extends Handler {
        @Override
        public void handleMessage( Message msg ){
            /*switch( msg.what ){
                //nothing yet;
            }*/
        }
    }



    //send data timer -----------------------------------------------------------------------------------------------------------------------------
    private Timer mTimer;
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {

            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    checkAndSendInfoToServer ();
                }
            };
            mainHandler.post(myRunnable);
        }
    };




    //리스너 -------------------------------------------------
    DataSenderUtil.DataSendCallback mCallback = new DataSenderUtil.DataSendCallback() {
        @Override
        public void onComplete(String mac) {
            //nothing yet;
        }

        @Override
        public void onTakeOver(final String mac) {
            
            final ArrayList<TemperatureTrackingDto> notReportedData = DataProviderUtil.getInstance(BaseApplication.getInstance()).getNotReportedData(mac);


            if (notReportedData.size() > 0) {

                String rk = "";
                for (int i = 0; i < notReportedData.size(); i++) {
                    rk += (i > 0) ?", " : "";
                    rk += notReportedData.get(i).seq;
                }



                ///로컬 테스트 전용
                if (Constants.TEST_FOR_LOCAL_ONLY) {

                    for (int i = 0; i < notReportedData.size(); i++) {
                        DataProviderUtil.getInstance(BaseApplication.getInstance ()).updateTemperatureData (String.valueOf(notReportedData.get(i).idx));
                    }

                    return;
                }



                MonitoringAPI.updateBeaconInfo(
                        BaseApplication.getInstance(),
                        true,
                        notReportedData,
                        new BaseAPI.ApiMapListenerWithFailedRest() {
                            @Override
                            public void onComplete() {

                                for (int i = 0; i < notReportedData.size(); i++) {
                                    DataProviderUtil.getInstance(BaseApplication.getInstance ()).updateTemperatureData (String.valueOf(notReportedData.get(i).idx));
                                }


                                setDisabledDeviceWithTakeOver(mac);
                            }

                            @Override
                            public void onComplete(Map<String, Object> map) {
                                for (int i = 0; i < notReportedData.size(); i++) {
                                    DataProviderUtil.getInstance(BaseApplication.getInstance ()).updateTemperatureData (String.valueOf(notReportedData.get(i).idx));
                                }

                                setDisabledDeviceWithTakeOver(mac);
                            }

                            @Override
                            public void onFailed(AjaxStatus result) {
                                setDisabledDeviceWithTakeOver(mac);
                            }

                            @Override
                            public void onFailed(AjaxStatus result, Map<String, Object> map) {
                                setDisabledDeviceWithTakeOver(mac);
                            }
                        }
                );
                
            } else {

                setDisabledDeviceWithTakeOver (mac);

            }

        }

        @Override
        public void onCheckedIn(String mac) {
            String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                    + "is_data_checked_in=" + BeaconItemDto.INT_TRUE
                    + " WHERE MAC='" + mac + "';";
            new DBHelper(BaseApplication.getInstance()).update(qry);

            checkAndSendInfoToServer();
        }

        @Override
        public void onCheckeInFailed(String mac) {
            //다음 기회에...
        }

        @Override
        public void onFailed(String mac) {
            //nothing yet;
        }
    };


    /** 인수인계된 비콘 처리
     *
     * @param mac
     */
    private void setDisabledDeviceWithTakeOver (String mac) {
        String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                + "delivery_step = " + BeaconItemDto.DELEVERY_STEP_HANDOVER + ", "
                + "islive = " + BeaconItemDto.INT_FALSE
                + " WHERE MAC='" + mac + "';";
        new DBHelper(BaseApplication.getInstance()).update(qry);
    }


    /** 위치 리스너
     *
     */
    private LocationCallback mLocationListener = new LocationCallback() {
        @Override
        public void onInitialized() {
            //nothing yet;
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            //nothing yet;
        }

        @Override
        public void onDataReceived(String lat, String lng) {

            LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "위치정보 도착! ---> " + lat + " -- " + lng);

            DataProviderUtil dpUtil = DataProviderUtil.getInstance(BaseApplication.getInstance());

            DriverInfoDto driver = AppSharedData.getDriverInfo();
            ArrayList<BeaconItemDto> beacons = dpUtil.getTrackingDeviceList();

            for (int i = 0; i < beacons.size(); i++) {

                if (beacons.get(i).delivery_step == BeaconItemDto.DELEVERY_STEP_DELIVERY) {


                    String last_ble_info = "--> " + beacons.get(i).last_seq
                            + "\n--> " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format (new Date(beacons.get(i).timestamp))
                            + "\n --> " + BeaconItemDto.getBleStatusName(beacons.get(i).bcn_status)
                            + "\n --> " + beacons.get(i).temp_probe + "℃ / " + beacons.get(i).temp_chipset + "℃"
                            + "\n --> 비콘 배터리 : " + beacons.get(i).bcn_battery;

                    String test_div_code = (Constants.TEST_DIV_CODE > 0) ? getResources().getString(Constants.TEST_DIV_CODE) : "NONE";


                    GeneralAPI.updateDebugInfo(
                            BaseApplication.getInstance(),
                            driver.id,
                            beacons.get(i).MAC,
                            beacons.get(i).rssi,
                            lat,
                            lng,
                            test_div_code,
                            Base64EncUtil.encode(last_ble_info, "utf-8"),
                            new BaseAPI.ApiMapListenerWithFailedRest() {
                                @Override
                                public void onComplete() {
                                    //nothing;
                                }

                                @Override
                                public void onComplete(Map<String, Object> map) {
                                    //nothing;
                                }

                                @Override
                                public void onFailed(AjaxStatus result) {
                                    //nothing;
                                }

                                @Override
                                public void onFailed(AjaxStatus result, Map<String, Object> map) {
                                    //nothing;
                                }
                            }
                    );


                    MonitoringAPI.sendLogerSignal(
                            BaseApplication.getInstance(),
                            false,
                            beacons.get(i).MAC,
                            beacons.get(i).rssi,
                            String.valueOf(beacons.get(i).bcn_battery),
                            StringUtils.convertToStringYYYY_MM_DD(new Date(beacons.get(i).timestamp)),
                            new BaseAPI.ApiMapListenerWithFailedRest() {
                                @Override
                                public void onComplete() {
                                    //nothing yet;
                                }

                                @Override
                                public void onComplete(Map<String, Object> map) {
                                    //nothing yet;
                                }

                                @Override
                                public void onFailed(AjaxStatus result) {
                                    //nothing yet;
                                }

                                @Override
                                public void onFailed(AjaxStatus result, Map<String, Object> map) {
                                    //nothing yet;
                                }
                            }
                    );


                    LocationDto loc = new LocationDto();
                    loc.lat = Double.valueOf (lat);
                    loc.lng = Double.valueOf (lng);
                    loc.timestamp = Calendar.getInstance().getTimeInMillis();
                    AppSharedData.setLastLocationInfo(loc);


                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }



            /** 가자 */
            MonitoringAPI.sendPhoneSignal(
                    BaseApplication.getInstance(),
                    false,
                    AppSharedData.getInt(AppSharedData.DNAME_USER_INFO, AppSharedData.KEY_LTE_RSSI),
                    String.valueOf(lat),
                    String.valueOf(lng),
                    String.valueOf(GeneralUtils.getBatteryStatusInfo(BaseApplication.getInstance()).life),
                    new BaseAPI.ApiMapListenerWithFailedRest() {
                        @Override
                        public void onComplete() {
                            //nothign yet;
                        }

                        @Override
                        public void onComplete(Map<String, Object> map) {
                            //nothign yet;
                        }

                        @Override
                        public void onFailed(AjaxStatus result) {
                            //nothign yet;
                        }

                        @Override
                        public void onFailed(AjaxStatus result, Map<String, Object> map) {
                            //nothign yet;
                        }
                    }
            );

        }
    };

}
