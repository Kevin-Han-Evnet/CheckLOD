package com.netmania.checklod.general.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

import com.androidquery.callback.AjaxStatus;
import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.R;
import com.netmania.checklod.general.data.DBHelper;
import com.netmania.checklod.general.data.DataProviderUtil;
import com.netmania.checklod.general.dto.AuthorizedDeviceItemDto;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.TakeOverHistoryItemDto;
import com.netmania.checklod.general.dto.TemperatureTrackingDto;
import com.netmania.checklod.general.http.BaseAPI;
import com.netmania.checklod.general.http.GalleryAPI;
import com.netmania.checklod.general.http.MonitoringAPI;
import com.netmania.checklod.general.manage.Constants;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.manage.FlagBox;
import com.netmania.checklod.general.utils.DialogUtils;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.general.utils.StringUtils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import lib.netmania.ble.model.BeaconDataModel;
import lib.netmania.dcamera.DCamera;
import lib.netmania.dcamera.dto.DCameraResultDto;

/**
 * Created by hansangcheol on 2017. 6. 10..
 */

public class JobProcessController {


    //상수
    public static final int PROCESS_TAKE_OVER = 0;
    public static final int PROCESS_CHECKOUT = 1;


    public static final int JOB_PROCESS_READY = 1;
    public static final int JOB_PROCESS_DELIVERY = 2;
    public static final int JOB_PROCESS_FINAL = 3;
    public static final int JOB_PROCESS_COMPLETE = 4;

    public static final int FAILED_TO_ADD_NOT_AUTHRIZED_BEACON = 0;
    public static final int FAILED_TO_ADD_ALREADY_TRACKING = 1;


    public static JobProcessController mInstance;

    public BaseActivity mActivity;
    public JobProcessCallback mCallback;
    //dCamera
    private DCamera dCamera;
    private ProgressDialog tProgressDialog;
    private String selectedLogerId;
    protected DBHelper mDBHelper;


    public JobProcessController(BaseActivity activity) {
        mActivity = activity;
        selectedLogerId = "";
        mDBHelper = new DBHelper(BaseApplication.getInstance());
    }


    public static JobProcessController getInstance (BaseActivity activity, JobProcessCallback callback) {
        if (mInstance == null) {
            mInstance = new JobProcessController (activity);
        }

        mInstance.mActivity = activity;
        mInstance.mCallback = callback;
        return mInstance;
    }


