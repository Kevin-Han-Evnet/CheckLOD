package com.netmania.checklod.tnt.activities.job;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;
import com.balysv.materialripple.MaterialRippleLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.netmania.checklod.general.AlarmPlayer;
import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.activities.IBaseJobActivity;
import com.netmania.checklod.general.data.DBHelper;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.LoggerData;
import com.netmania.checklod.general.dto.LoggerDataNew;
import com.netmania.checklod.general.dto.TemperatureTrackingDto;
import com.netmania.checklod.general.http.BaseAPI;
import com.netmania.checklod.general.http.MonitoringAPI;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.DialogUtils;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.general.utils.StringUtils;
import com.netmania.checklod.tnt.BaseApplication;
import com.netmania.checklod.tnt.R;
import com.netmania.checklod.tnt.adapter.TemperReportListAdapter;
import com.netmania.checklod.tnt.data.DataProviderUtil;
import com.netmania.checklod.tnt.manage.Constants;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import lib.netmania.ble.model.BeaconDataModel;
import lib.netmania.ble.model.BeaconDataModelNew;

/**
 * Created by hansangcheol on 2017. 12. 19..
 */

public class ActivityDataSync extends BaseActivity implements View.OnClickListener, View.OnLongClickListener, IBaseJobActivity, JobProcessController.JobProcessCallback {


    //상수
    public static final String EXTRA_KEY_MACADDRESS = "extraKeyMacAddress";
    public static final long SCAN_PERIOD = 1000 * 10;

    public static final int BTN_TAG_JOB_RECEIVE = 0;
    public static final int BTN_TAG_JOB_SEND = 1;
    public static final int BTN_TAG_JOB_RECONNECT = 2;


    //UI
    private View step_2_container, bcn_control_menu_container;
    private MenuItem ic_wifi, ic_sticker;
    private MaterialRippleLayout btn_next_step_container;
    private Button btn_clear_data, btn_disconnect, btn_restart_app, btn_next_step;

    //UI -- for beacon item
    public View item_container, item_info_container, item_ready_container, item_delivery_container, item_handover_container, item_invoice_container, item_complete_container, item_data_container, item_cargo_container;
    public TextView item_sticker, item_temperature_rage, item_temp_probe, item_temp_char, item_seq, fc_item_max_temperature, fc_item_min_temperature, item_timestamp;
    public ImageView item_ble_status, item_report;
    public Button item_btn_delete, item_btn_delete_2, item_btn_delete_3, item_btn_temp_range, item_btn_send_photo;


    //데이타
    private BeaconItemDto DATA;
    private String MAC;


    //객체 -- for beacon
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> deviceList;
    private Map<String, Integer> devRssiValues;
    private boolean mScanning = false;



    /** for step 2 ------------------------------------------------------------------------------------ */

    //UI
    private View top_container_asset, progressGo, mHeaderView;
    private LineChart item_chart;
    private ListView mListView;
    private Button btn_send_to_server;
    private TextView delivery_duration_field, item_min_temperature, item_max_temperature;
    private MaterialRippleLayout btn_send_to_server_container;


    //데이타
    private TemperReportListAdapter mAdapter;

    //객체
    /** for step 2 ------------------------------------------------------------------------------------ */



