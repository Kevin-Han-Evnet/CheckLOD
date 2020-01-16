package com.example.testforble.dto;


import lib.netmania.data.dtos.DataFormatDto;

public class SystemLogDto extends DataFormatDto {

    public String action;
    public String msg;
    public long timestamp;


    /** 싱글톤은 아님..
     *
     * @return
     */
    public static SystemLogDto getInstance () {
        return new SystemLogDto();
    }

}
