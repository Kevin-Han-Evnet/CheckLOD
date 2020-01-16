package com.netmania.checklod.tnt.activities.general;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.tnt.R;

public class ActivityLockCover extends BaseActivity implements View.OnClickListener, View.OnLongClickListener {

    //상수


    //UI
    private View root_view;


    //데이타


    //객체


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockcover);

        //늘깨어 있으라~ 잠들지 말아라~-----------------------------------------------------------------------
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getIntent().addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //----------------------------------------------------------------------------------------------

        setLayout ();
        initData ();
    }


    /** 레이아웃 셋팅
     *
     */
    private void setLayout () {
        root_view = findViewById(R.id.root_view);
        root_view.setOnLongClickListener(this);
    }


    /** 데이타 이니셜라이징
     *
     */
    private void initData () {
        //nothing yet;
    }


    /** 온클릭
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        //nothign yet;
    }


    /** 뒤로 가기
     *
     */
    @Override
    public void onBackPressed () {
        //nothing yet;
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.root_view :
                super.onBackPressed();
                break;
        }
        return true;
    }
}
