package com.netmania.checklod.general.data;

import android.content.Context;

import com.androidquery.callback.AjaxStatus;
import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.DriverInfoDto;
import com.netmania.checklod.general.dto.LocationDto;
import com.netmania.checklod.general.dto.TemperatureTrackingDto;
import com.netmania.checklod.general.http.BaseAPI;
import com.netmania.checklod.general.http.GeneralAPI;
import com.netmania.checklod.general.http.MonitoringAPI;
import com.netmania.checklod.general.manage.Constants;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.LogUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;

//import lib.netmania.log.Category;
//import lib.netmania.log.SendBroadcastingMessage;
//import lib.netmania.log.Status;

/**
 * Created by hansangcheol on 2017. 6. 8..
 */

public class DataSenderUtil {

    public static DataSenderUtil mSenderUtil;
    private Context mContext;
    private Timer mTimer;
    private boolean isRunning;
    private DataSendCallback mCallback;

    public DataSenderUtil(Context context, DataSendCallback mCallback) {
        this.mContext = context;
        this.isRunning = false;
        this.mCallback = mCallback;
    }

    //싱글톤 고고
    public static DataSenderUtil getInstance (Context context, DataSendCallback mCallback) {
        if (mSenderUtil == null) {
            mSenderUtil = new DataSenderUtil(context, mCallback);
        }

        return mSenderUtil;
    }


    //단순 인스턴스 반환
    public static DataSenderUtil getInstance () {
        return mSenderUtil;
    }


    /** 보내라!
     *
     */
    public void sendQueDatas () {

        LogUtil.I(DebugTags.TAG_DATA_FLOW, "데이타 전송 시도");

        if (!GeneralUtils.isNetworkAvaliable(mContext.getApplicationContext())) {
            LogUtil.I(DebugTags.TAG_DATA_FLOW, "인터넷 연결이 안되어 전송 불가 잠시후 재시도");
            return;
        }

        ArrayList<TemperatureTrackingDto> sendingTarget = DataProviderUtil.getInstance(mContext.getApplicationContext()).getNotReportedCount("");

        if (sendingTarget.size() == 0) {
            LogUtil.I(DebugTags.TAG_DATA_FLOW, "전송할 대상이 없음");
            return;
        }


        final TemperatureTrackingDto tItem = sendingTarget.get(0);

        LogUtil.I(DebugTags.TAG_DATA_FLOW, tItem.idx + " :: " + tItem.MAC + " --- " + tItem.temp + "℃ 전송 시도!");


        if (Constants.TEST_FOR_LOCAL_ONLY) {
            mCallback.onComplete(tItem.MAC);

            //전송완료 됐음을 업데이트
            DataProviderUtil.getInstance(mContext).updateTemperatureData (String.valueOf(tItem.idx));
            LogUtil.I(DebugTags.TAG_DATA_FLOW, tItem.idx + " :: " + tItem.MAC + " --- " + tItem.temp + "℃ 전송 성공!");
            sendQueDatas (); //제귀호출


        } else {
            MonitoringAPI.updateBeaconInfo(
                    mContext,
                    false,
                    tItem.MAC,
                    tItem.seq,
                    tItem.rtc,
                    Float.valueOf(tItem.temp),
                    Float.valueOf(tItem.hum),
                    Float.valueOf(tItem.outside_temp),
                    Float.valueOf(tItem.outside_hum),
                    tItem.timestamp,
                    new BaseAPI.ApiMapListenerWithFailedRest() {
                        @Override
                        public void onComplete() {
                            LogUtil.I(DebugTags.TAG_DATA_FLOW, "성공");

                            mCallback.onComplete(tItem.MAC);


                            //전송완료 됐음을 업데이트
                            DataProviderUtil.getInstance(mContext).updateTemperatureData (String.valueOf(tItem.idx));

                            LogUtil.I(DebugTags.TAG_DATA_FLOW, tItem.idx + " :: " + tItem.MAC + " --- " + tItem.temp + "℃ 전송 성공!");
                            sendQueDatas (); //제귀호출
                        }

                        @Override
                        public void onComplete(Map<String, Object> map) {
                            //사용안함
                        }

                        @Override
                        public void onFailed(AjaxStatus result) {

                            LogUtil.I(DebugTags.TAG_DATA_FLOW, "실패 -- AjaxStatus.getCode () = " + result.getCode());

                            switch (result.getCode()) {
                                case BaseAPI.REST_NO_CONTENTS :


                                    LogUtil.I(DebugTags.TAG_DATA_FLOW, "아하.. 니가 가져갔구나??? ---> " + tItem.MAC);
                                    mCallback.onTakeOver(tItem.MAC);

                                    //넘어갔으므로 패스
                                    DataProviderUtil.getInstance(mContext).updateTemperatureData (String.valueOf(tItem.idx));
                                    break;

                                case BaseAPI.REST_INTERNAL_SERVER_ERROR :

                                    mCallback.onFailed(tItem.MAC);

                                    break;
                            }

                            sendQueDatas (); //제귀호출
                        }

                        @Override
                        public void onFailed(AjaxStatus result, Map<String, Object> map) {
                            //nothin gyet;
                        }
                    }

            );
        }
    }


    /** 체크인
     *
     * @param bcnInfo
     */
    public void doCheckIn (final BeaconItemDto bcnInfo) {
        if (Constants.TEST_FOR_LOCAL_ONLY) {
            mCallback.onCheckedIn (bcnInfo.MAC);
        } else {
            MonitoringAPI.checkIn(
                    BaseApplication.getInstance(),
                    false,
                    bcnInfo.MAC,
                    "",
                    bcnInfo.min_temperature_limit,
                    bcnInfo.max_temperature_limit,
                    "",
                    "",
                    new BaseAPI.ApiMapListenerWithFailedRest() {
                        @Override
                        public void onComplete() {
                            mCallback.onCheckedIn (bcnInfo.MAC);
                        }

                        @Override
                        public void onComplete(Map<String, Object> map) {
                            mCallback.onCheckedIn (bcnInfo.MAC);
                        }

                        @Override
                        public void onFailed(AjaxStatus result) {
                            mCallback.onCheckeInFailed (bcnInfo.MAC);
                        }

                        @Override
                        public void onFailed(AjaxStatus result, Map<String, Object> map) {
                            mCallback.onCheckeInFailed (bcnInfo.MAC);
                        }
                    }

            );
        }
    }



    //인터페이스 -----------------------------------------------------------------------------------------------
    public interface DataSendCallback {
        void onComplete(String mac);
        void onFailed(String mac);

        void onTakeOver(String mac);

        void onCheckedIn (String mac);
        void onCheckeInFailed (String mac);
    }

}
