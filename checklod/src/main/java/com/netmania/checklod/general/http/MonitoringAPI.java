package com.netmania.checklod.general.http;

import android.content.Context;

import com.androidquery.callback.AjaxStatus;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.data.AppSharedData;
import com.netmania.checklod.general.dto.APIResutDtoFailed;
import com.netmania.checklod.general.dto.ApiContextInfoDto;
import com.netmania.checklod.general.dto.DriverInfoDto;
import com.netmania.checklod.general.dto.LocationDto;
import com.netmania.checklod.general.dto.TakeOverHistoryItemDto;
import com.netmania.checklod.general.dto.TemperatureTrackingDto;
import com.netmania.checklod.general.manage.Constants;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.JsonUtils;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.general.utils.StringUtils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hansangcheol on 2017. 1. 11..
 */




public class MonitoringAPI extends BaseAPI {


    /** 업데이트 해 보아요
     *
     * @param context
     * @param showProgress
     * @param MAC
     * @param sequence
     * @param RTC
     * @param int_temp
     * @param int_hum
     * @param ext_temp
     * @param ext_hum
     * @param timestamp
     * @param listener
     */
    public static void updateBeaconInfo (Context context,
                                         boolean showProgress,
                                         String MAC,
                                         int sequence,
                                         String RTC,
                                         float int_temp,
                                         float int_hum,
                                         float ext_temp,
                                         float ext_hum,
                                         long timestamp,
                                         final ApiMapListenerWithFailedRest listener) {



        JSONObject jsonParam = new JSONObject();
        String m = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date (timestamp));

