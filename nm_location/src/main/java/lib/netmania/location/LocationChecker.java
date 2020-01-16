package lib.netmania.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import lib.netmania.location.callback.LocationCallback;

/**
 * Created by hansangcheol on 2017. 4. 11..
 */

public class LocationChecker {


    private Context mContext;
    private LocationCallback listener;
    private LocationManager lm;


    private String gps_lat = "";//"37.499313";
    private String gps_lng = "";//"127.143410";
    private String network_lat = "";//"37.499313";
    private String network_lng = "";//"127.143410";
    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    private long checkTerm = 5000; //기본
    private Timer schedule;


    public LocationChecker(Context mContext) {
        this.mContext = mContext;

        checkPermissionAndStart();
    }


    public LocationChecker(Context mContext, LocationCallback listener) {
        this.mContext = mContext;
        this.listener = listener;

        checkPermissionAndStart();
    }


    public LocationChecker(Context mContext, LocationCallback listener, long checkTerm) {
        this.mContext = mContext;
        this.listener = listener;
        this.checkTerm = checkTerm;

        checkPermissionAndStart();
    }


    /** 퍼미션 체크하고 시작
     *
     */
    private void checkPermissionAndStart() {
        /** 불루투스 확인 권한 --------------------------------------------------------------------------------
         *
         */
        //폰상태 권한 확인
        new TedPermission(mContext)
                .setPermissionListener(permissionListener)
                .setDeniedMessage(R.string.permission_location)
                .setPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                .check();

    }


    /** 액티비티 온스탑에 호출
     *
     */
    public void onStop() {
        if (ActivityCompat.checkSelfPermission (mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        schedule.cancel();
        schedule = null;

        lm.removeUpdates(locationListener);
        lm = null;
    }


    /** 엑ㅌㅂㅌ 온리쥼에 호출
     *
     */
    public void onResume() {
        lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        lm.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                10,
                locationListener
        );

        lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                10,
                locationListener
        );

        if (schedule == null) {
            schedule = new Timer();
            schedule.schedule(timerTask, checkTerm, checkTerm);
        }
    }





    //빈문자열 체크
    public boolean isEmpty (String str) {
        return ( null == str || str.isEmpty() || 1 > str.replaceAll(" ", "").length() || "null".equals (str));
    }


    //리스너 ----------------------------------------------------------------------------------------------------------

    /** 로케이션 매니저 리스너
     *
     */
    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                gps_lat = String.valueOf(location.getLatitude());
                gps_lng = String.valueOf(location.getLongitude());
                gps_enabled = true;
            } else {
                network_lat = String.valueOf(location.getLatitude());
                network_lng = String.valueOf(location.getLongitude());
                network_enabled = true;
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                gps_enabled = false;
            } else {
                network_enabled = false;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                gps_enabled = true;
            } else {
                network_enabled = true;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //nothing yet;
        }

    };


    /** 권한 리스너
     *
     */
    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //Log.iTAG, "BleController (); ----------- 권한 설정 완료");
            onResume();
            if (listener != null) listener.onInitialized();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            //Log.iTAG, "BleController (); ----------- 권한 설정 거부");
            if (listener != null) listener.onPermissionDenied(deniedPermissions);
        }
    };


    /** 가자고
     *
     */
    public void goReportLocation () {
        String lat = "0";
        String lng = "0";

        //Log.i(DebugTags.TAG_LOCATION_INFO, "GPS에서 마지막으로 성공한 위치가 있네요...");
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if (gps_enabled && ! isEmpty(gps_lat)) {
            lat = gps_lat;
            lng = gps_lng;
            //Toast.makeText(mActivity, "GPS로 부터 좌표를 얻어 실행합니다.", Toast.LENGTH_SHORT).show ();
            //Log.i(DebugTags.TAG_LOCATION_INFO, "GPS로 부터 좌표를 얻어 실행합니다.");
        } else if (network_enabled && !isEmpty(network_lat)) {
            lat = network_lat;
            lng = network_lng;
            //Toast.makeText(mActivity, "기지국으로 부터 좌표를 얻어 실행합니다.", Toast.LENGTH_SHORT).show ();
            //Log.i (DebugTags.TAG_LOCATION_INFO, "기지국으로 부터 좌표를 얻어 실행합니다.");
        } else {

            //Toast.makeText(mActivity, "위치를 얻지 못햇습니다.", Toast.LENGTH_SHORT).show ();
            //Log.i(DebugTags.TAG_LOCATION_INFO, "위치를 얻지 못햇습니다.");

            if (lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {

                //Log.i (DebugTags.TAG_LOCATION_INFO, "GPS에서 마지막으로 성공한 위치가 있네요...");
                lat = String.valueOf (lm.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude());
                lng = String.valueOf (lm.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());

            } else if (lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)  {

                //Log.i (DebugTags.TAG_LOCATION_INFO, "네트워크에서 마지막으로 성공한 위치가 있네요...");
                lat = String.valueOf (lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude());
                lng = String.valueOf (lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());

            } else {
                lat = "0";
                lng = "0";
            }
        }

        if (listener != null) listener.onDataReceived (lat, lng);
    }


    /** 타이머 태스크
     *
     */
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

           goReportLocation ();

        }
    };
}
