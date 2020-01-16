package com.netmania.checklod.tnt.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jivimberg.library.AutoResizeTextView;
import com.netmania.checklod.general.adapter.BeaconItemListAdapter;
import com.netmania.checklod.general.data.AppSharedData;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.utils.StringUtils;
import com.netmania.checklod.tnt.R;
import com.netmania.checklod.tnt.manage.Constants;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import lib.netmania.ble.model.BeaconDataModel;

/**
 * Created by hansangcheol on 2018. 3. 13..
 */

public class BeaconListAdapterForJob extends BeaconItemListAdapter {

    public BeaconListAdapterForJob(Context context, ArrayList<BeaconItemDto> mItems, View.OnClickListener itemClickListener) {
        super(context, mItems, itemClickListener);
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_beaconlist_for_job, null);
            holder = new ViewHolder();

            holder.item_container = convertView.findViewById(R.id.item_container);
            holder.item_sticker = (TextView) convertView.findViewById(R.id.item_sticker);
            holder.item_ble_status = (ImageView) convertView.findViewById(R.id.item_ble_status);
            holder.item_temperature_rage = (TextView) convertView.findViewById(R.id.item_temperature_rage);

            holder.item_seq = (TextView) convertView.findViewById(R.id.item_seq);

            holder.item_data_container = convertView.findViewById(R.id.item_data_container);

            holder.item_report = (ImageView) convertView.findViewById(R.id.item_report);

            holder.item_timestamp = (TextView) convertView.findViewById(R.id.item_timestamp);

            holder.item_temp_probe = (AutoResizeTextView) convertView.findViewById(R.id.item_temp_probe);
            holder.item_temp_char = (TextView) convertView.findViewById(R.id.item_temp_char);

            holder.item_info_container = convertView.findViewById(R.id.item_info_container);

            holder.item_ready_container = convertView.findViewById(R.id.item_ready_container);
            holder.item_btn_delete = (Button) convertView.findViewById(R.id.item_btn_delete);
            holder.item_btn_temp_range = (Button) convertView.findViewById(R.id.item_btn_temp_range);

            holder.item_delivery_container = convertView.findViewById(R.id.item_delivery_container);
            holder.item_max_temperature = (TextView) convertView.findViewById(R.id.item_max_temperature);
            holder.item_min_temperature = (TextView) convertView.findViewById(R.id.item_min_temperature);

            holder.item_handover_container = convertView.findViewById(R.id.item_handover_container);
            holder.item_btn_delete_2 = (Button) convertView.findViewById(R.id.item_btn_delete_2);


            holder.item_invoice_container = convertView.findViewById(R.id.item_invoice_container);
            holder.item_btn_send_photo = (Button) convertView.findViewById(R.id.item_btn_send_photo);

            holder.item_complete_container = convertView.findViewById(R.id.item_complete_container);
            holder.item_btn_delete_3 = (Button) convertView.findViewById(R.id.item_btn_delete_3);

            holder.item_cargo_container = convertView.findViewById(R.id.item_cargo_container);

            holder.item_ble_check = (CheckBox) convertView.findViewById(R.id.item_ble_check);



