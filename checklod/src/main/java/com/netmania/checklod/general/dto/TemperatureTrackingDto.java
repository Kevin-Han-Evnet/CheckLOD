package com.netmania.checklod.general.dto;

import lib.netmania.data.dtos.DataFormatDto;

/**
 * Created by Kevin Han on 2017-01-13.
 */

public class TemperatureTrackingDto extends DataFormatDto {

    public static final int REPORTED = 1;
    public static final int NOT_REPORTED = 0;

    public static final int ADJUSTED_DATA = 1;
    public static final int LIVE_DATA = 0;

    public int idx;
    public int seq;
    public String MAC;
    public String temp;
    public String outside_temp;
    public String hum;
    public String outside_hum;
    public String rtc;
    public long timestamp;
    public long measured_at;
    public int sent;
    public int ble_status;
    public int is_first = 0;
    public int is_adjusted;
    public int sensor_disconnected = 0;

    public int $failed_count = 0;

    /** get instance not single ton
     *
     * @return
     */
    public static TemperatureTrackingDto getInstance () {
        return new TemperatureTrackingDto ();
    }

    @Override
    public String getCreateTableQuery () {
        return super.getCreateTableQuery (new String[] {"seq","MAC"});
    }

}

