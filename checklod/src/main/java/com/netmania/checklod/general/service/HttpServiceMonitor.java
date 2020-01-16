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


public class HttpServiceMonitor {


    public static final String ACTION_HTTP_SERVICE_RESURRECTION = "ACTION_HTTP_SERVICE_RESURRECTION";

    private static HttpServiceMonitor instance;
    private AlarmManager am;
    private Intent intent;
    private PendingIntent sender;
    private long interval = 5000;

    private HttpServiceMonitor() {}
    public static synchronized HttpServiceMonitor getInstance() {
        if (instance == null) {
            instance = new HttpServiceMonitor();
        }

        //LogUtil.I(DebugTags.TAG_SERVICE, "HttpServiceMonitor.getInstance () --> " + instance);
        
        return instance;
    }
 
    public static class HttpServiceMonitorBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            //LogUtil.I(DebugTags.TAG_SERVICE, "HttpServiceMonitorBR.onReceive () --> " + intent.getAction() + " || isRunningService = " + isRunningService(context, HttpProcessService.class));

            if (isRunningService(context, HttpProcessService.class) == false) {
                context.startService (new Intent(context, HttpProcessService.class));
            }
        }
    }
 
    public void setInterval(long interval) {
        this.interval = interval;
    }
 
    public void startMonitoring(Context context) {
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, HttpServiceMonitorBR.class);
        intent.setAction(ACTION_HTTP_SERVICE_RESURRECTION);
        sender = PendingIntent.getBroadcast(context, FlagBox.HTTP_SERVICE_PI_RQ, intent, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval, sender);
    }
 
    public void stopMonitoring(Context context) {
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, HttpServiceMonitorBR.class);
        sender = PendingIntent.getBroadcast(context, FlagBox.HTTP_SERVICE_PI_RQ, intent, 0);
        am.cancel(sender);
        am = null;
        sender = null;
    }
 
    public boolean isMonitoring() {
        return (HttpProcessService.mThread == null || HttpProcessService.mThread.isAlive() == false) ? false : true;
    }
}











