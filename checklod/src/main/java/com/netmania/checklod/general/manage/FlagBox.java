package com.netmania.checklod.general.manage;

/**
 * Created by hansangcheol on 2018. 2. 27..
 */

public class FlagBox {


    /** 권한 요청 관련 리퀘스트 코드 */
    public static final int PERMISSION_REQ_GENERAL = 1000;
    public static final int PERMISSION_REQ_LOCATION = 1002;
    public static final int PERMISSION_REQ_KEYGUARD = 1003;

    /** 서비스 관련 리퀘스트 코드 */
    public static final int SENSING_SERVICE_PI_RQ = 100;
    public static final int HTTP_SERVICE_PI_RQ = 101;
    public static final int JOB_ACTIVITY_PI_RQ = 102;
    public static final int FM_PI_RQ = 103;

    /** 유저 인풋 화면 리퀘스트 코드 */
    public static final int REQUEST_USER_INPUT = 2000;



    /** ActivityCustomUserInputUI 관련 상수 */
    public static final String EXTRA_KEY_MODE = "extrakeyMode";
    public static final String EXTRA_KEY_DATA = "extrakeyData";
    public static final int MODE_ADD_DEVICE = 0;
    public static final int MODE_ADMIN_DELIVERY = 1;


    public static final String INOUT_STR_1 = "inputStr_01";
    public static final String INOUT_STR_2 = "inputStr_02";


    /** 배송 음... 뭐지? */
    public static final int REQUEST_HAND_OUT = 9001;
    public static final int REQUEST_DATA_SYNC = 8001;
    public static final int REQUEST_ADD_DEVICE = 7001;
}
