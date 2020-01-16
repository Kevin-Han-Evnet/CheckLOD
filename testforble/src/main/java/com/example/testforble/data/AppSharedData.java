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
package com.example.testforble.data;

import android.content.Context;
import android.content.SharedPreferences.Editor;


import com.example.testforble.BaseApplication;
import com.example.testforble.R;
import com.example.testforble.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
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
	public static final String KEY_SCANFILTER = "keyScanFilter";

    public static final String KEY_TRACKING_DEVICE_LIST = "trackingDeviceList";

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



    /** 트래킹 디바이스
     *
     * @param macid
     */
    public static ArrayList<String> addTrackingDevice (String macid) {


        String tmp = getString(DNAME_PREFERENCE, KEY_TRACKING_DEVICE_LIST, "");

        String[] tmp2 = tmp.split(",");

        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < tmp2.length; i++) {
            result.add(tmp2[i]);
        }

        //겹치지 않는다면 추가하고 리스트 리턴
        if (!result.contains(macid)) {
            tmp += StringUtils.isEmpty(tmp) ? macid : "," + macid;
            put (DNAME_PREFERENCE, KEY_TRACKING_DEVICE_LIST, tmp);

            result.add (macid);
        } else {

            result.remove(macid);
            result.add(macid);

            tmp = "";
            for (int i = 0; i < result.size(); i++) {
                tmp += (i > 0) ? "," + result.get(i) : result.get(i);
            }
            put (DNAME_PREFERENCE, KEY_TRACKING_DEVICE_LIST, tmp);

        }

        Collections.reverse(result);

        return result;
    }


    /** 트래킹 디바이스 삭제
     *
     * @param macid
     */
    public static void removeTrackingDevice (String macid) {

        String tmp = getString(DNAME_PREFERENCE, KEY_TRACKING_DEVICE_LIST, "");

        String[] tmp2 = tmp.split(",");

        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < tmp2.length; i++) {
            if (!macid.equals(tmp2[i])) result.add(tmp2[i]);
        }

        tmp = "";
        for (int i = 0; i < result.size(); i++) {
            tmp += (i > 0) ? "," + result.get(i) : result.get(i);
        }
        put (DNAME_PREFERENCE, KEY_TRACKING_DEVICE_LIST, tmp);
    }

    /** 트래킹 디바이스 얻기
     *
     * @return
     */
    public static ArrayList<String> getTrackingDeviceList () {
        String tmp = getString(DNAME_PREFERENCE, KEY_TRACKING_DEVICE_LIST, "");
        String[] tmp2 = tmp.split(",");
        String[] tmp3 = BaseApplication.getInstance().getResources().getStringArray(R.array.scan_target);

        ArrayList<String> result = new ArrayList<>();
		for (int i = 0; i < tmp3.length; i++) {
			if (!StringUtils.isEmpty(tmp3[i])) result.add(tmp3[i]);
		}

        for (int i = 0; i < tmp2.length; i++) {
            if (!StringUtils.isEmpty(tmp2[i])) result.add(tmp2[i]);
        }

        Collections.reverse(result);

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









