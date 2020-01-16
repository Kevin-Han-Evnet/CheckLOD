package com.example.testforble.manage;


import java.util.Locale;

/**
 * Created by hansangcheol on 2017. 10. 10..
 */

public class Constants {


    public static int TEST_DIV_CODE = -1;

    public final static boolean IS_LOG = true;                                       //---> 로그를 찍을것인가??
    public final static boolean IS_RELEASED = false;                                 //---> 릴리즈 버전인가??
    public final static boolean DO_ADJUST_DATA = true;                               //---> 데이타 보정은 할것인가??? --> 허용 한계는 있음...
    public static final int ADJUSTABLE_GAB = 7;                                      //---> 데이타 누락시 보정 허용 최대치



    /** 나의 국가 */
    public static final Locale CURRENT_LOCALE = Locale.KOREA;

    /** 각종 주기들 */
    public static final long REPORT_TIME_GAB_1 = 1000 * 30;
    public static final long REPORT_TIME_GAB_2 = 1000 * 60;
    public static final long REPORT_TIME_GAB_3 = 1000 * 60 * 5;
    public static final long REPORT_TIME_GAB_4 = 1000 * 60 * 10;
    public static final long REPORT_TIME_GAB_5 = 1000 * 60 * 15;
    public static final long REPORT_TIME_GAB_6 = 1000 * 60 * 30;
    public static final int ALLOW_BACKGROUND_DURATION = 1000 * 10;      //그래도 백그라운드에 들어가 기다릴 수 있는 시간;


    /** FUCK */
    public static final String RTC_DATE_FORMAT = "yy-MM-DDHH:mm:ss";
    public static final long BASE_SIGNAL_CYCLE = 1000 * 30;


    public static final long UI_UPDATE_CYCLE = 1000 * 10;

}
