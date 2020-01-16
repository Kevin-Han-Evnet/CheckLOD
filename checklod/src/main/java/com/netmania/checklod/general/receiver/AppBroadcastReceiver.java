package com.netmania.checklod.general.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.activities.IBaseJobActivity;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.LogUtil;


/**
 * Created by hansangcheol on 2017. 10. 24..
 */

public class AppBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        LogUtil.I(DebugTags.TAG_BROADCAST, "onReceived -- " + intent.getAction());

        BaseApplication mApp = BaseApplication.getInstance();

        /** 배터리 상황 변화 */
        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)
                || intent.getAction().equals(Intent.ACTION_BATTERY_LOW )
                || intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)) {

            if (mApp.getTaskOnTop() != null && mApp.getTaskOnTop() instanceof IBaseJobActivity) {
                ((IBaseJobActivity) mApp.getTaskOnTop()).updateBatteryStatus();
            }

        }

    }
}
