package com.example.testforble.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.testforble.BaseApplication;
import com.example.testforble.R;
import com.example.testforble.data.AppSharedData;
import com.example.testforble.data.DBHelper;
import com.example.testforble.data.DataProviderUtil;
import com.example.testforble.dto.SystemLogDto;
import com.example.testforble.dto.TemperatureTrackingDto;
import com.example.testforble.manage.Constants;
import com.example.testforble.utils.AlarmWakeLock;
import com.example.testforble.utils.DialogUtils;
import com.example.testforble.utils.GeneralUtils;
import com.example.testforble.utils.LogUtil;
import com.example.testforble.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lib.netmania.ble.model.BeaconDataModel;

public class ActvitySplash extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    //상수
    private final String TAG = "MainActivity";
    private static final long REPORT_DELAY = 0;


    //UI
    private int[] item_views = {
            R.id.item_0,
            R.id.item_1,
            R.id.item_2,
            R.id.item_3,
            R.id.item_4,
            R.id.item_5,
            R.id.item_6,
            R.id.item_7,
            R.id.item_8,
            R.id.item_9,
    };

    private Button btn_delete_data_all, btn_start_all, btn_finish_all, btn_show_system_log, btn_delete_system_log, btn_show_app_info, btn_add_item;
    private ToggleButton btn_start_again_with_scanfilter;


    //데이타
    private boolean mScanning = false;
    private ArrayList<String> mTargetList;


    //객체
    private BluetoothAdapter mBluetoothAdapter;
    private Timer ui_update_timer = new Timer();
    private TimerTask mUpdateTask = new TimerTask() {
        @Override
        public void run() {
            updateUI();
        }
    };
    private DBHelper mDBHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mDBHelper = new DBHelper(BaseApplication.getInstance());

        //늘깨어 있으라~ 잠들지 말아라~-----------------------------------------------------------------------
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getIntent().addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //----------------------------------------------------------------------------------------------

        setLayout();
        initData();
    }

    /**
     * 레이아웃 셋팅
     */
    private void setLayout() {

        btn_delete_data_all = (Button) findViewById(R.id.btn_delete_data_all);
        btn_delete_data_all.setOnClickListener(this);

        btn_start_all = (Button) findViewById(R.id.btn_start_all);
        btn_start_all.setOnClickListener(this);

        btn_finish_all = (Button) findViewById(R.id.btn_finish_all);
        btn_finish_all.setOnClickListener(this);

        btn_show_system_log = (Button) findViewById(R.id.btn_show_system_log);
        btn_show_system_log.setOnClickListener(this);

        btn_delete_system_log = (Button) findViewById(R.id.btn_delete_system_log);
        btn_delete_system_log.setOnClickListener(this);

        btn_add_item = (Button) findViewById(R.id.btn_add_item);
        btn_add_item.setOnClickListener(this);

        btn_show_app_info = (Button) findViewById(R.id.btn_show_app_info);
        btn_show_app_info.setOnClickListener(this);

        btn_start_again_with_scanfilter = (ToggleButton) findViewById(R.id.btn_start_again_with_scanfilter);
        btn_start_again_with_scanfilter.setOnCheckedChangeListener(this);

    }


    /**
     * 데이타 이니셜라이징
     */
    private void initData () {

        mTargetList = AppSharedData.getTrackingDeviceList();


        for (int i = 0; i < item_views.length; i++) {

            if (i < mTargetList.size()) {

                findViewById(item_views[i]).setVisibility(View.VISIBLE);

                ((CheckBox) findViewById(item_views[i]).findViewById(R.id.btn_check)).setText(mTargetList.get(i));
                ((CheckBox) findViewById(item_views[i]).findViewById(R.id.btn_check)).setTag(mTargetList.get(i));
                ((CheckBox) findViewById(item_views[i]).findViewById(R.id.btn_check)).setOnCheckedChangeListener(this);

                ((Button) findViewById(item_views[i]).findViewById(R.id.btn_show_data)).setTag(mTargetList.get(i));
                ((Button) findViewById(item_views[i]).findViewById(R.id.btn_show_data)).setOnClickListener(this);

                ((Button) findViewById(item_views[i]).findViewById(R.id.btn_delete_data)).setTag(mTargetList.get(i));
                ((Button) findViewById(item_views[i]).findViewById(R.id.btn_delete_data)).setOnClickListener(this);
            } else {
                findViewById(item_views[i]).setVisibility(View.GONE);
            }

        }

        for (int i = 0; i < mTargetList.size(); i++) {
            ((CheckBox) findViewById(item_views[i]).findViewById(R.id.btn_check)).setChecked(AppSharedData.getBoolean(AppSharedData.DNAME_PREFERENCE, mTargetList.get(i)));
        }

        try {
            ui_update_timer.schedule(mUpdateTask, 0, Constants.UI_UPDATE_CYCLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * UI 업데이트
     */
    private void updateUI() {

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {

                        int count = 0;
                        TemperatureTrackingDto item_first, item_final;
                        String str = "";

                        try {
                            for (int i = 0; i < mTargetList.size(); i++) {
                                count = DataProviderUtil.getInstance(BaseApplication.getInstance()).getReportedTemperatures(mTargetList.get(i), false, 0, 1, false).size();
                                item_first = DataProviderUtil.getInstance(BaseApplication.getInstance()).getSavedFirstData(mTargetList.get(i));
                                item_final = DataProviderUtil.getInstance(BaseApplication.getInstance()).getSavedFinalData(mTargetList.get(i));
                                str = (count > 0) ? "\n데이타 -->  " + item_final.temp + "℃ / " + item_final.hum
                                        + "%\n시작->현재 --> " + new SimpleDateFormat("H시m분s초").format(new Date(item_first.timestamp)) + " ~ " + new SimpleDateFormat("H시m분s초").format(new Date(item_final.timestamp))
                                        + "\n측정시간 --> " + StringUtils.getMilliSecondsToTimeString(item_final.timestamp - item_first.timestamp)
                                        + "\nSEQ --> " + item_final.seq
                                        + "\nBTR --> " + item_final.battery + "\nRSSI --> " + item_final.rssi : "";

                                ((TextView) findViewById(item_views[i]).findViewById(R.id.data_field)).setText("데이타 건수 --> " + String.valueOf(count) + str);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }


    /**
     * for life cycle
     */
    @Override
    public void onPause() {
        super.onPause();
        AlarmWakeLock.wakeLock(mActivity);
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if (GeneralUtils.isScreenOn(BaseApplication.getInstance())) {
            AlarmWakeLock.releaseWakeLock ();
        }*/

        btn_start_again_with_scanfilter.setOnCheckedChangeListener(null);
        btn_start_again_with_scanfilter.setChecked(AppSharedData.getBoolean(AppSharedData.DNAME_PREFERENCE, AppSharedData.KEY_SCANFILTER));
        btn_start_again_with_scanfilter.setOnCheckedChangeListener(this);

        AlarmWakeLock.releaseWakeLock();
    }


    /**
     * 온클릭
     *
     * @param view
     */
    @Override
    public void onClick(final View view) {

        switch (view.getId()) {

            case R.id.btn_show_data:
                ActivityControl.openJobReportsActivity(getIntent(), mActivity, (String) view.getTag(), "NONE");
                break;

            case R.id.btn_delete_data:

                DialogUtils.confirm(
                        mActivity,
                        "",
                        "진정 삭제??",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DBHelper(mApp).delete(TemperatureTrackingDto.getInstance().getTblName(), "MAC=?", new String[]{(String) view.getTag()});
                                AppSharedData.removeTrackingDevice((String) view.getTag());
                                initData();
                                updateUI();
                            }
                        },
                        true
                );

                break;

            case R.id.btn_delete_data_all:

                DialogUtils.confirm(
                        mActivity,
                        "전체 데이타를 모두 삭제합니다. 계속 하시겠습니까?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DBHelper mDB = new DBHelper(mApp);
                                for (int i = 0; i < mTargetList.size(); i++) {
                                    mDB.delete(TemperatureTrackingDto.getInstance().getTblName(), "MAC=?", new String[]{mTargetList.get(i)});
                                    AppSharedData.removeTrackingDevice(mTargetList.get(i));
                                }
                                mDB = null;

                                initData();
                                updateUI();

                            }
                        }, true
                );

                break;

            case R.id.btn_start_all:

                for (int i = 0; i < mTargetList.size(); i++) {
                    AppSharedData.put(AppSharedData.DNAME_PREFERENCE, mTargetList.get(i), true);
                }
                initData();

                break;

            case R.id.btn_finish_all:

                for (int i = 0; i < mTargetList.size(); i++) {
                    AppSharedData.put(AppSharedData.DNAME_PREFERENCE, mTargetList.get(i), false);
                }
                initData();

                break;

            case R.id.btn_show_system_log:
                ActivityControl.openSystemLogActivity(getIntent(), mActivity);
                break;


            case R.id.btn_delete_system_log:
                new DBHelper(mApp).deleteAll(SystemLogDto.getInstance().getTblName());
                break;

            case R.id.btn_add_item:

                DialogUtils.getUserInput(
                        mActivity,
                        "로거 추가",
                        R.string.dialog_add_item,
                        new DialogUtils.UserInputListener() {
                            @Override
                            public void onYesClick(String value) {
                                AppSharedData.addTrackingDevice(value);
                                initData();
                                updateUI();
                            }
                        }, false
                );

                break;

            case R.id.btn_show_app_info:
                GeneralUtils.showInstalledAppDetails(mActivity, getPackageName());
                break;


            case R.id.btn_start_again_with_scanfilter:
                //nothing yet;
                break;

        }

    }


    /**
     * 체크 체인지 리스너
     *
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {

            case R.id.btn_check:
                AppSharedData.put(AppSharedData.DNAME_PREFERENCE, (String) buttonView.getTag(), isChecked);
                break;

        }
    }


    /**
     * 뒤로 가기
     */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }

}
