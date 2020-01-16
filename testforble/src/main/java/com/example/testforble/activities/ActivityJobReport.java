package com.example.testforble.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.testforble.R;
import com.example.testforble.adapter.TemperReportListAdapter;
import com.example.testforble.data.DataProviderUtil;
import com.example.testforble.dto.TemperatureTrackingDto;
import com.example.testforble.utils.DialogUtils;
import com.example.testforble.utils.LogUtil;

import java.util.ArrayList;

public class ActivityJobReport extends BaseActivity implements View.OnClickListener {

    //상수
    public static final String EXTRA_KEY_MACADDRESS = "extraKeyMacAdderess";
    public static final String EXTRA_KEY_STICKER = "extraKeySticker";


    //UI
    private ListView mListView;
    private Button btn_next;


    //데이타
    private TemperReportListAdapter mAdapter;
    private String MAC;
    private String STICKER;
    private int currentPos = 0;


    //객체


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_report);

        MAC = getIntent().getStringExtra(EXTRA_KEY_MACADDRESS);
        STICKER = getIntent().getStringExtra(EXTRA_KEY_STICKER);

        setLayout ();
        initData ();
    }


    /** 레이아웃 셋팅
     *
     */
    public void setLayout () {

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new TemperReportListAdapter(mActivity, new ArrayList<TemperatureTrackingDto>(), this);
        mListView.setAdapter(mAdapter);

        //일단 임시로 여기다 붙임 ----------------------------------------------------------------------------------------------------------------------
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //nothing yet;
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

            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //nothing yet;
            }

        });


        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);

    }


    /** 데이타 이니셜라이징
     *
     */
    public void initData () {

        ArrayList<TemperatureTrackingDto> tempList = DataProviderUtil.getInstance(mApp.getApplicationContext()).getReportedTemperatures(MAC, false, 0, 0, false);

        mAdapter.clear();
        mAdapter.addAll(tempList);
        //mListView.smoothScrollToPosition(mAdapter.getCount() - 1);
    }


    /** 온클릭
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_next :

                boolean k = true;
                //LogUtil.I("왜? --> " + currentPos + " --> " + mAdapter.getCount());

                if (currentPos < mListView.getFirstVisiblePosition()) currentPos = mListView.getFirstVisiblePosition();

                for (int i = currentPos; i < mAdapter.getCount(); i++) {

                    //LogUtil.I("왜? --> " + i);
                    if (i > 0 && mAdapter.getItem(i).seq - mAdapter.getItem(i - 1).seq > 1
                            || mAdapter.getItem(i).is_adjusted == 1) {
                        k = false;

                        if (i - currentPos > 50) mListView.setSelection(i - 20);
                        mListView.smoothScrollToPosition(i);
                        currentPos = i + 1;
                        break;
                    }
                }

                //LogUtil.I("왜? --> " + k);
                if (k) {
                    Toast.makeText(mActivity, "누락구간이 더이상 없습니다.", Toast.LENGTH_SHORT).show();
                    mListView.smoothScrollToPosition(mAdapter.getCount() - 1);
                }

                break;

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