            convertView.setTag (holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BeaconItemDto item = getItem (position);


        //스티커 표시
        holder.item_sticker.setText(item.sticker);


        // BLE 신호 표시
        if (item.delivery_step > BeaconItemDto.DELEVERY_STEP_READY
                && item.ble_signal_cycle > 0
                && Calendar.getInstance().getTimeInMillis() - Long.valueOf(item.timestamp) >= (item.ble_signal_cycle * Constants.BLE_SIGNAL_CHECK_CYCLE_K)) {

                item.bcn_status = BeaconDataModel.BCN_STATUS_OFF;
                item.temp_probe = Constants.NO_DATA;
        }

        switch (item.bcn_status) {

            case BeaconDataModel.BCN_STATUS_OFF :
                holder.item_ble_status.setImageResource(R.drawable.ic_bluetooth_off);
                break;


            case BeaconDataModel.BCN_STATUS_READY :
                holder.item_ble_status.setImageResource(R.drawable.ic_bluetooth_on);
                break;


            case BeaconDataModel.BCN_STATUS_RUN :
                holder.item_ble_status.setImageResource(R.drawable.ic_bluetooth_run);
                break;
        }


        //온도 범위 항목
        String[] asset = Constants.getTemperatureRangeAsset ();
        String[] rangeList = new String[asset.length];


        int selectedItem = -1;

        String[] tAsset;
        for (int i = 0; i < asset.length; i++) {
            tAsset = asset[i].split(",");
            rangeList[i] = tAsset[0];

            if (item.min_temperature_limit == Integer.valueOf(tAsset[1])
                    && item.max_temperature_limit == Integer.valueOf(tAsset[2])) {

                selectedItem = i;

            }
        }

        if (selectedItem >= 0) {
            holder.item_temperature_rage.setText(rangeList[selectedItem]);
        } else if (item.is_tookover == BeaconItemDto.INT_TRUE) {
            holder.item_temperature_rage.setText(R.string.info_takeover_temp_range_2);
        } else {
            holder.item_temperature_rage.setText(R.string.info_takeover_temp_range_1);
        }






        //온도 상태에 따른 status 셋팅
        double safe_gab = (item.max_temperature_limit - item.min_temperature_limit) * Constants.SAFE_GAB_T;

        try {
            //인수 인계건이고 준비단계일때 ---> 상태이상을 표시하면 안됨 번위를 모름....
            if (item.delivery_step == BeaconItemDto.DELEVERY_STEP_READY && item.is_tookover == BeaconItemDto.INT_TRUE) {
                item.$status = BeaconItemDto.STATUS_STABLE;
            }
            //거시기 냉동 대응 봅세...
            else if (item.min_temperature_limit <= -1000) {

                //범위 초과일때 --->
                if (Double.valueOf(item.temp_probe) > item.max_temperature_limit) {
                    item.$status = BeaconItemDto.STATUS_EMERGNECY;
                }
                //위험구간일때 --->
                else if (Double.valueOf(item.temp_probe) > item.max_temperature_limit - Constants.FROZEN_SAFE_GAB) {
                    item.$status = BeaconItemDto.STATUS_CAUTION;
                }
                //안정권일때 --->
                else {
                    item.$status = BeaconItemDto.STATUS_STABLE;
                }

            } else {

                //안정권일때 --->
                if (Double.valueOf(item.temp_probe) > item.min_temperature_limit + safe_gab && Double.valueOf(item.temp_probe) < item.max_temperature_limit - safe_gab) {
                    item.$status = BeaconItemDto.STATUS_STABLE;
                }
                //위험구간일때 --->
                else if (Double.valueOf(item.temp_probe) > item.min_temperature_limit && Double.valueOf(item.temp_probe) < item.max_temperature_limit) {
                    item.$status = BeaconItemDto.STATUS_CAUTION;
                }
                //범위 초과일때 --->
                else {
                    item.$status = BeaconItemDto.STATUS_EMERGNECY;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();

            item.$status = BeaconItemDto.STATUS_STABLE;
        }


        //디버깅 정보
        if (AppSharedData.getShowAllReports()) {
            holder.item_seq.setVisibility(View.VISIBLE);
            holder.item_seq.setText(
                    item.MAC
                            + "\nlast seq = " + item.last_seq
                            + "\ndelivery_step = " + BeaconItemDto.getDeleryStepName(item.delivery_step)
                            + "\nisLive = " + item.islive
                            + "\nstatus = " + BeaconItemDto.getStatusName (item.$status)
                            + "\nis_data_checked_in = " + item.is_data_checked_in
                            + "\nis_data_downloaded = " + item.is_data_downloaded
                            + "\ntimestamp (long) = " + item.timestamp
                            + "\ntimestamp = " + StringUtils.convertToStringYYYY_MM_DD(new Date (item.timestamp))
                            + "\nble_signal_cycle = " + StringUtils.getMilliSecondsToTimeString(item.ble_signal_cycle)
                            + "\nble_signal_strength = " + item.rssi
                            + "\nbcn_battery = " + item.bcn_battery + "V"
            );
        } else {
            holder.item_seq.setText("");
            holder.item_seq.setVisibility(View.GONE);
        }


        //측정 시간 표시
        String date = new SimpleDateFormat("HH시 mm분 ss초", Locale.KOREA).format(new Date(item.timestamp));
        holder.item_timestamp.setText(date);


        //현재 온도 표시
        if (Constants.NO_DATA.equals(item.temp_probe)) {
            holder.item_temp_probe.setText(item.temp_probe);
        } else {
            holder.item_temp_probe.setText("" + String.format("%.1f" ,Double.valueOf(item.temp_probe)));
        }


        //경고 때려보자//경고 때려보자
        AnimationDrawable frameAnimation;
        switch (item.$status) {

            default :
            case BeaconItemDto.STATUS_STABLE :
                holder.item_info_container.setBackgroundResource(R.mipmap.steady_background_clip);
                holder.item_temp_probe.setTextColor(mContext.getResources().getColor(R.color.steady_text_color));
                holder.item_temp_char.setTextColor(mActivity.getResources().getColor(R.color.steady_text_color));
                break;

            case BeaconItemDto.STATUS_CAUTION :
                holder.item_info_container.setBackgroundResource(com.netmania.checklod.tnt.R.drawable.anim_caution);
                holder.item_temp_probe.setTextColor(mContext.getResources().getColor(R.color.white));
                holder.item_temp_char.setTextColor(mActivity.getResources().getColor(R.color.white));

                frameAnimation = (AnimationDrawable) holder.item_info_container.getBackground();
                frameAnimation.start();
                break;

            case BeaconItemDto.STATUS_EMERGNECY :
                holder.item_info_container.setBackgroundResource(com.netmania.checklod.tnt.R.drawable.anim_emergency);
                holder.item_temp_probe.setTextColor(mContext.getResources().getColor(R.color.white));
                holder.item_temp_char.setTextColor(mActivity.getResources().getColor(R.color.white));

                frameAnimation = (AnimationDrawable) holder.item_info_container.getBackground();
                frameAnimation.start();
                break;

        }


        //상황별 셋팅 ----------------------------------------------------------------------------------------
        holder.item_ready_container.setVisibility(View.GONE);
        holder.item_delivery_container.setVisibility(View.GONE);
        holder.item_handover_container.setVisibility(View.GONE);
        holder.item_invoice_container.setVisibility(View.GONE);
        holder.item_complete_container.setVisibility(View.GONE);
        holder.item_cargo_container.setVisibility(View.GONE);
        holder.item_ble_check.setVisibility(View.GONE);
        holder.item_ble_check.setTag(position);

        switch (item.delivery_step) {

            default :
            case BeaconItemDto.DELEVERY_STEP_READY :
                holder.item_ready_container.setVisibility(View.VISIBLE);

                holder.item_data_container.setVisibility(View.VISIBLE);

                /* 온도가 셋팅 되었거나 인수예정일때 온도설정 못하게 함 -- 셋팅되었어도 변경가능하도록 주석 */
                if (/*isTempRageSet(position) || */item.is_tookover == BeaconItemDto.INT_TRUE) {
                    holder.item_btn_temp_range.setEnabled(false);
                } else {
                    holder.item_btn_temp_range.setEnabled(true);
                }

                holder.item_ble_check.setVisibility(View.VISIBLE);
                holder.item_ble_check.setChecked(item.$check_to_start);
                holder.item_ble_check.setOnCheckedChangeListener(mItemCheckListener);


                break;

            case BeaconItemDto.DELEVERY_STEP_DELIVERY :
                holder.item_data_container.setVisibility(View.VISIBLE);
                holder.item_delivery_container.setVisibility(View.VISIBLE);
                break;

            case BeaconItemDto.DELEVERY_STEP_HANDOVER :
                holder.item_data_container.setVisibility(View.GONE);
                holder.item_handover_container.setVisibility(View.VISIBLE);
                break;

            case BeaconItemDto.DELEVERY_STEP_INVOICE :
                holder.item_data_container.setVisibility(View.GONE);
                holder.item_invoice_container.setVisibility(View.VISIBLE);
                break;

            case BeaconItemDto.DELEVERY_STEP_COMPLETE :
                holder.item_data_container.setVisibility(View.GONE);
                holder.item_complete_container.setVisibility(View.VISIBLE);
                break;

        }


        //최고 최저 온도
        holder.item_max_temperature.setText(String.format(mContext.getResources().getString(R.string.job_delivery_temp_max_format), String.valueOf(item.$max_temp)));
        holder.item_min_temperature.setText(String.format(mContext.getResources().getString(R.string.job_delivery_temp_min_format), String.valueOf(item.$min_temp)));


        if (item.$show_detail) {
            holder.item_timestamp.setVisibility(View.VISIBLE);
            holder.item_max_temperature.setVisibility(View.VISIBLE);
            holder.item_min_temperature.setVisibility(View.VISIBLE);
        } else {
            holder.item_timestamp.setVisibility(View.GONE);
            holder.item_max_temperature.setVisibility(View.GONE);
            holder.item_min_temperature.setVisibility(View.GONE);
        }


        if (item.min_temperature_limit <= -1000) {

            //상태 아아콘 뽑아 보자
            if (item.$max_temp > item.max_temperature_limit) {
                holder.item_report.setImageResource(R.drawable.ic_bad);
            } else if (item.$max_temp > item.max_temperature_limit - Constants.FROZEN_SAFE_GAB) {
                holder.item_report.setImageResource(R.drawable.ic_hmm);
            } else {
                holder.item_report.setImageResource(R.drawable.ic_good);
            }

        } else {

            //상태 아아콘 뽑아 보자 -------------------------------------------------------------------------------------
            /* 온도가 범위를 벗어남 -- 낮아짐 */
            if (item.$min_temp < item.min_temperature_limit
                    || item.$max_temp > item.max_temperature_limit) {
                holder.item_report.setImageResource(R.drawable.ic_bad);
            } else if (item.$min_temp < item.min_temperature_limit + safe_gab
                    || item.$max_temp > item.max_temperature_limit - safe_gab) {
                holder.item_report.setImageResource(R.drawable.ic_hmm);
            } else {
                holder.item_report.setImageResource(R.drawable.ic_good);
            }
        }

        if (item.delivery_step < BeaconItemDto.DELEVERY_STEP_DELIVERY
                || (item.$max_temp == 0 && item.$min_temp == 0)) {
            holder.item_report.setVisibility(View.GONE);
        } else if (item.$status == BeaconItemDto.STATUS_STABLE) {
            holder.item_report.setVisibility(View.VISIBLE);
        } else {
            holder.item_report.setVisibility(View.GONE);
        }




        //필요한 이벤트 선언
        holder.item_btn_delete.setTag(position);
        holder.item_btn_delete.setOnClickListener(itemClickListener);

        holder.item_btn_temp_range.setTag(position);
        holder.item_btn_temp_range.setOnClickListener(itemClickListener);

        holder.item_report.setTag(position);
        holder.item_report.setOnClickListener(itemClickListener);

        holder.item_btn_delete_2.setTag(position);
        holder.item_btn_delete_2.setOnClickListener(itemClickListener);

        holder.item_btn_delete_3.setTag(position);
        holder.item_btn_delete_3.setOnClickListener(itemClickListener);

        holder.item_btn_send_photo.setTag(position);
        holder.item_btn_send_photo.setOnClickListener(itemClickListener);

        if (!Constants.IS_RELEASED) {
            holder.item_sticker.setTag(position);
            holder.item_sticker.setOnClickListener(itemClickListener);
        }

        holder.item_container.setTag(position);
        holder.item_container.setOnClickListener(itemClickListener);


        item = null;


        mRecycleList.add(new WeakReference<View>(convertView));
        return convertView;
    }



    //아이템에 사용될 뷰홀더 객체
    public class ViewHolder extends BeaconItemListAdapter.ViewHolder {
        public View item_container, item_info_container, item_ready_container, item_delivery_container, item_handover_container, item_invoice_container, item_complete_container, item_data_container, item_cargo_container;
        public TextView item_sticker, item_temperature_rage, item_temp_char, item_seq, item_max_temperature, item_min_temperature, item_timestamp;
        public AutoResizeTextView item_temp_probe;
        public ImageView item_ble_status, item_report;
        public Button item_btn_delete, item_btn_delete_2, item_btn_delete_3, item_btn_temp_range, item_btn_send_photo;
        public CheckBox item_ble_check;
    }

    //리스너
    private CompoundButton.OnCheckedChangeListener mItemCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int tPosition = (int) buttonView.getTag();
            mItems.get(tPosition).$check_to_start = isChecked;
        }
    };
}
