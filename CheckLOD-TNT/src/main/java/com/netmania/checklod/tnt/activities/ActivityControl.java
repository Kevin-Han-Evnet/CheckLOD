package com.netmania.checklod.tnt.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.activities.BaseActivityControl;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.manage.FlagBox;
import com.netmania.checklod.tnt.R;
import com.netmania.checklod.tnt.activities.general.ActivityLockCover;
import com.netmania.checklod.tnt.activities.job.ActivityCustomUserInputUI;
import com.netmania.checklod.tnt.activities.job.ActivityDataSync;
import com.netmania.checklod.tnt.activities.job.ActivityJobMain;
import com.netmania.checklod.tnt.activities.job.ActivityJobReport;
import com.netmania.checklod.tnt.activities.job.ActivityPhotoComposition;

/**
 * Created by Kevin Han on 2017-10-25.
 */

public class ActivityControl extends BaseActivityControl {

    /** 홈 액티비티
     *
     * @param intent
     * @param context
     */
    public static void openJobReadyActivity (Intent intent, Context context) {
        Intent tIntent = new Intent (context, ActivityJobMain.class);
        context.startActivity(tIntent);
    }


    /** 유저 입력 화면
     *
     * @param intent
     * @param context
     */
    public static void openUserInputUI (Intent intent, Context context, int mode) {
        openUserInputUI (intent, context, mode, null);
    }


    /**
     *
     * @param intent
     * @param context
     * @param mode
     */
    public static void openUserInputUI (Intent intent, Context context, int mode, BeaconItemDto tItem) {
        Intent tIntent = new Intent (context, ActivityCustomUserInputUI.class);
        tIntent.putExtra(FlagBox.EXTRA_KEY_MODE, mode);
        tIntent.putExtra(FlagBox.EXTRA_KEY_DATA, (Parcelable) tItem);
        ((BaseActivity) context).startActivityForResult(tIntent, FlagBox.REQUEST_USER_INPUT);
    }





    /** 온도 통계 보여줭~~~
     *
     * @param intent
     * @param context
     * @param macaddress
     */
    public static void openJobReportsActivity (Intent intent, Context context, String macaddress, String sticker, int reauestCode) {

        Intent tIntent = new Intent (context, ActivityJobReport.class);
        tIntent.putExtra(ActivityJobReport.EXTRA_KEY_MACADDRESS, macaddress);
        tIntent.putExtra(ActivityJobReport.EXTRA_KEY_STICKER, sticker);
        ((BaseActivity) context).startActivityForResult(tIntent, reauestCode);

    }


    /** 비콘 데이타 싱크 화면
     *
     * @param intent
     * @param context
     */
    public static void openDataSyncActivity (Intent intent, Context context, String mac) {

        Intent tIntent = new Intent (context, ActivityDataSync.class);
        tIntent.putExtra(ActivityDataSync.EXTRA_KEY_MACADDRESS, mac);

        //등장효과
        ((BaseActivity) context).startActivityForResult(tIntent, FlagBox.REQUEST_DATA_SYNC);
        ((BaseActivity) context).overridePendingTransition(R.anim.push_left_in_fast, R.anim.push_left_out_half);
    }


    /**
     *
     * @param intent
     * @param context
     * @param mac
     */
    public static void openPhotoCompositionActviity (Intent intent, Context context, String mac) {
        Intent tIntent = new Intent(context, ActivityPhotoComposition.class);
        tIntent.putExtra(ActivityPhotoComposition.EXTRA_KEY_MACADDRESS, mac);

        //((BaseActivity) context).startActivityForResult(tIntent, FlagBox.REQUEST_HAND_OUT);
        context.startActivity(tIntent);
    }


    /** 락커버 화면 고고고
     *
     * @param intent
     * @param context
     */
    public static void openLockCoverActviity (Intent intent, Context context) {
        Intent tIntent = new Intent(context, ActivityLockCover.class);
        tIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(tIntent);
    }

}
