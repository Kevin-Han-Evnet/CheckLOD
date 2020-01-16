package lib.netmania.ble;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import lib.netmania.ble.callback.NetManiaBleCallbackListener;
import lib.netmania.ble.model.BeaconDataModel;
import lib.netmania.ble.model.BeaconDataModelNew;
import lib.netmania.ble.model.BeaconDeviceModel;
import lib.netmania.ble.model.BleControllerExecuteCode;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * Created by hansangcheol on 2017. 4. 5..
 */

public class BleController {


    public static final String TAG = "BleController";
    public static final String TAG_BLE_LIFE_CYCLE = "TAG_BLE_LIFE_CYCLE";
    public static BleController thisIsMe;


    private BluetoothAdapter mBtAdapter = null;
    private Context mContext;



    private BeaconDataModel updateBeacon = null;

    private ArrayList<BeaconDeviceModel> deviceModels;
    private NetManiaBleCallbackListener listener;
    private BluetoothLeScanner bluetoothLeScanner;


    public BleController (Context mContext, String[] mScanTargets, int from) {
        Log.i(TAG,"BleController 새로!!!  --- " + from);
        init (mContext, mScanTargets);
    }


    private int MAX_DELAY = 1000 * 30;

    public static BleController getInstance (Context mContext, String[] mScanTargets, NetManiaBleCallbackListener listener) {
        if (thisIsMe != null) thisIsMe.releaseBleController ();
        thisIsMe = new BleController (mContext, mScanTargets, 0);
        thisIsMe.listener = listener;
        return thisIsMe;
    }

    public static BleController getInstance (Context mContext, String[] mScanTargets) {
        if (thisIsMe != null) thisIsMe.releaseBleController ();
        thisIsMe = new BleController (mContext, mScanTargets, 1);
        return thisIsMe;
    }

    /** 단순 인스턴스 반환
     *
     * @return
     */
    public static BleController getInstance () {
        return thisIsMe;
    }


    /** ble controller 초기화
     *
     */
    public void releaseBleController () {
        if (thisIsMe == null) return;
        thisIsMe.listener = null;
        if (deviceModels != null) {
            deviceModels.clear();
            deviceModels = null;
        }

        mBtAdapter = null;
        mContext = null;
        updateBeacon = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner.flushPendingScanResults(mLeNewCallback);
            bluetoothLeScanner.stopScan(mLeNewCallback);
            mLeNewCallback = null;
            bluetoothLeScanner = null;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBtAdapter.stopLeScan(mLeOldCallback);
        }