    /** 액티비티 처리 결과 받기
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode != FlagBox.REQUEST_USER_INPUT) return;

        switch (resultCode) {
            case Activity.RESULT_OK :


                int mode = data.getIntExtra(FlagBox.EXTRA_KEY_MODE, FlagBox.MODE_ADD_DEVICE);
                String input_01 = data.getStringExtra(FlagBox.INOUT_STR_1);
                String input_02 = data.getStringExtra(FlagBox.INOUT_STR_2);
                BeaconItemDto tItem = data.getParcelableExtra(FlagBox.EXTRA_KEY_DATA);

                //음....
                switch (mode) {

                    case FlagBox.MODE_ADD_DEVICE :
                        checkAndStroreDeviceToTargetWithAlias(input_01);
                        break;

                    case FlagBox.MODE_ADMIN_DELIVERY :
                        //nothing yet;
                        break;

                }

                break;


            default :
            case Activity.RESULT_CANCELED :
                //nothing yet;
                break;
        }
    }


    /** 스캔된 맥어드레스 체크해서 등록할것
     *
     * @param scanedMAC
     */
    public void checkAndStroreDeviceToTarget (final String scanedMAC) {

        LogUtil.I("MAC = " + scanedMAC);

        if (DataProviderUtil.getInstance(BaseApplication.getInstance()).isTrakingData(scanedMAC)) {

            mActivity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onFailedAddBeaconDevice(scanedMAC, FAILED_TO_ADD_ALREADY_TRACKING);
                        }
                    }
            );

            return;
        }

        String tSticker = getStickerForService(scanedMAC);

        if (!StringUtils.isEmpty(tSticker)) {
            checkInManual (scanedMAC);
        } else {

            mActivity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onFailedAddBeaconDevice(scanedMAC, FAILED_TO_ADD_NOT_AUTHRIZED_BEACON);
                        }
                    }
            );


        }
    }

    /** 입력된 스티커 넘버 체크해서 등록할것
     *
     */
    public void checkAndStroreDeviceToTargetWithAlias (final String stickerNo) {

        final String tMAC = getMacForService(stickerNo);

        if (DataProviderUtil.getInstance(BaseApplication.getInstance()).isTrakingData(tMAC)) {

            mActivity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onFailedAddBeaconDevice(tMAC, FAILED_TO_ADD_ALREADY_TRACKING);
                        }
                    }
            );


            return;
        }

        if (!StringUtils.isEmpty(tMAC)) {
            checkInManual(tMAC);
        } else {

            mActivity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onFailedAddBeaconDevice(tMAC, FAILED_TO_ADD_NOT_AUTHRIZED_BEACON);
                        }
                    }
            );


        }
    }



    /** 인수인계인지 판단을 사용자가
     *
     */
    public static final int DELIVERY_START_OPTION_NEW = 0;
    public static final int DELIVERY_START_OPTION_TAKEOVER = 1;
    public static final int DELIVERY_START_OPTION_CARGO = 2;
    public static final int DELIVERY_START_CANCEL = 3;
    protected void checkInManual (final String scanedMAC) {

        String[] options= mActivity.getResources().getStringArray(R.array.job_select_options);

        if (mActivity == null) return;


        //인수인계 여부 수동으로 결정
        DialogUtils.singleChoice(
                mActivity,
                mActivity.getResources().getString(R.string.job_select_title),
                options,
                -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {


                        switch (which) {

                            case DELIVERY_START_OPTION_NEW: //새로운 배송 등록
                                //nothing yet;
                                break;

                            case DELIVERY_START_OPTION_TAKEOVER : // 기존 배송 인수
                                //nothing yet;
                                break;

                            case DELIVERY_START_OPTION_CARGO : //화물배송건 인수

                                mCallback.onCargoBaseStart (scanedMAC);
                                break;

                            case DELIVERY_START_CANCEL : //취소
                                //nothing;
                                break;

                        }



                        dialog.dismiss();
                        if (which > DELIVERY_START_OPTION_TAKEOVER) return; //KTX 인수(오프라인 데이타 인수) 건은 여기서 항목을 생성하지 않는다
                        boolean success = addTrackingDevice (scanedMAC, which); //-->등록
                        if (success) {

                            mActivity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            mCallback.onCompleteAddBeaconDevice (scanedMAC, which);
                                        }
                                    }
                            );


                        }

                    }
                }, false
        );
    }


    /** 배송 추적 비콘 저장
     *
     */
    public boolean addTrackingDevice (final String mac, int start_option) {

        if (DataProviderUtil.getInstance(BaseApplication.getInstance()).isTrakingData(mac)) {

            mActivity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onFailedAddBeaconDevice(mac, FAILED_TO_ADD_ALREADY_TRACKING);
                        }
                    }
            );


            return false;
        }

        BeaconItemDto tItem = new BeaconItemDto();
        tItem.MAC = mac;
        tItem.sticker = DataProviderUtil.getInstance(BaseApplication.getInstance()).getAutorizedBeaconWithId(mac).alias;
        tItem.last_seq = -1;
        tItem.temp_chipset = Constants.NO_DATA;
        tItem.temp_probe = Constants.NO_DATA;
        tItem.hum_probe = Constants.NO_DATA;
        tItem.hum_chipset = Constants.NO_DATA;
        tItem.is_tookover = (start_option == DELIVERY_START_OPTION_NEW) ? BeaconItemDto.INT_FALSE : BeaconItemDto.INT_TRUE;
        tItem.islive = BeaconItemDto.INT_FALSE;
        tItem.min_temperature_limit = 0;
        tItem.max_temperature_limit = 0;
        tItem.bcn_status = BeaconDataModel.BCN_STATUS_OFF;
        tItem.timestamp = Calendar.getInstance().getTimeInMillis();

        switch (start_option) {

            case DELIVERY_START_OPTION_NEW :
                //nothing yetl
                break;

            case DELIVERY_START_OPTION_TAKEOVER :
                //nothing yetl
                break;

            case DELIVERY_START_OPTION_CARGO :
                //nothing yetl
                break;

        }

        mDBHelper.insertWithAsset(tItem.getTblName(), tItem.getInsertAsset());
        return true;

    }


    /** 디바이스 삭제
     *
     */
    public void removeDevice (final String mac, final String stickerNo) {

        DialogUtils.confirm(
                mActivity,
                String.format(mActivity.getResources().getString (R.string.confirm_beacon_remove), stickerNo),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mDBHelper.delete(BeaconItemDto.getInstance().getTblName(), "MAC=?", new String[] {mac});
                        mDBHelper.delete(TemperatureTrackingDto.getInstance().getTblName(), "MAC=?", new String[] {mac});

                        mActivity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mCallback.onCompleteRemoveBeacon(mac);
                                    }
                                }
                        );



                    }
                }, false
        );

    }


    /**
     *
     * @param mac
     * @return
     */
    public String getStickerForService (String mac) {
        AuthorizedDeviceItemDto result = DataProviderUtil.getInstance(BaseApplication.getInstance()).getAutorizedBeaconWithId(mac);
        return (result != null) ? result.alias : null;
    }


    /**
     *
     * @param stickerNo
     * @return
     */
    public String getMacForService (String stickerNo) {
        AuthorizedDeviceItemDto result = DataProviderUtil.getInstance(BaseApplication.getInstance()).getAutorizedBeaconWithAlias(stickerNo);
        return (result != null) ? result.id : null;
    }


    /** 배송 시작
     *
     * @param bcn
     */
    public boolean startNewDeliveryJob (BeaconItemDto bcn) {

        //온도범위 설정 검사
        if (bcn.max_temperature_limit - bcn.min_temperature_limit == 0 && bcn.is_tookover == BeaconItemDto.INT_FALSE) { //--> 인수건이 아닌 새로운 배송만 체크

            DialogUtils.alert(
                    mActivity,
                    String.format(mActivity.getResources().getString(R.string.alert_temp_range_not_set), bcn.sticker),
                    true
            );

            return false;
        }


        //BLE 신호 검사
        if (bcn.bcn_status < BeaconDataModel.BCN_STATUS_RUN) {

            DialogUtils.alert(
                    mActivity,
                    String.format(mActivity.getResources().getString(R.string.alert_ble_not_run), bcn.sticker),
                    true
            );

            return false;
        }


        //온도 안정화 검사
        if (bcn.$status != BeaconItemDto.STATUS_STABLE && bcn.is_tookover == BeaconItemDto.INT_FALSE) { //--> 인수건이 아닌 새로운 배송만 체크

            DialogUtils.alert(
                    mActivity,
                    String.format(mActivity.getResources().getString(R.string.alert_temp_not_stable), bcn.sticker),
                    true
            );

            return false;
        }


        BeaconItemDto tBCN = DataProviderUtil.getInstance(BaseApplication.getInstance()).getTrackingDevice(bcn.MAC);

        //시작 합시다.
        tBCN.islive = BeaconItemDto.INT_TRUE;
        tBCN.delivery_step = BeaconItemDto.DELEVERY_STEP_DELIVERY;
        tBCN.start_seq = new Integer (tBCN.last_seq);


        //배송상태로 만들고...
        String qry = "UPDATE " + tBCN.getTblName() + " SET "
                + "start_seq=" + tBCN.start_seq + ", "
                + "islive=" + tBCN.islive + ", "
                + "delivery_step=" + tBCN.delivery_step
                + " WHERE MAC='" + tBCN.MAC + "';";

        mDBHelper.update(qry);

        if (tBCN.is_tookover == BeaconItemDto.INT_TRUE) {
            doTakeOver (tBCN.MAC); //--> 인수건이면 데이타 물려받기~~
        } else {                  //--> 아니라면 첫번째 데이타 저장.

            TemperatureTrackingDto firstData = new TemperatureTrackingDto();
            firstData.seq = tBCN.last_seq;
            firstData.MAC = tBCN.MAC;
            firstData.temp = tBCN.temp_probe;
            firstData.outside_temp = tBCN.temp_chipset;
            firstData.hum = tBCN.hum_probe;
            firstData.outside_hum = tBCN.hum_chipset;
            firstData.rtc = tBCN.rtc;
            firstData.timestamp = Long.valueOf(tBCN.timestamp);
            firstData.measured_at = Calendar.getInstance().getTimeInMillis();
            firstData.sent = TemperatureTrackingDto.NOT_REPORTED;
            firstData.ble_status = tBCN.bcn_status;
            firstData.is_first = 1;
            firstData.$failed_count = 0;


            if (firstData.timestamp > 0) mDBHelper.insertWithAsset(TemperatureTrackingDto.getInstance().getTblName(), firstData.getInsertAsset());
        }

        return true;
    }



    /** 인수인계및 등록
     *
     * @param mac
     */
    public void doTakeOver (final String mac) {


        MonitoringAPI.takeOver(
                mActivity,
                true,
                mac,
                "",
                new BaseAPI.ApiMapListenerWithFailedRest() {

                    @Override
                    public void onComplete() {
                        //nothing;
                    }

                    @Override
                    public void onComplete(Map<String, Object> map) {

                        //기존 데이타 삭제 -- 함더 깔끔하게..
                        mDBHelper.delete(
                                TemperatureTrackingDto.getInstance().getTblName(),
                                "MAC=?",
                                new String[]{mac}
                        );




                        final String minLimit = (String) map.get(BaseAPI.TEMPERATURE_MIN_LIMIT);
                        final String maxLimit = (String) map.get(BaseAPI.TEMPERATURE_MAX_LIMIT);

                        final ArrayList<TakeOverHistoryItemDto> historyList = (ArrayList<TakeOverHistoryItemDto>) map.get(BaseAPI.ARRAY_LIST);


                        LogUtil.I("인수인계 -- minLimit = " + minLimit);
                        LogUtil.I("인수인계 -- maxLimit = " + maxLimit);

                        //업데이트 합시다
                        String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                + "is_data_checked_in = " + BeaconItemDto.INT_TRUE + ", "
                                + "min_temperature_limit=" + minLimit + ", "
                                + "max_temperature_limit=" + maxLimit
                                + " WHERE MAC='" + mac + "';";
                        mDBHelper.update(qry);


                        if (tProgressDialog != null) tProgressDialog.dismiss();
                        tProgressDialog = DialogUtils.progress(
                                mActivity,
                                historyList.size(),
                                mActivity.getResources().getString(R.string.progress_takeover_title),
                                mActivity.getResources().getString(R.string.progress_takeover_message),
                                false
                        );


                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() { // Thread 로 작업

                                TemperatureTrackingDto tData;
                                Date mesuraed_at = null;
                                Calendar tCal = Calendar.getInstance();

                                long ble_signal_cycle = Constants.BASE_SIGNAL_CYCLE;

                                DBHelper mDBHelper = new DBHelper(mActivity.getApplicationContext());

                                for (int i = 0; i < historyList.size(); i++) {


                                    if (tProgressDialog != null) tProgressDialog.setProgress(i + 1);


                                    LogUtil.I("인수인계 -- historyList[" + i + "]" + historyList.get(i).MAC + " -- " + historyList.get(i).int_temp);

                                    try {
                                        mesuraed_at = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse (historyList.get(i).measured_at);
                                    } catch (ParseException e) {
                                        // TODO Auto-generated catch block
                                        //e.printStackTrace();
                                    }

                                    tCal.setTime(mesuraed_at);


                                    tData = new TemperatureTrackingDto();
                                    tData.seq = Integer.valueOf (historyList.get(i).sequence);
                                    tData.MAC = historyList.get(i).MAC;
                                    tData.temp = historyList.get(i).int_temp;
                                    tData.outside_temp = historyList.get(i).ext_temp;
                                    tData.hum = historyList.get(i).int_hum;
                                    tData.outside_hum = historyList.get(i).ext_hum;
                                    tData.timestamp = tCal.getTimeInMillis();
                                    tData.measured_at = tCal.getTimeInMillis();
                                    tData.rtc = historyList.get(i).RTC;
                                    tData.sent = TemperatureTrackingDto.REPORTED;
                                    tData.ble_status = BeaconDataModel.BCN_STATUS_RUN;
                                    tData.is_first = BeaconItemDto.INT_FALSE;
                                    if (i == 0) tData.is_first = 1;

                                    if (tData.timestamp > 0) mDBHelper.insertWithAsset(TemperatureTrackingDto.getInstance().getTblName(), tData.getInsertAsset());

                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }


                                    //시간차 구하기
                                    long gab = DataProviderUtil.getInstance(BaseApplication.getInstance()).getRtcGab(tData);

                                    if (ble_signal_cycle > gab || ble_signal_cycle <= 0) {

                                        LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "이전과의 시간차 --> " + (gab / 1000) + "초");
                                        ble_signal_cycle = gab;
                                    }

                                }

                                if (tProgressDialog != null) {
                                    tProgressDialog.dismiss();
                                    tProgressDialog = null;
                                }



                                //업데이트 합시다
                                String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                        + "is_data_downloaded = " + BeaconItemDto.INT_TRUE + ", "
                                        + "min_temperature_limit=" + minLimit + ", "
                                        + "ble_signal_cycle=" + ble_signal_cycle
                                        + " WHERE MAC='" + mac + "';";
                                mDBHelper.update(qry);


                                mActivity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                mCallback.onTakeOverSuccess (mac);
                                            }
                                        }
                                );




                            }
                        });
                        t.start(); // 쓰레드 시작


                    }

                    @Override
                    public void onFailed(final AjaxStatus result) {

                        mActivity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mCallback.onTakeOverFailed(mac, result.getCode());
                                    }
                                }
                        );


                    }

                    @Override
                    public void onFailed(final AjaxStatus result, Map<String, Object> map) {

                        mActivity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mCallback.onTakeOverFailed(mac, result.getCode());
                                    }
                                }
                        );


                    }
                }

        );

    }


    /** 끝
     *
     * @param mac
     */
    public void stopMonitoring (final String mac) {

        DialogUtils.confirm(
                mActivity,
                R.string.confirm_checkout,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dialog.dismiss();
                        doCheckOut (mac);
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing yet;
                    }
                }, false
        );

    }


    /** 체크 아웃
     *
     * @param mac
     */
    public void doCheckOut (final String mac) {
        doCheckOut (mac, false);
    }
    public void doCheckOut (final String mac, boolean checked) {

        if (Constants.TEST_FOR_LOCAL_ONLY) {

            String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                    + "islive=" + BeaconItemDto.INT_FALSE + ", "
                    + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_INVOICE
                    + " WHERE MAC='" + mac + "';";

            mDBHelper.update(qry);

            mActivity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onCheckOutSuccess(mac);
                        }
                    }
            );

            return;
        }




        //실제 함수 -----------------------------------------------------------------------------------------------------------------------------ㄴ
        try {
            DataProviderUtil.getInstance(BaseApplication.getInstance()).updateLostData(mac, 0); //--> 데이타 보정
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<TemperatureTrackingDto> notReportedData = DataProviderUtil.getInstance(mActivity.getApplicationContext()).getNotReportedData(mac);

        LogUtil.I(DebugTags.TAG_DATA_FLOW, "일단 안보낸 데이타가 있는지 확인해야지. --> " + notReportedData.size());

        if (notReportedData.size() > 0 && !checked) {
            sendNotReportedData(mac, PROCESS_CHECKOUT, notReportedData);
            return;
        }

        mCallback.onStartCheckOut (mac);


        MonitoringAPI.checkOut(
                mActivity,
                true,
                 mac,
                "",
                new BaseAPI.ApiMapListenerWithFailedRest() {
                    @Override
                    public void onComplete() {

                        String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                + "islive=" + BeaconItemDto.INT_FALSE + ", "
                                + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_INVOICE
                                + " WHERE MAC='" + mac + "';";

                        mDBHelper.update(qry);

                        mActivity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mCallback.onCheckOutSuccess(mac);
                                    }
                                }
                        );



                    }

                    @Override
                    public void onComplete(Map<String, Object> map) {
                        mActivity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mCallback.onCheckOutSuccess(mac);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onFailed(AjaxStatus status) {

                        mActivity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mCallback.onCheckOutFailed(mac);
                                    }
                                }
                        );


                    }

                    @Override
                    public void onFailed(AjaxStatus result, Map<String, Object> map) {

                        mActivity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mCallback.onCheckOutFailed(mac);
                                    }
                                }
                        );
                    }
                }
        );

    }



    /** 밀린 데이타를 보내 보나요
     *
     * @param mac
     * @param notReportedData
     */
    public void sendNotReportedData (final String mac, final int process, final ArrayList<TemperatureTrackingDto> notReportedData) {




        String rk = "";
        for (int i = 0; i < notReportedData.size(); i++) {
            rk += (i > 0) ?", " : "";
            rk += notReportedData.get(i).seq;
        }



        MonitoringAPI.updateBeaconInfo(
                mActivity,
                true,
                notReportedData,
                new BaseAPI.ApiMapListenerWithFailedRest() {
                    @Override
                    public void onComplete() {

                        for (int i = 0; i < notReportedData.size(); i++) {
                            DataProviderUtil.getInstance(mActivity.getApplicationContext()).updateTemperatureData (String.valueOf(notReportedData.get(i).idx));
                        }


                        if (process == PROCESS_TAKE_OVER) {
                            mActivity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            mCallback.onNotReportedDataSendComplete (mac, process);
                                        }
                                    }
                            );
                        } else {
                            doCheckOut(mac, true);
                        }
                    }

                    @Override
                    public void onComplete(Map<String, Object> map) {

                        for (int i = 0; i < notReportedData.size(); i++) {
                            DataProviderUtil.getInstance(mActivity.getApplicationContext()).updateTemperatureData (String.valueOf(notReportedData.get(i).idx));
                        }


                        if (process == PROCESS_TAKE_OVER) {
                            mActivity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            mCallback.onNotReportedDataSendComplete (mac, process);
                                        }
                                    }
                            );
                        } else {
                            doCheckOut(mac, true);
                        }
                    }

                    @Override
                    public void onFailed(AjaxStatus result) {

                        mActivity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mCallback.onNotReportedDataSendFailed(mac, process);
                                    }
                                }
                        );


                    }

                    @Override
                    public void onFailed(AjaxStatus result, Map<String, Object> map) {

                        mActivity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mCallback.onNotReportedDataSendFailed(mac, process);
                                    }
                                }
                        );


                    }
                }
        );

    }







    /** 카메라 이니셜라이징
     *
     */
    private void initCamera () {
        dCamera = new DCamera(mActivity, mActivity.mApp.getPicasso());
        dCamera.setLogging(false);
    }


    /** 온도 추적 종료
     *
     * @param mac
     */
    public boolean sendPhoto (final String mac) {


        ArrayList<TemperatureTrackingDto> result = DataProviderUtil.getInstance(mActivity.mApp.getApplicationContext()).getReportedTemperatures(mac);

        if (result == null || result.size() <= 0) {
            DialogUtils.confirm(
                    mActivity,
                    R.string.confirm_no_data,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                    + "isLive=" + BeaconItemDto.INT_FALSE + ", "
                                    + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_COMPLETE
                                    + " WHERE MAC='" + mac + "'";

                            mDBHelper.update(qry);

                            mActivity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            mCallback.onHandOutSuccess (null, mac);
                                        }
                                    }
                            );

                        }
                    },
                    true
            );

            return false;
        }

        return true;

    }




    /** 사진 내놔라.
     *
     */
    public void getPhoto (String msg, final String mac) {

        String[] pMenu = mActivity.getResources().getStringArray(R.array.menu_photo_select);

        DialogUtils.singleChoice(mActivity, msg, pMenu, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String tID = mac.replace(":", "");
                initCamera ();

                switch (which) {

                    case 0 :

                        try {
                            dCamera.startImageCapture(DCamera.PICK_CROP_FROM_DCAMERA, "sc_", tID, "", dCameraListener2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;


                    case 1 :

                        try {
                            dCamera.startImageCapture(DCamera.PICK_CROP_FROM_SINGLE_FILE, "sc_", tID, "", dCameraListener2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;

                    case 2 :
                        //nothing yet;
                        break;

                }

                dialog.dismiss();

            }
        }, true);

    }




    //리스너 인터페이스  ------------------------------------------------------------------------------------------------
    public interface JobProcessCallback {
        void onCompleteAddBeaconDevice (String mac, int start_option);
        void onFailedAddBeaconDevice(String mac, int failed_reson);
        void onTemperatureRangeSelected (String mac, double minimum, double maximum);
        void onCompleteRemoveBeacon (String mac);
        void onTakeOverSuccess (String mac);
        void onTakeOverFailed (String mac, int code);

        void onStartCheckOut (String mac);
        void onCheckOutSuccess(String mac);
        void onCheckOutFailed(String mac);
        void onNotReportedDataSendFailed (String mac, int process);
        void onNotReportedDataSendComplete (String mac, int process);

        void onHandOutSuccess (Map<String, Object> map, File file, String selectedLogerId);
        void onHandOutSuccess (File file, String selectedLogerId);
        void onHandOutFailed (int statusCode, String selectedLogerId);

        void onCargoBaseStart (String mac);

        void onDataCleared ();

        void onPhotoCompositionItemSelected (File file);
    }





    /** DCamera listener
     *
     */
    DCamera.DCameraListener dCameraListener =  new DCamera.DCameraListener() {
        @Override
        public void onResult(final File file) {


            if (Constants.TEST_FOR_LOCAL_ONLY) {
                String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                        + "isLive=" + BeaconItemDto.INT_FALSE + ", "
                        + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_COMPLETE
                        + " WHERE MAC='" + selectedLogerId + "'";

                mDBHelper.update(qry);

                mActivity.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                mCallback.onHandOutSuccess (null, file, selectedLogerId);
                            }
                        }
                );

                return;
            }


            if (tProgressDialog == null) {
                tProgressDialog = DialogUtils.progress(mActivity, 100, "업로드", "이미지 업로드 중입니다.", false);
            }

            GalleryAPI.fileupload(
                    mActivity,
                    true,
                    file,
                    selectedLogerId,
                    new BaseAPI.ApiMapListenerWithFailedForFiles() {

                        @Override
                        public void onProgress(int progress, int max) {
                            //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "uploaded --> " + progress + " / " + max);
                            if (tProgressDialog != null) {
                                tProgressDialog.setMax(max);
                                tProgressDialog.setProgress(progress);
                            }
                        }

                        @Override
                        public void onComplete(final Map<String, Object> map) {

                            tProgressDialog.setProgress(tProgressDialog.getMax());
                            mActivity.mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    tProgressDialog.dismiss();
                                    tProgressDialog = null;

                                    //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "성공! -->  000 :: " + map.toString());

                                    String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                            + "isLive=" + BeaconItemDto.INT_FALSE + ", "
                                            + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_COMPLETE
                                            + " WHERE MAC='" + selectedLogerId + "'";

                                    mDBHelper.update(qry);

                                    mActivity.runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    mCallback.onHandOutSuccess (map, file, selectedLogerId);
                                                }
                                            }
                                    );



                                }
                            }, 500);




                        }

                        @Override
                        public void onComplete(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                            tProgressDialog.setProgress(tProgressDialog.getMax());

                            mActivity.mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    tProgressDialog.dismiss();
                                    tProgressDialog = null;

                                    //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "성공! --> 001 :: " + statusCode);

                                    String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                            + "isLive=" + BeaconItemDto.INT_FALSE + ", "
                                            + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_COMPLETE
                                            + " WHERE MAC='" + selectedLogerId + "'";

                                    mDBHelper.update(qry);


                                    mActivity.runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    mCallback.onHandOutSuccess (file, selectedLogerId);
                                                }
                                            }
                                    );



                                }
                            }, 500);
                        }

                        @Override
                        public void onComplete(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {

                            tProgressDialog.setProgress(tProgressDialog.getMax());

                            mActivity.mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    tProgressDialog.dismiss();
                                    tProgressDialog = null;

                                    //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "성공! -->  002 :: " + statusCode);

                                    String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                            + "isLive=" + BeaconItemDto.INT_FALSE + ", "
                                            + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_COMPLETE
                                            + " WHERE MAC='" + selectedLogerId + "'";

                                    mDBHelper.update(qry);

                                    mActivity.runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    mCallback.onHandOutSuccess (file, selectedLogerId);
                                                }
                                            }
                                    );



                                }
                            }, 500);
                        }

                        @Override
                        public void onComplete(int statusCode, Header[] headers, Throwable throwable) {

                            tProgressDialog.setProgress(tProgressDialog.getMax());

                            mActivity.mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    tProgressDialog.dismiss();
                                    tProgressDialog = null;

                                    //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "성공! -->  003 :: " + statusCode);

                                    String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                            + "isLive=" + BeaconItemDto.INT_FALSE + ", "
                                            + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_COMPLETE
                                            + " WHERE MAC='" + selectedLogerId + "'";

                                    mDBHelper.update(qry);

                                    mActivity.runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    mCallback.onHandOutSuccess (file, selectedLogerId);
                                                }
                                            }
                                    );



                                }
                            }, 500);
                        }

                        @Override
                        public void onFailed(final int statusCode) {

                            tProgressDialog.setProgress(0);

                            mActivity.mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    tProgressDialog.dismiss();
                                    //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "실패! --> " + statusCode);
                                    DialogUtils.alert(mActivity, R.string.info_upload_failed, false);
                                    mCallback.onHandOutFailed (statusCode, selectedLogerId);

                                }
                            }, 500);
                        }
                    }
            );


        }

        @Override
        public void onArrayResult(ArrayList<DCameraResultDto> arrayList) {
            //nohing yetl
        }

        @Override
        public void onException(Exception e) {
            //nohing yetl
        }

        @Override
        public void onCancelled(String s) {
            //nohing yetl
        }
    };





    /** DCamera listener 2
     *
     */
    DCamera.DCameraListener dCameraListener2 =  new DCamera.DCameraListener() {
        @Override
        public void onResult(final File file) {
            mCallback.onPhotoCompositionItemSelected (file);
        }

        @Override
        public void onArrayResult(ArrayList<DCameraResultDto> arrayList) {
            //nothing yet;
        }

        @Override
        public void onException(Exception e) {
            //nothing yet;
        }

        @Override
        public void onCancelled(String s) {
            //nothign yet;
        }
    };




    /** 인수인계및 등록
     *
     * @param scanedMAC
     */
    public void doRetrieveData (final String scanedMAC) {
        doRetrieveData(scanedMAC, false);
    }
    public void doRetrieveData (final String scanedMAC, final boolean sync_again) {


        MonitoringAPI.retrieveData(
                mActivity,
                true,
                scanedMAC,
                "",
                new BaseAPI.ApiMapListenerWithFailedRest() {

                    @Override
                    public void onComplete() {
                        //nothing;
                    }

                    @Override
                    public void onComplete(Map<String, Object> map) {



                        String minLimit = (String) map.get(BaseAPI.TEMPERATURE_MIN_LIMIT);
                        String maxLimit = (String) map.get(BaseAPI.TEMPERATURE_MAX_LIMIT);
                        final ArrayList<TakeOverHistoryItemDto> historyList = (ArrayList<TakeOverHistoryItemDto>) map.get(BaseAPI.ARRAY_LIST);


                        LogUtil.I("온도 데이타 백업 -- minLimit" + minLimit);
                        LogUtil.I("온도 데이타 백업 -- maxLimit" + maxLimit);


                        if (mActivity != null) {
                            tProgressDialog = DialogUtils.progress(
                                    mActivity,
                                    historyList.size(),
                                    "데이타 저장중",
                                    mActivity.getResources().getString(R.string.progress_takeover_title),
                                    false
                            );
                        }

                        final int myFirst = DataProviderUtil.getInstance(BaseApplication.getInstance()).getSavedFirstData(scanedMAC).seq;

                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() { // Thread 로 작업

                                TemperatureTrackingDto tData;
                                Date mesuraed_at = null;
                                Calendar tCal = Calendar.getInstance();


                                DBHelper mDBHelper = new DBHelper(mActivity.getApplicationContext());

                                for (int i = 0; i < historyList.size(); i++) {


                                    if (tProgressDialog != null) tProgressDialog.setProgress(i + 1);


                                    try {
                                        mesuraed_at = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse (historyList.get(i).measured_at);
                                    } catch (ParseException e) {
                                        // TODO Auto-generated catch block
                                        //e.printStackTrace();
                                    }

                                    tCal.setTime(mesuraed_at);


                                    tData = new TemperatureTrackingDto();
                                    tData.seq = Integer.valueOf (historyList.get(i).sequence);
                                    tData.MAC = historyList.get(i).MAC;
                                    tData.temp = historyList.get(i).int_temp;
                                    tData.outside_temp = historyList.get(i).ext_temp;
                                    tData.hum = historyList.get(i).int_hum;
                                    tData.outside_hum = historyList.get(i).ext_hum;
                                    tData.timestamp = tCal.getTimeInMillis();
                                    tData.measured_at = tCal.getTimeInMillis();
                                    tData.rtc = historyList.get(i).RTC;
                                    tData.sent = TemperatureTrackingDto.REPORTED;
                                    tData.ble_status = BeaconDataModel.BCN_STATUS_RUN;

                                    if (myFirst >= tData.seq && tData.timestamp > 0) mDBHelper.insertWithAsset(TemperatureTrackingDto.getInstance().getTblName(), tData.getInsertAsset());

                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                }

                                if (tProgressDialog != null) {
                                    tProgressDialog.dismiss();
                                    tProgressDialog = null;
                                }

                                if (sync_again) {
                                    //nothing yet;
                                }

                            }
                        });
                        t.start(); // 쓰레드 시작



                        LogUtil.I("인수인계 -- 온도 범위 설정 완료");


                    }

                    @Override
                    public void onFailed(AjaxStatus result) {
                        //nothing yet;
                    }

                    @Override
                    public void onFailed(AjaxStatus result, Map<String, Object> map) {
                        //nothing yet;
                    }
                }

        );
    }




    /** 모든 작업 종료
     *
     */
    public void clearJobs () {

        DBHelper mDB = new DBHelper(BaseApplication.getInstance());

        mDB.deleteAll(TemperatureTrackingDto.getInstance().getTblName());
        mDB.deleteAll(BeaconItemDto.getInstance().getTblName());
        mCallback.onDataCleared ();

    }


    //그냥 확인용... ------- ------- ------- ------- ------- ------- ------- ------- -------
    public static String getJobProcessName (int value) {

        String result = "";

        switch (value) {
            case JOB_PROCESS_READY : result = "JOB_PROCESS_READY"; break;
            case JOB_PROCESS_DELIVERY : result = "JOB_PROCESS_DELIVERY"; break;
            case JOB_PROCESS_FINAL : result = "JOB_PROCESS_FINAL"; break;
            case JOB_PROCESS_COMPLETE : result = "JOB_PROCESS_COMPLETE"; break;
        }

        return result;

    }

    public static String getProcessName (int value) {

        String result = "";

        switch (value) {
            case PROCESS_TAKE_OVER : result = "PROCESS_TAKE_OVER"; break;
            case PROCESS_CHECKOUT : result = "PROCESS_CHECKOUT"; break;
        }

        return result;

    }

}
