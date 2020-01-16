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
import com.example.testforble.dto.SystemLogDto;
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
public class SystemLogListAdapter extends ArrayAdapter<SystemLogDto> {


    private List<SystemLogDto> mItems;
    private Context mContext;
    private LayoutInflater mInflater;
    private View.OnClickListener itemClickListener;
    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();
    private BaseActivity mActivity;
    private long report_term;


    //객체
    private DBHelper tntDBHelper;

    public SystemLogListAdapter(Context context, ArrayList<SystemLogDto> mItems, View.OnClickListener itemClickListener) {
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
    public SystemLogDto getItem (int position) {
        // TODO Auto-generated method stub
        if (position >= mItems.size()) return null;
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public ArrayList<SystemLogDto> getItems () {
        return (ArrayList<SystemLogDto>) mItems;
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_system_log, null);
            holder = new ViewHolder();


            holder.item_container = convertView.findViewById(R.id.item_container);
            holder.item_date = (TextView) convertView.findViewById(R.id.item_date);
            holder.item_dot = (ImageView) convertView.findViewById(R.id.item_dot);
            holder.item_log = (TextView) convertView.findViewById(R.id.item_log);


            convertView.setTag (holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SystemLogDto item = getItem (position);

        String strTime = new SimpleDateFormat(getContext().getResources().getString(R.string.report_timeformat_1), Locale.KOREA).format(new Date(item.timestamp));
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

        holder.item_log.setText(item.action);

        item = null;


        mRecycleList.add(new WeakReference<View>(convertView));
        return convertView;
    }


    //아이템에 사용될 뷰홀더 객체
    public class ViewHolder {
        public View item_container;
        public TextView item_date, item_log;
        public ImageView item_dot;
    }

}