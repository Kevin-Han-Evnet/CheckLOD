package lib.netmania.ble.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Kevin Han on 2017-01-13.
 */

public class BeaconDeviceModel implements Serializable {

    public String MAC;
    public String name;
    public String sticker;
    public double max_temperature_limit = 30;
    public double min_temperature_limit = -30;
    public long last_checked;
    public long created_at;

}