        try {
            jsonParam.put("MAC", MAC);
            jsonParam.put("sequence", sequence);
            jsonParam.put("RTC", RTC);
            jsonParam.put("int_temp", int_temp);
            jsonParam.put("int_hum", int_hum);
            jsonParam.put("ext_temp", ext_temp);
            jsonParam.put("ext_hum", ext_hum);
            jsonParam.put("measured_at", m);
            jsonParam.put("phone_no", GeneralUtils.getPhoneNumber(context.getApplicationContext()));


            SendAndReceiveMessage.post(context.getApplicationContext(), getV2Url(V2_API_REPORT), jsonParam,
                    new JsonHttpResponseHandler() {


                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            LogUtil.I ("어허어어 --> 성공 1 --> " + statusCode);

                            AjaxStatus result = new AjaxStatus();
                            result.code(statusCode);
                            result.message(response.toString());


                            switch (statusCode) {

                                case REST_SUCCESS :
                                    listener.onComplete();
                                    break;

                                default :

                                    listener.onFailed(result);

                            }
                        }


                        //바디가 없는 경우 핸들링
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                            LogUtil.I ("어허어어 --> 실패 1 --> " + statusCode);

                            switch (statusCode) {

                                case REST_SUCCESS :
                                    listener.onComplete();
                                    break;

                                default :
                                    AjaxStatus result = new AjaxStatus();
                                    result.code(statusCode);
                                    result.message(responseString);

                                    listener.onFailed(result);

                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            LogUtil.I ("어허어어 --> 실패 2 --> " + statusCode);


                            AjaxStatus result = new AjaxStatus();
                            result.code(statusCode);
                            if (errorResponse != null) result.message(errorResponse.toString());

                            HashMap<String, Object> map = null;

                            if (errorResponse != null) {
                                map = JsonUtils.parse(errorResponse);

                                //결과값 코드 및 메시지
                                APIResutDtoFailed resultDto = new APIResutDtoFailed();
                                try {
                                    JsonUtils.autoMappingJsonToObject(errorResponse, resultDto);
                                } catch (Exception e) {
                                    //nothing;
                                }
                                map.put(RESULT_DTO, resultDto);
                            }

                            listener.onFailed(result, map);
                        }


                    });

        } catch (Exception e) {
            e.printStackTrace();
        }




    }


    /** 업데이트 해 보아요 떼거지로
     *
     * @param context
     * @param showProgress
     * @param datas
     * @param listener
     */
    public static void updateBeaconInfo (Context context,
                                         boolean showProgress,
                                         ArrayList<TemperatureTrackingDto> datas,
                                         final ApiMapListenerWithFailedRest listener) {


        JSONArray jsonParam = new JSONArray();


        try {

            TemperatureTrackingDto tItem;
            for (int i = 0; i < datas.size(); i++) {
                JSONObject tmp = new JSONObject();
                tItem = datas.get(i);
                tmp.put("MAC", tItem.MAC);
                tmp.put("sequence", tItem.seq);

                tmp.put("RTC", tItem.rtc);
                tmp.put("int_temp", tItem.temp);
                tmp.put("int_hum", tItem.hum);
                tmp.put("ext_temp", tItem.outside_temp);
                tmp.put("ext_hum", tItem.outside_hum);
                tmp.put("measured_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date (tItem.timestamp)));
                tmp.put("phone_no", GeneralUtils.getPhoneNumber(context.getApplicationContext()));

                jsonParam.put(tmp);

                tmp = null;

            }



            SendAndReceiveMessage.post(context.getApplicationContext(), getV2Url(API_BALANCE_DATA), jsonParam,
                    new JsonHttpResponseHandler() {


                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            AjaxStatus result = new AjaxStatus();
                            result.code(statusCode);
                            result.message(response.toString());


                            switch (statusCode) {

                                case REST_SUCCESS :
                                    listener.onComplete();
                                    break;

                                default :

                                    listener.onFailed(result);

                            }
                        }


                        //바디가 없는 경우 핸들링
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                            switch (statusCode) {

                                case REST_SUCCESS :
                                    listener.onComplete();
                                    break;

                                default :
                                    AjaxStatus result = new AjaxStatus();
                                    result.code(statusCode);
                                    result.message(responseString);

                                    listener.onFailed(result);

                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {


                            AjaxStatus result = new AjaxStatus();
                            result.code(statusCode);
                            if (errorResponse != null) result.message(errorResponse.toString());

                            HashMap<String, Object> map = null;

                            if (errorResponse != null) {
                                map = JsonUtils.parse(errorResponse);

                                //결과값 코드 및 메시지
                                APIResutDtoFailed resultDto = new APIResutDtoFailed();
                                try {
                                    JsonUtils.autoMappingJsonToObject(errorResponse, resultDto);
                                } catch (Exception e) {
                                    //nothing;
                                }
                                map.put(RESULT_DTO, resultDto);
                            }

                            listener.onFailed(result, map);
                        }


                    });

        } catch (Exception e) {
            e.printStackTrace();
        }




    }




    /** 나 등록해줘요
     *
     */
    @SuppressWarnings("static-access")
    public static void checkIn (final Context context,
                                                boolean showProgress,
                                                final String mac,
                                                String invoiceId,
                                                double minLimit,
                                                double maxLimit,
                                                String orderNo,
                                                String boxSerial,
                                                final ApiMapListenerWithFailedRest listener) {


        JSONObject jsonParam = new JSONObject();

        try {
            jsonParam.put(P_LOGGER_ID, mac);
            jsonParam.put(P_INVOICE_ID, invoiceId);
            jsonParam.put(P_LOWER_LIMIT, minLimit);
            jsonParam.put(P_UPPER_LIMIT, maxLimit);
            jsonParam.put(P_PHONE_NO, GeneralUtils.getPhoneNumber(context.getApplicationContext()));

            String api_url = getV2Url(V3_API_CHECK_IN);

            LogUtil.I("[DATA] --> url : " + api_url);
            LogUtil.I("[DATA] --> json param : " + jsonParam.toString());

            SendAndReceiveMessage.post(context.getApplicationContext(), api_url, jsonParam,

                    new JsonHttpResponseHandler() {


                        //바디가 없는 경우 핸들링
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                            LogUtil.I("실패 --> 1 " + statusCode);

                            switch (statusCode) {

                                case REST_SUCCESS :
                                    listener.onComplete();
                                    break;

                                default :
                                    AjaxStatus result = new AjaxStatus();
                                    result.code(statusCode);
                                    result.message(responseString);

                                    listener.onFailed(result);

                            }

                            sendFuck (mac, "체크인 성공 --> 1 " + statusCode + " :: " + responseString);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                            LogUtil.I("실패 --> 2 " + statusCode);

                            AjaxStatus result = new AjaxStatus();
                            result.code(statusCode);
                            result.message(String.valueOf(errorResponse));


                            HashMap<String, Object> map = null;

                            if (errorResponse != null) {
                                map = JsonUtils.parse(errorResponse);

                                //결과값 코드 및 메시지
                                APIResutDtoFailed resultDto = new APIResutDtoFailed();
                                try {
                                    JsonUtils.autoMappingJsonToObject(errorResponse, resultDto);
                                } catch (Exception e) {
                                    //nothing;
                                }
                                map.put(RESULT_DTO, resultDto);
                            }

                            listener.onFailed(result, map);


                            sendFuck (mac, "체크인 실패 --> 2 " + statusCode + " :: " + errorResponse);
                        }

                    });

        } catch (Exception e) {
            e.printStackTrace();

            sendFuck (mac, "체크인 실패 --> 3 " + e.getStackTrace());
        }
    }



    /** 나 인수 받음
     *
     */
    @SuppressWarnings("static-access")
    public static void takeOver (final Context context,
                                boolean showProgress,
                                String mac,
                                String invoiceId,
                                final ApiMapListenerWithFailedRest listener) {

        final ApiContextInfoDto contextDto = getContextInfo (context);
        JSONObject jsonParam = new JSONObject();

        try {
            jsonParam.put(P_LOGGER_ID, mac);
            jsonParam.put(P_INVOICE_ID, invoiceId);
            jsonParam.put(P_PHONE_NO, GeneralUtils.getPhoneNumber(context.getApplicationContext()));

            LogUtil.I("[DATA] --> url : " + getV2Url(V2_API_TAKEOVER));
            LogUtil.I("[DATA] --> json param : " + jsonParam.toString());

            SendAndReceiveMessage.post(context.getApplicationContext(), getV2Url(V2_API_TAKEOVER), jsonParam,
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject json) {

                            LogUtil.I("성공  --> 1 " + json.toString());

                            AjaxStatus status = new AjaxStatus();
                            status.code (statusCode);
                            status.message(json.toString());

                            HashMap<String, Object> map = JsonUtils.parse(json);


                            String minLimit = json.optJSONObject("response").optString("lowerLimit");
                            String maxLimit = json.optJSONObject("response").optString("upperLimit");

                            map.put(TEMPERATURE_MIN_LIMIT, minLimit);
                            map.put(TEMPERATURE_MAX_LIMIT, maxLimit);


                            //결과값 코드 및 메시지
                            ArrayList<TakeOverHistoryItemDto> dtos = new ArrayList<> ();
                            try {
                                JSONArray items =  json.optJSONObject("response").optJSONArray("data");


                                for(int i=0;i<items.length();i++) {
                                    try {
                                        JSONObject item = items.getJSONObject(i);
                                        TakeOverHistoryItemDto dto = new TakeOverHistoryItemDto ();
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

                            listener.onComplete(map);

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                            LogUtil.I("실패  --> 1 " + statusCode);

                            AjaxStatus status = new AjaxStatus();
                            status.code (AjaxStatus.NETWORK_ERROR);
                            listener.onFailed (status);
                        }

                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    /** 나 인수 받음
     *
     */
    @SuppressWarnings("static-access")
    public static void retrieveData (final Context context,
                                 boolean showProgress,
                                 String mac,
                                 String invoiceId,
                                 final ApiMapListenerWithFailedRest listener) {

        final ApiContextInfoDto contextDto = getContextInfo (context);
        JSONObject jsonParam = new JSONObject();

        try {
            jsonParam.put(P_LOGGER_ID, mac);
            jsonParam.put(P_INVOICE_ID, invoiceId);
            jsonParam.put(P_PHONE_NO, GeneralUtils.getPhoneNumber(context.getApplicationContext()));

            LogUtil.I("[DATA] --> url : " + getV2Url(API_RETRIEVE_DATA));
            LogUtil.I("[DATA] --> json param : " + jsonParam.toString());

            SendAndReceiveMessage.post(context.getApplicationContext(), getV2Url(API_RETRIEVE_DATA), jsonParam,
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject json) {

                            LogUtil.I("성공  --> 1 " + json.toString());

                            AjaxStatus status = new AjaxStatus();
                            status.code (statusCode);
                            status.message(json.toString());

                            HashMap<String, Object> map = JsonUtils.parse(json);


                            String minLimit = json.optJSONObject("response").optString("lowerLimit");
                            String maxLimit = json.optJSONObject("response").optString("upperLimit");

                            map.put(TEMPERATURE_MIN_LIMIT, minLimit);
                            map.put(TEMPERATURE_MAX_LIMIT, maxLimit);


                            //결과값 코드 및 메시지
                            ArrayList<TakeOverHistoryItemDto> dtos = new ArrayList<> ();
                            try {
                                JSONArray items =  json.optJSONObject("response").optJSONArray("data");


                                for(int i=0;i<items.length();i++) {
                                    try {
                                        JSONObject item = items.getJSONObject(i);
                                        TakeOverHistoryItemDto dto = new TakeOverHistoryItemDto ();
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

                            listener.onComplete(map);

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                            LogUtil.I("실패  --> 1 " + statusCode);

                            AjaxStatus status = new AjaxStatus();
                            status.code (AjaxStatus.NETWORK_ERROR);
                            listener.onFailed (status);
                        }

                        @Override
                        public void onFailure (int statusCode, Header[] headers, String error, Throwable throwable) {

                            AjaxStatus status = new AjaxStatus();
                            status.code (AjaxStatus.NETWORK_ERROR);
                            listener.onFailed (status);
                        }

                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    /** 나 등록해줘요
     *
     */
    @SuppressWarnings("static-access")
    public static void checkOut (final Context context,
                                boolean showProgress,
                                String mac,
                                String invoiceId,
                                final ApiMapListenerWithFailedRest listener) {

        final ApiContextInfoDto contextDto = getContextInfo (context);
        JSONObject jsonParam = new JSONObject();

        try {
            jsonParam.put(P_LOGGER_ID, mac);
            jsonParam.put(P_INVOICE_ID, invoiceId);
            jsonParam.put(P_PHONE_NO, GeneralUtils.getPhoneNumber(context.getApplicationContext()));

            LogUtil.I("[DATA] --> url : " + getV2Url(V2_API_CHECK_OUT));
            LogUtil.I("[DATA] --> json param : " + jsonParam.toString());

            SendAndReceiveMessage.post(context.getApplicationContext(), getV2Url(V2_API_CHECK_OUT), jsonParam,
                    new JsonHttpResponseHandler() {


                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                            LogUtil.I("성공 --> " + statusCode);
                            listener.onComplete ();

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            LogUtil.I("실패  --> " + statusCode);
                            AjaxStatus status = new AjaxStatus();
                            status.code (AjaxStatus.NETWORK_ERROR);
                            listener.onFailed (status);
                        }

                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** 위치 및 각종 신호 데이타 전송
     *
     * @param context
     * @param showProgress
     * @param listener
     */
    @SuppressWarnings("static-access")
    public static void sendPhoneSignal (final Context context,
                                 boolean showProgress,
                                 int lte_rssi,
                                 String lat,
                                 String lng,
                                 String battery,
                                 final ApiMapListenerWithFailedRest listener) {


        JSONObject jsonParam = new JSONObject();

        try {


            jsonParam.put (P_PHONE_NO, GeneralUtils.getPhoneNumber(context.getApplicationContext()));
            jsonParam.put (P_LTE_RSSI, lte_rssi);
            jsonParam.put (P_LATITUDE, lat);
            jsonParam.put (P_LONGITUDE, lng);
            jsonParam.put (P_BATTERY, battery);
            jsonParam.put (P_MEASURED_AT, StringUtils.convertToStringYYYY_MM_DD(new Date(Calendar.getInstance().getTimeInMillis())));


            LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[DATA] --> url : " + getV2Url(API_PHONE_SIGNAL));
            LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[DATA] --> json param : " + jsonParam.toString());

            SendAndReceiveMessage.post(context.getApplicationContext(), getV2Url(API_PHONE_SIGNAL), jsonParam,
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                            LogUtil.I(DebugTags.TAG_PROCESS_CHECK, API_PHONE_SIGNAL + " :: 성공 --> " + statusCode);
                            listener.onComplete ();

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            LogUtil.I(DebugTags.TAG_PROCESS_CHECK, API_PHONE_SIGNAL + " :: 실패  --> " + statusCode);
                            AjaxStatus status = new AjaxStatus();
                            status.code (AjaxStatus.NETWORK_ERROR);
                            listener.onFailed (status);
                        }

                        // ----New Overridden method
                        @Override
                        public boolean getUseSynchronousMode() {
                            return false;
                        }

                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** 위치 및 각종 신호 데이타 전송
     *
     * @param context
     * @param showProgress
     * @param listener
     */
    @SuppressWarnings("static-access")
    public static void sendLogerSignal (final Context context,
                                        boolean showProgress,
                                        String mac,
                                        int ble_rssi,
                                        String battery,
                                        String measuredAt,
                                        final ApiMapListenerWithFailedRest listener) {


        JSONObject jsonParam = new JSONObject();

        try {


            jsonParam.put (P_MAC, mac);
            jsonParam.put (P_BLE_RSSI, ble_rssi);
            jsonParam.put (P_BATTERY, battery);
            jsonParam.put (P_MEASURED_AT, measuredAt);


            LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[DATA] --> url : " + getV2Url(API_LOGER_SIGNAL));
            LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[DATA] --> json param : " + jsonParam.toString());

            SendAndReceiveMessage.post(context.getApplicationContext(), getV2Url(API_LOGER_SIGNAL), jsonParam,
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                            LogUtil.I(DebugTags.TAG_PROCESS_CHECK, API_LOGER_SIGNAL + " :: 성공 --> " + statusCode);
                            listener.onComplete ();

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            LogUtil.I(DebugTags.TAG_PROCESS_CHECK, API_LOGER_SIGNAL + " :: 실패  --> " + statusCode);
                            AjaxStatus status = new AjaxStatus();
                            status.code (AjaxStatus.NETWORK_ERROR);
                            listener.onFailed (status);
                        }

                        // ----New Overridden method
                        @Override
                        public boolean getUseSynchronousMode() {
                            return false;
                        }

                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
