package lib.netmania.ble.callback;

import java.util.ArrayList;

import lib.netmania.ble.model.BeaconDataModelNew;
import lib.netmania.ble.model.BeaconDeviceModel;
import lib.netmania.ble.model.BeaconDataModel;

/**
 * Created by hansangcheol on 2017. 4. 6..
 */

public interface NetManiaBleCallbackListener {
    void onInitialized ();
    void onDataReceived (BeaconDataModel data);
    void onDataReceived (BeaconDataModelNew data);
    void onDeviceAdd (BeaconDeviceModel deviceModel);
    void onDeviceRemove (String macAddress);
    void onBluetoothFailed ();
}
