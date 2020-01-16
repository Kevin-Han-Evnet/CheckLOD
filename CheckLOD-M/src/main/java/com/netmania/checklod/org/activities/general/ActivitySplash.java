package com.netmania.checklod.org.activities.general;


import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.androidquery.callback.AjaxStatus;
import com.google.firebase.iid.FirebaseInstanceId;
import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.data.AppSharedData;
import com.netmania.checklod.general.data.DBHelper;
import com.netmania.checklod.general.dto.AuthorizedDeviceItemDto;
import com.netmania.checklod.general.http.BaseAPI;
import com.netmania.checklod.general.http.GeneralAPI;
import com.netmania.checklod.general.manage.FlagBox;
import com.netmania.checklod.general.service.SensingProcessService;
import com.netmania.checklod.general.utils.DialogUtils;
import com.netmania.checklod.general.utils.PermissionUtils;
import com.netmania.checklod.general.utils.StringUtils;
import com.netmania.checklod.org.BaseApplication;
import com.netmania.checklod.org.R;
import com.netmania.checklod.org.activities.ActivityControl;
import com.netmania.checklod.org.data.DataProviderUtil;
import com.netmania.checklod.org.manage.Constants;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;


/**
 * Created by hansangcheol on 2018. 2. 26..
 */
public class ActivitySplash extends BaseActivity {

    //상수


    //UI


    //데이타


    //객체


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        //블루투스 체크 -------------------------------------------------------------------------------------
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            DialogUtils.alert(
                    mActivity,
                    R.string.alert_need_bluetooth,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mActivity.forcedFinish(false);
                        }
                    }, false
            );

            return;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                DialogUtils.alert(
                        mActivity,
                        R.string.alert_need_bluetooth,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mApp.finishAll();
                                ActivityControl.openBluetoothSettingActivity (getIntent(), mActivity);
                            }
                        }, false
                );

                return;
            }
        }


        /*/NFC 체크 -------------------------------------------------------------------------------------
        NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            //continue;
        } else {
            DialogUtils.alert(
                    mActivity,
                    R.string.alert_need_nfc,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mApp.finishAll();
                            ActivityControl.openNFCSettingActivity (getIntent(), mActivity);
                        }
                    }, false
            );

            return;
        }*/

        setLayout ();
        if (PermissionUtils.checkGeneralPermission(mActivity)) {
            initData();
        }
    }



    /** 권한 설정 리스너
     *
     */
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantresults) {

        if (requestCode != FlagBox.PERMISSION_REQ_GENERAL) return;


        boolean AllPermissionGranted = true;
        for (int i = 0; i < grantresults.length; i++) {
            if (grantresults[i] != PackageManager.PERMISSION_GRANTED) {
                AllPermissionGranted = false;
                break;
            }
        }

        if (AllPermissionGranted) {
            initData ();
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

    }



    /** 레이아웃 셋팅
     *
     */
    private void setLayout () {
        //nothing yet;
    }


    /** 데이타 이니셜라이징
     *
     */
    private void initData () {
        AppSharedData.setShowAllReports(Constants.SHOW_STATS_DEFAULT);
        checkAndGo ();
    }


    /** LAL 비콘 리스트 다운로드 및 고고
     *
     */
    @Override
    protected void loadDeviceListAndGo () {

        GeneralAPI.getAutorizedBeaconList(
                mActivity,
                false,
                new BaseAPI.ApiMapListenerWithFailed() {
                    @Override
                    public void onComplete(Map<String, Object> map) {

                        ArrayList<AuthorizedDeviceItemDto> itemList = (ArrayList<AuthorizedDeviceItemDto>) map.get(BaseAPI.ARRAY_LIST);

                        if (itemList.size() > 0) {
                            String tblName = AuthorizedDeviceItemDto.getInstance().getTblName();

                            DBHelper tntDBHelper = new DBHelper(BaseApplication.getInstance());
                            tntDBHelper.deleteAll(tblName);

                            for (int i = 0; i < itemList.size(); i++) {

                                tntDBHelper.insertWithAsset(
                                        tblName,
                                        itemList.get(i).getInsertAsset()
                                );
                            }
                        }


                        checkConfigAndStar ();
                    }

                    @Override
                    public void onComplete(int statusCode, PreferenceActivity.Header[] headers, JSONObject json) {
                        checkConfigAndStar();
                    }

                    @Override
                    public void onFailed(AjaxStatus status) {
                        checkConfigAndStar();
                    }
                }
        );

    }


    /** 프리로드 설정값 확인후 실행
     *
     */
    private void checkConfigAndStar () {

        ArrayList<AuthorizedDeviceItemDto> deviceList = DataProviderUtil.getInstance(mActivity).getAuthorizedBeaconList();
        if (StringUtils.isEmpty(DVIF.name)
                || deviceList == null
                || deviceList.size() == 0) {

            DialogUtils.alert(
                    mActivity,
                    R.string.alert_not_enough_info,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mApp.finishAll();
                        }
                    }, false
            );

        } else {


            GeneralAPI.sendFcmRegId(
                    mActivity,
                    false,
                    DVIF.id,
                    FirebaseInstanceId.getInstance().getToken(),
                    getResources().getString(R.string.test_divsion_code),
                    new BaseAPI.ApiMapListenerWithFailedRest() {
                        @Override
                        public void onComplete() {
                            //nothing;
                        }

                        @Override
                        public void onComplete(Map<String, Object> map) {
                            //nothing;
                        }

                        @Override
                        public void onFailed(AjaxStatus result) {
                            //nothing;
                        }

                        @Override
                        public void onFailed(AjaxStatus result, Map<String, Object> map) {
                            //nothing;
                        }
                    }
            );

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {


                    /*if (CONFIG.is_vehicle.equals("1") && !GeneralUtils.isMyAppLauncherDefault(getPackageName())) {
                        mHandler.post(new InfoToast(mActivity, getResources().getString(R.string.info_set_home), Toast.LENGTH_LONG, 0));
                        mHandler.post(new InfoToast(mActivity, getResources().getString(R.string.info_set_home), Toast.LENGTH_LONG, 0));
                        mHandler.post(new InfoToast(mActivity, getResources().getString(R.string.info_set_home), Toast.LENGTH_LONG, 0));
                        ActivityControl.openHomeSelector(mApp);
                    } else {*/
                        ActivityControl.openJobReadyActivity(getIntent(), mActivity);
                    //}


                }
            }, 1000);
        }
    }



    /** 뒤로 가기
     *
     */
    @Override
    public void onBackPressed () {
        super.onBackPressed();
    }

}
