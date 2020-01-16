package com.netmania.checklod.general.adapter;

/**
 * Created by kevinhan on 16. 3. 8..
 */


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.general.utils.RecycleUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import lib.netmania.ble.model.BeaconDataModel;


/**
 * @author Kevin Han
 *
 */
public class BeaconItemListAdapter extends ArrayAdapter<BeaconItemDto> {

    public static final int RANGE_BTN_MODE_TEMP = 0;
    public static final int RANGE_BTN_MODE_ORDERNO = 1;


    protected List<BeaconItemDto> mItems;
    protected Context mContext;
    protected LayoutInflater mInflater;
    protected View.OnClickListener itemClickListener;
    protected List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();
    protected BaseActivity mActivity;


    //객체
    public BeaconItemListAdapter(Context context, ArrayList<BeaconItemDto> mItems, View.OnClickListener itemClickListener) {
        super (context, 0, mItems);

        this.mContext = context;
        this.mItems = mItems;
        this.mActivity = (BaseActivity) mContext;
        this.mInflater = LayoutInflater.from(mContext);
        this.itemClickListener = itemClickListener;

    }


    public void recycle() {
        for (WeakReference<View> ref : mRecycleList) {
            RecycleUtils.recursiveRecycle(ref.get());
        }
        if (mItems != null) {
            mItems.clear();
            mItems = null;
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mItems.size();
    }

    @Override
    public BeaconItemDto getItem (int position) {
        // TODO Auto-generated method stub
        return mItems.get(position);
    }

    /** 거시기..
     *
     * @param mac
     * @return
     */
    public BeaconItemDto getItem (String mac) {

        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).MAC.equals(mac)) return mItems.get(i);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public ArrayList<BeaconItemDto> getItems () {
        return (ArrayList<BeaconItemDto>) mItems;
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent) {
        //밑에서 한다. 하위 클래스에서..
        return convertView;
    }





    /** 존재하는 아이템인가?
     *
     * @param mac
     * @return
     */
    public boolean isExistBeacon (String mac) {

        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).MAC.equals(mac)) {
                return true;
            }
        }

        return false;
    }


    /** 온도범위 셋팅 여부
     *
     * @param position
     * @return
     */
    public boolean isTempRageSet (int position) {
        return (mItems.get(position).max_temperature_limit - mItems.get(position).min_temperature_limit != 0);
    }

    public boolean isTempRageSet (String mac) {

        int tPosition = -1;
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).MAC.equals(mac)) {
                tPosition = i;
                break;
            }
        }

        if (tPosition >= 0) {
            return (mItems.get(tPosition).max_temperature_limit - mItems.get(tPosition).min_temperature_limit != 0);
        } else {
            return true;
        }
    }

    /** 업데이트
     *
     * @param tItem
     * @return
     */
    public void update (BeaconItemDto tItem) {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).MAC.equals(tItem.MAC)) {

                mItems.get(i).last_seq = tItem.last_seq;
                mItems.get(i).sticker = tItem.sticker;
                mItems.get(i).timestamp = tItem.timestamp;
                mItems.get(i).temp_probe = tItem.temp_probe;
                mItems.get(i).temp_chipset = tItem.temp_chipset;
                mItems.get(i).hum_probe = tItem.hum_probe;
                mItems.get(i).hum_chipset = tItem.hum_chipset;
                mItems.get(i).islive = tItem.islive;
                mItems.get(i).max_temperature_limit = tItem.max_temperature_limit;
                mItems.get(i).min_temperature_limit = tItem.min_temperature_limit;
                mItems.get(i).bcn_status = tItem.bcn_status;
                mItems.get(i).rtc = tItem.rtc;

                mItems.get(i).delivery_step = tItem.delivery_step;
                mItems.get(i).is_data_checked_in = tItem.is_data_checked_in;
                mItems.get(i).is_data_downloaded = tItem.is_data_downloaded;
                mItems.get(i).ble_signal_cycle = tItem.ble_signal_cycle;
                mItems.get(i).rssi = tItem.rssi;

                mItems.get(i).$max_temp = tItem.$max_temp;
                mItems.get(i).$min_temp = tItem.$min_temp;
                mItems.get(i).$show_detail = tItem.$show_detail;

                break;
            }
        }
    }


    /** 배송중인 물품이 하나라도 있는가??
     *
     * @return
     */
    public boolean isWorkingOnIt () {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).delivery_step == BeaconItemDto.DELEVERY_STEP_DELIVERY
                    || mItems.get(i).delivery_step == BeaconItemDto.DELEVERY_STEP_INVOICE) return true;
        }

        return false;
    }


    /** 배송중인 물품이 하나라도 있는가??
     *
     * @return
     */
    public boolean isDeliverying () {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).delivery_step == BeaconItemDto.DELEVERY_STEP_DELIVERY) return true;
        }

        return false;
    }



    /** 배송중인데 잉??
     *
     * @return
     */
    public BeaconItemDto getItemNotRunning () {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).delivery_step == BeaconItemDto.DELEVERY_STEP_DELIVERY
                    && mItems.get(i).bcn_status < BeaconDataModel.BCN_STATUS_RUN) return mItems.get(i);
        }

        return null;
    }


    /** 알람용 전체 상태
     *
     */
    public int getTotalStatus () {

        //망한게 있나????
        for (int i = 0; i < mItems.size (); i++) {
            if (mItems.get(i).$status == BeaconItemDto.STATUS_EMERGNECY) {
                return BeaconItemDto.STATUS_EMERGNECY;
            }
        }

        //주의할게 있나????
        for (int i = 0; i < mItems.size (); i++) {
            if (mItems.get(i).$status == BeaconItemDto.STATUS_CAUTION) {
                return BeaconItemDto.STATUS_CAUTION;
            }
        }

        return BeaconItemDto.STATUS_STABLE;
    }




    //아이템에 사용될 뷰홀더 객체
    public class ViewHolder {
        //notthing yet;
    }

}

