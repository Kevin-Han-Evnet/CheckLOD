package com.netmania.checklod.general.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.activities.IBaseJobActivity;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.LogUtil;


/**
 * Created by hansangcheol on 2017. 9. 11..
 */

public class SystemBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.I(DebugTags.TAG_BROADCAST, "onReceived -- " + intent.getAction());


        BaseApplication mApp = BaseApplication.getInstance();

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            LogUtil.I(DebugTags.TAG_BROADCAST, getClass().getSimpleName() + " ------ ACTION_SCREEN_OFF");
            //GeneralUtils.checkAndRestartApp (context);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            LogUtil.I(DebugTags.TAG_BROADCAST, getClass().getSimpleName() + " ------ ACTION_SCREEN_ON");
        }



        /** 인터넷 상황 변경 */
        else if (intent.getAction ().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            if (mApp.getTaskOnTop() != null && mApp.getTaskOnTop() instanceof IBaseJobActivity) {
                ((IBaseJobActivity) mApp.getTaskOnTop()).updateWifiStatus();
            }

        }
    }
}
