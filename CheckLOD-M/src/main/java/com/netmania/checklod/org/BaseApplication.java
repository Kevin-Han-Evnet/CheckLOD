package com.netmania.checklod.org;

import android.content.Intent;
import android.content.IntentFilter;

import com.netmania.checklod.org.activities.general.ActivitySplash;
import com.netmania.checklod.org.manage.Constants;
import com.netmania.checklod.org.receiver.AppBroadcastReceiverM;

public class BaseApplication extends com.netmania.checklod.general.BaseApplication {


    private AppBroadcastReceiverM broadcastReceiverM;

    public void onCreate () {
        super.onCreate();

        spashActivityClass = ActivitySplash.class;
        /** 셋팅 오버라이딩 */
        Constants.configOverride ();

        broadcastReceiverM = new AppBroadcastReceiverM();
        IntentFilter intentFilters = new IntentFilter();
        intentFilters.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilters.addAction(Intent.ACTION_SCREEN_ON);

        getAppContext().registerReceiver(broadcastReceiverM, intentFilters);
    }

}
