package com.netmania.checklod.general.dto;

import lib.netmania.data.dtos.BaseDto;

/**
 * Created by hansangcheol on 2017. 6. 13..
 */

public class TakeOverHistoryItemDto extends BaseDto {

    //{"data":[{"MAC":"EB:29:55:02:94:51","sequence":"177","RTC":"17-06-1011:18:42","int_temp":"27.1","int_hum":"0","ext_temp":"26.5000","ext_hum":"0","measured_at":"2017-06-10 23:18:42","phone_no":"01026658956"},{"MAC":"EB:29:55:02:94:51","sequence":"179","RTC":"17-06-1011:18:52","int_temp":"27.1","int_hum":"0","ext_temp":"26.5000","ext_hum":"0","measured_at":"2017-06-10 23:18:52","phone_no":"01026658956"}]}

    public String MAC;
    public String sequence;
    public String RTC;
    public String int_temp;
    public String int_hum;
    public String ext_temp;
    public String ext_hum;
    public String measured_at;
    public String phone_no;

}
