package com.netmania.checklod.general.dto;

/**
 * Created by hansangcheol on 2017. 6. 12..
 */

public class TemperatureRangeItemDto {

    public int idx;
    public int minimum;
    public int maximum;


    public TemperatureRangeItemDto(int idx, int minimum, int maximum) {
        this.idx = idx;
        this.minimum = minimum;
        this.maximum = maximum;
    }

}
