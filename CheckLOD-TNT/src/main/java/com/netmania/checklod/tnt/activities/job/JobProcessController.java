package com.netmania.checklod.tnt.activities.job;

import android.content.DialogInterface;

import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.data.DBHelper;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.TemperatureRangeItemDto;
import com.netmania.checklod.general.utils.DialogUtils;
import com.netmania.checklod.tnt.BaseApplication;
import com.netmania.checklod.tnt.activities.ActivityControl;
import com.netmania.checklod.tnt.manage.Constants;

import java.util.ArrayList;

/**
 * Created by hansangcheol on 2018. 3. 13..
 */

public class JobProcessController extends com.netmania.checklod.general.activities.JobProcessController {

    public JobProcessController(BaseActivity activity) {
        super(activity);
    }


    //이래도 되는건가????
    public static JobProcessController mInstance;
    public static JobProcessController getInstance (BaseActivity activity, JobProcessCallback callback) {
        if (mInstance == null) {
            mInstance = new JobProcessController (activity);
        }

        mInstance.mActivity = activity;
        mInstance.mCallback = callback;
        return mInstance;
    }

    /** 온도 범위 셋팅
     *
     * @param tItem
     */
    private ArrayList<TemperatureRangeItemDto> ranges;
    public void setTemperatureRange (final String mac, double current_min, double current_max) {


        String[] asset = Constants.getTemperatureRangeAsset ();
        String[] rangeList = new String[asset.length];

        ranges = new ArrayList<>();

        int selectedItem = -1;


        String[] tAsset;//; = new String[3];
        for (int i = 0; i < asset.length; i++) {
            tAsset = asset[i].split(",");
            rangeList[i] = tAsset[0];// + "(" + tAsset[1] + "℃ ~ " + tAsset[2] + "℃)"; --> 라벨링 규칙 변경

            ranges.add(new TemperatureRangeItemDto(i, Integer.valueOf(tAsset[1]), Integer.valueOf(tAsset[2])));

            if (current_min == Integer.valueOf(tAsset[1])
                    && current_max == Integer.valueOf(tAsset[2])) {

                selectedItem = i;

            }
        }


        //온도 범위 설정 대화창 열기
        DialogUtils.singleChoice(
                mActivity,
                "온도범위 설정",
                rangeList,
                selectedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int minimum = ranges.get(which).minimum;
                        int maximum = ranges.get(which).maximum;

                        String qry = BeaconItemDto.getInstance().getUpdateTempRageQry(mac, minimum, maximum);
                        new DBHelper(BaseApplication.getInstance()).update(qry);

                        mCallback.onTemperatureRangeSelected(mac, minimum, maximum);

                        dialog.dismiss();

                    }
                }, true
        );


    }



    @Override
    public boolean sendPhoto (final String mac) {
        if (!super.sendPhoto(mac)) return false;
        ActivityControl.openPhotoCompositionActviity(mActivity.getIntent(), mActivity, mac);
        return true;
    }
}
