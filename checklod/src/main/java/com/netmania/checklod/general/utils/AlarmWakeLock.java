package com.netmania.checklod.general.utils;

/**
 * Created by Kevin Han on 2017-01-17.
 */

import android.content.Context;
import android.os.PowerManager;

public class AlarmWakeLock {

    private static final String TAG = "AlarmWakeLock";
    private static PowerManager.WakeLock mWakeLock;

    public static void wakeLock (Context context) {
        if(mWakeLock != null) {
            return;
        }

        PowerManager powerManager =
                (PowerManager)context.getSystemService(
                        Context.POWER_SERVICE);


        mWakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP, context.getClass().getName());
        mWakeLock.acquire();
    }



    public static void releaseWakeLock() {
        if(mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}

