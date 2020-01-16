package com.netmania.checklod.general.dto;

import java.io.File;

import lib.netmania.data.dtos.BaseDto;

/**
 * Created by hansangcheol on 2018. 5. 15..
 */

public class PhotoCompositinoItemDto extends BaseDto {

    public String type;
    public String label;
    public File file;

    public PhotoCompositinoItemDto () {
        //noting yet;
    }

    public PhotoCompositinoItemDto (String type, String label, File file) {
        this.file = file;
        this.type = type;
        this.label = label;
    }

}
