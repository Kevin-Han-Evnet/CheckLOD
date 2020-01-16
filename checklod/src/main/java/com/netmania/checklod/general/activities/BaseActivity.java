/*#############################################################
####
####	Develpment Information
####	
####	Developer : Kevin Han
####	
##############################################################*/

/*#############################################################
####
####	BaseActivity -- 바탕 액티비티
####	
##############################################################*/
package com.netmania.checklod.general.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;
import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.R;
import com.netmania.checklod.general.data.AppSharedData;
import com.netmania.checklod.general.dto.APIResutDtoFailed;
import com.netmania.checklod.general.dto.ConfigDto;
import com.netmania.checklod.general.dto.DriverInfoDto;
import com.netmania.checklod.general.http.BaseAPI;
import com.netmania.checklod.general.http.GeneralAPI;
import com.netmania.checklod.general.manage.Constants;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.DialogUtils;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.general.utils.RecycleUtils;
import com.netmania.checklod.general.utils.StringUtils;

import java.util.Map;

;


public class BaseActivity extends FragmentActivity implements TextToSpeech.OnInitListener {
    
    public static final String TAG = "BaseActivity";

	protected BaseActivity mActivity;
	public Handler mHandler;
	public LayoutInflater mInflater;
	protected Toast mToast;
	protected int keyboardHeight;
	
	public BaseApplication mApp;
	public DisplayMetrics mDisplayMetrics;
	public static int DENSITY = -1;

	//키보드 제어
	protected InputMethodManager mSoftKeyboard;

	
	public boolean isPaused = false;
	protected boolean isLongclicked;
	public boolean transitionEnd = false;

	public String DATA_IP;

	private ViewGroup mRootView;
	private View lockCover;
	protected boolean isLocked = false;


	/** ------------------------------------------------------------------------------------------------------------------------ */
	public DriverInfoDto DVIF;
	public ConfigDto CONFIG;



	private static final int LOW_DPI_STATUS_BAR_HEIGHT = 19;
	private static final int MEDIUM_DPI_STATUS_BAR_HEIGHT = 25;
	private static final int HIGH_DPI_STATUS_BAR_HEIGHT = 38;


	/** for skin */
	protected int app_theme;
	protected int topper_logo;
	protected int title_image_cut;
	protected int color_001;
	protected int color_002;
	protected int color_003;
	protected int color_004;


	/** 센서 인잇
     *
	 */
	protected SensorManager mSensorManager;
	protected Sensor mProximitySensor;
	protected Sensor mLightSensor;



	private TextToSpeech mTTS;

	
	
