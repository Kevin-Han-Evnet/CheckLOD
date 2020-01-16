package com.netmania.checklod.general.http;

import android.content.Context;

import com.androidquery.callback.AjaxStatus;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.dto.APIResutDtoFailed;
import com.netmania.checklod.general.dto.ConfigDto;
import com.netmania.checklod.general.dto.ApiContextInfoDto;
import com.netmania.checklod.general.dto.AuthorizedDeviceItemDto;
import com.netmania.checklod.general.dto.ContactItemDto;
import com.netmania.checklod.general.dto.DeviceAliasDto;
import com.netmania.checklod.general.dto.DriverInfoDto;
import com.netmania.checklod.general.dto.HttpResultDto;
import com.netmania.checklod.general.hash.Base64EncUtil;
import com.netmania.checklod.general.service.HttpProcessService;
import com.netmania.checklod.general.service.SensingProcessService;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.HttpUtils;
import com.netmania.checklod.general.utils.JsonUtils;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.general.utils.StringUtils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.netmania.checklod.general.utils.GeneralUtils.isRunningService;

/**
 * Created by hansangcheol on 2017. 6. 21..
 */

public class GeneralAPI extends BaseAPI {


    /** 컨피그 얻기
     *
     * @param context
     * @param showProgress
     * @param listener
     */
    public static void getConfig (Context context,
                                  boolean showProgress,
                                  String test_div_code,
                                  final ApiMapListenerWithFailedRest listener) {

        final ApiContextInfoDto contextDto = getContextInfo (context);

        //파라미터
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("test_div_code", test_div_code);
        params.put("phone_no", GeneralUtils.getPhoneNumber(BaseApplication.getInstance()));
        addCommonParam (context.getApplicationContext(), params);




        //http 리스너
        HttpListener httpListener = new HttpListener() {

            public void onSuccess (JSONObject json, HttpResultDto result) {

                LogUtil.W(json.toString());

                if(result.isSuccess) {

                    LogUtil.W(json.toString());

                    HashMap<String, Object> map = JsonUtils.parse(json.optJSONObject("body"));

                    //결과값 코드 및 메시지
                    ConfigDto resultDto = new ConfigDto();
                    try {
                        JsonUtils.autoMappingJsonToObject (json.optJSONObject("body"), resultDto);
                    } catch (Exception e) {
                        //nothing;
                    }
                    map.put (RESULT_DTO, resultDto);
                    listener.onComplete (map);

                }


            }

            @Override
            public void onFailed(AjaxStatus status) {

                LogUtil.I("실패!!!! --> " + status.getCode());
                LogUtil.I("실패!!!! --> " + status.getError());
                LogUtil.I("실패!!!! -----> " + status.getMessage());

                JSONObject tJSON = null;
                if (!StringUtils.isEmpty(status.getError())) {
                    try {
                        tJSON = new JSONObject(status.getError());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //nothing yet;
                }


                LogUtil.I("실패!!!!");

                HashMap<String, Object> map = null;

                if (tJSON != null) {
                    LogUtil.W(tJSON.toString());
                    map = JsonUtils.parse(tJSON);

                    //결과값 코드 및 메시지
                    APIResutDtoFailed resultDto = new APIResutDtoFailed();
                    try {
                        JsonUtils.autoMappingJsonToObject(tJSON, resultDto);
                    } catch (Exception e) {
                        //nothing;
                    }
                    map.put(RESULT_DTO, resultDto);
                }


                listener.onFailed(status, map);
            }
        };

        //실행!!
        String apiURL = getConfigURL (API_CONFIG);
        String paramK = getUrlEncodedParameter (params);
        params.clear();

        if (contextDto.mActivity != null) {
            new HttpUtils(contextDto.mActivity).httpExecute (apiURL + paramK, null, params, httpListener, showProgress);
        } else {
            new HttpUtils (context).httpExecute (apiURL + paramK, null, params, httpListener, false);
        }

    }


    /** 컨피그 얻기
     *
     * @param context
     * @param showProgress
     * @param driver_id
     * @param fcm_reg_id
     * @param test_div_code
     * @param listener
     */
    public static void sendFcmRegId (Context context,
                                  boolean showProgress,
                                  String driver_id,
                                  String fcm_reg_id,
                                  String test_div_code,
                                  final ApiMapListenerWithFailedRest listener) {

        final ApiContextInfoDto contextDto = getContextInfo (context);

        //파라미터
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("driver_id", driver_id);
        params.put("phone_no", GeneralUtils.getPhoneNumber(BaseApplication.getInstance()));
        params.put("fcm_reg_id", fcm_reg_id);
        params.put("test_div_code", test_div_code);
        addCommonParam (context.getApplicationContext(), params);




        //http 리스너
        HttpListener httpListener = new HttpListener() {

            public void onSuccess (JSONObject json, HttpResultDto result) {

                LogUtil.W(json.toString());

                if(result.isSuccess) {

                    LogUtil.W(json.toString());
                    HashMap<String, Object> map = JsonUtils.parse(json);

                    //결과값 코드 및 메시지
                    ConfigDto resultDto = new ConfigDto();
                    try {
                        JsonUtils.autoMappingJsonToObject (json, resultDto);
                    } catch (Exception e) {
                        //nothing;
                    }
                    map.put (RESULT_DTO, resultDto);



                    listener.onComplete (map);
                }


            }

            @Override
            public void onFailed(AjaxStatus status) {

                LogUtil.I("실패!!!! --> " + status.getCode());
                LogUtil.I("실패!!!! --> " + status.getError());
                LogUtil.I("실패!!!! -----> " + status.getMessage());

                JSONObject tJSON = null;
                if (!StringUtils.isEmpty(status.getError())) {
                    try {
                        tJSON = new JSONObject(status.getError());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //nothing yet;
                }


                LogUtil.I("실패!!!!");

                HashMap<String, Object> map = null;

                if (tJSON != null) {
                    LogUtil.W(tJSON.toString());
                    map = JsonUtils.parse(tJSON);

                    //결과값 코드 및 메시지
                    APIResutDtoFailed resultDto = new APIResutDtoFailed();
                    try {
                        JsonUtils.autoMappingJsonToObject(tJSON, resultDto);
                    } catch (Exception e) {
                        //nothing;
                    }
                    map.put(RESULT_DTO, resultDto);
                }


                listener.onFailed(status, map);
            }
        };

        if (contextDto.mActivity != null) {
            new HttpUtils(contextDto.mActivity).httpExecute (getConfigURL (API_UPDATE_FCM), null, params, httpListener, showProgress);
        } else {
            new HttpUtils (context).httpExecute (getConfigURL (API_UPDATE_FCM), null, params, httpListener, false);
        }

    }



    /** 컨피그 얻기
     *
     * @param context
     * @param showProgress
     * @param listener
     */
    public static void sendTakeOverFCM (Context context,
                                     boolean showProgress,
                                     String driver_id,
                                     String mac,
                                     final ApiMapListenerWithFailedRest listener) {

        final ApiContextInfoDto contextDto = getContextInfo (context);

        //파라미터
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("phone_no", GeneralUtils.getPhoneNumber(BaseApplication.getInstance()));
        params.put("driver_id", driver_id);
        params.put("mac", mac);
        addCommonParam (context.getApplicationContext(), params);




        //http 리스너
        HttpListener httpListener = new HttpListener() {

            public void onSuccess (JSONObject json, HttpResultDto result) {

                LogUtil.W(json.toString());

                if(result.isSuccess) {

                    LogUtil.W(json.toString());
                    HashMap<String, Object> map = JsonUtils.parse(json);

                    //결과값 코드 및 메시지
                    ConfigDto resultDto = new ConfigDto();
                    try {
                        JsonUtils.autoMappingJsonToObject (json, resultDto);
                    } catch (Exception e) {
                        //nothing;
                    }
                    map.put (RESULT_DTO, resultDto);



                    listener.onComplete (map);
                }


            }

            @Override
            public void onFailed(AjaxStatus status) {

                LogUtil.I("실패!!!! --> " + status.getCode());
                LogUtil.I("실패!!!! --> " + status.getError());
                LogUtil.I("실패!!!! -----> " + status.getMessage());

                JSONObject tJSON = null;
                if (!StringUtils.isEmpty(status.getError())) {
                    try {
                        tJSON = new JSONObject(status.getError());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //nothing yet;
                }


                LogUtil.I("실패!!!!");

                HashMap<String, Object> map = null;

                if (tJSON != null) {
                    LogUtil.W(tJSON.toString());
                    map = JsonUtils.parse(tJSON);

                    //결과값 코드 및 메시지
                    APIResutDtoFailed resultDto = new APIResutDtoFailed();
                    try {
                        JsonUtils.autoMappingJsonToObject(tJSON, resultDto);
                    } catch (Exception e) {
                        //nothing;
                    }
                    map.put(RESULT_DTO, resultDto);
                }


                listener.onFailed(status, map);
            }
        };

        if (contextDto.mActivity != null) {
            new HttpUtils(contextDto.mActivity).httpExecute (getConfigURL (API_SEND_TAKEOVER), null, params, httpListener, showProgress);
        } else {
            new HttpUtils (context).httpExecute (getConfigURL (API_SEND_TAKEOVER), null, params, httpListener, false);
        }

    }






    /** 비콘 기기정보 얻기
     *
     * @param context
     * @param showProgress
     * @param MAC
     * @param listener
     */
    public static void getDeviceAlias (Context context,
                                       boolean showProgress,
                                       String MAC,
                                       final ApiMpaListenerWithFailedRestGet listener) {


        final ApiContextInfoDto contextDto = getContextInfo (context);
        RequestParams params = new RequestParams();

        try {

            LogUtil.I("[DATA] --> url : " + getV2Url(API_DEVICE_ALIAS + MAC));

            SendAndReceiveMessage.get(
                    getV2Url(API_DEVICE_ALIAS + MAC),
                    params,
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String strJSON = null;
                            JSONObject tJson = null;

                            try {
                                strJSON = new String(responseBody, "ISO-8859-1");
                                tJson = new JSONObject(strJSON);

                                LogUtil.W(strJSON);

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            HashMap<String, Object> map = JsonUtils.parse(new JSONObject());

                            //결과값 코드 및 메시지
                            DeviceAliasDto resultDto = new DeviceAliasDto();
                            try {
                                JsonUtils.autoMappingJsonToObject(tJson, resultDto);
                            } catch (Exception e) {
                                //nothing;
                            }
                            map.put(RESULT_DTO, resultDto);
                            listener.onSuccess(statusCode, map);

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            listener.onFailure(statusCode);
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }


    }




    /** 비콘 기기정보 얻기
     *
     * @param context
     * @param showProgress
     * @param stickerNo
     * @param listener
     */
    public static void getDeviceMac (Context context,
                                     boolean showProgress,
                                     String stickerNo,
                                     final ApiMpaListenerWithFailedRestGet listener) {


        final ApiContextInfoDto contextDto = getContextInfo (context);
        RequestParams params = new RequestParams();

        try {

            LogUtil.I("[DATA] --> url : " + getV2Url(API_DEVICE_ALIAS + "?alias=" + stickerNo));

            SendAndReceiveMessage.get(
                    getV2Url(API_DEVICE_ALIAS + "?alias=" + stickerNo),
                    params,
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {


                            LogUtil.I("[DATA] --> onSuccess ()");

                            String strJSON = null;
                            JSONObject tJson = null;

                            try {
                                strJSON = new String(responseBody, "ISO-8859-1");
                                tJson = new JSONObject(strJSON);

                                LogUtil.W(strJSON);

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            HashMap<String, Object> map = JsonUtils.parse(new JSONObject());

                            //결과값 코드 및 메시지
                            DeviceAliasDto resultDto = new DeviceAliasDto();
                            try {
                                JsonUtils.autoMappingJsonToObject(tJson, resultDto);
                            } catch (Exception e) {
                                //nothing;
                            }
                            map.put(RESULT_DTO, resultDto);
                            listener.onSuccess(statusCode, map);

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            LogUtil.I("[DATA] --> onFailure ()");

                            listener.onFailure(statusCode);
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /** 비콘 기기정보 얻기
     *
     * @param context
     * @param showProgress
     * @param phoneNo
     * @param listener
     */
    public static void getDeliveryDriverInfo (Context context,
                                       boolean showProgress,
                                       String phoneNo,
                                       final ApiMpaListenerWithFailedRestGet listener) {


        final ApiContextInfoDto contextDto = getContextInfo (context);
        RequestParams params = new RequestParams();

        try {

            LogUtil.I("[DATA] --> url : " + getV2Url(API_PHONE_OWNER + phoneNo));

            SendAndReceiveMessage.get(
                    getV2Url(API_PHONE_OWNER + phoneNo),
                    params,
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String strJSON = null;
                            JSONObject tJson = null;

                            try {
                                strJSON = new String(responseBody, "ISO-8859-1");
                                tJson = new JSONObject(strJSON);

                                LogUtil.W(strJSON);

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            HashMap<String, Object> map = JsonUtils.parse(new JSONObject());

                            //결과값 코드 및 메시지
                            DriverInfoDto resultDto = new DriverInfoDto();
                            try {
                                JsonUtils.autoMappingJsonToObject(tJson.optJSONObject("driver"), resultDto);
                            } catch (Exception e) {
                                //nothing;
                            }
                            map.put(RESULT_DTO, resultDto);
                            listener.onSuccess(statusCode, map);

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            listener.onFailure(statusCode);
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }


    }




    /** 비콘 기기정보 얻기
     *
     * @param context
     * @param showProgress
     * @param listener
     */
    public static void sendMeAwake (Context context,
                                              boolean showProgress,
                                              final ApiMpaListenerWithFailedRestGet listener) {

        //FCM토큰
        String token = "";//FirebaseInstanceId.getInstance().getToken();


        final ApiContextInfoDto contextDto = getContextInfo (context);
        RequestParams params = new RequestParams();
        params.put("token", token);

        try {

            String phoneNo = GeneralUtils.getPhoneNumber(context.getApplicationContext());

            SendAndReceiveMessage.get(
                    getConfigURL(API_CONFIG_AWAKE_ME),
                    params,
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String strJSON = null;
                            JSONObject tJson = null;

                            try {
                                strJSON = new String(responseBody, "ISO-8859-1");
                                tJson = new JSONObject(strJSON);

                                LogUtil.W(strJSON);

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            HashMap<String, Object> map = JsonUtils.parse(new JSONObject());

                            /*/결과값 코드 및 메시지
                            DriverInfoDto resultDto = new DriverInfoDto();
                            try {
                                JsonUtils.autoMappingJsonToObject(tJson, resultDto);
                            } catch (Exception e) {
                                //nothing;
                            }
                            map.put(RESULT_DTO, resultDto);
                            listener.onSuccess(statusCode, map);*/

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            listener.onFailure(statusCode);
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }


    }






    /** 비콘 기기정보 얻기
     *
     * @param listener
     */
    public static void getRelativeAlarmReceiverList (final ApiMpaListenerWithFailedRestGet listener) {


        RequestParams params = new RequestParams();

        try {

            LogUtil.I("[DATA] --> url : " + getV2Url(API_ALARM_RECEIVER_LIST));

            SendAndReceiveMessage.get(
                    getV2Url(API_ALARM_RECEIVER_LIST),
                    params,
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String strJSON = null;
                            JSONObject tJson = null;

                            try {
                                strJSON = new String(responseBody, "ISO-8859-1");
                                tJson = new JSONObject(strJSON);

                                LogUtil.W(strJSON);

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            HashMap<String, Object> map = JsonUtils.parse(new JSONObject());

                            //결과값 코드 및 메시지
                            ArrayList<ContactItemDto> dtos = new ArrayList<>();
                            try {
                                JSONArray items =  tJson.optJSONArray("data");


                                for(int i=0;i<items.length();i++) {
                                    try {
                                        JSONObject item = items.getJSONObject(i);
                                        ContactItemDto dto = new ContactItemDto ();
                                        JsonUtils.autoMappingJsonToObject (item, dto);
                                        dtos.add (dto);
                                    } catch (Exception e) {
                                        LogUtil.E(e.toString());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            map.put(ARRAY_LIST, dtos);
                            listener.onSuccess(statusCode, map);

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            listener.onFailure(statusCode);
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /** 등록된 기기 리스트 받아오자.
     *
     * @return
     */
    public static void getAutorizedBeaconList (Context context,
                                               boolean showProgress,
                                               final ApiMapListenerWithFailed listener) {


        RequestParams params = new RequestParams();

        try {

            LogUtil.I("[DATA] --> url : " + getV2Url(API_GET_AUTHORIZED_BEACON_LIST));

            SendAndReceiveMessage.get(
                    getV2Url(API_GET_AUTHORIZED_BEACON_LIST),
                    params,
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String strJSON = null;
                            JSONArray tJson = null;

                            try {
                                strJSON = new String(responseBody, "ISO-8859-1");
                                tJson = new JSONArray(strJSON);

                                LogUtil.W(strJSON);

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            HashMap<String, Object> map = JsonUtils.parse(new JSONObject());

                            ArrayList<AuthorizedDeviceItemDto> dtos = new ArrayList<AuthorizedDeviceItemDto>();
                            try {


                                for (int i = 0; i < tJson.length(); i++) {
                                    try {
                                        JSONObject item = tJson.getJSONObject(i);
                                        AuthorizedDeviceItemDto dto = new AuthorizedDeviceItemDto();
                                        JsonUtils.autoMappingJsonToObject(item, dto);
                                        dtos.add(dto);
                                    } catch (Exception e) {
                                        LogUtil.E(e.toString());
                                    }
                                }
                            } catch (Exception e) {
                                //nothing;
                            }

                            map.put(ARRAY_LIST, dtos);


                            listener.onComplete(map);

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            AjaxStatus status = new AjaxStatus();
                            status.code(statusCode);
                            listener.onFailed(status);
                        }
                    }
            );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** 디버깅 조건 성립 해 봅시다
     *
     * @param context
     * @param driver_id
     * @param mac
     * @param rssi
     * @param lat
     * @param lng
     * @param listener
     */
    public static void updateDebugInfo (Context context,
                                   String driver_id,
                                   String mac,
                                   int rssi,
                                   String lat,
                                   String lng,
                                   String test_div_code,
                                   String last_ble_info,
                                   final ApiMapListenerWithFailedRest listener) {



        //파라미터
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("driver_id", driver_id);
        params.put("phone_no", GeneralUtils.getPhoneNumber(BaseApplication.getInstance()));

        params.put("mac", mac);
        params.put("rssi", rssi);

        int is_screen_on = GeneralUtils.isScreenOn(context) ? 1 : 0;
        int is_app_foreground = GeneralUtils.isApplicationBroughtToBackground(context) ? 0 : 1;
        String network_stat = Base64EncUtil.encode(GeneralUtils.getNetworkStatusInfo(context), "utf-8");
        String battery_stat = Base64EncUtil.encode(GeneralUtils.getBatteryStatus(context), "utf-8");
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Calendar.getInstance().getTimeInMillis()));
        String live_service_list = "--> SensingProcessService : " + isRunningService(context, SensingProcessService.class)
                + "\n--> HttpProcessService : " + isRunningService(context, HttpProcessService.class);


        params.put("battery_stat", battery_stat);
        params.put("lat", lat);
        params.put("lng", lng);
        params.put("is_screen_on", is_screen_on);
        params.put("is_app_foreground", is_app_foreground);
        params.put("network_stat", network_stat);
        params.put("live_service_list", live_service_list);

        params.put("test_div_code", test_div_code);
        params.put("date", date);
        params.put("last_ble_info", last_ble_info);
        addCommonParam (context.getApplicationContext(), params);




        //http 리스너
        HttpListener httpListener = new HttpListener() {

            public void onSuccess (JSONObject json, HttpResultDto result) {

                LogUtil.W(json.toString());

                if(result.isSuccess) {

                    LogUtil.W(json.toString());
                    HashMap<String, Object> map = JsonUtils.parse(json);

                    //결과값 코드 및 메시지
                    ConfigDto resultDto = new ConfigDto();
                    try {
                        JsonUtils.autoMappingJsonToObject (json, resultDto);
                    } catch (Exception e) {
                        //nothing;
                    }
                    map.put (RESULT_DTO, resultDto);

                    listener.onComplete (map);
                }


            }

            @Override
            public void onFailed(AjaxStatus status) {

                LogUtil.I("실패!!!! --> " + status.getCode());
                LogUtil.I("실패!!!! --> " + status.getError());
                LogUtil.I("실패!!!! -----> " + status.getMessage());

                JSONObject tJSON = null;
                if (!StringUtils.isEmpty(status.getError())) {
                    try {
                        tJSON = new JSONObject(status.getError());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //nothing yet;
                }


                LogUtil.I("실패!!!!");

                HashMap<String, Object> map = null;

                if (tJSON != null) {
                    LogUtil.W(tJSON.toString());
                    map = JsonUtils.parse(tJSON);

                    //결과값 코드 및 메시지
                    APIResutDtoFailed resultDto = new APIResutDtoFailed();
                    try {
                        JsonUtils.autoMappingJsonToObject(tJSON, resultDto);
                    } catch (Exception e) {
                        //nothing;
                    }
                    map.put(RESULT_DTO, resultDto);
                }


                listener.onFailed(status, map);
            }
        };

        new HttpUtils (context).httpExecute (getConfigURL (API_UPDATE_DEBUG_INFO), null, params, httpListener, false);


    }

}