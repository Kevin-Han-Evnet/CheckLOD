package com.netmania.checklod.general.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.manage.FlagBox;
import com.netmania.checklod.general.utils.LogUtil;

import static com.netmania.checklod.general.utils.GeneralUtils.isRunningService;


public class SensingServiceMonitor {


    public static final String ACTION_SENSING_SERVICE_RESURRECTION = "ACTION_SENSING_SERVICE_RESURRECTION";
 
    private static SensingServiceMonitor instance;
    private AlarmManager am;
    private Intent intent;
    private PendingIntent sender;
    private long interval = 5000;

    private SensingServiceMonitor() {}
    public static synchronized SensingServiceMonitor getInstance() {
        if (instance == null) {
            instance = new SensingServiceMonitor();
        }

       // LogUtil.I(DebugTags.TAG_SERVICE, "SensingServiceMonitor.getInstance () --> " + instance);
        
        return instance;
    }
 
    public static class SensingServiceMonitorBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            //LogUtil.I(DebugTags.TAG_SERVICE, "SensingServiceMonitorBR.onReceive () --> " + intent.getAction() + " || isRunningService = " + isRunningService(context, SensingProcessService.class));

            if (isRunningService(context, SensingProcessService.class) == false) {
                context.startService (new Intent(context, SensingProcessService.class));
            }
        }
    }
 
    public void setInterval(long interval) {
        this.interval = interval;
    }
 
    public void startMonitoring(Context context) {
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, SensingServiceMonitorBR.class);
        intent.setAction(ACTION_SENSING_SERVICE_RESURRECTION);
        sender = PendingIntent.getBroadcast(context, FlagBox.SENSING_SERVICE_PI_RQ, intent, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval, sender);
    }
 
    public void stopMonitoring(Context context) {
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, SensingServiceMonitorBR.class);
        sender = PendingIntent.getBroadcast(context, FlagBox.SENSING_SERVICE_PI_RQ, intent, 0);
        am.cancel(sender);
        am = null;
        sender = null;
    }
 
    public boolean isMonitoring() {
        return (SensingProcessService.mThread == null || SensingProcessService.mThread.isAlive() == false) ? false : true;
    }
}











