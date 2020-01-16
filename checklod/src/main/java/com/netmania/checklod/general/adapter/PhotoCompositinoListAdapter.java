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
import com.netmania.checklod.general.dto.PhotoCompositinoItemDto;
import com.netmania.checklod.general.dto.PhotoCompositinoItemDto;
import com.netmania.checklod.general.utils.RecycleUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import lib.netmania.ble.model.BeaconDataModel;


/**
 * @author Kevin Han
 *
 */
public class PhotoCompositinoListAdapter extends ArrayAdapter<PhotoCompositinoItemDto> {

    public static final int RANGE_BTN_MODE_TEMP = 0;
    public static final int RANGE_BTN_MODE_ORDERNO = 1;


    protected List<PhotoCompositinoItemDto> mItems;
    protected Context mContext;
    protected LayoutInflater mInflater;
    protected View.OnClickListener itemClickListener;
    protected List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();
    protected BaseActivity mActivity;


    //객체
    public PhotoCompositinoListAdapter(Context context, ArrayList<PhotoCompositinoItemDto> mItems, View.OnClickListener itemClickListener) {
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
    public PhotoCompositinoItemDto getItem (int position) {
        // TODO Auto-generated method stub
        return mItems.get(position);
    }


    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public ArrayList<PhotoCompositinoItemDto> getItems () {
        return (ArrayList<PhotoCompositinoItemDto>) mItems;
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent) {
        //밑에서 한다. 하위 클래스에서..
        return convertView;
    }




    //아이템에 사용될 뷰홀더 객체
    public class ViewHolder {
        //notthing yet;
    }

}

