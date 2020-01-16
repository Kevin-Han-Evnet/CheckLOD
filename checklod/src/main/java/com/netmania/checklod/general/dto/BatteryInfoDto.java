package com.netmania.checklod.general.dto;

import lib.netmania.data.dtos.BaseDto;

/**
 * Created by hansangcheol on 2018. 4. 23..
 */

public class BatteryInfoDto extends BaseDto {

    public static final int ACC = 1;
    public static final int USB = 2;
    public static final int WIRELESS = 4;



    public boolean isRecharging;
    public int chagingType;
    public float life;

}
