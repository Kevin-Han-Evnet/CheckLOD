package com.netmania.checklod.tnt.dto;

import lib.netmania.data.dtos.BaseDto;

/**
 * Created by hansangcheol on 2018. 3. 21..
 */

public class FcmMessageItemDto extends BaseDto {

    public String cmd;
    public String info;

    public FcmMessageItemDto() {
        //nothing yet;
    }

    public FcmMessageItemDto(String cmd, String info) {
        this.cmd = cmd;
        this.info = info;
    }
}
