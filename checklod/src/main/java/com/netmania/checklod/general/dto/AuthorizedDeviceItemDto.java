package com.netmania.checklod.general.dto;

import lib.netmania.data.dtos.DataFormatDto;

/**
 * Created by hansangcheol on 2018. 2. 27..
 */

public class AuthorizedDeviceItemDto extends DataFormatDto {

    public int idx;
    public String id;
    public String alias;


    /** get instance not single ton
     *
     * @return
     */
    public static AuthorizedDeviceItemDto getInstance () {
        return new AuthorizedDeviceItemDto ();
    }

}
