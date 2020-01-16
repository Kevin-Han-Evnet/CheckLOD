package com.netmania.checklod.general;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.netmania.checklod.general.data.CustomLruCache;
import com.netmania.checklod.general.data.DBHelper;
import com.netmania.checklod.general.dto.HistoryStackInfoDto;
import com.netmania.checklod.general.service.HttpProcessService;
import com.netmania.checklod.general.service.HttpServiceMonitor;
import com.netmania.checklod.general.service.SensingProcessService;
import com.netmania.checklod.general.service.SensingServiceMonitor;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.LogUtil;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import lib.netmania.data.DataController;

//import android.support.multidex.MultiDexApplication;


public class BaseApplication extends Application {
	/** description
	 * @param int currentAPIVersion		----------------------------	API 레벨
	 * @param String phoneModel			----------------------------	폰 모델 명
	 * @param String androidVersion		----------------------------	OS 버전
	 * @param boolean DEVELOPER_MODE	----------------------------	개발버전
	 */
	//버전 관련 데이타 ================================================================= 
	public static final int currentAPILevel = android.os.Build.VERSION.SDK_INT;
	public static final String buildId = android.os.Build.ID;
	public static final String dalvicVersion = android.os.Build.BOOTLOADER;
	public static final String phoneModel = android.os.Build.MODEL;
	public static final String androidVersion = android.os.Build.VERSION.RELEASE;
	public String DB_PATH;
	//==============================================================================

	//==========================================
    public static final String TAG = "BaseApplication";
	public String PICASSO_CACHE_PATH;

	
	public Handler bHandler;
    public List<Activity> mActivityStack;

	private static BaseApplication mInstance;
	private static Context mAppContext;

	//DB
	private SQLiteDatabase mWritableDB;

	//data controller
	private DataController dataController;

	/** private SensingServiceMonitor sensingServiceMonitor; */
	/** private HttpServiceMonitor httpServiceMonitor; */

	//피카소
	public Picasso dPicasso;
	public OkHttpDownloader dDownloader;
	public CustomLruCache dPicassoCache;



	/** BLE 리스너 */
	private SensingServiceMonitor sensingServiceMonitor;
	private HttpServiceMonitor httpServiceMonitor;




	/** 권한 관련  ----------------------------------------------------------------- */





	/** 히스토리 스택
     * @return
     */
	public List<Activity> getActivityStack() {
    	return this.mActivityStack;
    }
	
	/** 히스토리 스택에 액티비티 추가
	 * @param act
	 */
    public void addActivity(Activity act) {
    	if (mActivityStack == null) mActivityStack = new ArrayList<Activity>();
    	mActivityStack.add(act);
    }
    
    /** 히스토리 스택에서 액티비티 삭제
     * @param act
     */
    public void removeActivity(Activity act) {
    	if (mActivityStack != null) {
    		mActivityStack.remove(act);
    	}
    }
    
    /** 최상단 태스크 반환
     * @return
     */
    public Activity getTaskOnTop () {
    	Activity result = null;
    	
    	if (mActivityStack != null && mActivityStack.size () > 0) {
    		result = mActivityStack.get (getMaxStackIdx());
    	}
    	
    	return result;
    }

	/** 바로 아래 태스크 반환
	 * @return
	 */
	public Activity getTaskOnBack () {
		Activity result = null;

		if (mActivityStack != null && mActivityStack.size () > 1) {
			result = mActivityStack.get (getMaxStackIdx() - 1);
		}

		return result;
	}
    
    /** 원하는 액티비티 인스턴스 정보 얻기
     * @param target
     * @return
     */
    public HistoryStackInfoDto getStackInfo (Class target) {
    	return getStackInfo (target, "");
    }
    
    /** 원하는 액티비티 인스턴스 정보 얻기
     * @param target
     * @param room_id
     * @return
     */
    public HistoryStackInfoDto getStackInfo (Class target, String room_id) {
    	
    	HistoryStackInfoDto result = new HistoryStackInfoDto ();
    	
    	if (mActivityStack != null && mActivityStack.size() > 0) {
	    	for(int i = 0; i < mActivityStack.size (); i++) {
	    		if (target.equals (mActivityStack.get (i).getClass ())) {
	    			result.is_open = true;
	    			result.activity_instance = mActivityStack.get (i);
	    			result.stack_idx = i;
	    			break;
	    		}
	    	}
    	}

    	
    	return result;
    }
    
    /** 최상단 스택 idx
     * @return
     */
    public int getMaxStackIdx () {
    	int result = 0;
    	
    	if (mActivityStack != null && mActivityStack.size() > 0) {
	    	result = mActivityStack.size() - 1;
    	}
    	
    	return result;
    }


    /** 나 몇개?
	 *
	 * @return
     */
	public int getStackCount () {
		int count = (mActivityStack != null) ? mActivityStack.size() : 0;
		return count;
	}


    public void finishAll () {

    	if (mActivityStack != null && mActivityStack.size() > 0) {
	    	for(Activity act : mActivityStack) {
	    		act.finish ();
	    	}
	    	mActivityStack.clear();
    	}
    }
    
    @Override
    public void onCreate() {
        super.onCreate();

		/*StrictMode.setThreadPolicy( new StrictMode.ThreadPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.penaltyDialog()
				.build());

		StrictMode.setVmPolicy( new StrictMode.VmPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.build());*/


		bHandler = new Handler();


        
        //SD 카드에 디렉토리 생성
		PICASSO_CACHE_PATH = "/data/data/" + getPackageName() + "/chaches/";


		mInstance = this;
		this.setAppContext(getApplicationContext());


		//DB
		try {
			DBHelper.DatabaseHelper dbHelper = DBHelper.newInstance (getApplicationContext());
			mWritableDB = dbHelper.getWritableDatabase();
		} catch(Exception e){
			LogUtil.E ("DBHelper initialize failed...");
			e.printStackTrace ();
		}



		startSensingProcessService(); //--> 센싱 시작 할께요~~
		startHttpProcessService();    //--> 전송도 시작합니다~~


		//죽기전에 좀...
		//if (Constants.IS_RELEASED) Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(getBaseContext(), getContentResolver(), Thread.getDefaultUncaughtExceptionHandler()));
    }




