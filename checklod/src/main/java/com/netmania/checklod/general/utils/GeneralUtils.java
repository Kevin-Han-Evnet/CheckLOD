/*#############################################################
####
####	Develpment Information
####	
####	Developer : Kevin Han
####	
##############################################################*/

/*#############################################################
####
####	CashMallowUtils Class -- 걍 잡다구리한 함수들 모아놓은...
####	
##############################################################*/
package com.netmania.checklod.general.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.dto.BatteryInfoDto;
import com.netmania.checklod.general.dto.PhotoCompositinoItemDto;
import com.netmania.checklod.general.http.UpdateApp;
import com.netmania.checklod.general.manage.Constants;
import com.netmania.checklod.general.manage.DebugTags;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lib.netmania.ble.BleController;
import lib.netmania.data.dtos.BaseDto;


public class GeneralUtils {

	private static String TAG = "GenearlUtils";


	//네트워크 상태
	public static boolean isNetworkAvaliable(Context context) {

		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		/** 이제그만~*///LogUtil.I (TAG, "[NETWORK_INFO] 네트워크 타입 : " + wifi.getTypeName());
		/** 이제그만~*///LogUtil.I (TAG, "[NETWORK_INFO] 와이파이 연결 : " + wifi.isConnected ());
		/** 이제그만~*///LogUtil.I (TAG, "[NETWORK_INFO] 통신사 연결 : " + mobile.isConnected ());

		return (wifi.isConnected() || mobile.isConnected());
	}

	//네트워크 상태
	public static final int NETWORK_STATUS_NONE = 0;
	public static final int NETWORK_STATUS_WIFI = 1;
	public static final int NETWORK_STATUS_MOBILE = 2;

	public static int getNetworkStatus(Context context) {

		int result = 0;

		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		/*
	    LogUtil.I (TAG, "[NETWORK_INFO] 네트워크 타입 : " + wifi.getTypeName());
	    LogUtil.I (TAG, "[NETWORK_INFO] 와이파이 연결 : " + wifi.isConnected ());
	    LogUtil.I (TAG, "[NETWORK_INFO] 통신사 연결 : " + mobile.isConnected ());
	    */
		if (wifi.isConnected()) {
			result = NETWORK_STATUS_WIFI;
		} else if (mobile.isConnected()) {
			result = NETWORK_STATUS_MOBILE;
		} else {
			result = NETWORK_STATUS_NONE;
		}

		return result;
	}



	/** 배터리 상태 알아보자
	 *
	 * @param context
	 * @return
	 */
	public static String getBatteryStatus (Context context) {

		String result = "";

		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);

		// Are we charging / charged?
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
				status == BatteryManager.BATTERY_STATUS_FULL;


		if (isCharging) {
			// How are we charging?
			int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
			boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

			result += usbCharge ? "USB--충전중" : acCharge ? "ACC--충전중" : "기타--충전중";
		} else {
			result += "사용중";
		}

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float batteryPct = level / (float)scale;

		result += "-" + (batteryPct * 100.0f) + "%";

