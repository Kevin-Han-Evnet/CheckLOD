package com.example.testforble.adapter;

/**
 * Created by hansangcheol on 2017. 8. 3..
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testforble.R;
import com.example.testforble.activities.BaseActivity;
import com.example.testforble.data.AppSharedData;
import com.example.testforble.data.DBHelper;
import com.example.testforble.dto.TemperatureTrackingDto;
import com.example.testforble.manage.Constants;
import com.example.testforble.utils.RecycleUtils;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * @author Kevin Han
 *
 */
public class TemperReportListAdapter extends ArrayAdapter<TemperatureTrackingDto> {


    private List<TemperatureTrackingDto> mItems;
    private Context mContext;
    private LayoutInflater mInflater;
    private View.OnClickListener itemClickListener;
    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();
    private BaseActivity mActivity;
    private long report_term;


    //객체
    private DBHelper tntDBHelper;

    public TemperReportListAdapter(Context context, ArrayList<TemperatureTrackingDto> mItems, View.OnClickListener itemClickListener) {
        super (context, 0, mItems);

        this.mContext = context;
        this.mItems = mItems;
        this.mActivity = (BaseActivity) mContext;
        this.mInflater = LayoutInflater.from(mContext);
        this.itemClickListener = itemClickListener;
        this.report_term = Constants.REPORT_TIME_GAB_1;
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
    public TemperatureTrackingDto getItem (int position) {
        // TODO Auto-generated method stub
        if (position >= mItems.size()) return null;
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public ArrayList<TemperatureTrackingDto> getItems () {
        return (ArrayList<TemperatureTrackingDto>) mItems;
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_temperature_report, null);
            holder = new ViewHolder();


            holder.item_container = convertView.findViewById(R.id.item_container);
            holder.item_date = (TextView) convertView.findViewById(R.id.item_date);
            holder.item_dot = (ImageView) convertView.findViewById(R.id.item_dot);
            holder.item_temperature = (TextView) convertView.findViewById(R.id.item_temperature);
            holder.item_seq = (TextView) convertView.findViewById(R.id.item_seq);
            holder.item_im_first = (ImageView) convertView.findViewById(R.id.item_im_first);


            convertView.setTag (holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TemperatureTrackingDto item = getItem (position);


        int gab_idx = 0; //mItems.size() - 1;
        Date tRTC = new Date();
        try {
            tRTC = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(mItems.get(gab_idx).rtc);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long time_gab = mItems.get(gab_idx).timestamp - tRTC.getTime();

        Date rtc = new Date ();
        try {
            rtc = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(item.rtc);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String strTime = new SimpleDateFormat(getContext().getResources().getString(R.string.report_timeformat_1), Locale.KOREA).format(new Date(item.timestamp));

        if (AppSharedData.getShowAllReports()) {
            strTime += "\n" + new SimpleDateFormat(getContext().getResources().getString(R.string.report_timeformat_2), Locale.KOREA).format(rtc);
            strTime += "\n" + item.rtc;
        }
        holder.item_date.setText(strTime);

        if (getCount() == 1 && position == 0) {
            holder.item_dot.setImageResource(R.mipmap.ft_img_dot_bottom);
        } else if (position == 0) {
            holder.item_dot.setImageResource(R.mipmap.ft_img_dot_top);
        } else if (position == getCount() - 1) {
            holder.item_dot.setImageResource(R.mipmap.ft_img_dot_bottom);
        } else {
            holder.item_dot.setImageResource(R.mipmap.ft_img_dot_mid);
        }


        String strValue = Double.valueOf(item.temp) + "℃ / " + Double.valueOf(item.hum) + "% / " + item.battery;
        holder.item_temperature.setText(strValue);

        int seq_gab = (position > 0) ? item.seq - mItems.get(position - 1).seq : 0;
        item.$failed_count = (position > 0) ? seq_gab - 1 : 0;

        String str_check;
        int tColor;

        //if (item.sent == 1 && AppSharedData.getShowAllReports()) {
            str_check = String.valueOf(item.seq) + " -- ";
            str_check += (item.$failed_count  < 1) ? getContext().getResources().getString(R.string.report_item_status_normal) : "(" + item.$failed_count + ")";
            tColor = (item.$failed_count  < 1 && item.is_adjusted == TemperatureTrackingDto.LIVE_DATA) ? Color.parseColor("#50c62f") : Color.RED;
            if (item.$failed_count > 0) {
                holder.item_container.setBackgroundColor(Color.parseColor("#ccf480"));
            } else if (position % 2 == 0) {
                holder.item_container.setBackgroundColor(Color.parseColor("#f6f8f8"));
            } else {
                holder.item_container.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        /*} else if (item.sent == 1) {
            str_check = getContext().getResources().getString(R.string.report_item_status_reported);
            tColor = Color.parseColor("#50c62f");
        } else {
            str_check = (AppSharedData.getShowAllReports()) ? String.valueOf(item.seq) + " -- " : "";
            str_check += getContext().getResources().getString(R.string.report_item_status_standby);
            tColor = Color.RED;
        }*/


        String t = (item.is_adjusted == TemperatureTrackingDto.LIVE_DATA) ? "L" : "F";
        if (item.is_adjusted == TemperatureTrackingDto.ADJUSTED_DATA) tColor = Color.parseColor("#f3aa64");

            holder.item_seq.setText(str_check + " :: " + t);
        holder.item_seq.setTextColor(tColor);


        if (AppSharedData.getShowAllReports() && item.is_first > 0) {
            holder.item_im_first.setVisibility(View.VISIBLE);
            holder.item_seq.setVisibility(View.GONE);
        } else {
            holder.item_im_first.setVisibility(View.GONE);
            holder.item_seq.setVisibility(View.VISIBLE);
        }

        item = null;


        mRecycleList.add(new WeakReference<View>(convertView));
        return convertView;
    }


    /** 총 실패 갯수
     *
     * @return
     */
    public int getFailedTotal () {
        int seq_gab = 0;
        int total = 0;
        for (int i = 0; i < mItems.size(); i++) {
            seq_gab = (i > 0) ? mItems.get(i).seq - mItems.get(i - 1).seq : 0;
            mItems.get(i).$failed_count = (i > 0) ? seq_gab - 1 : 0;

            total += mItems.get(i).$failed_count;
        }

        return total;
    }


    /** 체크
     * @param tItem
     */
    public boolean checkSequenceDup (TemperatureTrackingDto tItem) {

        boolean result = false;

        int start = (mItems.size() >= 20) ? mItems.size() - 20 : 0;
        for (int i = start; i < mItems.size(); i++) {
            if (mItems.get(i).seq == tItem.seq) {
                result = true;
                break;
            }
        }

        return result;
    }


    //아이템에 사용될 뷰홀더 객체
    public class ViewHolder {
        public View item_container;
        public TextView item_date, item_temperature, item_seq;
        public ImageView item_dot, item_im_first;
    }

}