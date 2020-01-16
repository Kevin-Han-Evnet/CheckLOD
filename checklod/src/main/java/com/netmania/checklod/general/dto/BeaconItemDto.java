package com.netmania.checklod.general.dto;

import com.netmania.checklod.general.manage.Constants;

import lib.netmania.ble.model.BeaconDataModel;
import lib.netmania.data.dtos.DataFormatDto;

/**
 * Created by hansangcheol on 2018. 3. 13..
 */

public class BeaconItemDto extends DataFormatDto {

    //상수
    public static final int STATUS_STABLE = 0;
    public static final int STATUS_CAUTION = 1;
    public static final int STATUS_EMERGNECY = 2;

    public static final int DELEVERY_STEP_READY = 0;
    public static final int DELEVERY_STEP_DELIVERY = 1;
    public static final int DELEVERY_STEP_HANDOVER = 2;
    public static final int DELEVERY_STEP_INVOICE = 3;
    public static final int DELEVERY_STEP_COMPLETE = 4;

    public static final int INT_TRUE = 1;
    public static final int INT_FALSE = 0;

    public int idx;
    public String MAC;
    public String sticker;
    public int last_seq = -1;
    public long timestamp;
    public String rtc = "";
    public String temp_probe = "0.0";
    public String temp_chipset = "0.0";
    public String hum_probe = "0.0";
    public String hum_chipset = "0.0";
    public int islive = INT_FALSE;
    public double max_temperature_limit = 0;
    public double min_temperature_limit = 0;
    public int bcn_status = BeaconDataModel.BCN_STATUS_OFF;
    public int is_tookover = INT_FALSE;
    public int delivery_step = DELEVERY_STEP_READY;
    public int is_data_checked_in = INT_FALSE;
    public int is_data_downloaded = INT_FALSE;
    public int start_seq = -1;
    public int rssi = 0;
    public double bcn_battery = 0;
    public long ble_signal_cycle = Constants.BASE_SIGNAL_CYCLE;
    public int sensor_disconnected = 0;

    //아래는 테이블에는 안들어가는 값
    public int $status = STATUS_STABLE;
    public double $max_temp = 0.0;
    public double $min_temp = 0.0;
    public boolean $show_detail = false;
    public boolean $check_to_start = false;

    /** get instance not single ton
     *
     * @return
     */
    public static BeaconItemDto getInstance () {
        return new BeaconItemDto ();
    }

    @Override
    public String getCreateTableQuery () {
        return super.getCreateTableQuery (new String[] {"MAC"});
    }


    /** update temp range query
     *
     * @param mac
     * @param minimum
     * @param maximum
     * @return
     */
    public String getUpdateTempRageQry (String mac, double minimum, double maximum) {
        String qry = "UPDATE " + getTblName() + " SET min_temperature_limit=" + minimum + ", max_temperature_limit=" + maximum + " WHERE MAC='" + mac + "';";
        return qry;
    }

    //그냥 확인용 ---------------------------------------------------------------------------
    public static String getDeleryStepName (int value) {

        String result = "";

        switch (value) {
            case DELEVERY_STEP_READY : result = "DELEVERY_STEP_READY"; break;
            case DELEVERY_STEP_DELIVERY : result = "DELEVERY_STEP_DELIVERY"; break;
            case DELEVERY_STEP_HANDOVER : result = "DELEVERY_STEP_HANDOVER"; break;
            case DELEVERY_STEP_INVOICE : result = "DELEVERY_STEP_INVOICE"; break;
            case DELEVERY_STEP_COMPLETE : result = "DELEVERY_STEP_COMPLETE"; break;
        }

        return result;

    }

    public static String getStatusName (int value) {

        String result = "";

        switch (value) {
            case STATUS_STABLE : result = "STATUS_STABLE"; break;
            case STATUS_CAUTION : result = "STATUS_CAUTION"; break;
            case STATUS_EMERGNECY : result = "STATUS_EMERGNECY"; break;
        }

        return result;

    }

    public static String getBleStatusName (int value) {

        String result = "";

        switch (value) {
            case BeaconDataModel.BCN_STATUS_OFF : result = "BCN_STATUS_OFF"; break;
            case BeaconDataModel.BCN_STATUS_READY : result = "BCN_STATUS_READY"; break;
            case BeaconDataModel.BCN_STATUS_RUN : result = "BCN_STATUS_RUN"; break;
        }

        return result;

    }


}