		return result;

	}


	/** 배터리 상태
	 *
	 * @param context
	 * @return
	 */
	public static BatteryInfoDto getBatteryStatusInfo (Context context) {

		BatteryInfoDto result = new BatteryInfoDto ();

		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);

		// Are we charging / charged?
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
				status == BatteryManager.BATTERY_STATUS_FULL;


		if (isCharging) {
			// How are we charging?
			int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			result.isRecharging = true;
			result.chagingType = chargePlug;

		} else {
			result.isRecharging = false;
		}

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float batteryPct = level / (float)scale;
		result.life =  (batteryPct * 100.0f);

		return result;

	}


	//네트워크 상태
	public static String getNetworkStatusInfo(Context context) {

		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		String statusInfo = "";

		statusInfo += "와이파이 사용가능? : " + wifi.isAvailable();
		statusInfo += "\nLTE사용가능? : " + mobile.isAvailable();
		statusInfo += "\n그럼뭘쓰는겨? : " + connMgr.getActiveNetworkInfo().getTypeName();
		/*statusInfo += "\n연결은 잘됐고? : " + connMgr.getActiveNetworkInfo().isConnected();
		statusInfo += "\n쓸만은 해? : " + connMgr.getActiveNetworkInfo().isAvailable();
		statusInfo += "\n상태는? : " + connMgr.getActiveNetworkInfo().getDetailedState();
		statusInfo += "\n네트워크이름 : " + connMgr.getActiveNetworkInfo().getExtraInfo().toString();
		statusInfo += "\n안되는이유 : " + connMgr.getActiveNetworkInfo().getReason();
		statusInfo += "\n사용중 : " + connMgr.getActiveNetworkInfo().getSubtypeName();*/


		return statusInfo;
	}


	//네트워크 상태
	public static String getNetworkStatusInfoForLog(Context context) {

		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		String statusInfo = "";

		JSONObject infoAsset = new JSONObject();
		JSONObject networkInfo = new JSONObject();
		JSONObject systemInfo = new JSONObject();
		try {
			networkInfo.put("wifi_available", StringUtils.convertBooleanToYn(wifi.isAvailable()));
			networkInfo.put("mobile_available", StringUtils.convertBooleanToYn(mobile.isAvailable()));

			String availableNetworkTypeName;
			String extraNetworkInfo;
			boolean isFuckingConnected;
			String fuckingConnectedStatus;
			String fuckingReason;

			if (connMgr.getActiveNetworkInfo() != null) {

				availableNetworkTypeName = connMgr.getActiveNetworkInfo().getTypeName();
				extraNetworkInfo = connMgr.getActiveNetworkInfo().getExtraInfo();
				isFuckingConnected = connMgr.getActiveNetworkInfo().isConnected();
				fuckingConnectedStatus = "NONE";
				fuckingReason = connMgr.getActiveNetworkInfo().getReason();

			} else {

				availableNetworkTypeName = "NONE";
				extraNetworkInfo = "NONE";
				isFuckingConnected = false;
				fuckingConnectedStatus = "NONE";
				fuckingReason = "NONE";

			}

			networkInfo.put("acvive_network_type", availableNetworkTypeName);
			networkInfo.put("active_network_name", extraNetworkInfo);
			networkInfo.put("is_connected", isFuckingConnected);
			networkInfo.put("connect_status", fuckingConnectedStatus);
			networkInfo.put("failed_reason", fuckingReason);


			//systemInfo 
			//systemInfo.put ("", System.get)


			infoAsset.put("networkInfo", networkInfo);
			infoAsset.put("systemInfo", systemInfo);


			statusInfo = infoAsset.toString();

		} catch (JSONException e) {
			statusInfo = "{}";
			e.printStackTrace();
		}

		statusInfo = networkInfo.toString();

		return statusInfo;
	}

	//메모리 상태 체크
	public static long getMemoryAvailable(Context context) {
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		long availableMegs = mi.availMem / 1048576L;
		return availableMegs;
	}


	/** 폰번호 반환
	 *
	 * @param context
	 * @return
	 */
	@SuppressLint("MissingPermission")
	public static String getPhoneNumber(Context context) {
		TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String telPhoneNo = "";
		try {
			telPhoneNo = telephony.getLine1Number(); //--> 권한 체크 앱 시작시 한다.
			telPhoneNo = telPhoneNo.replace ("+82", "0");
		} catch (Exception e) {
			//테스트 용 ---------------------------------------------------------------------------
            telPhoneNo = "*";
			//테스트 용 ---------------------------------------------------------------------------

			if (BaseApplication.phoneModel.equals("SM-A605K") && !Constants.IS_RELEASED) {
				telPhoneNo = "01063279830";
			}
		}


		LogUtil.I("[PHONE_NO] telPhoneNo = " + telPhoneNo);
		if ("*".equals(telPhoneNo)) {
            final String tmDevice, tmSerial, androidId;
            tmDevice = "" + telephony.getDeviceId();
            tmSerial = "" + telephony.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
            String deviceId = deviceUuid.toString();

            LogUtil.I("[PHONE_NO] device id --> " + deviceId);
        }
		
		return telPhoneNo;
	}


	/**
	 * 번호인증 SNS 발송
	 * @param ctx
	 * @param phoneNumber
	 * @param message
	 */
	public static void sendSMS(Context ctx, String phoneNumber, String message, double lat, double lng) {

		LogUtil.I("sendSMS ---> " + message + " --- " + lat + "E," + lng + "W");

		try {
			PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent("SMS_SENT"), 0);
			PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0, new Intent("SMS_DELIVERED"), 0);

			SmsManager sms = SmsManager.getDefault();
			StringBuffer smsBody = new StringBuffer();

			sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

			/*
			smsBody.append("http://maps.google.com?q=");
			smsBody.append(lat);
			smsBody.append(",");
			smsBody.append(lng);
			*/

			smsBody.append("http://tnt.kevinhan.me/tnt/apis/show_map.php?lat=");
			smsBody.append(lat);
			smsBody.append("&lng=");
			smsBody.append(lng);

			sms.sendTextMessage(phoneNumber, null, smsBody.toString(), sentPI, deliveredPI);

		} catch (Exception e) {
			LogUtil.E("sendSMS ---> " + e.toString());
		}
	}
	
	/**
	 * 어플 버전 정보 반환
	 * @param context
	 * @return
	 */
	public static String getAppVersion(Context context) {
		String version = "";
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}
	
	/**
	 * 어플이 백그라운드에 있는지 여부 반환
	 * @param context
	 * @return
	 */
	public static boolean isApplicationBroughtToBackground (Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningTaskInfo> tasks = am.getRunningTasks(1);
	    if (!tasks.isEmpty()) {
	        ComponentName topActivity = tasks.get(0).topActivity;
	        
	        if (topActivity.getPackageName().equals(context.getPackageName())) {
	            return false;
	        }
	    }

	    return true;
	}
	
	/**
	 * 스크린이 꺼져있느냐?
	 * @param context
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static boolean isScreenOn (Context context) {
		PowerManager pm = (PowerManager) context.getSystemService (Context.POWER_SERVICE);
		return pm.isScreenOn ();
	}
	
	//facebook
	public static String getFaceBookHashKey(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName().toString(),
					PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				LogUtil.D("HASHKEY",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
				return Base64.encodeToString(md.digest(), Base64.DEFAULT);
			}
		} catch (NameNotFoundException e) {

		} catch (NoSuchAlgorithmException e) {

		}
		return null;
	}
	
	/**
	 * 전화번호 있는지 검색.
	 * @param context
	 * @param number
	 * @return
	 */
	public static String getContactDisplayNameByNumber (Context context, String number) {
	    Uri uri = Uri.withAppendedPath (ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	    String name = "";

	    ContentResolver contentResolver = context.getContentResolver();
	    Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
	            ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

	    try {
	        if (contactLookup != null && contactLookup.getCount() > 0) {
	            contactLookup.moveToNext();
	            name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
	            //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
	        }
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }

	    return name;
	}
	
	
	//기타함수
	public static float DptoPixel(float density, float dp) {
		return dp*(density/160f);
	}

	public static float PixelToDp(float density, float px) {
		return px/(density/160f);
	}

	public static float dipToPixels (DisplayMetrics metrics, float dipValue) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
	}
	




	//config data 확인
	public static void checkDtoData (BaseDto t) {

		for (Field param:t.params()) {
			try {
				LogUtil.I ("[" + t.getClass().getSimpleName() + "] " + param.getName() + " = " + param.get (t));
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	//config data 확인
	public static void checkDtoData (String tag, BaseDto t) {

		for (Field param:t.params()) {
			try {
				LogUtil.I (tag, "[" + t.getClass().getSimpleName() + "] " + param.getName() + " = " + param.get (t));
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}







	//캐시 방지용 타임코드
	public static String getTimeCode () {
		Date tDate = new Date();
		return Long.toString(tDate.getTime());
	}

	
	
	//로컬 웹페이지 주소 얻기
	public static String getLocalFileURL (Context context, Uri uri) {

        StringBuffer sbb = new StringBuffer();
        try {
            InputStream is = context.getContentResolver().openInputStream (uri);

            CharBuffer cb = CharBuffer.allocate(1024);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            cb.clear();
            while (-1 != br.read(cb)) {
                cb.flip();
                sbb.append(cb);
                cb.clear();
            }
        } catch (FileNotFoundException e) {
            /** 이제그만~*///LogUtil.E ("FileNotFoundException");
            
        } catch (IOException e) {
            /** 이제그만~*///LogUtil.E ("IOException");
        }
        
        return sbb.toString();
    }
	
	
	/** 미디어 파일 브라우저 (갤러리, 파일탐색기 등) 갱신
	 * @param context
	 * @param path
	 */
	public static void reloadMediaFiles (Context context, String path) {
		MediaScannerConnection.scanFile (context.getApplicationContext(), new String[]
				{path}, null, new MediaScannerConnection.OnScanCompletedListener() {

			public void onScanCompleted(String path, Uri uri) {
				// nothing yet;
			}

		});
	}


	/** 아이콘에 뱃지 달아줘.
	 *
	 * @param context
	 * @return
	 */
	public static String getLauncherClassName (Context context) {
	    PackageManager pm = context.getPackageManager();
	 
	    Intent intent = new Intent(Intent.ACTION_MAIN);
	    intent.addCategory(Intent.CATEGORY_LAUNCHER);
	 
	    List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
	    for (ResolveInfo resolveInfo : resolveInfos) {
	        String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
	        if (pkgName.equalsIgnoreCase(context.getPackageName())) {
	            String className = resolveInfo.activityInfo.name;
	            return className;
	        }
	    }
	    return null;
	}


	/** 뱃지 아이콘이요
	 *
	 * @param context
	 * @param notiCnt
	 */
	public static void showBadgeOnIcon (Context context, int notiCnt) {

		
	    Intent badgeIntent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
	    badgeIntent.putExtra("badge_count", notiCnt);
	    badgeIntent.putExtra("badge_count_package_name", context.getPackageName());
	    badgeIntent.putExtra("badge_count_class_name", getLauncherClassName (context));
	    context.sendBroadcast(badgeIntent);
	}


	/** 사용중인 메모리 리턴
	 *
	 * @return
	 */
	public static int getUsedMemoryRatio () {
		Runtime runtime = Runtime.getRuntime();
		int availHeapSizeRatio = (int) Math.round ((((double) runtime.totalMemory() - (double) runtime.freeMemory()) / (double) runtime.totalMemory()) * 100.0);
		return availHeapSizeRatio;
	}


	/** 스토리지 용량 리포트
	 *
	 * @param f
	 * @return
	 */
	public static float getFreeStorageSize (File f) {
		StatFs stat = new StatFs(f.getPath());
		long bytesAvailable = 0;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
			bytesAvailable = (long) stat.getBlockSizeLong() * (long) stat.getAvailableBlocksLong();
		else
			bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
		return bytesAvailable / (1024.f * 1024.f);
	}






	/**
	 * @return Number of bytes available on External storage
	 */
	public static long getAvailableSpaceInBytes() {
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();

		return availableSpace;
	}


	/**
	 * @return Number of kilo bytes available on External storage
	 */
	public static long getAvailableSpaceInKB(){
		final long SIZE_KB = 1024L;
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		return availableSpace/SIZE_KB;
	}
	/**
	 * @return Number of Mega bytes available on External storage
	 */
	public static long getAvailableSpaceInMB(){
		final long SIZE_KB = 1024L;
		final long SIZE_MB = SIZE_KB * SIZE_KB;
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		return availableSpace/SIZE_MB;
	}

	/**
	 * @return Number of gega bytes available on External storage
	 */
	public static long getAvailableSpaceInGB(){
		final long SIZE_KB = 1024L;
		final long SIZE_GB = SIZE_KB * SIZE_KB * SIZE_KB;
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		return availableSpace/SIZE_GB;
	}



	/** 해시키 출력
	 *
	 * @param context
     */
	public static void getAppKeyHash (Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;

				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encode(md.digest(), 0));
				LogUtil.I("Hash key", "hash key = " + something);
			}
		}
		catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			LogUtil.E("name not found", e1.toString());
		}

		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			LogUtil.E("no such an algorithm", e.toString());
		}
		catch (Exception e){
			LogUtil.E("exception", e.toString());
		}

	}


	/** 히히
	 *
	 * @param millitime
	 * @return
     */
	public static String convertTime(long millitime){

		//today
		//target
		Calendar target = Calendar.getInstance();
		target.setTimeInMillis(millitime);

		Calendar today = Calendar.getInstance();

		if( target.get(Calendar.YEAR) == today.get(Calendar.YEAR) && target.get(Calendar.MONTH) == today.get(Calendar.MONTH) && target.get(Calendar.DATE) == today.get(Calendar.DATE)  ){

			if( target.get(Calendar.HOUR_OF_DAY) == today.get(Calendar.HOUR_OF_DAY)  ){
				return (today.get(Calendar.MINUTE) - target.get(Calendar.MINUTE)) + " 분전" ;
			}else{
				return (today.get(Calendar.HOUR_OF_DAY) - target.get(Calendar.HOUR_OF_DAY)) + " 시간전" ;
			}

		}else{

			int month = target.get(Calendar.MONTH)+1;
			int date = target.get(Calendar.DATE);

			return target.get(Calendar.YEAR)+"."+( month<10?"0"+month:month )+"."+( date<10?"0"+date:date );
		}

	}

	/** 히히
	 *
	 * @param millitime
	 * @return
	 */
	public static String convertTime2 (long millitime) {

		Calendar t = Calendar.getInstance();
		long timeMillis = t.getTimeInMillis();
		String currentDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(new Date(timeMillis));

		return currentDateStr;

	}

	/** 아이디 찾아라
	 *
	 * @param context
	 * @param pVariableName
	 * @param pResourcename
	 * @param pPackageName
     * @return
     */
	public static int getResourceId(Context context, String pVariableName, String pResourcename, String pPackageName) {
		try {
			return context.getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}


	/** 알파버전 어플리케이션 업데이트
	 *
	 * @param context
	 * @param version
	 */
	public static void updateAlphaApplication (Context context, String version, String code, String appUpdateURL){

		String fName = "CheckLOD-DEV-" + version.substring (1) + "-" + code + ".apk";
		appUpdateURL = appUpdateURL + fName;

		final ProgressDialog t = DialogUtils.progress (context, 100, "자동 업데이트", "업데이트 파일을 다운로드 합니다", false);

		UpdateApp updater = new UpdateApp();
		updater.setContext (context.getApplicationContext(), new UpdateApp.UpdateAppListener() {

			@Override
			public void onProgress(int current) {
				t.setProgress (current);
			}

			@Override
			public void onComplete() {
				t.dismiss();
			}

		});

		updater.execute (appUpdateURL);

	}


	/** 짐 돌고 있는 서미스냐????
	 *
	 * @param context
	 * @param cls
	 * @return
	 */
	public static boolean isRunningService(Context context, Class<?> cls) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (cls.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/** 짐 돌고있는 서비스 인스턴스 내놔봐
	 *
	 * @param context
	 * @param cls
	 * @return
	 */
	public static ActivityManager.RunningServiceInfo getRunnsingServiceInstance (Context context, Class<?> cls) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (cls.getName().equals(service.service.getClassName())) {
				return service;
			}
		}
		return null;
	}


	/** 니홈화면 누구???
	 *
	 * @return
	 */
	public static boolean isMyAppLauncherDefault (String packageName) {
		final Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		final ResolveInfo res = BaseApplication.getInstance().getPackageManager().resolveActivity(intent, 0);
		if (res.activityInfo != null && packageName.equals(res.activityInfo.packageName)) {
			return true;
		}
		return false;
	}



	/** 사이즈 조정한 파일
	 *
	 * @param file
	 * @return
	 */
	public static File getFile (File file) {

		//비트맵 가공
		File sendfile = null;
		try {
			File tempfile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), file.getName());

			BitmapFactory.Options size_option = new BitmapFactory.Options();
			size_option.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file.getAbsolutePath(), size_option);

			int big_scale = Math.max(size_option.outWidth, size_option.outHeight);
			big_scale /= 2048;
			big_scale += 1;

			Bitmap bmp = null;
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = big_scale;
			bmp = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

			ExifInterface exif = new ExifInterface(file.getAbsolutePath());
			int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			int exifDegree = BitmapUtils.exifOrientationToDegrees(exifOrientation);

			Matrix m = new Matrix();
			m.setRotate(exifDegree, (float) bmp.getWidth(), (float) bmp.getHeight());
			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);


			FileOutputStream out = new FileOutputStream(tempfile);
			bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			bmp.recycle();

			sendfile = tempfile;

		} catch (Throwable th) {
			sendfile = file;
		}

		return sendfile;

	}


	/** 업로드에 사용한 갤러리의 임시파일 삭제
	 *
	 * @param files
	 */
	public static void deleteTempFiles (ArrayList<PhotoCompositinoItemDto> files) {
		for (int i = 0; i < files.size(); i++) {
			getFile(files.get(i).file).delete();
			//addImageGallery (getFile(files.get(i).file));
		}
	}

	/** 걍 확인차....
	 *
	 * @param file
	 */
	public static void addImageGallery(File file) {
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // or image/png
		BaseApplication.getInstance().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}



	/** 어플리케이션 설치 정보 보기 =
	 *
	 * @param context
	 * @param packageName
	 */
	private static final String SCHEME = "package";
	private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
	private static final String APP_PKG_NAME_22 = "pkg";
	private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
	private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";
	public static void showInstalledAppDetails (Context context, String packageName) {
		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) { // above 2.3
			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts(SCHEME, packageName, null);
			intent.setData(uri);
		} else { // below 2.3
			final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
					: APP_PKG_NAME_21);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(APP_DETAILS_PACKAGE_NAME,
					APP_DETAILS_CLASS_NAME);
			intent.putExtra(appPkgName, packageName);
		}
		context.startActivity(intent);
	}
}