	@Override
	public void onCreate (Bundle savedInstanceState) {



		isLongclicked = false;

		StrictMode.enableDefaults(); //데이타 주고 받아야지?
		
		super.onCreate(savedInstanceState);
		
		mApp = BaseApplication.getInstance();
		mApp.addActivity(this);
		mActivity = this;
		mInflater = LayoutInflater.from(mActivity);
		mToast = Toast.makeText(mActivity, "", Toast.LENGTH_SHORT);

		handlerEvent(mHandler);
		mDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		if(DENSITY == -1) {
			DENSITY = mDisplayMetrics.densityDpi;
		}
		
		
		mSoftKeyboard = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

		
		/** ---------------------- 유저액션로그 ---------------------------------- */
		LogUtil.I(DebugTags.TAG_LIFE_CYCLE, getExtendedClassName() + ":onCreate ();");
		/** ---------------------- 끝 ---------------------------------- */
		
		keyboardHeight = (int) GeneralUtils.DptoPixel(DENSITY, 300);


		mRootView = ((ViewGroup) this.findViewById(android.R.id.content));

		/** 락 커버 관련 */
		/*lockCover = mInflater.inflate(R.layout.layout_black, null);
		lockCover.setOnClickListener(lockScreenOnClick);*/



		//미친짓 금지 --- 액티비티 스타트하자마나 뒤로가기 스타트 뒤로가기 존나 미친짓 막기.
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				transitionEnd = true;
			}
		}, 500);


		setBaseInfo();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			try {
				mTTS = new TextToSpeech(mActivity, this);
			} catch (Exception e) {
				//nothign yet;
			}
		}

		isLocked = false;

	}


	/** 센서 이니셜라이징
	 *
	 */
	protected void initSensor () {

		try {
			mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

			mSensorManager.registerListener(sensorListener, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
			//mSensorManager.registerListener(sensorListener, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/** 상태바 높이
	 *
	 */
	public int getStatusBarSizeOnCreate (){
		((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getMetrics(mDisplayMetrics);

		int statusBarHeight;

		switch (mDisplayMetrics.densityDpi) {

			case DisplayMetrics.DENSITY_HIGH:
				statusBarHeight = HIGH_DPI_STATUS_BAR_HEIGHT;
				break;
			case DisplayMetrics.DENSITY_MEDIUM:
				statusBarHeight = MEDIUM_DPI_STATUS_BAR_HEIGHT;
				break;
			case DisplayMetrics.DENSITY_LOW:
				statusBarHeight = LOW_DPI_STATUS_BAR_HEIGHT;
				break;
			default:
				statusBarHeight = MEDIUM_DPI_STATUS_BAR_HEIGHT;
		}

		return statusBarHeight;
	}



	/** 기본정보 고고
	 *
	 */
	public void setBaseInfo () {
		DVIF = AppSharedData.getDriverInfo();
		CONFIG = AppSharedData.getConfigInfo();

	}





	/** TextToSpeach init listener
	 *
	 * @param status
	 */
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {

			int result = mTTS.setLanguage(Constants.CURRENT_LOCALE);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				LogUtil.E ("This Language is not supported");
			} else {
				//mHandler.postDelayed(letMeKnow, Constants.TOUCH_CYCLE); --> 하지말자..
			}

		} else {
			LogUtil.E ("TTS", "Initilization Failed!");
		}
	}



	/** 말해 이새퀴
	 *
	 */
	public void speakOut (String msg) {
		mTTS.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
	}



	/** 기본정보 현재상태로 저장.
	 *
	 */
	public void saveBaseInfo () {
		if (DVIF != null) {
			AppSharedData.setDriverInfo(DVIF);
		}

		if (CONFIG != null) {
			AppSharedData.setConfigInfo(CONFIG);
		}
	}



	
	//오버라이드 하숑.
	private String getExtendedClassName () {
		return getClass ().getSimpleName();
	}
	
	@Override
	public void startActivity(Intent intent) {
		/** 이제그만~*///LogUtil.I (TAG, "startActivity ();");

		if (!transitionEnd) return;

		//long click 시 linkfy 작동하지 않기.
		if (TextUtils.equals (intent.getAction(), Intent.ACTION_VIEW) && isLongclicked) {
	        //링크 실행하지 마숑.
		} else {

	    	super.startActivity (intent);
	    }
		
		/** ---------------------- 유저액션로그 ---------------------------------- */
		LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getExtendedClassName () + ":startActivity ();");
		/** ---------------------- 끝 ---------------------------------- */

		isLongclicked = false; //초기화
	}

	@Override
	protected void onResume() {
		/** ---------------------- 유저액션로그 ---------------------------------- */
		LogUtil.I(DebugTags.TAG_LIFE_CYCLE, getExtendedClassName() + ":onResume ();");
		/** ---------------------- 끝 ---------------------------------- */

		super.onResume();
		setBaseInfo();
	}
	
	@Override
	protected void onRestart() {
		/** ---------------------- 유저액션로그 ---------------------------------- */
		LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getExtendedClassName () + ":onRestart ();");
		/** ---------------------- 끝 ---------------------------------- */
		super.onRestart();
	}
	
	@Override
	protected void onPause () {
		/** ---------------------- 유저액션로그 ---------------------------------- */
		LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getExtendedClassName () + ":onPause ();");
		/** ---------------------- 끝 ---------------------------------- */
		
		isPaused = true;
		
		super.onPause();
	}
	
	@Override
	protected void onStop () {
		
		/** ---------------------- 유저액션로그 ---------------------------------- */
		LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getExtendedClassName () + ":onStop ();");
		/** ---------------------- 끝 ---------------------------------- */
		
		//런쳐 아이콘 뱃지 갱신
		GeneralUtils.showBadgeOnIcon(mApp.getApplicationContext(), 0);
		
		super.onStop();
	}

	
	@Override
	protected void onDestroy() {
		
		/** ---------------------- 유저액션로그 ---------------------------------- */
		LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getExtendedClassName () + ":onDestroy ();");
		/** ---------------------- 끝 ---------------------------------- */
		
		LogUtil.I (TAG, "onDestroy ();");
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		mApp.removeActivity(this);

		try {
			unbindDrawables(mRootView);
			super.onDestroy();
			//System.gc();
			//Runtime.getRuntime().gc();
		} catch (Exception e) {
			//nothign yet;
		}

	}

	@Override
	public void onBackPressed() {

		/** ---------------------- 유저액션로그 ---------------------------------- */
		LogUtil.I(DebugTags.TAG_LIFE_CYCLE, getExtendedClassName () + ":onBackPressed ();");
		/** ---------------------- 끝 ---------------------------------- */
		if (transitionEnd) {
			super.onBackPressed();
		}
	}

	/** 메모리 해제
	 *
	 * @param view
     */
	protected void unbindDrawables (View view) {
		if (view.getBackground() != null)
		{
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup && !(view instanceof AdapterView))
		{
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
			{
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}
	
	protected void handlerEvent(Handler handler) {
		if (null == mHandler) {
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					handlerCustomWork(msg);
				}

			};
		}
	}

	protected void handlerCustomWork(Message msg) {
		
	}
	
	//키보드 숨기기
	public void hideKeyboard (View src, int option) {
		mSoftKeyboard.hideSoftInputFromWindow(src.getWindowToken(), option);
	}
	
	//키보드 보이기
	public void showKeyBoard (View src, int option) {
		mSoftKeyboard.showSoftInput(src, option);
	}


	/** 버전체크
	 *
	 */
	protected void checkAndGo () {

		/** 셋팅 오버라이딩 */
		String code = getResources().getString(Constants.TEST_DIV_CODE);
		GeneralAPI.getConfig(
				mActivity,
				false,
				code,
				new BaseAPI.ApiMapListenerWithFailedRest() {
					@Override
					public void onComplete() {
						//nothing yet;
					}

					@Override
					public void onComplete(Map<String, Object> map) {

						ConfigDto resultBody = (ConfigDto) map.get(BaseAPI.RESULT_DTO);

						AppSharedData.setConfigInfo(resultBody);
						setBaseInfo();

						checkFinalVersionAndLaunch (resultBody.version);

					}

					@Override
					public void onFailed(AjaxStatus result) {
						checkFinalVersionAndLaunch (GeneralUtils.getAppVersion (mActivity));
					}

					@Override
					public void onFailed(AjaxStatus result, Map<String, Object> map) {

						switch (result.getCode()) {

							case BaseAPI.REST_NOT_FOUND :

								APIResutDtoFailed resultBody = (APIResutDtoFailed) map.get(BaseAPI.RESULT_DTO);

								DialogUtils.alert(
										mActivity,
										resultBody.reason,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												mApp.finishAll();
											}
										}, false
								);

								break;


							default :
							case BaseAPI.REST_INTERNAL_SERVER_ERROR :
								checkFinalVersionAndLaunch (GeneralUtils.getAppVersion (mActivity));
								break;


						}

					}
				}
		);
	}


	 /**최신버전 체크
	 *
	 * @param finalVersion
	 */
	 protected void checkFinalVersionAndLaunch (String finalVersion) {

		//우선 메인 버전
		final int mAppNewVersion = StringUtils.getVersionNum (finalVersion);
		final int mNowVersion = StringUtils.getVersionNum (GeneralUtils.getAppVersion (mActivity));

		LogUtil.I (DebugTags.TAG_VERSION_CHECK, "[VERSION] 비교!! 최신버전 = " + mAppNewVersion);
		LogUtil.I (DebugTags.TAG_VERSION_CHECK, "[VERSION] 비교!! 지금버전 = " + mNowVersion);



		//일단 버전 체크부터해야지.
		if (mAppNewVersion > mNowVersion) {

			DialogUtils.confirm (mActivity, "최신버전이 아닙니다.", "업데이트 하시겠습니까?", "지금하기", "나중에하기",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();

							//업데이트
							GeneralUtils.updateAlphaApplication (
									mActivity, String.valueOf (mAppNewVersion),
									getResources().getString(Constants.TEST_DIV_CODE),
									Constants.getBaseURL(Constants.URL_CATEGORY_CONFIG) + "alpha_down/"
							);
						}

					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();

							mApp.finishAll();
						}

					}, false);

		} else {
			checkDriverAndGo();
		}
	}

	/** 오버라이드 해서 써용..
	 *
	 */
	protected void checkDriverAndGo (){
		String phoneNo = GeneralUtils.getPhoneNumber(mApp.getApplicationContext());


		GeneralAPI.getDeliveryDriverInfo(
				mActivity,
				true,
				phoneNo,
				new BaseAPI.ApiMpaListenerWithFailedRestGet() {
					@Override
					public void onSuccess(int statusCode, Map<String, Object> map) {

						DriverInfoDto driverinfo = (DriverInfoDto) map.get(BaseAPI.RESULT_DTO);
						AppSharedData.setDriverInfo(driverinfo);
						setBaseInfo();

						loadDeviceListAndGo();

					}

					@Override
					public void onFailure(int statusCode) {

						switch (statusCode) {

							default :
							case BaseAPI.REST_CONNECTION_FAILED :

								if (DVIF != null) {
									loadDeviceListAndGo();
								} else {
									DialogUtils.alert(
											mActivity,
											R.string.error_network,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialogInterface, int i) {
													mApp.finishAll();
												}
											}, false
									);
								}



								break;

							case BaseAPI.REST_NOT_FOUND :

								if (DVIF != null) {
									loadDeviceListAndGo();
								} else {
									DialogUtils.alert(
											mActivity,
											R.string.alert_driver_not_found,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialogInterface, int i) {
													mApp.finishAll();
												}
											}, false
									);
								}



								break;

						}
					}
				}
		);
	}

	/** 오버라이드용
	 *
	 */
	protected void loadDeviceListAndGo () {
		//nothign yet;
	}




	/** 네트워크 체크 앤 종료
	 *
	 * @param quit
     */
	public void forcedFinish (final boolean quit) {
		DialogUtils.alert(
				mActivity,
				R.string.error_network,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (quit) mApp.finishAll();
					}
				}, false
		);
	}

	//리스너 -------------------------------------------------------------------------------------------------------------------------
	View.OnClickListener lockScreenOnClick = new View.OnClickListener() {

		@Override
		public void onClick(View src) {
			//nothing yet;
		}
	};



	/** 센서 리스너
	 *
	 */
	protected SensorEventListener sensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent sensorEvent) {
			//nothing yet;
			LogUtil.I("[SENSOR] onSensorChanged.... ---> " + sensorEvent.sensor.getPower());
			LogUtil.I("[SENSOR] onSensorChanged.... ---> " + sensorEvent.accuracy);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int i) {
			LogUtil.I("[SENSOR] onAccuracyChanged.... ---> " + sensor.getName() + " --- " + i);
		}
	};

	/** ------------------------------------------------------------------------------------------------------------------------ */
}
