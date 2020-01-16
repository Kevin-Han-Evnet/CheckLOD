package com.example.testforble.activities;

import android.content.Context;
import android.content.Intent;

public class ActivityControl {



    /** 온도 통계 보여줭~~~
     *
     * @param intent
     * @param context
     * @param macaddress
     */
    public static void openJobReportsActivity (Intent intent, Context context, String macaddress, String sticker) {

        Intent tIntent = new Intent (context, ActivityJobReport.class);
        tIntent.putExtra(ActivityJobReport.EXTRA_KEY_MACADDRESS, macaddress);
        tIntent.putExtra(ActivityJobReport.EXTRA_KEY_STICKER, sticker);
        context.startActivity(tIntent);

    }



    /** 시스템 로그 보여줭~~~
     *
     * @param intent
     * @param context
     */
    public static void openSystemLogActivity (Intent intent, Context context) {

        Intent tIntent = new Intent (context, ActivitySystemLog.class);
        context.startActivity(tIntent);

    }

}
