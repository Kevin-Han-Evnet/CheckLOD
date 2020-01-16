/*#############################################################
####
####	Develpment Information
####	
####	Developer : Kevin Han
####	
##############################################################*/

/*#############################################################
####
####	 ShopplSharedData Class
####	
##############################################################*/
package com.netmania.checklod.general.data;

import android.content.Context;
import android.content.SharedPreferences.Editor;


import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.dto.ConfigDto;
import com.netmania.checklod.general.dto.DriverInfoDto;
import com.netmania.checklod.general.dto.LocationDto;
import com.netmania.checklod.general.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;


public class AppSharedData {

	public static final String TAG = "AppSharedData";

	//유저정보
	public static final String DNAME_USER_INFO = "user_info";

	//셋팅정보
	public static final String DNAME_PREFERENCE = "checklod_pref";

	//GCM
	public static final String DNAME_FMC_PREFER = "fcmPreference";
	public static final String KEY_FCM_TOKEN = "keyFcmToken";
	public static final String KEY_LOCATION = "keyLocation";
	public static final String KEY_LTE_RSSI = "lteRssi";



	/* keys */
	public static final String KEY_SHOW_ALL_REPORTS = "KEY_SHOW_ALL_REPORTS";


	//String ---------------------------------------------------------------------
	/** String put
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @param value		------------	참조할 데이타 값
	 */
	public static void put (String d_name, String key, String value) {
		Editor spe = BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).edit ();
		spe.putString(key, value);
		spe.commit ();
	}

	/** String get
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @return
	 */
	public static String getString (String d_name, String key) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getString(key, "");
	}

	public static String getString (String d_name, String key, String df) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getString (key, df);
	}



	//Set<String> ---------------------------------------------------------------------
	/** String put
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @param value		------------	참조할 데이타 값
	 */
	public static void put (String d_name, String key, Set<String> value) {
		Editor spe = BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).edit ();
		spe.putStringSet (key, value);
		spe.commit ();
	}

	/** String get\
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @return
	 */
	public static Set<String> getStringSet (String d_name, String key) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getStringSet(key, null);
	}



	//Boolean ---------------------------------------------------------------------
	/** String put
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @param value		------------	참조할 데이타 값
	 */
	public static void put (String d_name, String key, boolean value) {
		Editor spe = BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).edit ();
		spe.putBoolean(key, value);
		spe.commit ();
	}

	/** String get
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @return
	 */
	public static boolean getBoolean (String d_name, String key) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getBoolean(key, false);
	}
	/** 디폴트가 있는경우 */
	public static boolean getBoolean (String d_name, String key, boolean df) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getBoolean(key, df);
	}




	//int ---------------------------------------------------------------------
	/** String put
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @param value		------------	참조할 데이타 값
	 */
	public static void put (String d_name, String key, int value) {
		Editor spe = BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).edit ();
		spe.putInt (key, value);
		spe.commit ();
	}

	/** String get
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @return
	 */
	public static int getInt (String d_name, String key) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getInt(key, 0);
	}

	public static int getIntDefault2 (String d_name, String key) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getInt (key, 2);
	}

	public static int getIntDefaultFree (String d_name, String key, int defaultValue) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getInt(key, defaultValue);
	}



	//long ---------------------------------------------------------------------
	/** String put
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @param value		------------	참조할 데이타 값
	 */
	public static void put (String d_name, String key, long value) {
		Editor spe = BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).edit ();
		spe.putLong (key, value);
		spe.commit ();
	}

	/** String get
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @return
	 */
	public static long getLong (String d_name, String key) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getLong(key, 0);
	}



	//float ---------------------------------------------------------------------
	/** String put
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @param value		------------	참조할 데이타 값
	 */
	public static void put (String d_name, String key, float value) {
		Editor spe = BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).edit ();
		spe.putFloat(key, value);
		spe.commit ();
	}

	/** String get
	 * @param d_name	------------	참조할 쉐어드프리퍼런스 이름
	 * @param key		------------	참조할 데이타 이름
	 * @return
	 */
	public static float getFloat (String d_name, String key) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getFloat(key, 0);
	}

	public static float getFloatDefaultFree (String d_name, String key, float value) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getFloat(key, value);
	}




	/** 다보기
	 *
	 */
	public static Map<String,?> getAll (String d_name) {
		return BaseApplication.getInstance().getSharedPreferences (d_name, Context.MODE_MULTI_PROCESS).getAll();
	}


	/** 알람상태
	 *
	 * @param data
	 */
	public static void setDriverInfo (DriverInfoDto data) {


		for (Field param:data.params()) {
			try {

				if (param.getType ().equals(String.class)) {
					if (param.get(data).equals ("true") || param.get(data).equals ("false")) {
						put(DNAME_USER_INFO, param.getName(), param.get(data).equals ("true"));
					} else {
						put(DNAME_USER_INFO, param.getName(), (String) param.get(data));
					}
				} else if (param.getType ().equals(int.class)) {
					put(DNAME_USER_INFO, param.getName(), (int) param.get(data));
				} else if (param.getType ().equals(boolean.class)) {
					put (DNAME_USER_INFO, param.getName(), (boolean) param.get(data));
				}
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	/** 유저 인포 리턴
	 *
	 * @return
	 */
	public static DriverInfoDto getDriverInfo () {
		DriverInfoDto tmpItem = new DriverInfoDto ();

		for (Field param : tmpItem.params()) {
			try {

				if (param.getType ().equals(String.class)) {
					tmpItem.setValue(param.getName(), getString (DNAME_USER_INFO, param.getName()));
				} else if (param.getType ().equals(int.class)) {
					tmpItem.setIntValue(param.getName(), getInt (DNAME_USER_INFO, param.getName()));
				} else if (param.getType ().equals(boolean.class)) {
					tmpItem.setBooleanValue (param.getName(), getBoolean (DNAME_USER_INFO, param.getName()));
				}


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return tmpItem;
	}




	/** 알람상태
	 *
	 * @param data
	 */
	public static void setConfigInfo (ConfigDto data) {


		for (Field param:data.params()) {
			try {

				if (param.getType ().equals(String.class)) {
					if (param.get(data).equals ("true") || param.get(data).equals ("false")) {
						put(DNAME_USER_INFO, param.getName(), param.get(data).equals ("true"));
					} else {
						put(DNAME_USER_INFO, param.getName(), (String) param.get(data));
					}
				} else if (param.getType ().equals(int.class)) {
					put(DNAME_USER_INFO, param.getName(), (int) param.get(data));
				} else if (param.getType ().equals(boolean.class)) {
					put (DNAME_USER_INFO, param.getName(), (boolean) param.get(data));
				}
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	/** 유저 인포 리턴
	 *
	 * @return
	 */
	public static ConfigDto getConfigInfo () {
		ConfigDto tmpItem = new ConfigDto ();

		for (Field param : tmpItem.params()) {
			try {

				if (param.getType ().equals(String.class)) {
					tmpItem.setValue(param.getName(), getString (DNAME_USER_INFO, param.getName()));
				} else if (param.getType ().equals(int.class)) {
					tmpItem.setIntValue(param.getName(), getInt (DNAME_USER_INFO, param.getName()));
				} else if (param.getType ().equals(boolean.class)) {
					tmpItem.setBooleanValue (param.getName(), getBoolean (DNAME_USER_INFO, param.getName()));
				}


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return tmpItem;
	}




	/** 위치정보
	 *
	 * @param location
	 */
	public static void setLastLocationInfo (LocationDto location) {
		String tmp = location.lat + "," + location.lng + "," + location.timestamp;
		put (DNAME_PREFERENCE, KEY_LOCATION, tmp);
	}


	/** 위치정보
	 *
	 * @return
	 */
	public static LocationDto getLastLocationInfo () {
		String[] tmp;
		try {
			tmp = getString (DNAME_PREFERENCE, KEY_LOCATION).split(",");
		} catch (Exception e) {
			tmp = new String[] {"0", "0", "0"};
		}
		LocationDto result = new LocationDto();

		result.lat = (tmp.length > 0 && !StringUtils.isEmpty(tmp[0])) ? Double.valueOf(tmp[0]) : 0;
		result.lng = (tmp.length > 1 && !StringUtils.isEmpty(tmp[1])) ? Double.valueOf(tmp[1]) : 0;
		result.timestamp = (tmp.length > 2 && !StringUtils.isEmpty(tmp[2])) ? Long.valueOf(tmp[2]) : 0;

		return result;
	}



	/** 전체 데이타 리포팅
	 * 
	 * @param report_all
	 */
	public static void setShowAllReports (boolean report_all) {
		put (DNAME_PREFERENCE, KEY_SHOW_ALL_REPORTS, report_all);
	}


	/** 전체 데이타 리포팅 ... 이냐?
	 *
	 * @return
	 */
	public static boolean getShowAllReports () {
		return getBoolean (DNAME_PREFERENCE, KEY_SHOW_ALL_REPORTS);
	}

}









