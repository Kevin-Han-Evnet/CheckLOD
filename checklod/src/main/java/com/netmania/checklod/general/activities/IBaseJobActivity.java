package com.netmania.checklod.general.activities;

/**
 * Created by hansangcheol on 2017. 7. 26..
 */

public interface IBaseJobActivity {
    void updateWifiStatus();
    void updateBatteryStatus();
    int getBeaconCount();
    void doUpdateUI ();
    void doDisableMe(String mac);
}
