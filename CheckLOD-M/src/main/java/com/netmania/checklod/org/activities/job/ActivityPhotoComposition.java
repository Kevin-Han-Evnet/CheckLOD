package com.netmania.checklod.org.activities.job;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.data.DBHelper;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.PhotoCompositinoItemDto;
import com.netmania.checklod.general.http.BaseAPI;
import com.netmania.checklod.general.http.GalleryAPI;
import com.netmania.checklod.general.utils.DialogUtils;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.general.view.InfoToast;
import com.netmania.checklod.org.BaseApplication;
import com.netmania.checklod.org.R;
import com.netmania.checklod.org.adapter.PhotoCompositionListAdapter;
import com.netmania.checklod.org.manage.Constants;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by hansangcheol on 2018. 5. 15..
 */

public class ActivityPhotoComposition
        extends BaseActivity
        implements View.OnClickListener, View.OnLongClickListener, JobProcessController.JobProcessCallback {

    //상수
    public static final String EXTRA_KEY_MACADDRESS = "extraKeyMacAddress";


    //UI
    private MenuItem ic_add_photo;
    private ListView mListView;
    private View tab_add_photo;
    private MaterialRippleLayout btn_send_all_container, btn_add_photo_container;
    private Button btn_send_all;
    private ImageButton btn_add_photo;
    private ProgressDialog tProgressDialog;


    //데이타
    private PhotoCompositionListAdapter mAdapter;
    private String MAC;


    //객체


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_composition);

        overridePendingTransition(R.anim.slide_up_in_fast, R.anim.empty);

        MAC = getIntent().getStringExtra(EXTRA_KEY_MACADDRESS);

        setLayout ();
        initData ();
    }


    /** 레이아웃 셋팅
     *
     */
    private void setLayout () {
        //로고표현
        getActionBar().setIcon(R.mipmap.topper_logo);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(true);

        tab_add_photo = findViewById(R.id.tab_add_photo);
        tab_add_photo.setOnClickListener(this);

        mListView = findViewById(R.id.listView);
        mAdapter = new PhotoCompositionListAdapter(mActivity, new ArrayList<PhotoCompositinoItemDto>(), this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mItemClickListener);

        btn_send_all = (Button) findViewById(R.id.btn_send_all);
        btn_send_all.setOnClickListener(this);

        btn_add_photo = (ImageButton) findViewById(R.id.btn_add_photo);
        btn_add_photo.setOnClickListener(this);

    }


    /** 데이타 이니셜라이징
     *
     */
    private void initData () {
        if (mAdapter.getCount() > 0){
            mListView.setVisibility(View.VISIBLE);
            tab_add_photo.setVisibility(View.GONE);
        }
    }


    /** 사진 추가 합시다
     *
     */
    private void addPhotoToComposition () {
        JobProcessController.getInstance(mActivity, this).getPhoto ("", MAC);
    }


    /** 선택된 사진 가져오자..
     *
     * @param file
     */
    private void addPhotoToCompositionComplete (final File file) {
        //nothing yet;
        final String[] tmp = Constants.getDocumentTypes ();
        final String[] types = new String[tmp.length];
        final String[] labels = new String[tmp.length];

        for (int i = 0; i < tmp.length; i++) {
            types[i] = tmp[i].split(",")[0];
            labels[i] = tmp[i].split(",")[1];
        }


        mActivity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {

                        DialogUtils.singleChoice(
                                mActivity,
                                R.string.title_for_type_choice,
                                labels,
                                -1,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        PhotoCompositinoItemDto tItem = new PhotoCompositinoItemDto(types[which], labels[which], file);
                                        mAdapter.add(tItem);
                                        mListView.smoothScrollToPosition(mAdapter.getCount());
                                        initData();
                                        dialog.dismiss();

                                    }
                                }, true
                        );

                    }
                }
        );
    }


    /** 서버로 전송
     *
     */
    private void sendDocumentsToServer () {

        if (mAdapter.getCount() == 0) {
            mHandler.post(new InfoToast(mActivity, getResources().getString(R.string.toast_no_photos_to_send)));
            return;
        }


        if (tProgressDialog == null) {
            tProgressDialog = DialogUtils.progress(mActivity, 100, getResources().getString(R.string.title_for_upload_progress), getResources().getString(R.string.message_for_upload_progress), false);
        }

        GalleryAPI.fileupload(
                mActivity,
                true,
                mAdapter.getItems(),
                MAC,
                new BaseAPI.ApiMapListenerWithFailedForFiles() {
                    @Override
                    public void onProgress(int progress, int max) {


                        //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "uploaded --> " + progress + " / " + max);
                        if (tProgressDialog != null) {
                            tProgressDialog.setMax(max);
                            tProgressDialog.setProgress(progress);
                        }
                    }

                    @Override
                    public void onComplete(final Map<String, Object> map) {


                        tProgressDialog.setProgress(tProgressDialog.getMax());

                        mActivity.mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                tProgressDialog.dismiss();
                                tProgressDialog = null;

                                //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "성공! -->  000 :: " + map.toString());

                                String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                        + "isLive=" + BeaconItemDto.INT_FALSE + ", "
                                        + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_COMPLETE
                                        + " WHERE MAC='" + MAC + "'";

                                new DBHelper(BaseApplication.getInstance()).update(qry);


                                mHandler.post(new InfoToast(mActivity, getResources().getString(R.string.delivery_state_complete)));
                                finish ();


                            }
                        }, 500);




                    }

                    @Override
                    public void onComplete(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                        tProgressDialog.setProgress(tProgressDialog.getMax());

                        mActivity.mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                tProgressDialog.dismiss();
                                tProgressDialog = null;

                                //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "성공! --> 001 :: " + statusCode);

                                String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                        + "isLive=" + BeaconItemDto.INT_FALSE + ", "
                                        + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_COMPLETE
                                        + " WHERE MAC='" + MAC + "'";

                                new DBHelper(BaseApplication.getInstance()).update(qry);


                                mHandler.post(new InfoToast(mActivity, getResources().getString(R.string.delivery_state_complete)));
                                finish ();



                            }
                        }, 500);
                    }

                    @Override
                    public void onComplete(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {

                        tProgressDialog.setProgress(tProgressDialog.getMax());

                        mActivity.mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                tProgressDialog.dismiss();
                                tProgressDialog = null;

                                //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "성공! -->  002 :: " + statusCode);

                                String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                        + "isLive=" + BeaconItemDto.INT_FALSE + ", "
                                        + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_COMPLETE
                                        + " WHERE MAC='" + MAC + "'";

                                new DBHelper(BaseApplication.getInstance()).update(qry);

                                mHandler.post(new InfoToast(mActivity, getResources().getString(R.string.delivery_state_complete)));
                                finish ();



                            }
                        }, 500);
                    }

                    @Override
                    public void onComplete(int statusCode, Header[] headers, Throwable throwable) {

                        tProgressDialog.setProgress(tProgressDialog.getMax());

                        mActivity.mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                tProgressDialog.dismiss();
                                tProgressDialog = null;

                                //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "성공! -->  003 :: " + statusCode);

                                String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                        + "isLive=" + BeaconItemDto.INT_FALSE + ", "
                                        + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_COMPLETE
                                        + " WHERE MAC='" + MAC + "'";

                                new DBHelper(BaseApplication.getInstance()).update(qry);

                                mHandler.post(new InfoToast(mActivity, getResources().getString(R.string.delivery_state_complete)));
                                finish ();



                            }
                        }, 500);
                    }

                    @Override
                    public void onFailed(final int statusCode) {

                        tProgressDialog.setProgress(0);

                        mActivity.mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                tProgressDialog.dismiss();
                                //LogUtil.I(DebugTags.TAG_FILE_UPLOAD, "실패! --> " + statusCode);
                                DialogUtils.alert(mActivity, com.netmania.checklod.general.R.string.info_upload_failed, false);
                                //여기서 실패처리

                            }
                        }, 500);
                    }
                }
        );

    }



    /** 온클릭
     *
     * @param view
     */
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_add_photo :
            case R.id.tab_add_photo :
                addPhotoToComposition ();
                break;

            case R.id.btn_send_all :

                sendDocumentsToServer ();

                break;

            case R.id.item_btn_delete :

                final int tPosition = (int) view.getTag();
                String msg = String.format(getResources().getString(R.string.confirm_delete_photo_item), mAdapter.getItem(tPosition).label);
                DialogUtils.confirm(
                        mActivity,
                        msg,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAdapter.remove(mAdapter.getItem(tPosition));
                                initData();
                            }
                        }, true
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


    /** 액션메뉴 커스터 마이징
     *
     * @param menu
     * @return

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.photo_composition, menu);
        ic_add_photo = menu.findItem(R.id.ic_add_photo);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {

            case R.id.ic_add_photo :
                addPhotoToComposition ();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    */


    /** 뒤로 가기
     *
     */
    @Override
    public void onBackPressed () {
        super.onBackPressed();
        overridePendingTransition (R.anim.empty, R.anim.slide_down_out_fast);
    }


    //리스너 ------------------------------------------------------------------------------------------------------------------------

    /** 리스트 아이템 클릭 리스너
     *
     */
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {


            final PhotoCompositinoItemDto tItem = mAdapter.getItem(i);

            final String[] tmp = Constants.getDocumentTypes ();
            final String[] types = new String[tmp.length];
            final String[] labels = new String[tmp.length];


            int selected = -1;
            for (int j = 0; j < tmp.length; j++) {
                types[j] = tmp[j].split(",")[0];
                labels[j] = tmp[j].split(",")[1];
                if (types[j].equals(tItem.type)) selected = j;
            }



            DialogUtils.singleChoice(
                    mActivity,
                    R.string.title_for_type_choice,
                    labels,
                    selected,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            tItem.type = types[which];
                            tItem.label = labels[which];
                            mAdapter.notifyDataSetChanged();

                            initData();
                            dialog.dismiss();

                        }
                    }, true
            );

        }
    };


    /** for JobProcessCallback -------------------------------------------------------------------------------------------
     *
     * @param mac
     * @param start_option
     */
    @Override
    public void onCompleteAddBeaconDevice(String mac, int start_option) {
        //nothing yet;
    }

    @Override
    public void onFailedAddBeaconDevice(String mac, int failed_reson) {
        //nothing yet;
    }

    @Override
    public void onTemperatureRangeSelected(String mac, double minimum, double maximum) {
        //nothing yet;
    }

    @Override
    public void onCompleteRemoveBeacon(String mac) {
        //nothing yet;
    }

    @Override
    public void onTakeOverSuccess(String mac) {
        //nothing yet;
    }

    @Override
    public void onTakeOverFailed(String mac, int code) {
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
        //nothing yet;
    }

    @Override
    public void onNotReportedDataSendFailed(String mac, int process) {
        //nothing yet;
    }

    @Override
    public void onNotReportedDataSendComplete(String mac, int process) {
        //nothing yet;
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
        //nothing yet;
    }

    @Override
    public void onPhotoCompositionItemSelected(File file) {
        addPhotoToCompositionComplete (file);
    }
}
