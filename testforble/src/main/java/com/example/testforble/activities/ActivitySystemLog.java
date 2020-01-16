package com.example.testforble.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.testforble.R;
import com.example.testforble.adapter.SystemLogListAdapter;
import com.example.testforble.data.DataProviderUtil;
import com.example.testforble.dto.SystemLogDto;

import java.util.ArrayList;

public class ActivitySystemLog extends BaseActivity implements View.OnClickListener {


    //상수


    //UI
    private ListView mListView;
    private Button btn_next;


    //데이타
    private SystemLogListAdapter mAdapter;
    private String MAC;
    private String STICKER;
    private int currentPos = 0;


    //객체



    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_report);

        setLayout ();
        initData ();
    }


    /** 레이아웃 셋팅
     *
     */
    private void setLayout () {
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new SystemLogListAdapter(mActivity, new ArrayList<SystemLogDto>(), this);
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
        btn_next.setVisibility(View.GONE);
    }


    /** 데이타 이니셜라이징
     *
     */
    private void initData () {
        ArrayList<SystemLogDto> tempList = DataProviderUtil.getInstance(mApp.getApplicationContext()).getSystemLog ();

        mAdapter.clear();
        mAdapter.addAll(tempList);
        //mListView.smoothScrollToPosition(mAdapter.getCount() - 1);
    }


    /** 온 클릭
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        //nothing yet;
    }


    /** 온 백프레스
     *
     */
    @Override
    public void onBackPressed () {
        super.onBackPressed();
    }
}
