package com.netmania.checklod.general.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import com.netmania.checklod.general.BaseApplication;

/**
 * Created by hansangcheol on 2018. 3. 28..
 */

public class BaseActivityControl {

    //공지사항등 웹뷰 열기
    //브라우저 띄우기
    public static void openWebBrowser (Context context, String linkURL) {

        Intent browserIntent = new Intent (Intent.ACTION_VIEW, Uri.parse (linkURL));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }


    /** 불루투스 설정창 오픈
     *
     * @param intent
     * @param context
     */
    public static void openBluetoothSettingActivity (Intent intent, Context context) {
        Intent tIntent = new Intent();
        tIntent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
        context.startActivity(tIntent);
    }


    /** NFC 설정 창 오픈
     *
     * @param intent
     * @param context
     */
    public static void openNFCSettingActivity (Intent intent, Context context) {
        Intent tIntent = new Intent();
        tIntent.setAction(Settings.ACTION_NFC_SETTINGS);
        context.startActivity(tIntent);
    }


    /** 위치정보 셋팅 창 오픈
     *
     * @param intent
     * @param context
     */
    public static void openLocationSettingActivity (Intent intent, Context context) {
        //GPS 설정화면으로 이동
        Intent tIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        context.startActivity(tIntent);
    }




    /** 홈화면 선택
     *
     * @param context
     */
    public static void openHomeSelector (Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, ActivityEmptyHome.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        BaseApplication.getInstance().finishAll();

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(selector);

        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }
}
