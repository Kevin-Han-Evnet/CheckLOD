package com.netmania.checklod.general.utils;

import android.Manifest;

import com.netmania.checklod.general.R;
import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.manage.FlagBox;
import com.netmania.checklod.general.manage.Permissions;;


/**
 * Created by hansangcheol on 2018. 2. 27..
 */

public class PermissionUtils {

    /** 폰 관련 권한 설정
     *
     * @return
     */
    public static boolean checkGeneralPermission (BaseActivity mActivity) {

        boolean hasPhoneStatePermission = Permissions.hasPermition(mActivity, Manifest.permission.READ_PHONE_STATE);

        if (hasPhoneStatePermission) {
            return true;
        } else {
            Permissions.requestPermission(
                    mActivity,
                    new String[] {Manifest.permission.READ_PHONE_STATE},
                    FlagBox.PERMISSION_REQ_GENERAL,
                    R.string.permission_request_phone_state
            );
            return false;
        }
    }


    /** Location 관련 권한 설정 -- 사실은 BLE
     *
     */
    public static boolean checkLocationPermission (BaseActivity mActivity) {

        boolean hasCoarseLocation = Permissions.hasPermition(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION);
        boolean hasFineLocation = Permissions.hasPermition(mActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        boolean cameraPermission = Permissions.hasPermition(mActivity, Manifest.permission.CAMERA);
        boolean readExternalPermission = Permissions.hasPermition(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean writeExternalPermission = Permissions.hasPermition(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        if (hasCoarseLocation
                && hasFineLocation
                && cameraPermission
                && readExternalPermission
                && writeExternalPermission) {
            return true;
        } else {
            Permissions.requestPermission(
                    mActivity,
                    new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    FlagBox.PERMISSION_REQ_LOCATION,
                    R.string.permission_request_location
            );
            return false;
        }
    }

}
