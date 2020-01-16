package com.netmania.checklod.tnt.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.netmania.checklod.general.adapter.PhotoCompositinoListAdapter;
import com.netmania.checklod.general.dto.PhotoCompositinoItemDto;
import com.netmania.checklod.tnt.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by hansangcheol on 2018. 3. 13..
 */

public class PhotoCompositionListAdapter extends PhotoCompositinoListAdapter {

    public PhotoCompositionListAdapter(Context context, ArrayList<PhotoCompositinoItemDto> mItems, View.OnClickListener itemClickListener) {
        super(context, mItems, itemClickListener);
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_photo_composition, null);
            holder = new ViewHolder();

            holder.item_container = convertView.findViewById(R.id.item_container);
            holder.item_photo = (ImageView) convertView.findViewById(R.id.item_photo);
            holder.item_label = (TextView) convertView.findViewById(R.id.item_label);
            holder.item_btn_delete = convertView.findViewById(R.id.item_btn_delete);

            convertView.setTag (holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PhotoCompositinoItemDto item = getItem (position);


        holder.item_photo.setImageBitmap(BitmapFactory.decodeFile(item.file.getPath()));
        holder.item_label.setText(item.label);


        holder.item_btn_delete.setTag(position);
        holder.item_btn_delete.setOnClickListener(itemClickListener);

        item = null;


        mRecycleList.add(new WeakReference<View>(convertView));
        return convertView;
    }



    //아이템에 사용될 뷰홀더 객체
    public class ViewHolder extends PhotoCompositinoListAdapter.ViewHolder {
        public View item_container, item_btn_delete;
        public ImageView item_photo;
        public TextView item_label;
    }
}
