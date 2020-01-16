package com.netmania.checklod.tnt.activities.job;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;
import com.balysv.materialripple.MaterialRippleLayout;
import com.netmania.checklod.general.AlarmPlayer;
import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.activities.IBaseJobActivity;
import com.netmania.checklod.general.data.AppSharedData;
import com.netmania.checklod.general.data.DBHelper;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.TemperatureTrackingDto;
import com.netmania.checklod.general.dto.WarnningMsgDto;
import com.netmania.checklod.general.http.BaseAPI;
import com.netmania.checklod.general.http.GeneralAPI;
import com.netmania.checklod.general.manage.Constants;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.manage.FlagBox;
import com.netmania.checklod.general.receiver.SystemBroadcastReceiver;
import com.netmania.checklod.general.utils.AlarmWakeLock;
import com.netmania.checklod.general.utils.DialogUtils;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.general.utils.PermissionUtils;
import com.netmania.checklod.general.utils.StringUtils;
import com.netmania.checklod.general.view.HeaderGridView;
import com.netmania.checklod.general.view.InfoToast;
import com.netmania.checklod.tnt.BaseApplication;
import com.netmania.checklod.tnt.R;
import com.netmania.checklod.tnt.activities.ActivityControl;
import com.netmania.checklod.tnt.adapter.BeaconListAdapterForJob;
import com.netmania.checklod.tnt.data.DataProviderUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lib.netmania.ble.model.BeaconDataModelNew;

/**
 * Created by hansangcheol on 2018. 2. 27..
 */

