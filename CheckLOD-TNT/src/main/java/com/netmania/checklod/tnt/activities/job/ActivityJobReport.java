package com.netmania.checklod.tnt.activities.job;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.activities.IBaseJobActivity;
import com.netmania.checklod.general.data.AppSharedData;
import com.netmania.checklod.general.data.DBHelper;
import com.netmania.checklod.general.data.DataProviderUtil;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.TemperatureTrackingDto;
import com.netmania.checklod.general.service.JobForegroundMoinitor;
import com.netmania.checklod.general.utils.DialogUtils;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.StringUtils;
import com.netmania.checklod.tnt.BaseApplication;
import com.netmania.checklod.tnt.R;
import com.netmania.checklod.tnt.adapter.TemperReportListAdapter;
import com.netmania.checklod.tnt.manage.Constants;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Created by hansangcheol on 2017. 11. 23..
 */

public class ActivityJobReport
        extends BaseActivity
        implements View.OnClickListener, View.OnLongClickListener, JobProcessController.JobProcessCallback, RadioGroup.OnCheckedChangeListener,
                        IBaseJobActivity {

    //상수
    public static final String EXTRA_KEY_MACADDRESS = "extraKeyMacAddress";
    public static final String EXTRA_KEY_STICKER = "extraKeySticker";
    public static final String EXTRA_KEY_PHOTO = "exraKeyMacPhoto";


    //UI
    private View top_container_asset, progressGo, mHeaderView, inc_report_view;
    private LineChart item_chart;
    private ListView mListView;
    private Button btn_job_finish;
    private TextView delivery_duration_field, item_min_temperature, item_max_temperature, item_avrg_temperature,
            fv_delivery_duration_field, fv_item_min_temperature, fv_item_max_temperature, fv_item_avrg_temperature;
    private MenuItem ic_sticker;
    private MenuItem ic_driver_info;
    private RadioGroup rdo_report_term;
    private RadioButton rdo_btn_30s, rdo_btn_1m, rdo_btn_5m, rdo_btn_15m, rdo_btn_30m;
    private MaterialRippleLayout btn_job_finish_container;


    //데이타
    private TemperReportListAdapter mAdapter;
    private String MAC;
    private String STICKER;
    private double max_temperature_limit = 30.00;
    private double min_temperature_limit = 15.00;


    //데이타
    private boolean tab_visible = false;
    private float position_1;


    //객체
    private Animation showTabAni, hideTabAni;



    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_report);


        //퇴장효과
        overridePendingTransition(R.anim.slide_up_in_fast, R.anim.empty);

        MAC = getIntent().getStringExtra(EXTRA_KEY_MACADDRESS);
        STICKER = getIntent().getStringExtra(EXTRA_KEY_STICKER);

        BeaconItemDto bcn = DataProviderUtil.getInstance(mApp).getTrackingDevice(MAC);
        max_temperature_limit = bcn.max_temperature_limit;
        min_temperature_limit = bcn.min_temperature_limit;

        //늘깨어 있으라~ 잠들지 말아라~-----------------------------------------------------------------------
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getIntent().addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //----------------------------------------------------------------------------------------------


        initSensor();
        setLayout ();
        initData ();


        position_1 = GeneralUtils.DptoPixel(BaseActivity.DENSITY, -140);

        hideTabAni = new Animation()
        {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t)
            {

                RelativeLayout.LayoutParams params_1 = (RelativeLayout.LayoutParams) inc_report_view.getLayoutParams();
                params_1.topMargin = (int) (position_1 * interpolatedTime);
                inc_report_view.setLayoutParams(params_1);
            }

        };
        hideTabAni.setDuration(250);
        hideTabAni.setInterpolator(new AccelerateInterpolator());

        showTabAni = new Animation()
        {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t)
            {

                inc_report_view.setVisibility (View.VISIBLE);
                RelativeLayout.LayoutParams params_1 = (RelativeLayout.LayoutParams) inc_report_view.getLayoutParams();
                params_1.topMargin = (int) (position_1 - (position_1 * interpolatedTime));
                inc_report_view.setLayoutParams(params_1);
            }

        };
        showTabAni.setDuration(250);
        showTabAni.setInterpolator(new AccelerateInterpolator());
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


        rdo_report_term = (RadioGroup) findViewById(R.id.rdo_report_term);
        rdo_report_term.setOnCheckedChangeListener(this);

        rdo_btn_30s = (RadioButton) findViewById(R.id.rdo_btn_30s);
        rdo_btn_1m = (RadioButton) findViewById(R.id.rdo_btn_1m);
        rdo_btn_5m = (RadioButton) findViewById(R.id.rdo_btn_5m);
        rdo_btn_15m = (RadioButton) findViewById(R.id.rdo_btn_15m);
        rdo_btn_30m = (RadioButton) findViewById(R.id.rdo_btn_30m);


        if (Constants.IS_RELEASED) {
            rdo_btn_30s.setVisibility(View.GONE);
            rdo_btn_1m.setVisibility(View.GONE);
            rdo_btn_5m.setVisibility(View.VISIBLE);
            rdo_btn_5m.setVisibility(View.VISIBLE);
            rdo_btn_15m.setVisibility(View.VISIBLE);
            rdo_btn_30m.setVisibility(View.VISIBLE);
        } else {
            rdo_btn_30s.setVisibility(View.VISIBLE);
            rdo_btn_1m.setVisibility(View.VISIBLE);
            rdo_btn_5m.setVisibility(View.VISIBLE);
            rdo_btn_5m.setVisibility(View.VISIBLE);
            rdo_btn_15m.setVisibility(View.GONE);
            rdo_btn_30m.setVisibility(View.GONE);
        }



        inc_report_view = findViewById(R.id.inc_report_view);
        inc_report_view.setVisibility(View.GONE);


        fv_delivery_duration_field = (TextView) inc_report_view.findViewById(R.id.delivery_duration_field);
        fv_delivery_duration_field.setText("");
        if (!Constants.IS_RELEASED) fv_delivery_duration_field.setOnLongClickListener(this);

        fv_item_max_temperature = (TextView) inc_report_view.findViewById(R.id.item_max_temperature);
        fv_item_max_temperature.setText("");

        fv_item_min_temperature = (TextView) inc_report_view.findViewById(R.id.item_min_temperature);
        fv_item_min_temperature.setText("");

        fv_item_avrg_temperature = (TextView) inc_report_view.findViewById(R.id.item_avrg_temperature);
        fv_item_avrg_temperature.setText("");




        mHeaderView = mInflater.inflate(R.layout.layout_header_job_report_list, null);

        item_chart = (LineChart) mHeaderView.findViewById(R.id.item_chart);

        delivery_duration_field = (TextView) mHeaderView.findViewById(R.id.delivery_duration_field);
        delivery_duration_field.setText("");
        if (!Constants.IS_RELEASED) delivery_duration_field.setOnLongClickListener(this);

        item_min_temperature = (TextView) mHeaderView.findViewById(R.id.item_min_temperature);
        item_min_temperature.setText("");

        item_max_temperature = (TextView) mHeaderView.findViewById(R.id.item_max_temperature);
        item_max_temperature.setText("");

        item_avrg_temperature = (TextView) mHeaderView.findViewById(R.id.item_avrg_temperature);
        item_avrg_temperature.setText ("");

        mListView = (ListView) findViewById(R.id.listView);
        mListView.addHeaderView(mHeaderView);
        mAdapter = new TemperReportListAdapter(mActivity, new ArrayList<TemperatureTrackingDto>(), this);
        mListView.setAdapter(mAdapter);

        //일단 임시로 여기다 붙임 ----------------------------------------------------------------------------------------------------------------------
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //JobProcessController.getInstance(mActivity, ActivityJobReport.this).doRetrieveData (MAC);

                /*
                for (int k = 0; k < mAdapter.getCount(); k++) {
                    String mesuraed_at = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format (new Date(mAdapter.getItem(k).timestamp));

                    int seq_gab = (k > 0) ? mAdapter.getItem(k).seq - mAdapter.getItem(k - 1).seq : 0;
                    String str_check = (seq_gab <= 1) ? "정상" : (seq_gab - 1) + "개 누락";

                    LogUtil.I(DebugTags.SHIT, mAdapter.getItem(k).MAC + "|" + mAdapter.getItem(k).seq + "|" + mesuraed_at + "|" + mAdapter.getItem(k).temp + "|" + str_check);
                }
                */

            }
        });
        //일단 임시로 여기다 붙임 ----------------------------------------------------------------------------------------------------------------------


        //스크롤로 다음페이지. 이거 좀 어색하긴한데. -_-;;
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                /* LogUtil.I("뭐가 보이나!!! --> "
                        + mListView.getRefreshableView().getFirstVisiblePosition()
                        + " -- " + mListView.getRefreshableView().getChildAt(0).getTop());*/


                //그거 볼거야 ㅋㅋㅋ
                if (mAdapter.getCount() > 0
                        && mListView.getFirstVisiblePosition() >= 2
                        && !tab_visible) {

                    inc_report_view.startAnimation(showTabAni);
                    tab_visible = true;

                } else if (mAdapter.getCount() > 0
                        && mListView.getFirstVisiblePosition() < 2
                        && tab_visible) {
                    inc_report_view.startAnimation(hideTabAni);
                    tab_visible = false;
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //nothing yet;
            }

        });

        btn_job_finish_container = (MaterialRippleLayout) findViewById(R.id.btn_job_finish_container);
        btn_job_finish = (Button) findViewById(R.id.btn_job_finish);
        btn_job_finish.setOnClickListener(this);


        progressGo = findViewById(R.id.progressGo);
        progressGo.setVisibility(View.GONE);

    }


    /** 데이타 이니셜라이징
     *
     */
    private void initData () {
        ArrayList<TemperatureTrackingDto> tempList = DataProviderUtil.getInstance(mApp.getApplicationContext()).getReportedTemperatures(MAC, false, 0, 0, false);
        ArrayList<TemperatureTrackingDto> finalList = new ArrayList<>();


        //우선은 리스트 ------------------------------------------------------------------------------------------------------------------------------
        mAdapter.clear();
        if (tempList.size() > 0) {

            //---------- 시간대 별로 끊어 보자 ----------------------------------------------------------------
            if (AppSharedData.getShowAllReports()
                    || tempList.size() <= 1
                    || rdo_report_term.getCheckedRadioButtonId() == R.id.rdo_btn_30s) {
                finalList = tempList;
                mAdapter.setReportTemr(Constants.REPORT_TIME_GAB_1);
            } else {



                Date startRTC = new Date ();
                try {
                    startRTC = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(tempList.get(0).rtc);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                long before = startRTC.getTime();
                long before_k;
                long current_k;

                long reportTerm = Constants.REPORT_TIME_GAB_3;
                switch (rdo_report_term.getCheckedRadioButtonId()) {

                    case R.id.rdo_btn_1m :
                        reportTerm = Constants.REPORT_TIME_GAB_2;
                        break;

                    case R.id.rdo_btn_5m :
                        reportTerm = Constants.REPORT_TIME_GAB_3;
                        break;

                    case R.id.rdo_btn_10m :
                        reportTerm = Constants.REPORT_TIME_GAB_4;
                        break;

                    case R.id.rdo_btn_15m :
                        reportTerm = Constants.REPORT_TIME_GAB_5;
                        break;

                    case R.id.rdo_btn_30m :
                        reportTerm = Constants.REPORT_TIME_GAB_6;
                        break;

                }


                finalList.add(tempList.get(0));

                for (int i = 0; i < tempList.size(); i++) {

                    Date rtc = new Date ();
                    try {
                        rtc = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(tempList.get(i).rtc);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    before_k = before / 1000;
                    current_k = (rtc.getTime() - reportTerm) / 1000;


                    if (before_k == current_k) {
                        finalList.add(tempList.get(i));
                        before += reportTerm;
                    } else if (before_k < current_k) {
                        finalList.add(tempList.get(i));
                        before = rtc.getTime();
                    }
                }
                mAdapter.setReportTemr(reportTerm);
            }
            //---------- 시간대 별로 끊어 보자 ----------------------------------------------------------------



            mAdapter.addAll(finalList);
            showMeYouDancing (finalList);
        }


        //이제 배송 정보 요약 ------------------------------------------------------------------------------------------------------------------------------
        double maxTemp = DataProviderUtil.getInstance(mApp.getApplicationContext()).getSavedMaxTemp(MAC);
        double minTemp = DataProviderUtil.getInstance(mApp.getApplicationContext()).getSavedMinTemp(MAC);
        double avrgTemp = (maxTemp + minTemp) / 2.00;


        String max = String.format(getResources().getString(R.string.job_delivery_temp_max_format), String.valueOf(maxTemp));
        String min = String.format(getResources().getString(R.string.job_delivery_temp_min_format), String.valueOf(minTemp));
        String average = String.format(getResources().getString(R.string.job_delivery_temp_avrg_format), String.valueOf(String.format("%.1f", avrgTemp)));

        item_max_temperature.setText(max);
        item_min_temperature.setText(min);
        item_avrg_temperature.setText(average);

        fv_item_max_temperature.setText(max);
        fv_item_min_temperature.setText(min);
        fv_item_avrg_temperature.setText(average);



        long start = 0;
        long end = 0;

        if (tempList.size() > 0 && AppSharedData.getShowAllReports()) {


            try {
                start = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(tempList.get(0).rtc).getTime();
                end = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(tempList.get(tempList.size() - 1).rtc).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long duration = end - start;

            String t = "";

            try {
                t = String.format(
                        getResources().getString(R.string.info_delivery_duration),
                        StringUtils.getMilliSecondsToTimeString (duration).split(",")[1],
                        String.valueOf(mAdapter.getFailedTotal()),
                        String.valueOf(String.format("%.2f",((double) mAdapter.getFailedTotal() / (double) (mAdapter.getCount() + mAdapter.getFailedTotal())) * 100.00)) + "%"
                );
            } catch (Exception e) {
                e.printStackTrace();
                t = "계산이 불가함";
            }

            delivery_duration_field.setText(t);
            fv_delivery_duration_field.setText(t);

        } else if (tempList.size() > 0) {

            try {
                start = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(tempList.get(0).rtc).getTime();
                end = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(tempList.get(tempList.size() - 1).rtc).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long duration = end - start;

            String t = String.format(
                    getResources().getString(R.string.info_delivery_duration_for_release),
                    StringUtils.getMilliSecondsToTimeString (duration).split(",")[1]
            );
            delivery_duration_field.setText(t);
            fv_delivery_duration_field.setText(t);
        }

        if (StringUtils.isEmpty(STICKER) && ic_sticker != null) ((TextView) ic_sticker.getActionView().findViewById(R.id.item_sticker)).setText(STICKER);

        if (StringUtils.isEmpty(DVIF.id) && ic_driver_info != null) {
            String driverInfo = String.format(getResources().getString(R.string.driver_info_text), DVIF.name, DVIF.vehicleNo);
            ((TextView) ic_driver_info.getActionView().findViewById(R.id.profile_driver)).setText(driverInfo);
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

        LimitLine ll1 = new LimitLine((float) max_temperature_limit, "");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        //ll1.setTypeface(tf);

        double min = (min_temperature_limit <= -1000) ? -120 : min_temperature_limit;
        LimitLine ll2 = new LimitLine((float) min, "");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        //ll2.setTypeface(tf);

        YAxis leftAxis = item_chart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaximum((float) (max_temperature_limit + 10));
        leftAxis.setAxisMinimum((float) (min - 10));
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

        item_chart.animateX(1000);

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
            set1 = new LineDataSet(values, getResources().getString(R.string.label_for_graph_temp));

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(2f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(false);
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


    /** 이전 화면에 결과 전송
     *
     * @param file
     */
    private void sendActivityResult (final File file, final String selectedLogerId) {


        DialogUtils.alert(
                mActivity,
                R.string.info_upload_complete,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent resultData = new Intent();
                        resultData.putExtra(EXTRA_KEY_MACADDRESS, selectedLogerId);
                        resultData.putExtra(EXTRA_KEY_PHOTO, file.getName());
                        setResult(RESULT_OK, resultData);
                        finish();
                    }
                }, false
        );
    }


    /** 배송 종료 인지 KTX 인지 인지
     *
     * @param tItem
     */
    private void checkAndStopMonitoring (final BeaconItemDto tItem) {

        String[] options = getResources().getStringArray(R.array.job_finish_options);

        if (options.length == 1) {
            JobProcessController.getInstance(mActivity, ActivityJobReport.this).stopMonitoring(tItem.MAC);
            return;
        }

        DialogUtils.singleChoice(
                mActivity,
                R.string.title_job_finish_option,
                options,
                -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                        switch (which) {

                            case 0 :
                                JobProcessController.getInstance(mActivity, ActivityJobReport.this).stopMonitoring(tItem.MAC);
                                break;

                            case 1 :

                                DialogUtils.alert(
                                        mActivity,
                                        R.string.alert_job_send_by_ktx,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                                                        + "islive=" + BeaconItemDto.INT_FALSE + ", "
                                                        + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_HANDOVER
                                                        + " WHERE MAC='" + tItem.MAC + "';";
                                                new DBHelper(BaseApplication.getInstance()).update(qry);

                                                onBackPressed();

                                            }
                                        }, true
                                );

                                break;

                        }

                    }
                }, true
        );
    }


    /** 러아프 싸이클 관련
     *
     */
    @Override
    public void onResume () {
        super.onResume();

        int step = DataProviderUtil.getInstance(mApp).getTrackingDevice(MAC).delivery_step;

        switch (step) {
            case BeaconItemDto.DELEVERY_STEP_READY :
                //이단계에서 이럴리가
                break;

            case BeaconItemDto.DELEVERY_STEP_DELIVERY :

                btn_job_finish.setText(R.string.delivery_state_btn_finish);
                btn_job_finish.setEnabled(true);

                break;

            case BeaconItemDto.DELEVERY_STEP_HANDOVER :

                btn_job_finish.setText(R.string.delivery_state_btn_switched);
                btn_job_finish.setEnabled(false);

                break;

            case BeaconItemDto.DELEVERY_STEP_INVOICE :
                btn_job_finish.setText(R.string.delivery_state_btn_invoice);
                btn_job_finish.setEnabled(true);
                break;

            case BeaconItemDto.DELEVERY_STEP_COMPLETE :
                btn_job_finish.setText(R.string.delivery_state_btn_finished);
                btn_job_finish.setEnabled(false);
                break;
        }




        if (JobForegroundMoinitor.getInstance().isMonitoring()) {
            JobForegroundMoinitor.getInstance().stopMonitoring (BaseApplication.getInstance());
        }
    }


    @Override
    public void onPause () {
        super.onPause();

        if (GeneralUtils.isApplicationBroughtToBackground(mApp)
                && !JobForegroundMoinitor.getInstance().isMonitoring()) {
            JobForegroundMoinitor.getInstance().startMonitoring(BaseApplication.getInstance());
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
        inflater.inflate(R.menu.job_report_actions, menu);
        ic_sticker = menu.findItem(R.id.ic_sticker);
        ((TextView) ic_sticker.getActionView().findViewById(R.id.item_sticker)).setText(STICKER);

        ic_driver_info = menu.findItem(R.id.ic_driver_info);

        String driverInfo = String.format(getResources().getString(R.string.driver_info_text), DVIF.name, DVIF.vehicleNo);
        ((TextView) ic_driver_info.getActionView().findViewById(R.id.profile_driver)).setText(driverInfo);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {

            case R.id.ic_driver_info :
                //nothing yet;
                return true;

            case R.id.ic_sticker :
                //nothing yet;
                return true;

            case R.id.ic_close :

                onBackPressed();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /** 온클릭
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_job_finish :

                BeaconItemDto tItem = new BeaconItemDto();
                tItem.MAC = MAC; //--> 나머지 데이타는 쓰지 않는다. 맥어드레스만 장전!!
                tItem.sticker = STICKER;


                int step = DataProviderUtil.getInstance(mApp).getTrackingDevice(MAC).delivery_step;
                switch (step) {
                    case BeaconItemDto.DELEVERY_STEP_READY :
                        //이단계에서 이럴리가
                        break;

                    case BeaconItemDto.DELEVERY_STEP_DELIVERY :
                        checkAndStopMonitoring (tItem);
                        break;

                    case BeaconItemDto.DELEVERY_STEP_HANDOVER :
                        //nothing yet;
                        break;

                    case BeaconItemDto.DELEVERY_STEP_INVOICE :
                        JobProcessController.getInstance(mActivity, this).sendPhoto(MAC);
                        break;

                    case BeaconItemDto.DELEVERY_STEP_COMPLETE :
                        //noting yet;
                        break;
                }

                break;

        }
    }


    /**롱클릭
     *
     * @param view
     * @return
     */
    @Override
    public boolean onLongClick(View view) {

        switch (view.getId()) {

            case R.id.delivery_duration_field :

                boolean t = !AppSharedData.getShowAllReports();
                AppSharedData.setShowAllReports(t);
                initData();

                break;

        }

        return true;
    }




    /** 뒤로가기
     *
     */
    @Override
    public void onBackPressed () {

        if (isLocked) return;

        setResult(RESULT_CANCELED);
        finish();

        //퇴장효과
        overridePendingTransition(R.anim.empty, R.anim.slide_down_out_fast);
    }




    //리스너 ----------------------------------------------------------------------------------------------------------------------------------------

    /** JobProcessListener ------------------------------------------------------------------------------ */


    /** 리포팅 간격 설정
     *
     * @param radioGroup
     * @param i
     */
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
        switch (radioGroup.getId()) {
            case R.id.rdo_report_term :
                initData();
                break;
        }
    }

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
        progressGo.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCheckOutSuccess(final String mac) {
        progressGo.setVisibility(View.GONE);
        DialogUtils.alert(
                mActivity,
                String.format(getResources().getString(R.string.info_delivery_is_over), JobProcessController.getInstance(mActivity, this).getStickerForService(mac)),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        JobProcessController.getInstance(mActivity, ActivityJobReport.this).sendPhoto(mac);
                    }
                }, false
        );


        onResume ();
        initData();
    }

    @Override
    public void onCheckOutFailed(String mac) {
        progressGo.setVisibility(View.GONE);
        DialogUtils.alert(
                mActivity,
                R.string.error_network,
                true
        );

        onResume();
        initData();
    }

    @Override
    public void onNotReportedDataSendFailed(String mac, int process) {
        onResume();
        initData();
    }

    @Override
    public void onNotReportedDataSendComplete(String mac, int process) {
        //nothing yet;
    }

    @Override
    public void onHandOutSuccess(Map<String, Object> map, File file, String selectedLogerId) {
        sendActivityResult (file, selectedLogerId);
    }

    @Override
    public void onHandOutSuccess(File file, String selectedLogerId) {
        sendActivityResult (file, selectedLogerId);
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
        mAdapter.clear();
        initData();
    }

    @Override
    public void onPhotoCompositionItemSelected(File file) {
        //nothign yet;
    }


    /** for IBaseAJobActivity --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- */
    @Override
    public void updateWifiStatus() {
        //nothing yet;
    }

    @Override
    public void updateBatteryStatus() {

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
                        //이거 작동안하는듯??/
                        initData();
                        mListView.smoothScrollToPosition(mAdapter.getCount());
                    }
                }
        );
    }

    @Override
    public void doDisableMe(String mac) {
        //nothing yet;
    }
}
