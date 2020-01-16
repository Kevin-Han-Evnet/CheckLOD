package com.netmania.checklod.general.http;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.preference.PreferenceActivity;

import com.androidquery.callback.AjaxStatus;
import com.loopj.android.http.RequestParams;
import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.data.AppSharedData;
import com.netmania.checklod.general.data.DataProviderUtil;
import com.netmania.checklod.general.dto.ApiContextInfoDto;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.DriverInfoDto;
import com.netmania.checklod.general.dto.HttpResultDto;
import com.netmania.checklod.general.dto.LocationDto;
import com.netmania.checklod.general.hash.Base64EncUtil;
import com.netmania.checklod.general.manage.Constants;
import com.netmania.checklod.general.utils.StringUtils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class BaseAPI {


	public static final String P_LOGGER_ID = "loggerId";
	public static final String P_INVOICE_ID = "invoiceId";
	public static final String P_PHONE_NO = "phoneNo";
	public static final String P_LOWER_LIMIT = "lowerLimit";
	public static final String P_UPPER_LIMIT = "upperLimit";
	public static final String P_ORDERNO = "orderNo";
	public static final String P_BOX_SERIAL = "boxSerial";
	public static final String P_LTE_RSSI = "lteRssi";
	public static final String P_LATITUDE = "latitude";
	public static final String P_LONGITUDE = "longitude";
	public static final String P_BATTERY = "battery";
	public static final String P_MEASURED_AT = "measuredAt";
	public static final String P_MAC = "MAC";
	public static final String P_BLE_RSSI = "bleRssi";



	public static final String API_CONFIG = "config/config_lookup.php";
	public static final String API_GET_AUTHORIZED_BEACON_LIST = "api.php/loggers";
	public static final String API_CONFIG_AWAKE_ME = "send_me_awake.php";
	public static final String API_APP_VERSION = "appversion.php";
	public static final String API_UPDATE_FCM = "update_fcm.php";
	public static final String API_SEND_TAKEOVER = "takeover.php";
	public static final String API_UPDATE_DEBUG_INFO = "update_debug_info.php";
	public static final String API_CHECK_IN = "checkin.php";
	public static final String API_REPORT = "storedata.php";
	public static final String API_TAKEOVER = "takeover.php";
	public static final String API_CHECK_OUT = "checkout.php";
	public static final String API_HANDOUT = "handout.php";
	public static final String API_BALANCE_DATA = "balancedata.v2.php";
	public static final String API_RETRIEVE_DATA = "retrievedata.php";

	public static final String V2_API_CHECK_IN = "checkin.v2.php";
	public static final String V2_API_REPORT = "storedata.v2.php";
	public static final String V2_API_TAKEOVER = "takeover.v2.php";
	public static final String V2_API_CHECK_OUT = "checkout.v2.php";
	public static final String V2_API_HANDOUT = "handout_v2";
	public static final String API_DEVICE_ALIAS = "api.php/loggers/";
	public static final String API_PHONE_OWNER = "phoneOwner.php/";
	public static final String API_PHONE_SIGNAL = "storePhoneSignal.php";
	public static final String API_LOGER_SIGNAL = "storeLoggerSignal.php";
	public static final String API_ALARM_RECEIVER_LIST = "retrieveAlarmSubscribers.php";

	public static final String V3_API_CHECK_IN = "checkin.v3.php";



	public static final String TEMPERATURE_MAX_LIMIT = "upperLimit";
	public static final String TEMPERATURE_MIN_LIMIT = "lowerLimit";


	public static final String RESULT_DTO = "result";
	public static final String ARRAY_LIST = "arrayList";


	public static final int REST_CONNECTION_FAILED = 0;
	public static final int REST_SUCCESS = 200;
	public static final int REST_NO_CONTENTS = 204;
	public static final int REST_NOT_FOUND = 404;
	public static final int REST_PARAMETER_NONE = 400;
	public static final int REST_INTERNAL_SERVER_ERROR = 500;






	//getTestURL
	public static String getTestURL(String url) {
		return String.format("%s%s", "http://220.118.0.195/tnt-ex-mgr-pilot/service/", url);
	}

	//기본 API URL
	public static String getV2Url(String url) {
		return String.format("%s%s", Constants.getBaseURL(Constants.URL_CATEGORY_API), url);
	}

	public static String getV2UploadUrl(String url) {
		return String.format("%s%s", Constants.getBaseURL(Constants.URL_CATEGORY_UPLOAD), url);
	}

	//kevin API URL
	public static String getConfigURL(String url) {
		return String.format("%s%s", Constants.getBaseURL(Constants.URL_CATEGORY_CONFIG), url);
	}
	
	
	//분기 함수 둡시다 
	public static ApiContextInfoDto getContextInfo (Context context) {

		
		ApiContextInfoDto result = new ApiContextInfoDto ();
		
		if (context.getClass ().equals (BaseApplication.class)) {
			result.mApp = (BaseApplication) context;
			result.mActivity = null;
		} else if (((BaseActivity) context).mApp != null) {
			result.mApp = ((BaseActivity) context).mApp;
			result.mActivity = (BaseActivity) context;
		}
		
		return result;
	}


	/** 결과 코드
	 *
	 * @param code
	 * @return
	 */
	public static int getErrorCode (String code) {
		int error_code = 0;
		try {
			if(!StringUtils.isEmpty (code)) {
				error_code = Integer.valueOf(code);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return error_code;
	}


	/** 레스트 풀 에러코드
	 *
	 * @param code
	 * @return
     */
	public static int getRestErrorCode (String code) {
		int error_code = REST_SUCCESS;
		try {
			if(!StringUtils.isEmpty (code)) {
				error_code = Integer.valueOf(code);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return error_code;
	}


	/** 커먼 파라미터 넣기
	 *
	 * @param context
	 * @param params
	 */
	protected static void addCommonParam (Context context, Map<String, Object> params){
		//nothing yet;
	}

	/** 커먼 파라미터 넣기
	 *
	 * @param context
	 * @param params
	 */
	protected static void addCommonParam (Context context, RequestParams params){
		//nothing yet;
	}


	/** 버전코드
	 *
	 * @param context
	 * @return
	 */
	private static String getAppVersion(Context context){
		String app_version = "";
		try{
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			app_version = pInfo.versionName;
		}catch (Exception e){
			e.printStackTrace();
		}
		return app_version;
	}



	/** URL 인코딩 파라미터 얻기
	 *
	 * @param params
	 * @return
	 */
	public static String getUrlEncodedParameter (Map<String, Object> params, boolean url_encode) {
		String paramK = "?";
		String value = "";
		for (String key:params.keySet()) {

			try {
				value = url_encode ? URLEncoder.encode(String.valueOf(params.get (key)), "utf-8") : String.valueOf(params.get (key));
			} catch (UnsupportedEncodingException e) {
				//nothign;
			}

			paramK += key + "=" + value + "&";
		}

		return paramK;
	}



	/** URL 인코딩 파라미터 얻기
	 *
	 * @param params
	 * @return
	 */
	public static String getUrlEncodedParameter (Map<String, Object> params) {
		String paramK = "?";
		String value = "";
		for (String key:params.keySet()) {

			try {
				value = URLEncoder.encode(String.valueOf(params.get (key)), "utf-8");
			} catch (UnsupportedEncodingException e) {
				//nothign;
			}

			paramK += key + "=" + value + "&";
		}

		return paramK;
	}


	/** URL 인코딩 파라미터 얻기
	 *
	 * @param params
	 * @return
	 */
	public static String getUrlEncodedParameterTNT (Map<String, Object> params) {
		String paramK = "?";
		String value = "";
		for (String key:params.keySet()) {

			value = String.valueOf(params.get (key));
			paramK += key + "=" + value + "&";
		}

		try {
			return URLEncoder.encode(paramK, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}




	/** 거시기 보내보아라..
	 *
	 * @param mac
	 * @param msg
	 */
	public static void sendFuck (String mac, String msg) {


		final DriverInfoDto DVIF = AppSharedData.getDriverInfo();
		final LocationDto lastLocation = AppSharedData.getLastLocationInfo();
		final BeaconItemDto bcnInfo = DataProviderUtil.getInstance(BaseApplication.getInstance()).getTrackingDevice(mac);

		GeneralAPI.updateDebugInfo(
				BaseApplication.getInstance(),
				DVIF.id,
				mac,
				bcnInfo.rssi,
				String.valueOf(lastLocation.lat),
				String.valueOf(lastLocation.lng),
				BaseApplication.getInstance().getResources().getString(Constants.TEST_DIV_CODE),
				Base64EncUtil.encode(msg, "utf-8"),
				new BaseAPI.ApiMapListenerWithFailedRest() {
					@Override
					public void onComplete() {
						//nothing yet;
					}

					@Override
					public void onComplete(Map<String, Object> map) {
						//nothing yet;
					}

					@Override
					public void onFailed(AjaxStatus result) {
						//nothing yet;
					}

					@Override
					public void onFailed(AjaxStatus result, Map<String, Object> map) {
						//nothing yet;
					}
				}

		);

	}



	
	//class =======================================================================================
	public interface ApiJsonListener {
		void callBack(JSONObject json);
	}
	
	public interface ApiAuthHeaderJsonListener {
		void callBack(JSONObject json, HttpResultDto result);
	}
	
	public interface ApiMapListener {
		void callBack(Map<String, Object> map);
	}
	
	public interface ApiMapListenerWithFailed {
		void onComplete(Map<String, Object> map);
		void onComplete(int statusCode, PreferenceActivity.Header[] headers, JSONObject json);
		void onFailed(AjaxStatus status);
	}

	public interface ApiMapListenerWithFailedForFiles {
		void onProgress(int progress, int max);
		void onComplete(Map<String, Object> map);
		void onComplete(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse);
		void onComplete(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse);
		void onComplete(int statusCode, Header[] headers, Throwable throwable);
		void onFailed(int statusCode);
	}
	
	public interface ApiAuthHeaderMapListener {
		void callBack(Map<String, Object> map, HttpResultDto result);
	}


	public interface ApiMapListenerWithFailedRest {
		void onComplete();
		void onComplete(Map<String, Object> map);
		void onFailed(AjaxStatus result);
		void onFailed(AjaxStatus result, Map<String, Object> map);
	}


	public interface  ApiMpaListenerWithFailedRestGet {
		void onSuccess(int statusCode, Map<String, Object> map);
		void onFailure(int statusCode);
	}
    
}
