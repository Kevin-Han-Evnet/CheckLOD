package com.netmania.checklod.general.manage;


import com.netmania.checklod.general.BuildConfig;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by hansangcheol on 2017. 10. 10..
 */

public class Constants {


    public static int TEST_DIV_CODE = -1;

    public static boolean IS_LOG = false;                                       //---> 로그를 찍을것인가??
    public static boolean IS_RELEASED = true;                                   //---> 릴리즈 버전인가??
    public static final boolean IS_AUTO_TEST = false;                           //---> 자동으로 테스트 할건가???
    public static boolean TEST_FOR_LOCAL_ONLY = false;                           //로컬 테스트만 진행
    public static final boolean AUTO_CONTROL_CHECKIN = false;                   //배송건이 인수인계인지 새 배송인지 판단을 자동으로 할것인가?
    public static final boolean SHOW_STATS_DEFAULT = false;                     //모든 데이타 보기 기본값


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


    /** BLE 리스너 갱신 주기 */
    public static final long BLE_LISTENER_RECREATE_CYCLE = 1000 * 60 * 15;

    /** BLE 신호 누락 판단 배수 (센싱컴의  x배) */
    public static final long BLE_SIGNAL_CHECK_CYCLE_K = 15;


    /** 시스템 리포트 주의 단계 */
    public static final int MEMORY_LIMIT_MAX = 90;
    public static final double STORAGE_LIMIT_MIN = 0.7;

    /** 20% 안쪽으로 진입시 경고때림 */
    public static final double SAFE_GAB_T = 0.15;

    /** UI 업데이트 추기 */
    public static final long UI_UPDATE_CYCLE = 1000 * 15;

    /** 서버 업데이트 주기 */
    public static final long REPORT_CYCLE = 1000 * 60;

    /** 신호 주기 ------------------ */
    public static final long BASE_SIGNAL_CYCLE = 1000 * 30;
    public static final int ADJUSTABLE_GAB = 7;

    /** 미니멈 신호세기 */
    public static final long MINIMUN_RSSI = -90;


    /** FUCK */
    public static final String RTC_DATE_FORMAT = "yy-MM-DDHH:mm:ss";


    /** 냉동 테스트 여부 및 온도 차감치 */
    public static final boolean IS_FROZEN_TEST = false;
    public static final double FROZEN_SAFE_GAB = 3.0;


    /** 레디모드 무시 테스트 */
    public static final boolean ALAWAYS_RUN_MODE = false;


    /** 배터리 체크 */
    public static final double BATTERY_LOW_LIMIT = 0.0;

    /** 거시기 */
    public static final String NO_DATA = "--";


    public static String[] frozenTestDevices = new String[] {"EF:B8:D0:51:18:95", "EB:0F:75:FB:C6:CD"};
    public static double[] frozenTestDevicesTempAdaust = new double[] {-22.0, -22.0};
    public static int isFrozenTestDevice (String mac) {

        for (int i = 0; i < frozenTestDevices.length; i++) {
            if (frozenTestDevices[i].equals(mac)) {
                return i;
            }
        }

        return -1;
    }


    /** 미전송 데이타 수신 딜레이 */
    public static final long DATA_RETRIEVE_ATAIN_DELAY = 1000 * 15;

    /** 각 테스트 기관별 기본 URL 리턴 ---------------------------------------------------------------------------------------------
     *
     * @return
     */
    public static final int URL_CATEGORY_API = 0;
    public static final int URL_CATEGORY_UPLOAD = 1;
    public static final int URL_CATEGORY_CONFIG = 2;

    public static String API_URL = "";
    public static String UPLOAD_URL = "";
    public static String CONFIG_URL = "";

    public static String getBaseURL (int category) {

        String server_url = "";

        switch (category) {

            case URL_CATEGORY_CONFIG : server_url = CONFIG_URL; break;
            case URL_CATEGORY_UPLOAD : server_url = UPLOAD_URL;  break;
            default :
            case URL_CATEGORY_API : server_url = API_URL; break;

        }

        return server_url;

    }




    /** 디버깅 정보 가져가 볼까? */
    public static final long SEND_DEBUG_INFO_TERM = 1000 * 60 * 1;

    public static String APP_ID = BuildConfig.APPLICATION_ID;


    /** 새 로거 싸이클 */
    public static final long NEW_LOGGER_CYCLE = 1000 * 60;

}
