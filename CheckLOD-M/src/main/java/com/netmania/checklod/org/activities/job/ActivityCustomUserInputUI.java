package com.netmania.checklod.org.activities.job;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.manage.FlagBox;
import com.netmania.checklod.org.R;
import com.netmania.checklod.org.manage.Constants;

/**
 * Created by hansangcheol on 2018. 2. 8..
 */

public class ActivityCustomUserInputUI extends BaseActivity implements View.OnClickListener, View.OnLongClickListener {


    //상수


    //UI
    private View dialog_container, dialog_title_container, dialog_input_container, dialog_input_1_container, dialog_input_2_container, dialog_spacer, click_protector;
    private ImageView dialog_logo;
    private TextView dialog_msg, dialog_input_label_1, dialog_input_label_2;
    private EditText dialog_input_1, dialog_input_2;
    private MaterialRippleLayout dialog_btn_ok_container, dialog_btn_no_container;
    private Button dialog_btn_ok, dialog_btn_no;


    //데이타
    private int MODE;
    private BeaconItemDto DATA;


    //객체




    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_input_ui);

        //등장효과
        overridePendingTransition(R.anim.fade_in, R.anim.empty);

        MODE = getIntent().getIntExtra(FlagBox.EXTRA_KEY_MODE, FlagBox.MODE_ADD_DEVICE);
        DATA = getIntent().getParcelableExtra(FlagBox.EXTRA_KEY_DATA);

        setaLayout ();
        initData ();
    }


    /** 레이아웃 셋팅
     *
     */
    private void setaLayout () {

        dialog_container = findViewById(R.id.dialog_container);
        dialog_title_container = findViewById(R.id.dialog_title_container);
        dialog_logo = (ImageView) findViewById(R.id.dialog_logo);

        dialog_msg = (TextView) findViewById(R.id.dialog_msg);

        dialog_input_container = findViewById(R.id.dialog_input_container);

        dialog_input_1_container = findViewById(R.id.dialog_input_1_container);
        dialog_input_label_1 = (TextView) findViewById(R.id.dialog_input_label_1);
        dialog_input_1 = (EditText) findViewById(R.id.dialog_input_1);


        dialog_input_2_container = findViewById(R.id.dialog_input_2_container);
        dialog_input_label_2 = (TextView) findViewById(R.id.dialog_input_label_2);
        dialog_input_2 = (EditText) findViewById(R.id.dialog_input_2);

        dialog_spacer = findViewById(R.id.dialog_spacer);

        dialog_btn_ok_container = (MaterialRippleLayout) findViewById(R.id.dialog_btn_ok_container);
        dialog_btn_ok = (Button) findViewById(R.id.dialog_btn_ok);
        dialog_btn_ok.setOnClickListener(this);

        dialog_btn_no_container = (MaterialRippleLayout) findViewById(R.id.dialog_btn_no_container);
        dialog_btn_no = (Button) findViewById(R.id.dialog_btn_no);
        dialog_btn_no.setOnClickListener(this);



        switch (MODE) {

            default :
            case FlagBox.MODE_ADD_DEVICE :

                dialog_msg.setVisibility(View.VISIBLE);
                dialog_msg.setText(R.string.job_ready_give_me_sticker_no);

                dialog_input_1_container.setVisibility(View.VISIBLE);
                dialog_input_label_1.setText(R.string.label_for_input_sticker_no);

                //if (Constants.IS_RELEASED) {
                    dialog_input_1.setInputType(InputType.TYPE_CLASS_NUMBER);
                /*} else {
                    dialog_input_1.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }*/

                dialog_input_2_container.setVisibility(View.GONE);

                break;

            case FlagBox.MODE_ADMIN_DELIVERY :

                dialog_msg.setVisibility(View.VISIBLE);
                dialog_msg.setText(R.string.admin_delivery_msg);

                dialog_input_1_container.setVisibility(View.VISIBLE);
                dialog_input_label_1.setText(R.string.label_for_input_phone_no);
                dialog_input_1.setInputType(InputType.TYPE_CLASS_PHONE);

                dialog_input_2_container.setVisibility(View.VISIBLE);
                dialog_input_label_2.setText(R.string.label_for_input_sticker_no);
                dialog_input_2.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;

        }

        click_protector = findViewById(R.id.click_protector);
        click_protector.setOnClickListener(this);
    }


    /** 데이타 이니셜라이징
     *
     */
    private void initData () {
        //nothing yet;
    }


    /** 온클릭
     *
     * @param v
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.dialog_btn_ok :

                Intent resultData = new Intent();
                resultData.putExtra(FlagBox.EXTRA_KEY_MODE, MODE);
                resultData.putExtra(FlagBox.INOUT_STR_1, String.valueOf(dialog_input_1.getText()).trim());
                resultData.putExtra(FlagBox.INOUT_STR_2, String.valueOf(dialog_input_2.getText()).trim());
                resultData.putExtra(FlagBox.EXTRA_KEY_DATA, getIntent().getParcelableExtra(FlagBox.EXTRA_KEY_DATA));

                setResult(RESULT_OK, resultData);

                finish();
                //퇴장 효과
                overridePendingTransition(R.anim.empty, R.anim.fade_out);

                break;

            case R.id.dialog_btn_no :
                onBackPressed();
                break;

        }

    }


    /** 온 롱클릭
     *
     * @param v
     * @return
     */
    @Override
    public boolean onLongClick(View v) {
        //nothign yet;
        return false;
    }


    /** 뒤로가기
     *
     */
    @Override
    public void onBackPressed () {
        super.onBackPressed();

        //퇴장 효과
        overridePendingTransition(R.anim.empty, R.anim.fade_out);
    }
}