	@Override
	public void onLowMemory() {
		super.onLowMemory();
		LogUtil.E("GlobalApplication onLowMemory------------------");
		//finishAll ();
	}
    
    
    @Override
    public void onTerminate() {
        super.onTerminate();

		LogUtil.E ("GlobalApplication onTerminate");
		try {
			if (mWritableDB != null && mWritableDB.isOpen()) {
				mWritableDB.close();
			}
		} catch (Exception e) {
			LogUtil.E (e.getMessage());
		}
    }



	public static BaseApplication getInstance (){
		return mInstance;
	}


	public static Context getAppContext() {
		return mAppContext;
	}


	public void setAppContext (Context mAppContext) {
		BaseApplication.mAppContext = mAppContext;
	}
    
    
 	//죽기전에.... 좀..
    public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {
    	private Context mContext;
    	private ContentResolver mContentResolver;
    	private UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    	public MyUncaughtExceptionHandler(Context context , ContentResolver contentResolver , UncaughtExceptionHandler uncaughtExceptionHandler){
    		this.mContext = context;
    		this.mContentResolver = contentResolver;
    		this.mDefaultUncaughtExceptionHandler = uncaughtExceptionHandler;
    	}

    	@Override
    	public void uncaughtException (final Thread _thread, final Throwable _throwable) {
    		final Writer result = new StringWriter();
    		final PrintWriter printWriter = new PrintWriter(result);
    		_throwable.printStackTrace( printWriter );
    		String stacktrace = result.toString();


			printWriter.close();
			//일단 둡시다...

			if(Thread.currentThread() == Looper.getMainLooper().getThread()) {
				finishAll ();
				android.os.Process.killProcess ( android.os.Process.myPid() );
				System.exit(10);
			}

    	}
	}

	public SQLiteDatabase getWritableDatabase() {
		return mWritableDB;
	}


	/** 센싱 서비스 시작
	 *
	 */
	public void startSensingProcessService () {
		sensingServiceMonitor = SensingServiceMonitor.getInstance ();
		if (!sensingServiceMonitor.isMonitoring()) {
			sensingServiceMonitor.startMonitoring(getApplicationContext());
		}
	}

	/** 센싱 서비스 종료
	 *
	 */
	public void stopSensingProcessService () {
		sensingServiceMonitor = SensingServiceMonitor.getInstance ();
		if (sensingServiceMonitor.isMonitoring()) {
			sensingServiceMonitor.stopMonitoring(this);
		}

		try {
			stopService(new Intent(getApplicationContext(), SensingProcessService.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	/** http 서비스 시작
	 *
	 */
	public void startHttpProcessService () {
		httpServiceMonitor = HttpServiceMonitor.getInstance ();
		if (!httpServiceMonitor.isMonitoring()) {
			httpServiceMonitor.startMonitoring(getApplicationContext());
		}
	}


	/** http 서비스 종료
	 *
	 */
	public void stopHttpProcessService () {
		httpServiceMonitor = HttpServiceMonitor.getInstance ();
		if (httpServiceMonitor.isMonitoring()) {
			httpServiceMonitor.stopMonitoring(this);
		}

		try {
			stopService(new Intent(getApplicationContext(), HttpProcessService.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	/** 앱 재시작
	 *
	 */
	protected static Class<?> spashActivityClass;
	public void restartApplication () {

		if (spashActivityClass == null) return;
		Intent intent = new Intent(getAppContext(), spashActivityClass);

		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);

		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		PendingIntent pendingIntent = PendingIntent.getActivity(
				BaseApplication.getInstance().getBaseContext(), 0,
				intent, intent.getFlags());

		AlarmManager mgr = (AlarmManager) BaseApplication.getInstance().getBaseContext()
				.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, pendingIntent);



	}


	/** 센싱 서비스 죽이기 (죽이면 다시 살아나니 리셋 대용으로 쓰긴 쓰는데... 참 모양 빠진다.. 공부조 더하자...
	 *
	 */
	public static void killSensingService () {
		try {
			android.os.Process.killProcess(GeneralUtils.getRunnsingServiceInstance(BaseApplication.getInstance(), SensingProcessService.class).pid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/** 데이타 컨트롤러
	 *
	 * @return
     */
	public DataController getDataController () {
		if (dataController == null) dataController = new DataController(this);
		return dataController;
	}


	//피카소 얻기
	public Picasso getPicasso () {


		if (dPicasso == null) {


			/**
			 *
			 */

			dDownloader = new OkHttpDownloader(new File(PICASSO_CACHE_PATH), Long.MAX_VALUE);
			dPicassoCache = new CustomLruCache (1024 * 1000 * 128);

			Picasso.Builder builder = new Picasso.Builder (this).memoryCache(dPicassoCache);
			builder.downloader (dDownloader);
			dPicasso = builder.listener(new Picasso.Listener() {
				@Override
				public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
					exception.printStackTrace();
				}
			}).build();
			//dPicasso.setIndicatorsEnabled(true);
			//dPicasso.setLoggingEnabled(true);
			dPicasso.setSingletonInstance (dPicasso);
		}


		return dPicasso;
	}

	public CustomLruCache getPicassoCache () {
		return dPicassoCache;
	}

}
