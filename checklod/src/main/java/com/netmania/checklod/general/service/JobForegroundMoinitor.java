package com.netmania.checklod.general.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.data.DataProviderUtil;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.manage.FlagBox;
import com.netmania.checklod.general.utils.AlarmWakeLock;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.LogUtil;

import java.util.ArrayList;

import static com.netmania.checklod.general.utils.GeneralUtils.*;


public class JobForegroundMoinitor {


    public static final String ACTION_JOB_ACTIVITY_RESURRECTION = "ACTION_JOB_ACTIVITY_RESURRECTION";

    private static JobForegroundMoinitor instance;
    private AlarmManager am;
    private Intent intent;
    private PendingIntent sender;
    private long interval = 1000 * 30;

    private JobForegroundMoinitor() {}
    public static synchronized JobForegroundMoinitor getInstance() {
        if (instance == null) {
            instance = new JobForegroundMoinitor();
        }

        
        return instance;
    }
 
    public static class JobForegroundMoinitorBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean isDeliverying = false;
            ArrayList<BeaconItemDto> beacons = DataProviderUtil.getInstance(BaseApplication.getInstance()).getTrackingDeviceList();
            for (int i = 0; i < beacons.size(); i++) {
                if (beacons.get(i).islive == BeaconItemDto.INT_TRUE && beacons.get(i).delivery_step == BeaconItemDto.DELEVERY_STEP_DELIVERY) {
                    isDeliverying = true;
                    break;
                }
            }


            LogUtil.I(DebugTags.TAG_CHECK_ALIVE, "돌긴 도냐??");

            if (isDeliverying && !GeneralUtils.isScreenOn(BaseApplication.getInstance())) {
                BaseApplication.getInstance().restartApplication();
            }

        }
    }
 
    public void setInterval(long interval) {
        this.interval = interval;
    }
 
    public void startMonitoring(Context context) {

        LogUtil.I(DebugTags.TAG_CHECK_ALIVE, "함 돌아봐라...");

        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, JobForegroundMoinitorBR.class);
        intent.setAction(ACTION_JOB_ACTIVITY_RESURRECTION);
        sender = PendingIntent.getBroadcast(context, FlagBox.JOB_ACTIVITY_PI_RQ, intent, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, sender);

        isMonitoring = true;
    }
 
    public void stopMonitoring(Context context) {

        LogUtil.I(DebugTags.TAG_CHECK_ALIVE, "고만돌아라......");

        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, JobForegroundMoinitorBR.class);
        sender = PendingIntent.getBroadcast(context, FlagBox.JOB_ACTIVITY_PI_RQ, intent, 0);
        am.cancel(sender);
        am = null;
        sender = null;

        isMonitoring = false;
    }

    private boolean isMonitoring = false;
    public boolean isMonitoring() {
        return isMonitoring;
    }
}











