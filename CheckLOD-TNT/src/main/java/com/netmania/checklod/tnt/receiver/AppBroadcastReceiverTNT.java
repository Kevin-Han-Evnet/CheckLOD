package com.netmania.checklod.tnt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.AlarmWakeLock;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.tnt.BaseApplication;
import com.netmania.checklod.tnt.activities.ActivityControl;
import com.netmania.checklod.tnt.data.DataProviderUtil;

import java.util.ArrayList;

public class AppBroadcastReceiverTNT extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        LogUtil.I(DebugTags.TAG_BROADCAST, "onReceived -- " + intent.getAction());
        if (intent.getAction() == Intent.ACTION_SCREEN_OFF) {


            boolean isLiveTracking = false;
            ArrayList<BeaconItemDto> beacons = DataProviderUtil.getInstance(BaseApplication.getInstance()).getTrackingDeviceList();
            for (int i = 0; i < beacons.size(); i++) {
                if (beacons.get(i).delivery_step == BeaconItemDto.DELEVERY_STEP_DELIVERY) {
                    isLiveTracking = true;
                    break;
                }
            }

            if (isLiveTracking) {
                try {
                    ActivityControl.openLockCoverActviity(intent, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                AlarmWakeLock.wakeLock(BaseApplication.getInstance());
                new Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                AlarmWakeLock.releaseWakeLock();
                            }
                        }, 1000
                );
            }
        }

    }
}
