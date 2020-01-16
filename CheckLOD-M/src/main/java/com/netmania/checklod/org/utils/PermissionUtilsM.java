package com.netmania.checklod.org.utils;

import android.Manifest;

import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.manage.Permissions;
import com.netmania.checklod.org.R;
import com.netmania.checklod.org.manage.FlagBox;

/**
 * Created by hansangcheol on 2018. 7. 18..
 */

public class PermissionUtilsM {

    /** Location 관련 권한 설정 -- 사실은 BLE
     *
     */
    public static boolean checkJavisPermission (BaseActivity mActivity) {

        boolean hasJavisPermission = Permissions.hasPermition(mActivity, Manifest.permission.RECORD_AUDIO);


        if (hasJavisPermission) {
            return true;
        } else {
            Permissions.requestPermission(
                    mActivity,
                    new String[] {
                            Manifest.permission.RECORD_AUDIO
                    },
                    FlagBox.PERMISSION_REQ_JAVIS,
                    R.string.permission_request_javis
            );
            return false;
        }
    }

}
