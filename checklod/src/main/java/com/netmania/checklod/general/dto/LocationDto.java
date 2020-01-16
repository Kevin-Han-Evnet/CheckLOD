package com.netmania.checklod.general.dto;

import lib.netmania.data.dtos.DataFormatDto;

/**
 * Created by hansangcheol on 2017. 5. 30..
 */

public class LocationDto extends DataFormatDto {

    public long timestamp;
    public double lat;
    public double lng;


    /** get instance not single ton
     *
     * @return
     */
    public static LocationDto getInstance () {
        return new LocationDto ();
    }

}
