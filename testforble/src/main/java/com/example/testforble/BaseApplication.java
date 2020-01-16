package com.example.testforble;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.example.testforble.data.DBHelper;
import com.example.testforble.dto.HistoryStackInfoDto;
import com.example.testforble.manage.Constants;
import com.example.testforble.service.SensingProcessService;
import com.example.testforble.service.SensingServiceMonitor;
import com.example.testforble.utils.LogUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import lib.netmania.data.DataController;


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


	private SensingServiceMonitor sensingServiceMonitor;



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

		//BLE센싱 서비스 시작
		startSensingProcessService ();

		//죽기전에 좀...
		if (Constants.IS_RELEASED) Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(getBaseContext(), getContentResolver(), Thread.getDefaultUncaughtExceptionHandler()));
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

	/** 데이타 컨트롤러
	 *
	 * @return
     */
	public DataController getDataController () {
		if (dataController == null) dataController = new DataController(this);
		return dataController;
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

}