        thisIsMe = null;
    }


    /** 초기화
     *##3
     * @param mContext
     */
    private void init (Context mContext, String[] mScanTargets) {
        this.mContext = mContext;

        //Log.i(TAG, "BleController (); -----------");
        if (deviceModels == null) deviceModels = new ArrayList<>();

        startMonitoringBeacon (mScanTargets);
        if (listener != null) {
            listener.onInitialized();
        } else if (mContext != null) {
            /** callback 이 없으면 브로드 캐스팅 한다. */
            Intent thisAction = new Intent();
            thisAction.setAction(BleAction.ACTION_BLE_INITIALIZED);
            mContext.sendBroadcast(thisAction);
        }

        //Log.i(TAG_BLE_LIFE_CYCLE, "init ()");
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void startMonitoringBeacon (String[] mScanTargets) {


        //Log.i(TAG_BLE_LIFE_CYCLE, "BleController (); ----------- startMonitoringBeacon ();");

        if (!isBluetoothAvailableAndEnabled()) {
            //Log.i(TAG, "블루투스가 활성화 안되어있음");
            if (listener != null) {
                listener.onBluetoothFailed();
            } else if (mContext != null) {
                /** callback 이 없으면 브로드 캐스팅 한다. */
                Intent thisAction = new Intent();
                thisAction.setAction(BleAction.ACTION_BLOOTOOTH_INACTIVATED);
                mContext.sendBroadcast(thisAction);
            }
            return;
        }



        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mBtAdapter.startLeScan(mLeOldCallback);
            String filteredTarget = "\n------------- ScanFilter 불가. 난 늙었소. ---------------\n";
        } else {

            bluetoothLeScanner = mBtAdapter.getBluetoothLeScanner();

            if (bluetoothLeScanner != null) {

                ScanSettings.Builder sb = new ScanSettings.Builder();
                sb.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                sb.setReportDelay(0);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    sb.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
                    sb.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
                }

                ScanSettings scanSettings = sb.build();

                bluetoothLeScanner.startScan(Collections.singletonList(new ScanFilter.Builder().build()), scanSettings, mLeNewCallback);

            } else {
                //nothing;
            }
        }

    }



    /** 블루투스 체크
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean isBluetoothAvailableAndEnabled() {
        BluetoothManager btManager = null;
        btManager = (BluetoothManager) mContext.getSystemService(BLUETOOTH_SERVICE);

        mBtAdapter = btManager.getAdapter();

        return (mBtAdapter != null && mBtAdapter.isEnabled());
    }


    /** 디바이스 추가
     *
     * @param beaconDeviceModel
     */
    public void addBeaconDevice (BeaconDeviceModel beaconDeviceModel) {
        //Log.i(TAG, "기기 추가 --> " + beaconDeviceModel.MAC);
        if (deviceModels == null) deviceModels = new ArrayList<>();
        deviceModels.add(beaconDeviceModel);
        if (listener != null) {
            listener.onDeviceAdd(beaconDeviceModel);
        } else if (mContext != null) {
            /** callback 이 없으면 브로드 캐스팅 한다. */
            Intent thisAction = new Intent();
            thisAction.setAction(BleAction.ACTION_DEVICE_ADDED);
            mContext.sendBroadcast(thisAction);
        }

       //Log.i(TAG, beaconDeviceModel.MAC + " -- 비콘 추가 완료");

    }


    /** 디바이스 추가
     *
     * @param mac
     * @param name
     * @param sticker
     * @param created_at
     */
    public void addBeaconDevice (String mac, String name, String sticker, long created_at) {
        BeaconDeviceModel tmp = new BeaconDeviceModel();
        tmp.MAC = mac;
        tmp.sticker = sticker;
        tmp.name = name;
        tmp.created_at = created_at;

        if (!contains(tmp.MAC)) {
            addBeaconDevice(tmp);
        }
    }


    /** 디바이스 삭제
     *
     * @param beaconDeviceModel
     */
    public void removeBeaconDevice (BeaconDeviceModel beaconDeviceModel) {
        //Log.i(TAG, "기기 제거 --> " + beaconDeviceModel.MAC);
        deviceModels.remove (beaconDeviceModel);
        if (listener != null) {
            listener.onDeviceRemove(beaconDeviceModel.MAC);
        } else if (mContext != null) {
            /** callback 이 없으면 브로드 캐스팅 한다. */
            Intent thisAction = new Intent();
            thisAction.setAction(BleAction.ACTION_DEVICE_REMOVED);
            thisAction.putExtra(BleAction.PARAM_MAC, beaconDeviceModel.MAC);
            mContext.sendBroadcast(thisAction);
        }
    }


    /** 디바이스 삭제
     *
     * @param macAddress
     */
    public void removeBeaconDevice (String macAddress) {
        //Log.i(TAG, "기기 제거 --> " + macAddress);

        String removedTargetMAC = null;

        for (int i = 0; i < deviceModels.size(); i++) {
            if (deviceModels.get(i).MAC.equals(macAddress)) {
                removedTargetMAC = deviceModels.get(i).MAC;
                deviceModels.remove(i);
                break;
            }
        }

        if (listener != null) {
            listener.onDeviceRemove (removedTargetMAC);
        } else if (mContext != null) {
            /** callback 이 없으면 브로드 캐스팅 한다. */
            Intent thisAction = new Intent();
            thisAction.setAction(BleAction.ACTION_DEVICE_REMOVED);
            thisAction.putExtra(BleAction.PARAM_MAC, macAddress);
            mContext.sendBroadcast(thisAction);
        }
    }


    /** BLE 디바이스 모드 클리어
     *
     */
    public void clearAllDevices () {
        if (deviceModels != null) deviceModels.clear();
        /** callback 이 없으면 브로드 캐스팅 한다. */
        Intent thisAction = new Intent();
        thisAction.setAction(BleAction.ACTION_DEVICE_REMOVED);
        thisAction.putExtra(BleAction.PARAM_MAC, "ALL");
        if (mContext != null) {
            mContext.sendBroadcast(thisAction);
        }

    }


    /** 등록된 기기 인가??
     *
     * @param macAddress
     * @return
     */
    public boolean contains (String macAddress) {
        boolean result = false;

        if (deviceModels == null) return result;

        for (int i = 0; i < deviceModels.size(); i++) {
            if (deviceModels.get(i).MAC.equals(macAddress)) {
                result = true;
                break;
            }
        }

        return result;
    }



    //리스너 ----------------------------------------------------------------------------



    /** BLE 스캐너
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback mLeNewCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            super.onScanResult(callbackType, result);

            /**  아래 조건에 스캐닝 리스트도 추가가 필요함 */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                handleNewBeaconDiscoverd(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            for (int i = 0; i < results.size(); i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    handleNewBeaconDiscoverd(results.get(i).getDevice(), results.get(i).getRssi(), results.get(i).getScanRecord().getBytes());
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            //Log.i(TAG, "ScanCallback (); onScanFailed (); --> " + errorCode);
        }
    };;

    // callback for Android before Lollipop
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothAdapter.LeScanCallback mLeOldCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            handleNewBeaconDiscoverd(device, rssi, scanRecord);
        }
    };




    /** 데헷.. 여기서 저장하면 되나?? **/
    private ArrayList<BluetoothDevice> mDevice = new ArrayList<>();
    public void handleNewBeaconDiscoverd (final BluetoothDevice bleDevice, final int rssi, final byte[] advertisement)  {

        Log.i("FUCK", bleDevice.getAddress() + " --> " + bleDevice.getName());

        byte[] tBytes = advertisement; //getGaraData(advertisement);

        // Cehck BLE Logger
        if(BeaconDataModel.isBeaconBleLogger(tBytes)) {

            BeaconDataModel tmpBeacon = new BeaconDataModel();
            try {
                tmpBeacon.addFrom (bleDevice, rssi, tBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (listener != null) {
                //Log.i(TAG, bleDevice.getAddress() + " --> onDataReceived 콜백 보냄");
                listener.onDataReceived(tmpBeacon);
            } else if (mContext != null) {
                //callback 이 없으면 브로드 캐스팅 한다.
                Intent thisAction = new Intent();
                thisAction.setAction(BleAction.ACTION_BLE_DATA);
                thisAction.putExtra(BleAction.PARAM_BLE_DATA, tmpBeacon);
                mContext.sendBroadcast(thisAction);
            }

            tmpBeacon = null;
        } else if (BeaconDataModelNew.isBeaconBleLogger(tBytes, bleDevice.getAddress())) {

            BeaconDataModelNew tmpBeacon = new BeaconDataModelNew();
            try {
                tmpBeacon.addFrom (bleDevice, rssi, tBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (listener != null) {
                //Log.i(TAG, bleDevice.getAddress() + " --> onDataReceived 콜백 보냄");
                listener.onDataReceived(tmpBeacon);
            } else if (mContext != null) {
                //callback 이 없으면 브로드 캐스팅 한다.
                Intent thisAction = new Intent();
                thisAction.setAction(BleAction.ACTION_BLE_DATA);
                thisAction.putExtra(BleAction.PARAM_BLE_DATA, tmpBeacon);
                mContext.sendBroadcast(thisAction);
            }

            tmpBeacon = null;

        }

    }


    /** BLE 갱신 고고고
     *
     */
    public void recreateBluetoothLeScanner () {
        Log.i(TAG, "씨발 지랄함 해봐라...ㅋㅋㅋ");
    }


    /** 비콘 리스트
     *
     * @return
     */
    public ArrayList<BeaconDeviceModel> getBeacons () {
        return deviceModels;
    }


    /** BLE 연결이 되었는고?
     *
     * @param macaddress
     * @return
     */
    public boolean isConnected (String macaddress) {
        boolean result = true;

        long now = Calendar.getInstance().getTimeInMillis();

        for (int i = 0; i < deviceModels.size(); i++) {
            if (deviceModels.get(i).MAC.equals(macaddress) && now - deviceModels.get(i).last_checked > MAX_DELAY) {
                result = false;
                break;
            }
        }

        return result;
    }

    /** 모르겠다...
     *
     * @return
     */
    public BluetoothLeScanner getBleSacanner () {
        return bluetoothLeScanner;
    }




    /** ------------------------------------------------------------------------------------------------------ */
    private static final byte NONE = 0x00;
    private static final byte VOLT_LEVEL = 0x01;
    private static final byte SENSOR_INSERTED = 0x02;
    private static final byte NEXT_DATA_EXIST = 0x04;
    private byte[] getGaraData (byte[] org_data) {

        BeaconDataModel org = new BeaconDataModel();
        org.addFrom(null, -100, org_data);

        byte[] tag = "CL".getBytes();
        byte[] seq = intToByteArray (Integer.valueOf(org.beaconSeq), 4);
        byte[] probe_temp = shortToByteArray((short) ((short) org.dTemp * 100.00));
        byte[] probe_hum = shortToByteArray((short) ((short) org.dHum * 100.00));
        byte[] chipset_temp = shortToByteArray((short) ((short) org.dTempOnChip * 100.00));
        byte[] chipset_hum = shortToByteArray((short) ((short) org.dHumOnChip * 100.00));
        byte[] bat_vol = shortToByteArray((short) ((short) org.dvBat * 20.00));

        byte status = NONE;

        status |= VOLT_LEVEL;
        status |= SENSOR_INSERTED;
        status |= NEXT_DATA_EXIST;

        byte[] gara = new byte[] {
                0,1,2,3,4,5,6, //--> 데이타 오프셋
                tag[0],
                tag[1],
                org_data[BeaconDataModel.BCN_RESERVED_PTR],
                seq[0], seq[1], seq[2], seq[3],
                probe_temp[0], probe_temp[1],
                probe_hum[0], probe_hum[1],
                chipset_temp[0], chipset_temp[1],
                chipset_hum[0], chipset_hum[1],
                status,
                bat_vol[0]
        };

        return gara;
    }

    /** 가라 데이타용 각종 함수  --------------------------------------------------------------------------------------------- */
    private static byte[] intToByteArray(final int integer, int size) {
        ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
        buff.putInt(integer);
        buff.order(ByteOrder.BIG_ENDIAN);
        //buff.order(ByteOrder.LITTLE_ENDIAN);
        return buff.array();
    }


    private static int byteArrayToInt(byte[] bytes) {
        final int size = Integer.SIZE / 8;
        ByteBuffer buff = ByteBuffer.allocate(size);
        final byte[] newBytes = new byte[size];
        for (int i = 0; i < size; i++) {
            if (i + bytes.length < size) {
                newBytes[i] = (byte) 0x00;
            } else {
                newBytes[i] = bytes[i + bytes.length - size];
            }
        }
        buff = ByteBuffer.wrap(newBytes);
        buff.order(ByteOrder.BIG_ENDIAN); // Endian에 맞게 세팅
        return buff.getInt();
    }

    public static short readShort(byte[] data, int offset) {
        return (short) (((data[offset] << 8)) | ((data[offset + 1] & 0xff)));
    }

    public static byte[] shortToByteArray(short s) {
        return new byte[] { (byte) ((s & 0xFF00) >> 8), (byte) (s & 0x00FF) };
    }

    // hex to byte[]
    public static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }

        byte[] ba = new byte[hex.length() / 2];

        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }

        return ba;
    }

    /** ------------------------------------------------------------------------------------------------------ */
}
