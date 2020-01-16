package com.netmania.checklod.general.view;


import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netmania.checklod.general.R;


public class InfoToast implements Runnable, OnClickListener {
    private final Context mContext;

    //UI
    private CharSequence mContent;
    private TextView contentField;
    private int y_offset;
    private int duration;

    public InfoToast(Context mContext, CharSequence mContent){
        this.mContext = mContext;
        this.mContent = mContent;
        this.y_offset = 0;
        this.duration = Toast.LENGTH_SHORT;
    }

    public InfoToast(Context mContext, CharSequence mContent, int y_offset){
        this.mContext = mContext;
        this.mContent = mContent;
        this.y_offset = y_offset;
        this.duration = Toast.LENGTH_SHORT;
    }

    public InfoToast(Context mContext, CharSequence mContent, int duration, int y_offset){
        this.mContext = mContext;
        this.mContent = mContent;
        this.y_offset = y_offset;
        this.duration = duration;
    }

    public void run(){
    	View toastLayout = (RelativeLayout) View.inflate (mContext, R.layout.info_toast, null);
    	
    	
    	contentField = (TextView) toastLayout.findViewById (R.id.INFO_TOAST_content);
    	contentField.setText (mContent);

    	Toast toast = new Toast(mContext);
    	
    	int gravity = Gravity.CENTER_VERTICAL | Gravity.FILL_HORIZONTAL;
    	toast.setGravity (gravity, 0, y_offset);
    	toast.setDuration (duration);
    	toast.setView (toastLayout);
    	toast.show();
    }

	@Override
	public void onClick(View src) {
        int i = src.getId();
        if (i == R.id.INFO_TOAST_content) {//암것도 하지마 에러만 방지.
            //nothing yet;
        }
	}
    
}