    /** for bluetooth gatt connect -------------------------------------------------------------------- */
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private LoggerData mLogger;
    private LoggerDataNew mLoggerNew;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private boolean connected = false;
    /** for bluetooth gatt connect -------------------------------------------------------------------- */



    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_sync_step_1);


        //등장애니..
        overridePendingTransition(R.anim.push_left_in_fast, R.anim.push_left_out_half);

        MAC = getIntent().getStringExtra(EXTRA_KEY_MACADDRESS);
        DATA = new BeaconItemDto ();
        DATA.sticker = JobProcessController.getInstance(mActivity, this).getStickerForService(MAC);
        DATA.MAC = MAC;
        DATA.min_temperature_limit = 0;
        DATA.max_temperature_limit = 0;

        mLogger = new LoggerData();
        mLoggerNew = new LoggerDataNew();

        initBluetoothAdapter ();
        setLayout ();
        initData ();
    }


    /** 블루투스 어댑터 이니셜라이즈
     *
     */
    private void initBluetoothAdapter () {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        deviceList = new ArrayList<BluetoothDevice>();
        devRssiValues = new HashMap<String, Integer>();

        if (!mScanning) scanLeDevice(true);
    }



    /** 레이아웃 쎄팅
     *
     */
    private void setLayout () {
        //로고표현
        getActionBar().setIcon(R.mipmap.topper_logo);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(true);


        bcn_control_menu_container = findViewById(R.id.bcn_control_menu_container);

        btn_clear_data = (Button) findViewById(R.id.btn_clear_data);
        btn_clear_data.setOnClickListener(this);

        btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
        btn_disconnect.setOnClickListener(this);

        btn_restart_app = (Button) findViewById(R.id.btn_restart_app);
        btn_restart_app.setOnClickListener(this);


        item_container = findViewById(R.id.item_container);
        item_sticker = (TextView) findViewById(R.id.item_sticker);

        item_btn_temp_range = (Button) findViewById(R.id.item_btn_temp_range);
        item_btn_temp_range.setOnClickListener(this);
        item_btn_temp_range.setVisibility(View.GONE);

        findViewById(R.id.item_ble_check).setVisibility(View.GONE);

        item_temperature_rage = (TextView) findViewById(R.id.item_temperature_rage);
        item_temperature_rage.setText(R.string.info_takeover_temp_range_2);

        item_ble_status = (ImageView) findViewById(R.id.item_ble_status);
        item_ble_status.setImageResource(R.drawable.ic_bluetooth_off);
        item_temp_probe = (TextView) findViewById(R.id.item_temp_probe);

        item_btn_delete = (Button) findViewById(R.id.item_btn_delete);
        item_seq = findViewById(R.id.item_seq);

        btn_next_step_container = (MaterialRippleLayout) findViewById(R.id.btn_next_step_container);
        btn_next_step = (Button) findViewById(R.id.btn_next_step);
        btn_next_step.setOnClickListener(this);



        //상황별 셋팅 ----------------------------------------------------------------------------------------
        item_info_container = findViewById(R.id.item_info_container);

        item_ready_container = findViewById(R.id.item_ready_container);
        item_ready_container.setVisibility(View.GONE);

        item_delivery_container = findViewById(R.id.item_ready_container);
        item_delivery_container.setVisibility(View.GONE);

        item_handover_container = findViewById(R.id.item_ready_container);
        item_handover_container.setVisibility(View.GONE);

        item_invoice_container = findViewById(R.id.item_ready_container);
        item_invoice_container.setVisibility(View.GONE);

        item_complete_container = findViewById(R.id.item_ready_container);
        item_complete_container.setVisibility(View.GONE);

        item_cargo_container = findViewById(R.id.item_ready_container);
        item_cargo_container.setVisibility(View.GONE);



        //for step 2 -----------------------------------------------------------------------------------------------------------------------
        step_2_container = findViewById(R.id.step_2_container);
        step_2_container.setVisibility(View.GONE);




        mHeaderView = mInflater.inflate(R.layout.layout_header_job_report_list, null);

        item_chart = (LineChart) mHeaderView.findViewById(R.id.item_chart);

        delivery_duration_field = (TextView) mHeaderView.findViewById(R.id.delivery_duration_field);
        delivery_duration_field.setText("");

        item_min_temperature = (TextView) mHeaderView.findViewById(R.id.item_min_temperature);
        item_min_temperature.setText("");

        item_max_temperature = (TextView) mHeaderView.findViewById(R.id.item_max_temperature);
        item_max_temperature.setText("");

        mListView = (ListView) findViewById(R.id.listView);
        mListView.addHeaderView(mHeaderView);
        mAdapter = new TemperReportListAdapter(mActivity, new ArrayList<TemperatureTrackingDto>(), this);
        mListView.setAdapter(mAdapter);


        //스크롤로 다음페이지. 이거 좀 어색하긴한데. -_-;;
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                /* LogUtil.I("뭐가 보이나!!! --> "
                        + mListView.getRefreshableView().getFirstVisiblePosition()
                        + " -- " + mListView.getRefreshableView().getChildAt(0).getTop());*/
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //nothing yet;
            }

        });

        btn_send_to_server_container = (MaterialRippleLayout) findViewById(R.id.btn_send_to_server_container);
        btn_send_to_server = (Button) findViewById(R.id.btn_send_to_server);
        btn_send_to_server.setTag(BTN_TAG_JOB_RECEIVE);
        btn_send_to_server.setText(R.string.label_btn_receive_to_phone);
        btn_send_to_server.setOnClickListener(this);

        /** 자동으로 고고고 **/
        if (Constants.IS_RELEASED) {
            btn_send_to_server_container.setVisibility(View.GONE);
            bcn_control_menu_container.setVisibility(View.GONE);
        } else {
            btn_send_to_server_container.setVisibility(View.VISIBLE);
            bcn_control_menu_container.setVisibility(View.VISIBLE);
        }


        progressGo = findViewById(R.id.progressGo);
        progressGo.setVisibility(View.GONE);
    }


    /** 데이타 이니셜라이징
     *
     */
    private void initData () {
        item_sticker.setText(DATA.sticker);
        item_temp_probe.setPaintFlags(item_temp_probe.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        item_temp_probe.setText(Constants.NO_DATA);
        item_ble_status.setImageResource(R.drawable.ic_bluetooth_off);


        //온도 범위 설정 버튼 값 셋팅
        if (isTemperatureRangeSet ()) {
            item_btn_temp_range.setText(R.string.label_btn_temp_range);
            item_btn_temp_range.setSelected(false);
        } else {
            item_btn_temp_range.setText(DATA.min_temperature_limit + "℃ ~ " + DATA.max_temperature_limit + "℃");
            item_btn_temp_range.setSelected(true);
        }

        //ble 신호 함 봅시다...
        if (DATA.bcn_status == BeaconDataModel.BCN_STATUS_OFF) {
            item_ble_status.setImageResource(R.drawable.ic_bluetooth_off);
        } else {
            item_ble_status.setImageResource(R.drawable.ic_bluetooth_on);
        }
    }


    /** 배송정보 요약 리포트
     *
     */
    private double maxTemp = 0.0;
    private double minTemp = 0.0;
    private void updateDeliveryReport () {

        if (mAdapter == null || mAdapter.getCount() == 0) return;

        //이제 배송 정보 요약 ------------------------------------------------------------------------------------------------------------------------------


        String max = String.format(getResources().getString(R.string.job_delivery_temp_max_format), String.valueOf(maxTemp));
        String min = String.format(getResources().getString(R.string.job_delivery_temp_min_format), String.valueOf(minTemp));
        item_max_temperature.setText(max);
        item_min_temperature.setText(min);



        long start = 0;
        long end = 0;

        try {
            start = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(mAdapter.getItem(0).rtc).getTime();
            end = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(mAdapter.getItem((mAdapter.getCount() - 1)).rtc).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long duration = end - start;

        String t = String.format(
                getResources().getString(R.string.info_delivery_duration_for_release),
                StringUtils.getMilliSecondsToTimeString (duration).split(",")[1]
        );
        delivery_duration_field.setText(t);

        if (StringUtils.isEmpty(DATA.sticker) && ic_sticker != null) ((TextView) ic_sticker.getActionView().findViewById(R.id.item_sticker)).setText(DATA.sticker);
    }




    /** 온도는 셋팅 되었는가??
     *
     * @return
     */
    private boolean isTemperatureRangeSet () {
        return (DATA.max_temperature_limit - DATA.min_temperature_limit == 0);
    }


    /** ble scan start
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }


    /** 스캔콜백 리스너
     *
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addDevice(device,rssi);
                }
            });
        }
    };


    /** 디바이스 타겟 추가
     *
     * @param device
     * @param rssi
     */
    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;
        boolean isAcceptable = true;

        // no device name pass
        if( device.getName() == null ) {
            //LogUtil.I(TAG, "No Name pass:" + device.getAddress() );
            isAcceptable = false;
        }

        // no device BLE type pass
        if( device.getType() != BluetoothDevice.DEVICE_TYPE_LE ) {
            //LogUtil.I(TAG, "No BLE pass:" + device.getAddress() );
            isAcceptable = false;
        }

        if ( !isAcceptable ) return;

        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }


        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
            deviceList.add(device);
        }

        if (device.getAddress().equals(DATA.MAC)) {
            DATA.bcn_status = BeaconDataModel.BCN_STATUS_READY;
        }

        initData();
    }



    /** next step
     *
     */
    private void nextStep () {


        /*/온도 범위는 셋팅 되어 있는가??
        if (isTemperatureRangeSet()) {
            DialogUtils.alert(mActivity, R.string.alert_temperature_range_not_set);
            return;
        }*/

        //BLE 상태 검사
        if (DATA.bcn_status == BeaconDataModel.BCN_STATUS_OFF) {
            DialogUtils.alert(mActivity, R.string.alert_no_ble_signal, true);
            return;
        }


        step_2_container.setVisibility(View.VISIBLE);

        connectToGatt ();
    }


    /** gatt서버 접속 -----------------------------------------------
     *
     * @return
     */
    private String mBluetoothDeviceAddress;
    private boolean connectToGatt () {


        String msg = String.format(getResources().getString(R.string.data_sync_connecting_beacon), DATA.sticker);
        updateBeaconConnectionState (msg);
        btn_send_to_server.setText(R.string.label_btn_receive_to_phone);
        btn_send_to_server.setTag(BTN_TAG_JOB_RECEIVE);


        if (mBluetoothAdapter == null || DATA.MAC == null) {
            LogUtil.I(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && DATA.MAC.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            LogUtil.I(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(DATA.MAC);
        if (device == null) {
            LogUtil.I(TAG, "Device not found.  Unable to connect.");

            msg = String.format(getResources().getString(R.string.data_sync_cant_connect_beacon), DATA.sticker);
            updateBeaconConnectionState (msg);
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        LogUtil.I(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = DATA.MAC;
        mConnectionState = STATE_CONNECTING;


        return true;
    }


    /** GATT서버 메시지 수신 초기화
     *
     */
    private void enableCmdReceveToGatt () {
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null)
        {
            LogUtil.I (TAG, "Rx service not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            LogUtil.I (TAG, "Tx charateristic not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar,true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }


    /** Gatt 서버에 메시지 전송
     *
     */
    public void sendCmdToGatt (byte byteCmd) {


        byte [] byteValues = {byteCmd};

        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        LogUtil.I (TAG,"mBluetoothGatt null --> " + mBluetoothGatt);

        if (RxService == null) {
            LogUtil.I  (TAG, "Rx service not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }

        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            LogUtil.I (TAG, "Rx charateristic not found!");
            return;
        }
        RxChar.setValue(byteValues);
        boolean status = mBluetoothGatt.writeCharacteristic(RxChar);

        LogUtil.I (DebugTags.TAG_SHIT, "write TXchar - status=" + status);
    }

    /** Gatt 서버에 메시지 전송
     *
     */
    public void sendCmdToGattNew (byte byteCmd, int tParam) {

        LogUtil.I(DebugTags.TAG_SHIT, "tParam --> " + tParam);

        byte[] tag = "CL".getBytes();
        byte[] params = ByteBuffer.allocate(4).putInt(tParam).array();
        byte [] byteValues = {tag[0], tag[1], byteCmd, params[0], params[1], params[2], params[3]};

        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        LogUtil.I (DebugTags.TAG_SHIT,"mBluetoothGatt null"+ mBluetoothGatt);
        if (RxService == null) {
            LogUtil.I  (TAG, "Rx service not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            LogUtil.I (TAG, "Rx charateristic not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        RxChar.setValue(byteValues);
        boolean status = mBluetoothGatt.writeCharacteristic(RxChar);

        LogUtil.I (TAG, "write TXchar - status=" + status);
    }


    /** 비콘 연결 상태 업데이트
     *
     */
    private void updateBeaconConnectionState (final String msg) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        delivery_duration_field.setText(msg);
                    }
                }
        );
    }


    /** 받은 데이타 처리
     *
     * @param characteristic
     */
    private void updateAndHandleGatt (final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(

                new Runnable() {
                    public void run() {
                        byte[] rxBytes = characteristic.getValue();
                        String tag = new String(new byte[]{rxBytes[0], rxBytes[1]});

                        if (tag.equals("CL")) {
                            treatLoggerDataNew (rxBytes);
                        } else {
                            treatLoggerData (rxBytes);
                        }
                    }
                }
            );
    }


    /** 기존 로거 데이타
     *
     * @param rxBytes
     */
    private void treatLoggerData (final byte[] rxBytes) {

        try {



            //String mStrRsp = new String(rxValue, "UTF-8");
            String strRspHex = String.format("len:%d=",rxBytes.length);
            for (final byte b : rxBytes) {
                strRspHex += String.format("%02X:", b & 0xFF);
            }

            /*String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
            LogUtil.I("[" + currentDateTimeString + "]RX:" + strRspHex);*/

            byte scnRspType = mLogger.scanResponse(mLogger, rxBytes);
            boolean rspResult = mLogger.getRspResult();

            //switch( scnRspId )  // constant err , use if else instead switch after ADT14
            if (scnRspType == mLogger.BYTE_RSP_START) {
                LogUtil.I("RSP<Total Records : " + mLogger.getRecordSize() + ">");
                LogUtil.I("RSP<Beacon Cycle : " + mLogger.getBcnCycleSec() + ">");
            } else if (scnRspType == mLogger.BYTE_RSP_RECORD) {
                mLogger.updateRecord(rxBytes);
                //LogUtil.I(mLogger.toString());


                LogUtil.I (DebugTags.TAG_SHIT, "R---------------------------------------------------R");
                LogUtil.I (DebugTags.TAG_SHIT, "m_sSeqNo = " + mLogger.m_sSeqNo);
                LogUtil.I (DebugTags.TAG_SHIT, "m_dInTemp = " + mLogger.m_dInTemp);
                LogUtil.I (DebugTags.TAG_SHIT, "m_dInRh = " + mLogger.m_dInRh);
                LogUtil.I (DebugTags.TAG_SHIT, "m_dExTemp = " + mLogger.m_dExTemp);
                LogUtil.I (DebugTags.TAG_SHIT, "m_dExRh = " + mLogger.m_dExRh);
                LogUtil.I (DebugTags.TAG_SHIT, "m_dBatV = " + mLogger.m_dBatV);
                LogUtil.I (DebugTags.TAG_SHIT, "m_strRtc = " + mLogger.m_strRtc);
                LogUtil.I (DebugTags.TAG_SHIT, "R---------------------------------------------------R");


                //현재 배송중인 데이타 아니면 아웃. ----------------------------------------------
                int finalSeqFromServer = DataProviderUtil.getInstance(mApp).getSavedFinalData(DATA.MAC).seq;
                if (finalSeqFromServer >= mLogger.m_sSeqNo) {
                    progressGo.setVisibility(View.VISIBLE);
                    //toastIt(R.string.toast_sync_data_select);
                    sendCmdToGatt(LoggerData.BYTE_CMD_RECORD);
                    return;
                }
                progressGo.setVisibility(View.GONE);
                //현재 배송중인 데이타 아니면 아웃. ----------------------------------------------

                TemperatureTrackingDto cItem = new TemperatureTrackingDto();
                cItem.ble_status = BeaconDataModel.BCN_STATUS_READY;
                cItem.seq = mLogger.m_sSeqNo;
                cItem.MAC = DATA.MAC;
                cItem.$failed_count = 0;
                cItem.idx = 0;
                cItem.outside_hum = String.valueOf (mLogger.m_dExRh);
                cItem.hum = String.valueOf(mLogger.m_dInRh);
                cItem.outside_temp = String.valueOf(mLogger.m_dExTemp);
                cItem.temp = String.valueOf(mLogger.m_dInTemp);

                //00-01-0100:07:22
                Date tRTC = new Date();
                try {
                    tRTC = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(mAdapter.getItem(mAdapter.getCount() - 1).rtc);
                    tRTC.setTime(tRTC.getTime() + Constants.BASE_SIGNAL_CYCLE); //-> 1분 단위
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                cItem.rtc = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).format(tRTC);
                cItem.timestamp = Calendar.getInstance().getTimeInMillis();
                cItem.sent = TemperatureTrackingDto.NOT_REPORTED;


                long t = new DBHelper(BaseApplication.getInstance()).insertWithAsset(cItem.getTblName(), cItem.getInsertAsset());
                cItem.idx = (int) t;

                if (cItem.idx >= 0) {

                    mAdapter.add(cItem);
                    mListView.smoothScrollToPosition(mAdapter.getCount());

                    if (maxTemp <= mLogger.m_dInTemp || maxTemp == 0.0) maxTemp = mLogger.m_dInTemp;
                    if (minTemp >= mLogger.m_dInTemp || minTemp == 0.0) minTemp = mLogger.m_dInTemp;

                    updateDeliveryReport();

                } else {
                    //nothing yet;
                }



                sendCmdToGatt (mLogger.BYTE_CMD_RECORD);


            } else if(
                    scnRspType == mLogger.BYTE_RSP_EMPTY) {
                LogUtil.I("RSP<Data Download Fin>");
                showMeYouDancing (mAdapter.getItems());


                btn_send_to_server.setTag(BTN_TAG_JOB_SEND);
                btn_send_to_server.setText(R.string.label_btn_send_to_server);

                progressGo.setVisibility(View.GONE);

                if (Constants.IS_RELEASED) startUploadProcess ();

            } else if (scnRspType == mLogger.BYTE_RSP_CLEAR) {
                if( rspResult ) {
                    LogUtil.I("RSP<OK Data Cleared>");
                } else {
                    LogUtil.I("RSP<FAIL Data Cleared>");
                }
            } else if (scnRspType == mLogger.BYTE_RSP_DISCON) {
                if( rspResult ) {
                    LogUtil.I("RSP<OK Disconnected>");
                } else {
                    LogUtil.I("RSP<FAIL Disconnected>");
                }
            } else {
                return;
            }


            //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /** 새로거 데이타
     *
     * @param rxBytes
     */
    private void treatLoggerDataNew (final byte[] rxBytes) {

        mLoggerNew.updateRecord(rxBytes);


        TemperatureTrackingDto cItem = new TemperatureTrackingDto();
        cItem.ble_status = BeaconDataModel.BCN_STATUS_READY;
        cItem.seq = Integer.valueOf(mLoggerNew.m_sSeqNo);
        cItem.MAC = DATA.MAC;
        cItem.$failed_count = 0;
        cItem.idx = 0;
        cItem.outside_hum = String.valueOf (mLoggerNew.dHumOnChip);
        cItem.hum = String.valueOf(mLoggerNew.dHum);
        cItem.outside_temp = String.valueOf(mLoggerNew.dTempOnChip);
        cItem.temp = String.valueOf(mLoggerNew.dTemp);



        //00-01-0100:07:22
        Date tRTC = new Date();
        try {
            tRTC = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(mAdapter.getItem(mAdapter.getCount() - 1).rtc);
            tRTC.setTime(tRTC.getTime() + Constants.NEW_LOGGER_CYCLE); //-> 1분 단위
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cItem.rtc = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).format(tRTC);
        cItem.timestamp = mAdapter.getItem(mAdapter.getCount() - 1).timestamp + Constants.NEW_LOGGER_CYCLE;
        cItem.sent = TemperatureTrackingDto.NOT_REPORTED;


        long t = new DBHelper(BaseApplication.getInstance()).insertWithAsset(cItem.getTblName(), cItem.getInsertAsset());
        cItem.idx = (int) t;

        if (cItem.idx >= 0) {

            mAdapter.add(cItem);
            mListView.smoothScrollToPosition(mAdapter.getCount());

            if (maxTemp <= mLoggerNew.dTemp || maxTemp == 0.0) maxTemp = mLoggerNew.dTemp;
            if (minTemp >= mLoggerNew.dTemp || minTemp == 0.0) minTemp = mLoggerNew.dTemp;



        } else {
            //nothing yet;
        }

        if (mLoggerNew.hasNext == 1) {
            sendCmdToGattNew (mLoggerNew.BYTE_CMD_RECORD, Integer.valueOf(mLoggerNew.m_sSeqNo) + 1);
        } else {

            showMeYouDancing (mAdapter.getItems());

            btn_send_to_server.setTag(BTN_TAG_JOB_SEND);
            btn_send_to_server.setText(R.string.label_btn_send_to_server);

            progressGo.setVisibility(View.GONE);

            if (Constants.IS_RELEASED) startUploadProcess ();
        }
    }


    /**
     *
     */
    private void showMeYouDancing (ArrayList<TemperatureTrackingDto> datas) {

        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(1f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = item_chart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);

        LimitLine ll1 = new LimitLine((float) DATA.max_temperature_limit, "");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        //ll1.setTypeface(tf);

        LimitLine ll2 = new LimitLine((float) DATA.min_temperature_limit, "");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        //ll2.setTypeface(tf);

        YAxis leftAxis = item_chart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaximum((float) (DATA.max_temperature_limit + 10));
        leftAxis.setAxisMinimum((float) (DATA.min_temperature_limit - 10));
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        item_chart.getAxisRight().setEnabled(false);

        // add data
        setData (item_chart, datas);

        // get the legend (only possible after setting data)
        Legend l = item_chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);

        for (IDataSet set : item_chart.getData().getDataSets()) {
            set.setDrawValues(false);
        }
        item_chart.invalidate();

        item_chart.animateX(3000);

        /*
        for (int i = 0; i < datas.size(); i++) {
            LogUtil.I ("온도도도도도도도도 ::: " + datas.get(i).temp);
        }
        */
    }



    /** 챠트 데이타 고고
     *
     * @param mChart
     * @param datas
     */
    private void setData(LineChart mChart, final ArrayList<TemperatureTrackingDto> datas) {

        ArrayList<Entry> values = new ArrayList<Entry>();

        if (datas != null && datas.size() > 0)
            for (int i = 0; i < datas.size(); i++) {
                float val = Float.valueOf(datas.get(i).temp);
                values.add(new Entry(i, val));
            }

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "온도추이");

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(1f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{13f, 2f}, 0f));
            set1.setFormSize(15.f);

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(mActivity, R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            }
            else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);
        }


        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(21, 76, 182));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                if (value < 1 || value >= datas.size()) return "";
                long tValue = datas.get((int) value).timestamp;
                return mFormat.format(new Date(tValue));
            }
        });
    }


    /** 업로드 갑시다.
     *
     */
    private ProgressDialog tProgress;
    ArrayList<TemperatureTrackingDto> targets;
    private void startUploadProcess () {
        //자동으로 업로드 합시다.

        sendCmdToGatt(LoggerData.BYTE_CMD_DISCON);
        mBluetoothGatt.disconnect();

        toastIt (R.string.toast_beacon_data_upload);
        tProgress = DialogUtils.progress(
                mActivity,
                mAdapter.getCount(),
                getResources().getString(R.string.label_btn_send_to_server),
                getResources().getString(R.string.toast_beacon_data_upload),
                false
        );

        if (mAdapter ==  null || mAdapter.getCount() == 0) return;

        targets = DataProviderUtil.getInstance(mApp).getNotReportedData(DATA.MAC);//(ArrayList<TemperatureTrackingDto>) mAdapter.getItems().clone();
        //JobProcessController.getInstance(mActivity, this).startMonitoring(DATA, true); //체크인 안함 인계니까..
        uploadData ();
    }


    /** 데이타 업로드
     *
     */
    private int tIdx = 0;
    private void uploadData () {

        if (Constants.TEST_FOR_LOCAL_ONLY) {

            for (int i = 0; i < targets.size(); i++) {
                DataProviderUtil.getInstance(mActivity).updateTemperatureData(String.valueOf(targets.get(i).idx));
            }

            tProgress.setProgress(tProgress.getMax());
            mHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            startLiveTracking ();
                        }
                    }, 1000
            );

            return;
        }


        final int updateIdx = mAdapter.getItem(tIdx).idx;

        MonitoringAPI.updateBeaconInfo(
                mActivity,
                false,
                DATA.MAC,
                mAdapter.getItem(tIdx).seq,
                mAdapter.getItem(tIdx).rtc,
                Float.valueOf(mAdapter.getItem(tIdx).temp),
                Float.valueOf(mAdapter.getItem(tIdx).hum),
                Float.valueOf(mAdapter.getItem(tIdx).outside_temp),
                Float.valueOf(mAdapter.getItem(tIdx).outside_hum),
                mAdapter.getItem(tIdx).timestamp,
                new BaseAPI.ApiMapListenerWithFailedRest() {
                    @Override
                    public void onComplete() {
                        checkAndGo (updateIdx);
                    }

                    @Override
                    public void onComplete(Map<String, Object> map) {
                        checkAndGo (updateIdx);
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


        /*final int end = (targets.size() > 10) ? 10 : targets.size();
        final ArrayList<TemperatureTrackingDto> t = new ArrayList<>(targets.subList(0, end - 1));

        MonitoringAPI.updateBeaconInfo(
                mActivity,
                false,
                t,
                new BaseAPI.ApiMapListenerWithFailedRest() {
                    @Override
                    public void onComplete() {
                        checkAndGo (end, t);
                    }

                    @Override
                    public void onComplete(Map<String, Object> map) {
                        checkAndGo (end, t);
                    }

                    @Override
                    public void onFailed(AjaxStatus result) {
                        //nothign yet;
                    }

                    @Override
                    public void onFailed(AjaxStatus result, Map<String, Object> map) {
                        //nothign yet;
                    }
                }
        );*/
    }


    /** 체크하해서 업로드 혹은 종료
     *
     * @param updateIdx
     */
    private void checkAndGo (int updateIdx) {
        tProgress.setProgress(tProgress.getProgress() + 1);
        DataProviderUtil.getInstance(mApp).updateTemperatureData(String.valueOf(updateIdx));
        tIdx += 1;
        if (tIdx < mAdapter.getCount() - 1) {
            uploadData();
        } else {


            tProgress.setProgress(tProgress.getMax());
            mHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            startLiveTracking ();
                        }
                    }, 1000
            );
        }
    }


    /**
     *
     */
    private void checkAndGo (int end, ArrayList<TemperatureTrackingDto> t) {
        tProgress.setProgress(tProgress.getProgress() + end);
        for (int i = 0; i < t.size(); i++) {
            DataProviderUtil.getInstance(mActivity).updateTemperatureData(String.valueOf(t.get(i).idx));
        }

        if (end < mAdapter.getCount()) {
            uploadData();
        } else {

            /*DialogUtils.confirm(
                    mActivity,
                    R.string.confirm_beacon_clear,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendCmdToGatt(mLogger.BYTE_CMD_DISCON);
                            startLiveTracking ();
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startLiveTracking ();
                        }
                    }
            );*/

            tProgress.setProgress(tProgress.getMax());
            startLiveTracking ();
        }
    }


    /** 라이프 트래킹 시작
     *
     */
    private void startLiveTracking () {
        tProgress.dismiss();

        /**

         public int is_tookover = INT_FALSE;
         public int delivery_step = DELEVERY_STEP_READY;
         public int is_data_checked_in = INT_FALSE;
         public int is_data_downloaded = INT_FALSE;
         */

        String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                + "isLive=" + BeaconItemDto.INT_TRUE + ", "
                + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_DELIVERY + ", "
                + "is_tookover=" + BeaconItemDto.INT_TRUE + ", "
                + "is_data_checked_in=" + BeaconItemDto.INT_TRUE + ", "
                + "is_data_downloaded=" + BeaconItemDto.INT_TRUE
                + " WHERE MAC='" + DATA.MAC + "'";
        new DBHelper(BaseApplication.getInstance()).update(qry);

        finish ();
    }



    /** 데이타 싱크 처리
     *
     * @param tag
     */
    private void doSyncData (int tag) {


        switch (tag) {
            case BTN_TAG_JOB_RECEIVE :
                startDownloadServerData ();
                break;

            case BTN_TAG_JOB_SEND :
                startUploadProcess();
                break;

            case BTN_TAG_JOB_RECONNECT :
                nextStep ();
                break;
        }

    }


    /** 토스트 메시지
     *
     */
    private Toast t;
    private void toastIt (final int str) {
        toastIt(str, Toast.LENGTH_SHORT);
    }
    private void toastIt (final int str, final int term) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {

                        if (t != null) {
                            t.cancel();
                            t = null;
                        }

                        t = Toast.makeText(mActivity, str, term);
                        t.show();
                    }
                }
        );
    }


    /** 서버에서 기존 데이타 다운로드
     *
     */
    private void startDownloadServerData () {
        //sendCmdToGatt(mLogger.BYTE_CMD_RECORD);
        progressGo.setVisibility(View.VISIBLE);
        JobProcessController.getInstance(mActivity, this).doTakeOver(DATA.MAC);
    }



    /** 비콛 데이타 다운로드
     *
     */
    private void doDownloadDataFromBeacon () {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        progressGo.setVisibility(View.GONE);

                        ArrayList<TemperatureTrackingDto> datasFromServer = DataProviderUtil.getInstance(mApp).getReportedTemperatures(DATA.MAC, false, 0, 0, false);
                        mAdapter.addAll(datasFromServer);
                        mListView.smoothScrollToPosition(mAdapter.getCount());

                    }
                }
        );

        //LogUtil.I(DebugTags.TAG_SHIT, "RTC -----> " + mAdapter.getItem (mAdapter.getCount() - 1).rtc);

        toastIt(R.string.toast_beacon_data_download);

        //새 로거인지 판단...
        if (Double.valueOf(mAdapter.getItem(mAdapter.getCount() - 1).hum) > 0) {
            sendCmdToGatt(mLogger.BYTE_CMD_RECORD);
        } else {
            sendCmdToGattNew(mLogger.BYTE_CMD_RECORD, mAdapter.getItem (mAdapter.getCount() - 1).seq + 1);
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
        inflater.inflate(R.menu.job_data_sync_actions, menu);

        ic_wifi = menu.findItem(R.id.ic_wifi);
        ic_sticker = menu.findItem(R.id.ic_sticker);

        ((TextView) ic_sticker.getActionView().findViewById(R.id.item_sticker)).setText(DATA.sticker);
        updateWifiStatus ();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.ic_wifi :
                //nothing yet;
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //리스너 ------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void updateWifiStatus() {
        if (ic_wifi == null) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int status = GeneralUtils.getNetworkStatus(mApp.getApplicationContext());
                int icon = R.drawable.ic_wifi_off;


                switch (status) {
                    case GeneralUtils.NETWORK_STATUS_NONE :
                        icon = R.drawable.ic_wifi_off;
                        break;

                    case GeneralUtils.NETWORK_STATUS_MOBILE :
                        icon = R.drawable.ic_lte_on;
                        break;

                    case GeneralUtils.NETWORK_STATUS_WIFI :
                        icon = R.drawable.ic_wifi_on;
                        break;
                }

                ic_wifi.setIcon(icon);
            }
        });

    }

    @Override
    public void updateBatteryStatus() {
        //nothing yet;
    }

    @Override
    public int getBeaconCount() {
        return 0;
    }

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
    public void doDisableMe(String mac) {
        //nothing yet;
    }


    /** 온클릭
     *
     * @param view
     */
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.item_btn_temp_range :
                JobProcessController.getInstance(mActivity, this).setTemperatureRange(DATA.MAC, DATA.min_temperature_limit, DATA.max_temperature_limit);
                break;

            case R.id.btn_next_step :
                nextStep ();
                break;

            case R.id.btn_send_to_server :
                int btnTag = (int) view.getTag();
                doSyncData (btnTag);
                break;

            case R.id.btn_clear_data :
                sendCmdToGatt(mLogger.BYTE_CMD_CLERA);
                break;

            case R.id.btn_disconnect :
                sendCmdToGatt(mLogger.BYTE_CMD_DISCON);
                break;

            case R.id.btn_restart_app :


                DialogUtils.confirm(
                        mActivity,
                        R.string.confrim_reset_for_job,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mAdapter.clear();
                                mAdapter.notifyDataSetChanged();

                                AlarmPlayer.getInstance(mApp).stopAll();


                                dialogInterface.dismiss();

                                new DBHelper(BaseApplication.getInstance()).delete(TemperatureTrackingDto.getInstance().getTblName(), "", null);
                                new DBHelper(BaseApplication.getInstance()).delete(BeaconItemDto.getInstance().getTblName(), "", null);

                                mApp.finishAll();
                                mApp.restartApplication();

                                try {
                                    mApp.stopSensingProcessService();
                                    mApp.stopHttpProcessService();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, false
                );


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
        //nothing yet;
        return false;
    }


    /** 뒤로가기
     *
     */
    @Override
    public void onBackPressed () {

        if (step_2_container.getVisibility() == View.VISIBLE) {
            //step_2_container.setVisibility(View.GONE);
            return;
        }

        super.onBackPressed();
        //퇴장애니...
    }



    //리스너 ---------------------------------------------------------------------------------------------------

    @Override
    public void onCompleteAddBeaconDevice(String mac, int start_option) {

    }

    @Override
    public void onFailedAddBeaconDevice(String mac, int failed_reson) {

    }

    @Override
    public void onTemperatureRangeSelected(String mac, double minimum, double maximum) {

    }

    @Override
    public void onCompleteRemoveBeacon(String mac) {

    }

    /** for JobProcessController */
    @Override
    public void onTakeOverSuccess(String mac) {
        doDownloadDataFromBeacon ();
    }

    @Override
    public void onTakeOverFailed(String mac, int statusCode) {
        //nothing yet;
    }

    @Override
    public void onStartCheckOut(String mac) {
        //nothing yet;
    }

    @Override
    public void onCheckOutSuccess(String mac) {
        //nothing yet;
    }

    @Override
    public void onCheckOutFailed(String mac) {

    }



    @Override
    public void onHandOutSuccess(Map<String, Object> map, File file, String selectedLogerId) {
        //nothing yet;
    }

    @Override
    public void onHandOutSuccess(File file, String selectedLogerId) {
        //nothing yet;
    }

    @Override
    public void onHandOutFailed(int statusCode, String selectedLogerId) {
        //nothing yet;
    }

    @Override
    public void onCargoBaseStart(String mac) {
        //nothing yet;
    }

    @Override
    public void onDataCleared() {
        //nothign yet;
    }

    @Override
    public void onPhotoCompositionItemSelected(File file) {
        //nothign yet;
    }

    @Override
    public void onNotReportedDataSendComplete(String mac, int process) {
        //nothing yet;
    }

    @Override
    public void onNotReportedDataSendFailed(String mac, int process) {
        //nothing yet;
    }



    /** bluetooth gatt listener
     *
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                //broadcastUpdate(intentAction);
                LogUtil.I(TAG, "Connected to GATT server.");
                LogUtil.I(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

                String msg = String.format(getResources().getString(R.string.data_sync_connected_beacon), DATA.sticker);
                updateBeaconConnectionState (msg);

                if (Constants.IS_RELEASED) {
                    btn_send_to_server_container.setVisibility(View.GONE);
                }

                connected = true;

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                LogUtil.I(TAG, "Disconnected from GATT server.");
                //broadcastUpdate(intentAction);

                String msg = String.format(getResources().getString(R.string.data_sync_connecting_beacon), DATA.sticker);
                updateBeaconConnectionState (msg);

                mConnectionState = STATE_DISCONNECTED;

                ActivityDataSync.this.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                btn_send_to_server.setText(R.string.label_btn_reconnect);
                                btn_send_to_server.setTag(BTN_TAG_JOB_RECONNECT);
                                btn_send_to_server_container.setVisibility(View.VISIBLE);
                            }
                        }
                );


                if (!connected && Constants.IS_RELEASED) {

                    Toast.makeText(mActivity, R.string.toast_can_not_connect_to_beacon, Toast.LENGTH_SHORT).show();
                    step_2_container.setVisibility(View.GONE);
                    onBackPressed();
                }

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.I(TAG, "mBluetoothGatt = " + mBluetoothGatt );
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                LogUtil.I(TAG, "onServicesDiscovered received: " + status);
            }

            enableCmdReceveToGatt ();


            JobProcessController.getInstance(mActivity, ActivityDataSync.this).addTrackingDevice(DATA.MAC, JobProcessController.DELIVERY_START_OPTION_CARGO);

            if (Constants.IS_RELEASED) {
                //자동으로 시작합시다.
                mHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                startDownloadServerData ();
                            }
                        }, 1000
                );
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                updateAndHandleGatt (characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            updateAndHandleGatt (characteristic);
        }
    };
}
