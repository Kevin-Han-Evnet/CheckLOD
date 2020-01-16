package com.netmania.checklod.org.manage;


import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.org.BaseApplication;
import com.netmania.checklod.org.BuildConfig;
import com.netmania.checklod.org.R;

/**
 * Created by hansangcheol on 2017. 10. 10..
 */

public class Constants extends com.netmania.checklod.general.manage.Constants {



    /** 컨피그 오버라이드
     *
     */
    public static void configOverride () {

        TEST_DIV_CODE = R.string.test_divsion_code;

        IS_LOG = false;
        IS_RELEASED = true;
        TEST_FOR_LOCAL_ONLY = false;
        LogUtil.IS_LOG = IS_LOG;

        CONFIG_URL = "http://54.180.120.102/tnt/apis/";
        //CONFIG_URL = "http://tnt.kevin75.com/tnt/apis/";
        UPLOAD_URL = "https://dev.checklod.com/wp-json/tntapi/v1/";
        API_URL = "https://dev.checklod.com/tnt-ex-mgr-pilot/service/";

        APP_ID = BuildConfig.APPLICATION_ID;

    }


    /** 각 테스트 기관별 온도 어셋 리턴 --------------------------------------------------------------------------------
     *
     */
    public static String[] getTemperatureRangeAsset () {
        String [] result = BaseApplication.getInstance().getResources().getStringArray(R.array.temperature_range_asset);
        return result;
    }

    /** 각 테스트 기관별 도큐먼트 리스트 리턴 --------------------------------------------------------------------------------
     *
     */
    public static String[] getDocumentTypes () {
        String [] result = BaseApplication.getInstance().getResources().getStringArray(R.array.document_types);
        return result;
    }

}
