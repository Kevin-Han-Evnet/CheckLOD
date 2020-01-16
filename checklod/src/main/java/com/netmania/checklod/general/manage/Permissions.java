package com.netmania.checklod.general.manage;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.utils.DialogUtils;


/**
 * Created by hansangcheol on 2018. 2. 27..
 */

public class Permissions {


    /** 해당 퍼미션을 가지고 있는지 체크
     *
     * @param context
     * @return
     */
    public static boolean hasPermition (Context context, String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
        return (permissionCheck == PackageManager.PERMISSION_GRANTED);
    }


    /** 권한 요청
     *
     * @param mActivity
     * @return
     */
    public static void requestPermission (final BaseActivity mActivity, final String[] permission, final int requestCode, int request_reason) {

        if (ContextCompat.checkSelfPermission(mActivity,Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {

            // 이 권한을 필요한 이유를 설명해야하는가?
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity ,permission[0])) {

                DialogUtils.confirm(
                        mActivity,
                        request_reason,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(mActivity,
                                        permission,
                                        requestCode);

                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BaseApplication.getInstance().finishAll();
                            }
                        }, false
                );

            } else {

                ActivityCompat.requestPermissions(mActivity,
                        permission,
                        requestCode);

            }
        }

    }

}