public class ActivityJobMain
        extends BaseActivity
        implements View.OnClickListener, View.OnLongClickListener, IBaseJobActivity, JobProcessController.JobProcessCallback {


    //상수


    //UI
    private MenuItem ic_wifi, ic_more, ic_driver_info;
    private View mHeaderView, noItemView, system_warnning_container, progressGo, title_image;
    private HeaderGridView mGridView;
    private TextView profile_name, profile_vehicle_no, network_status_desc, battery_status_desc, memory_status_desc, storage_status_desc, system_warnning_msg;
    private ImageView network_status_icon, battery_status_icon, memory_status_icon, storage_status_icon, no_item_icon;
    private Button btn_job_start;
    private ImageButton btn_device_add;
    private TextView no_item_desc;
    private MaterialRippleLayout btn_job_start_container, btn_device_add_container;
    private Toast finishConfirmToast;


    //데이타
    private BeaconListAdapterForJob mAdapter;
    private WarnningMsgDto msg_warnning;


    //객체
    private SystemBroadcastReceiver systemBR;



    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //등장효과
        overridePendingTransition (R.anim.anim_fade_in, R.anim.empty);
        msg_warnning = new WarnningMsgDto ();

        //늘깨어 있으라~ 잠들지 말아라~-----------------------------------------------------------------------
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getIntent().addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //----------------------------------------------------------------------------------------------


        PermissionUtils.checkLocationPermission (mActivity);

        if (savedInstanceState == null) {
            setReceiver();
        }

        initNFC();
        setLayout();
        initData();
        startUpdateUI();

        if (PermissionUtils.checkLocationPermission (mActivity)) {
            //그냥 진행하면 되고...
        }
    }



    /** 시스템 리시버 선언
     *
     */
    private void setReceiver () {

        try {

            /** 시스템 모니터링용 리시버 선언 */
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

            if (systemBR != null) {
                systemBR = new SystemBroadcastReceiver();
                registerReceiver(systemBR, intentFilter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** NFC 셋팅
     *
     */
    private NfcAdapter mNFCAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mNFCTechLists;

    private void initNFC () {

        mNFCAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNFCAdapter != null) {
            LogUtil.I(DebugTags.TAG_NFC, "Read an NFC tag");
        } else {
            LogUtil.I(DebugTags.TAG_NFC, "This phone is not NFC enabled.");
        }

        // create an intent with tag data and deliver to this activity
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


        // set an intent filter for all MIME data
        IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {

            ndefIntent.addDataType("*/*");
            mIntentFilters = new IntentFilter[] { ndefIntent };
        } catch (Exception e) {
            LogUtil.I(DebugTags.TAG_NFC, e.toString());
        }

        mNFCTechLists = new String[][] { new String[] { NfcF.class.getName() } };
    }



    /** NFC 태깅
     *
     * @param intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        LogUtil.I(DebugTags.TAG_NFC, "와우");

        String scanedMAC = "";
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (data != null) {
            try {
                for (int i = 0; i < data.length; i++) {
                    NdefRecord[] recs = ((NdefMessage)data[i]).getRecords();
                    for (int j = 0; j < recs.length; j++) {
                        if (recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                                Arrays.equals(recs[j].getType(), NdefRecord.RTD_TEXT)) {
                            byte[] payload = recs[j].getPayload();
                            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                            int langCodeLen = payload[0] & 0077;

                            scanedMAC += new String (payload, langCodeLen + 1, payload.length - langCodeLen - 1,  textEncoding);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.E(DebugTags.TAG_NFC, e.toString());
            }
        }


        LogUtil.I(DebugTags.TAG_NFC, "from nfc : " + scanedMAC);
        JobProcessController.getInstance(mActivity, this).checkAndStroreDeviceToTarget (scanedMAC);
    }


    /** 권한 설정 리스너
     *
     */
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantresults) {



        boolean AllPermissionGranted = true;

        switch (requestCode) {

            case FlagBox.PERMISSION_REQ_LOCATION :

                AllPermissionGranted = true;

                for (int i = 0; i < grantresults.length; i++) {
                    if (grantresults[i] != PackageManager.PERMISSION_GRANTED) {
                        AllPermissionGranted = false;
                        break;
                    }
                }


                if (AllPermissionGranted) {
                    mHandler.post(new InfoToast(mActivity, getResources().getString(R.string.toast_location_permission)));
                } else {
                    DialogUtils.alert(
                            mActivity,
                            R.string.permission_deny_message,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mApp.finishAll();
                                }
                            }, false
                    );
                }

                break;

        }

    }


    //자식 화면에서 결과값을 받아오자
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case FlagBox.REQUEST_USER_INPUT :


                if (resultCode == RESULT_CANCELED) return;

                final int mode = data.getIntExtra(FlagBox.EXTRA_KEY_MODE, FlagBox.MODE_ADD_DEVICE);
                final String input_01 = data.getStringExtra(FlagBox.INOUT_STR_1);
                final String input_02 = data.getStringExtra(FlagBox.INOUT_STR_2);

                if (mode == FlagBox.MODE_ADMIN_DELIVERY) {

                } else {
                    JobProcessController.getInstance(mActivity, this).onActivityResult(requestCode, resultCode, data);
                }

                break;

            case FlagBox.REQUEST_HAND_OUT :

                switch (resultCode) {

                    case RESULT_OK :

                        initData();

                        break;

                    case RESULT_CANCELED :
                        //nothing yet;
                        break;

                }

                break;
        }
    }


    /** 액션메뉴 커스터 마이징
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        ic_wifi = menu.findItem(R.id.ic_wifi);
        ic_more = menu.findItem(R.id.ic_more);

        ic_driver_info = menu.findItem(R.id.ic_driver_info);

        String driverInfo = String.format(getResources().getString(R.string.driver_info_text), DVIF.name, DVIF.vehicleNo);
        ((TextView) ic_driver_info.getActionView().findViewById(R.id.profile_driver)).setText(driverInfo);

        updateWifiStatus ();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {

            case R.id.ic_driver_info :
                //nothing yet;
                return true;

            case R.id.ic_wifi :

                if (Constants.IS_LOG) {
                    AppSharedData.setShowAllReports(!AppSharedData.getShowAllReports());
                    initData();
                }

                return true;

            case R.id.ic_more :

                String vText = String.format(getResources().getString (R.string.info_version_text), GeneralUtils.getAppVersion(mApp));
                DialogUtils.alert(
                        mActivity,
                        vText,
                        false
                );

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /** 레이아웃 셋팅
     *
     */
    private void setLayout () {
        //로고표현
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(true);

        mGridView = (HeaderGridView) findViewById(R.id.grid_view);
        mHeaderView = mInflater.inflate(R.layout.layout_header_job_list, null);

        mAdapter = new BeaconListAdapterForJob(mActivity, new ArrayList<BeaconItemDto>(), mClickListener);
        mGridView.addHeaderView(mHeaderView);


        noItemView = mInflater.inflate(R.layout.layout_no_item, null);
        no_item_icon = (ImageView) noItemView.findViewById(R.id.no_item_icon);
        no_item_desc = (TextView) noItemView.findViewById(R.id.no_item_desc);
        mGridView.addHeaderView(noItemView);


        title_image = mHeaderView.findViewById(R.id.title_image);

        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(mItemCliclListener);
        mGridView.setOnItemLongClickListener(mItemLongCliclListener);


        profile_name = (TextView) mHeaderView.findViewById(R.id.profile_name);
        profile_vehicle_no = (TextView) mHeaderView.findViewById(R.id.profile_vehicle_no);

        network_status_icon = (ImageView) mHeaderView.findViewById(R.id.network_status_icon);
        network_status_desc = (TextView) mHeaderView.findViewById(R.id.network_status_desc);

        battery_status_icon = (ImageView) mHeaderView.findViewById(R.id.battery_status_icon);
        battery_status_desc = (TextView) mHeaderView.findViewById(R.id.battery_status_desc);

        memory_status_icon = (ImageView) mHeaderView.findViewById(R.id.memory_status_icon);
        memory_status_desc = (TextView) mHeaderView.findViewById(R.id.memory_status_desc);

        storage_status_icon = (ImageView) mHeaderView.findViewById(R.id.storage_status_icon);
        storage_status_desc = (TextView) mHeaderView.findViewById(R.id.storage_status_desc);

        system_warnning_container = mHeaderView.findViewById(R.id.system_warnning_container);
        system_warnning_msg = (TextView) mHeaderView.findViewById(R.id.system_warnning_msg);

        system_warnning_container.setVisibility(View.GONE); // 일단은 워닝 없음


        btn_job_start_container = (MaterialRippleLayout) findViewById(R.id.btn_job_start_container);
        btn_job_start = (Button) findViewById(R.id.btn_job_start);
        btn_job_start.setOnClickListener(this);

        btn_device_add_container = (MaterialRippleLayout) findViewById(R.id.btn_device_add_container);
        btn_device_add = (ImageButton) findViewById(R.id.btn_device_add);
        btn_device_add.setOnClickListener(this);
        if (!Constants.IS_RELEASED) btn_device_add.setOnLongClickListener(this);


        progressGo = findViewById(R.id.progressGo);
        progressGo.setVisibility(View.GONE);

    }


    /** 데이타 이니셜라이징
     *
     */
    private void initData () {


        updateSystemInfo ();

        //데이타 로드해서 이름 바꿔라..
        profile_name.setText(String.format(getResources().getString(R.string.name_tag), DVIF.name));
        profile_vehicle_no.setText(DVIF.vehicleNo);

        ArrayList<BeaconItemDto> items = DataProviderUtil.getInstance(mActivity).getTrackingDeviceList ();

        if (items.size() > 0) {

            for (int i = 0; i < items.size(); i++) {

                items.get(i).$max_temp = DataProviderUtil.getInstance(mApp).getSavedMaxTemp(items.get(i).MAC);
                items.get(i).$min_temp = DataProviderUtil.getInstance(mApp).getSavedMinTemp(items.get(i).MAC);

                if (!mAdapter.isExistBeacon(items.get(i).MAC)) {
                    mAdapter.add(items.get(i));
                } else {
                    mAdapter.update (items.get(i));
                }
            }

            mAdapter.notifyDataSetChanged();
        }


        /*if (GeneralUtils.isMyAppLauncherDefault(getPackageName())) {
            KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
            lock.disableKeyguard();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }*/

        setTrackingControl ();
        setAlarmControl ();


        if (items == null || items.size() == 0) {
            //자도된다~~~ -------------------------------------------------------------------------------------
            AlarmWakeLock.releaseWakeLock();
            //-----------------------------------------------------------------------------------------------
        }

    }




    /** UI 업데이트 개시
     *
     */
    private void startUpdateUI () {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(mTimerTask, 0, Constants.UI_UPDATE_CYCLE);
        }
    }



    /** 아이템 없니??
     *
     */
    private void setTrackingControl () {

        if (mAdapter.getCount() > 0) {
            mHeaderView.setVisibility(View.GONE);
            noItemView.setVisibility(View.GONE);
        } else {
            mHeaderView.setVisibility(View.VISIBLE);
            noItemView.setVisibility(View.VISIBLE);
        }

    }


    /** 알람 콘트롤
     *
     */
    private int before_status = BeaconItemDto.STATUS_STABLE;
    private boolean isSpeaking = false;
    private long SPEAK_DURATION = 1000 * 20;
    private void setAlarmControl () {


        if (before_status != mAdapter.getTotalStatus()) {
            switch (mAdapter.getTotalStatus()) {

                default:
                case BeaconItemDto.STATUS_STABLE:
                    AlarmPlayer.getInstance(mApp).stopAll();
                    break;

                case BeaconItemDto.STATUS_CAUTION:
                    AlarmPlayer.getInstance(mApp).stopEmergency();
                    AlarmPlayer.getInstance(mApp).playCaution();
                    break;

                case BeaconItemDto.STATUS_EMERGNECY:
                    AlarmPlayer.getInstance(mApp).stopCaution();
                    AlarmPlayer.getInstance(mApp).playEmergency();
                    break;

            }
        }

        before_status = mAdapter.getTotalStatus();

        /* 차량용 디바이스는 충전 유도
        if (CONFIG.is_vehicle.equals("1")
                && !GeneralUtils.getBatteryStatusInfo(mApp).isRecharging
                && mAdapter.isDeliverying()
                && !isSpeaking) {


            mHandler.post(new InfoToast(mApp, getResources().getString (R.string.voice_recharging_alert)));
            mHandler.post(new InfoToast(mApp, getResources().getString (R.string.voice_recharging_alert)));
            mHandler.post(new InfoToast(mApp, getResources().getString (R.string.voice_recharging_alert)));
            mHandler.post(new InfoToast(mApp, getResources().getString (R.string.voice_recharging_alert)));
            speakOut(getResources().getString(R.string.voice_recharging_alert));

            isSpeaking = true;
            mHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            isSpeaking = false;
                        }
                    }, SPEAK_DURATION
            );
        }*/


        //서버에 보내지지 않은 데이타가 있는지 체크.
        int notSendedDataCount = DataProviderUtil.getInstance(BaseApplication.getInstance()).getNotReportedCount("").size();
        if (notSendedDataCount > mAdapter.getCount() * 10) {

            if (GeneralUtils.isNetworkAvaliable(mApp)) {

                mApp.startSensingProcessService();
                mApp.startHttpProcessService();

            } else if (!isSpeaking) {

                mHandler.post(new InfoToast(mApp, getResources().getString (R.string.error_network)));
                mHandler.post(new InfoToast(mApp, getResources().getString (R.string.error_network)));
                mHandler.post(new InfoToast(mApp, getResources().getString (R.string.error_network)));
                mHandler.post(new InfoToast(mApp, getResources().getString (R.string.error_network)));
                speakOut(getResources().getString(R.string.error_network));

                isSpeaking = true;
                mHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                isSpeaking = false;
                            }
                        }, SPEAK_DURATION
                );

            }

        }

        //배송중인데도 혹시나 거시기 뭐시냐 알자나 거시기. 있나???
        if (mAdapter.getItemNotRunning () != null) {

            String aMsg = String.format(getResources().getString (R.string.voice_check_beacon), mAdapter.getItemNotRunning().sticker);

            mHandler.post(new InfoToast(mApp, aMsg));
            mHandler.post(new InfoToast(mApp, aMsg));
            mHandler.post(new InfoToast(mApp, aMsg));
            mHandler.post(new InfoToast(mApp, aMsg));
            speakOut(aMsg);

            isSpeaking = true;
            mHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            isSpeaking = false;
                        }
                    }, SPEAK_DURATION
            );
        }


        ///5분 이상 체크하지 못하도록 하세요.
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mAdapter.getItem(i).ble_signal_cycle >= 1000 * 60 * 5) {

                String aMsg = String.format(getResources().getString (R.string.voice_check_signal_term), mAdapter.getItem(i).sticker);
                mHandler.post(new InfoToast(mApp, aMsg));
                mHandler.post(new InfoToast(mApp, aMsg));
                mHandler.post(new InfoToast(mApp, aMsg));
                mHandler.post(new InfoToast(mApp, aMsg));
                speakOut(aMsg);

                isSpeaking = true;
                mHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                isSpeaking = false;
                            }
                        }, SPEAK_DURATION
                );

                break;
            }
        }


        /** 센서 이단 및 배터리 체크 --------------------------------------------------------------------------------- */

        try {

            ArrayList<String> noSensorBeaconList = new ArrayList<>();
            String noSensorBeacons = "";
            ArrayList<String> noBatteryBeaconList = new ArrayList<>();
            String noBatteryBeacons = "";
            ArrayList<String> signalWronningBeaconList = new ArrayList<>();
            String signalWornningBecaons = "";

            for (int i = 0; i < mAdapter.getCount(); i++) {

                if (mAdapter.getItem(i).sensor_disconnected == BeaconDataModelNew.DISCONNECTED) {
                    noSensorBeaconList.add(mAdapter.getItem(i).sticker);
                    if (!StringUtils.isEmpty(noSensorBeacons)) noSensorBeacons += ", ";
                    noSensorBeacons += mAdapter.getItem(i).sticker;
                }

                if (mAdapter.getItem(i).bcn_battery < Constants.BATTERY_LOW_LIMIT && mAdapter.getItem(i).bcn_battery > 0 && mAdapter.getItem(i).delivery_step == BeaconItemDto.DELEVERY_STEP_DELIVERY) {
                    noBatteryBeaconList.add(mAdapter.getItem(i).sticker);
                    if (!StringUtils.isEmpty(noBatteryBeacons)) noBatteryBeacons += ", ";
                    noBatteryBeacons += mAdapter.getItem(i).sticker;
                }

                if (mAdapter.getItem(i).rssi <= Constants.MINIMUN_RSSI && mAdapter.getItem(i).delivery_step == BeaconItemDto.DELEVERY_STEP_DELIVERY) {
                    signalWronningBeaconList.add(mAdapter.getItem(i).sticker);
                    if (!StringUtils.isEmpty(signalWornningBecaons)) signalWornningBecaons += ", ";
                    signalWornningBecaons += mAdapter.getItem(i).sticker;
                }

                //LogUtil.I("배터리 체크(" + mAdapter.getItem(i).sticker + ") -- " + mAdapter.getItem(i).bcn_battery + " / 4.0");
            }

            String tMsg = "";
            if (noSensorBeaconList.size() > 0) {
                tMsg += String.format(getResources().getString(R.string.toast_beacon_sensor_removed), noSensorBeacons);
            }

            if (noBatteryBeaconList.size() > 0) {
                if (!StringUtils.isEmpty(tMsg)) tMsg += "\n\n";
                tMsg += String.format(getResources().getString(R.string.toast_beacon_low_battery), noBatteryBeacons);
            }


            if (signalWronningBeaconList.size() > 0) {
                if (!StringUtils.isEmpty(tMsg)) tMsg += "\n\n";
                tMsg += String.format(getResources().getString(R.string.toast_beacon_signal_wornning), signalWornningBecaons);
            }

            if (!StringUtils.isEmpty(tMsg) && !isSpeaking) {
                speakOut(tMsg);
                mHandler.post(new InfoToast(mApp, tMsg));
                mHandler.post(new InfoToast(mApp, tMsg));
                mHandler.post(new InfoToast(mApp, tMsg));
                mHandler.post(new InfoToast(mApp, tMsg));

                isSpeaking = true;
                mHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                isSpeaking = false;
                            }
                        }, SPEAK_DURATION
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /** 센서 이단 체크 --------------------------------------------------------------------------------- */

    }


    /** 시스템 리포트 업데이트
     *
     */
    public void updateSystemInfo () {
        updateWifiStatus ();
        updateBatteryStatus ();

        //메모리는 그냥 여기서 제어 합시다
        int usedMemoryRatio = GeneralUtils.getUsedMemoryRatio ();
        memory_status_desc.setText (String.format(getResources().getString(R.string.label_remain), usedMemoryRatio + "%"));

        if (usedMemoryRatio > Constants.MEMORY_LIMIT_MAX) {
            msg_warnning.memory = "";//getResources().getString(R.string.msg_warnning_memory);
        } else {
            msg_warnning.memory = "";
        }


        double freeStorageSize = (double) GeneralUtils.getAvailableSpaceInMB () /  1024.0;
        storage_status_desc.setText(String.format(getResources().getString(R.string.label_remain), String.format("%.2f", freeStorageSize) + "GB"));

        if (freeStorageSize <= Constants.STORAGE_LIMIT_MIN) {
            msg_warnning.storage = String.format(getResources().getString(R.string.msg_warnning_storage), String.valueOf(freeStorageSize) + "GB");
        } else {
            msg_warnning.storage = "";
        }

        updateWarnningMsg ();
    }


    /** 주의 메시지 업데이트
     *
     */
    private void updateWarnningMsg () {

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        String msg = "";
                        if (!StringUtils.isEmpty(msg_warnning.network)) {
                            msg += msg_warnning.network;
                        }

                        if (!StringUtils.isEmpty(msg_warnning.battery)) {
                            msg += StringUtils.isEmpty(msg) ? msg_warnning.battery : "\n\n" + msg_warnning.battery;
                        }

                        if (!StringUtils.isEmpty(msg_warnning.memory)) {
                            msg += StringUtils.isEmpty(msg) ? msg_warnning.memory : "\n\n" + msg_warnning.memory;
                        }

                        if (!StringUtils.isEmpty(msg_warnning.storage)) {
                            msg += StringUtils.isEmpty(msg) ? msg_warnning.storage : "\n\n" + msg_warnning.storage;
                        }

                        if (StringUtils.isEmpty(msg)) {
                            system_warnning_msg.setText("");
                            system_warnning_container.setVisibility(View.GONE);
                        } else {
                            system_warnning_msg.setText(msg);
                            system_warnning_container.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

    }




    /** 배송 작업 시작
     *
     */
    private void startDeliveryJob () {

        LogUtil.I("뭐니???");

        BeaconItemDto bcn;
        int i = 0;
        for (i = 0; i < mAdapter.getCount(); i++) {
            bcn = mAdapter.getItem(i);

            if (bcn.delivery_step > BeaconItemDto.DELEVERY_STEP_READY) {
                //nothing yet;
            } else if (!bcn.$check_to_start) {
                //nothing yet;
            } else if (bcn.bcn_battery < Constants.BATTERY_LOW_LIMIT && bcn.bcn_battery > 0) {
                mHandler.post(
                        new InfoToast(mApp, String.format(getResources().getString(R.string.toast_beacon_low_battery), bcn.sticker))
                );
            } else {
                JobProcessController.getInstance(mActivity, this).startNewDeliveryJob (bcn);
            }
        }


        //늘깨어 있으라~ 잠들지 말아라~------------------------------------------------------------------------
        if (i > 0) {
            AlarmWakeLock.wakeLock(mActivity);
        }
        //-----------------------------------------------------------------------------------------------

        initData();
    }




    /** 라이브 싸이클 관련
     *
     */
    /** life cycle ---- pause
     *
     */
    @Override
    public void onPause () {
        super.onPause();

        if (mNFCAdapter != null) {
            mNFCAdapter.disableForegroundDispatch(this);
        }

        /*if (GeneralUtils.isApplicationBroughtToBackground(mApp)
                && mAdapter.getCount() > 0
                && !JobForegroundMoinitor.getInstance().isMonitoring()) {
            JobForegroundMoinitor.getInstance().startMonitoring(BaseApplication.getInstance());
        }*/
    }


    /** life cycle ---- resume
     *
     */
    @Override
    public void onResume () {
        super.onResume();

        if (mNFCAdapter != null)
            mNFCAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);

        initData();
        /*if (JobForegroundMoinitor.getInstance().isMonitoring()) {
            JobForegroundMoinitor.getInstance().stopMonitoring (BaseApplication.getInstance());
        }*/

        //버전 체크 및 거시기....
        checkAndGo();

    }

    /** 온클릭
     *
     * @param view
     */
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_device_add :

                if (Constants.IS_RELEASED) {
                    ActivityControl.openUserInputUI(mActivity.getIntent(), mActivity, FlagBox.MODE_ADD_DEVICE);
                } else {

                    DialogUtils.singleChoice(
                            mActivity,
                            R.string.title_for_test_single_choice,
                            getResources().getStringArray(R.array.options_for_test_single_choice),
                            -1,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    switch (which) {

                                        case 0:
                                            ActivityControl.openUserInputUI(mActivity.getIntent(), mActivity, FlagBox.MODE_ADD_DEVICE);
                                            break;

                                        case 1:
                                            addTestDevices();
                                            break;

                                    }

                                    dialog.dismiss();

                                }
                            },
                            true
                    );
                }

                break;

            case R.id.btn_job_start :
                startDeliveryJob ();
                break;

        }

    }



    /** 온 롱클릭
     *
     * @param view
     * @return
     */
    @Override
    public boolean onLongClick(View view) {

        switch (view.getId()) {

            case R.id.btn_device_add :

                DialogUtils.confirm(
                        mActivity,
                        R.string.confirm_delete_all,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                JobProcessController.getInstance(mActivity, ActivityJobMain.this).clearJobs();
                                initData();

                            }
                        },
                        true
                );

                break;
        }

        return true;
    }


    /** 테스트 디바이스 자동등록
     *
     */
    private void addTestDevices () {

        String[] targets = getResources().getStringArray(R.array.test_target);

        //아래 주석내용은 자동으로 네개의 기기를 등록하도록 되어있음 (온도 셋팅도 자동)
        for (int i = 0; i < targets.length; i++) {
            JobProcessController.getInstance(mActivity, this).addTrackingDevice(targets[i], JobProcessController.DELIVERY_START_OPTION_NEW);
            new DBHelper(BaseApplication.getInstance()).update(BeaconItemDto.getInstance().getUpdateTempRageQry(targets[i], 20, 30));
        }

        initData();
    }


    /** 뒤로 가기
     *
     */
    @Override
    public void onBackPressed () {

        /*if (CONFIG.is_vehicle.equals("1")) {
            mHandler.post (new InfoToast(mActivity, getResources().getString(R.string.message_not_alowed_background_forever)));
            return;
        }

        if (mAdapter.getCount() > 0) {
            mHandler.post (new InfoToast(mActivity, getResources().getString(R.string.message_not_alowed_background)));
            return;
        }*/

        //메시지 확인후 종료 -- 토스트
        /*if (finishConfirmToast == null || finishConfirmToast.getView().getWindowToken() == null) {
            finishConfirmToast = Toast.makeText (mActivity, R.string.message_quit, Toast.LENGTH_SHORT);
            finishConfirmToast.show ();
        } else {
            finishConfirmToast.cancel();
            //moveTaskToBack(true);
            mApp.finishAll();

            //퇴장효과
            overridePendingTransition(R.anim.empty, R.anim.anim_fade_out);
        }*/


        if (mAdapter.getCount() > 0 && mAdapter.isWorkingOnIt ()) {
            moveTaskToBack(false);
            return;
        }

        DialogUtils.confirm (
                mActivity,
                R.string.confirm_finish,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        JobProcessController.getInstance(mActivity, ActivityJobMain.this).clearJobs ();

                        mApp.finishAll();

                        //퇴장효과
                        overridePendingTransition(R.anim.empty, R.anim.anim_fade_out);
                    }
                },
                true
        );
    }



    /** for IBaseJobActivity ----------------------------------------------------------------------------------------------
     *
     */
    @Override
    public void updateWifiStatus() {
        if (ic_wifi == null) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int status = GeneralUtils.getNetworkStatus(mApp.getApplicationContext());
                int icon = R.drawable.ic_wifi_off;
                String network_status = "OFF";


                switch (status) {
                    case GeneralUtils.NETWORK_STATUS_NONE :
                        icon = R.drawable.ic_wifi_off;
                        network_status = "OFFLINE";

                        msg_warnning.network = getResources().getString(R.string.msg_warnning_no_connection);

                        break;

                    case GeneralUtils.NETWORK_STATUS_MOBILE :
                        icon = R.drawable.ic_lte_on;
                        network_status = "MOBILE";

                        msg_warnning.network = "";

                        break;

                    case GeneralUtils.NETWORK_STATUS_WIFI :
                        icon = R.drawable.ic_wifi_on;
                        network_status = "WIFI";

                        msg_warnning.network = getResources().getString(R.string.msg_warnning_wifi);

                        break;
                }

                ic_wifi.setIcon(icon);
                network_status_icon.setImageResource(icon);
                network_status_desc.setText(network_status);

                updateWarnningMsg ();
            }
        });
    }

    @Override
    public void updateBatteryStatus() {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {

                        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
                        int batLevel = 0;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                        }

                        int icon = R.drawable.ic_battery_20;

                        if (batLevel <= 20) {
                            icon = R.drawable.ic_battery_20;
                        } else if (batLevel <= 30) {
                            icon = R.drawable.ic_battery_30;
                        } else if (batLevel <= 50) {
                            icon = R.drawable.ic_battery_50;
                        } else if (batLevel <= 60) {
                            icon = R.drawable.ic_battery_60;
                        } else if (batLevel <= 80) {
                            icon = R.drawable.ic_battery_80;
                        } else if (batLevel <= 90) {
                            icon = R.drawable.ic_battery_90;
                        } else {
                            icon = R.drawable.ic_battery_100;
                        }


                        battery_status_icon.setImageResource(icon);
                        battery_status_desc.setText(String.format(getResources().getString(R.string.label_remain), String.valueOf (batLevel)));


                        if (batLevel <= 30) {
                            msg_warnning.battery = String.format(getResources().getString(R.string.msg_warnning_battery), String.valueOf(batLevel) + "%");
                        } else {
                            msg_warnning.battery = "";
                        }

                    }
                }
        );
    }

    @Override
    public int getBeaconCount() {
        //nothing yet;
        return (mAdapter != null) ? mAdapter.getCount() : 0;
    }

    /** 고고고
     *
     */
    @Override
    public void doUpdateUI() {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        initData();
                    }
                }
        );
    }

    @Override
    public void doDisableMe(final String mac) {

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        final ArrayList<TemperatureTrackingDto> notReportedData = DataProviderUtil.getInstance(BaseApplication.getInstance()).getNotReportedData(mac);
                        if (notReportedData.size() > 0) {
                            JobProcessController.getInstance(mActivity, ActivityJobMain.this).sendNotReportedData(mac, JobProcessController.PROCESS_TAKE_OVER, notReportedData);
                        } else {
                            initData();
                        }
                    }
                }
        );
    }
    /** ------------------------------------------------------------------------------------------------------------------- */


    /** for JobProcessCallback listener -------------------------------------------------------------------------------------
     *
     */
    @Override
    public void onCompleteAddBeaconDevice(String mac, int start_option) {

        switch (start_option) {

            case JobProcessController.DELIVERY_START_OPTION_NEW :
                JobProcessController.getInstance(mActivity, this).setTemperatureRange (mac, 0, 0);
                break;

            case JobProcessController.DELIVERY_START_OPTION_TAKEOVER :
                //nothing yet;
                break;

            case JobProcessController.DELIVERY_START_OPTION_CARGO :
                //nothing yet;
                break;
        }

        initData ();
    }

    @Override
    public void onFailedAddBeaconDevice(String mac, int failed_reson) {

        switch (failed_reson) {

            case JobProcessController.FAILED_TO_ADD_NOT_AUTHRIZED_BEACON :

                DialogUtils.alert(
                        mActivity,
                        R.string.alert_not_allowed_beacon,
                        false
                );

                break;

            case JobProcessController.FAILED_TO_ADD_ALREADY_TRACKING :

                mHandler.post(new InfoToast(mActivity, getResources().getString(R.string.toast_dup_device)));

                break;

        }
    }

    @Override
    public void onTemperatureRangeSelected(String mac, double minimum, double maximum) {
        initData();
    }

    @Override
    public void onCompleteRemoveBeacon(final String mac) {
        mAdapter.remove(mAdapter.getItem(mac));
        initData();
    }

    @Override
    public void onTakeOverSuccess(String mac) {

        initData();

        GeneralAPI.sendTakeOverFCM(
                mActivity,
                false,
                DVIF.id,
                mac,
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

    @Override
    public void onTakeOverFailed(String mac, int code) {
        initData();
    }

    @Override
    public void onStartCheckOut(String mac) {
        //nothing yet;
    }

    @Override
    public void onCheckOutSuccess(String mac) {
        initData();
    }

    @Override
    public void onCheckOutFailed(String mac) {
        initData();
    }

    @Override
    public void onNotReportedDataSendFailed(String mac, int process) {
        initData();
    }

    @Override
    public void onNotReportedDataSendComplete(String mac, int process) {
        initData();
    }

    @Override
    public void onHandOutSuccess(Map<String, Object> map, File file, String selectedLogerId) {
        initData();
    }

    @Override
    public void onHandOutSuccess(File file, String selectedLogerId) {
        initData();
    }

    @Override
    public void onHandOutFailed(int statusCode, String selectedLogerId) {
        initData();
    }

    @Override
    public void onCargoBaseStart(String mac) {
        ActivityControl.openDataSyncActivity(getIntent(), mActivity, mac);
    }

    @Override
    public void onDataCleared() {
        mAdapter.clear();
        initData();
    }

    @Override
    public void onPhotoCompositionItemSelected(File file) {
        //nothign yet;
    }
    /** ------------------------------------------------------------------------------------------------------------------- */




    /** 그리드뷰 아이템 클릭 리스너 ---------------------------------------------------------------------------------------------- */
    private AdapterView.OnItemClickListener mItemCliclListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (mAdapter == null || mAdapter.getCount() == 0) {
                ActivityControl.openUserInputUI(mActivity.getIntent(), mActivity, FlagBox.MODE_ADD_DEVICE);
                return;
            }

            BeaconItemDto tItem = mAdapter.getItem(position - (mGridView.getHeaderViewCount() * mGridView.getNumColumns()));
            ActivityControl.openJobReportsActivity(getIntent(), mActivity, tItem.MAC, tItem.sticker, FlagBox.REQUEST_HAND_OUT);
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            final int tPosition = (int) view.getTag();
            final BeaconItemDto tItem = mAdapter.getItem(tPosition);

            switch (view.getId()) {

                case R.id.item_btn_delete : //삭제 ---->
                case R.id.item_btn_delete_2 : //삭제 ---->
                case R.id.item_btn_delete_3 : //삭제 ---->
                case R.id.item_sticker :

                    JobProcessController.getInstance(mActivity, ActivityJobMain.this).removeDevice(tItem.MAC, tItem.sticker);

                    break;

                case R.id.item_btn_temp_range : //온도범위 설정 ----->

                    JobProcessController.getInstance(mActivity, ActivityJobMain.this).setTemperatureRange(tItem.MAC, tItem.min_temperature_limit, tItem.max_temperature_limit);

                    break;

                case R.id.item_btn_send_photo :
                    JobProcessController.getInstance(mActivity, ActivityJobMain.this).sendPhoto(tItem.MAC);
                    break;

                case R.id.item_report :

                    boolean showDetail = !mAdapter.getItem(0).$show_detail;
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        mAdapter.getItem(i).$show_detail = showDetail;
                    }

                    mAdapter.notifyDataSetChanged();

                    /** 일단 여기에 */
                    JobProcessController.getInstance(mActivity, ActivityJobMain.this).doRetrieveData(mAdapter.getItem(tPosition).MAC);

                    break;

                case R.id.item_container :
                    ActivityControl.openJobReportsActivity(getIntent(), mActivity, tItem.MAC, tItem.sticker, FlagBox.REQUEST_HAND_OUT);
                    break;

            }

        }
    };


    private AdapterView.OnItemLongClickListener mItemLongCliclListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            BeaconItemDto tItem = mAdapter.getItem(position - (mGridView.getHeaderViewCount() * mGridView.getNumColumns()));
            //JobProcessController.getInstance(mActivity, ActivityJobMain.this).removeDevice(tItem.MAC, tItem.sticker);
            JobProcessController.getInstance(mActivity, ActivityJobMain.this).doRetrieveData (tItem.MAC);

            return true;
        }
    };
    /** ------------------------------------------------------------------------------------------------------------------- */



    //UI update timer -----------------------------------------------------------------------------------------------------------------------------
    private Timer mTimer;
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initData();

                }
            });
        }
    };
}
