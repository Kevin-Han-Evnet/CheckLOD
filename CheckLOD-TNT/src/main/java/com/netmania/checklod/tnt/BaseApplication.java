package com.netmania.checklod.tnt;

import android.content.Intent;
import android.content.IntentFilter;

import com.netmania.checklod.tnt.activities.general.ActivitySplash;
import com.netmania.checklod.tnt.manage.Constants;
import com.netmania.checklod.tnt.receiver.AppBroadcastReceiverTNT;

public class BaseApplication extends com.netmania.checklod.general.BaseApplication {


    private AppBroadcastReceiverTNT broadcastReceiverTNT;

    public void onCreate () {
        super.onCreate();

        spashActivityClass = ActivitySplash.class;
        /** 셋팅 오버라이딩 */
        Constants.configOverride ();

        broadcastReceiverTNT = new AppBroadcastReceiverTNT();
        IntentFilter intentFilters = new IntentFilter();
        intentFilters.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilters.addAction(Intent.ACTION_SCREEN_ON);

        getAppContext().registerReceiver(broadcastReceiverTNT, intentFilters);
    }

}
