package lib.netmania.location.callback;

import java.util.ArrayList;

/**
 * Created by hansangcheol on 2017. 4. 11..
 */

public interface LocationCallback {
    void onInitialized ();
    void onPermissionDenied (ArrayList<String> deniedPermissions);
    void onDataReceived (String lat, String lng);
}
