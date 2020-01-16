/*#############################################################
####
####	Develpment Information
####	
####	Developer : Kevin Han
####	
##############################################################*/

/*#############################################################
####
####	BaseFragment -- 바탕 프래그먼트
####	
##############################################################*/
package com.netmania.checklod.general.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.LogUtil;


public class BaseFragment extends Fragment {
	
	public static final String TAG = "BaseFragment";
	protected BaseActivity mParent;
    protected boolean isViewDestroyed;

    protected View mRootView;

    public BaseFragment () {
        super();
        setRetainInstance(true);
        if (getActivity() != null) {
            this.mParent = (BaseActivity) getActivity();
        }
    }

	@Override
	public void onAttach (Activity activity) {
		LogUtil.I(DebugTags.TAG_LIFE_CYCLE, getClass().getSimpleName() + ":onAttach (" + activity.getClass().getSimpleName() + ");");
		mParent = (BaseActivity) activity;
        isViewDestroyed = false;
		super.onAttach (activity);
	}
    
    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
    	LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getClass ().getSimpleName() + ":onActivityCreated (" + String.valueOf(savedInstanceState) + ");");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
    	LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getClass ().getSimpleName() + ":onStart ();" + " --> mParent = " + getActivity ().getClass().getSimpleName());
    	mParent = (BaseActivity) getActivity ();
        super.onStart();
    }

    @Override
    public void onResume() {
    	LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getClass ().getSimpleName() + ":onResume ();");
        super.onResume();
    }
    
    @Override
    public void onPause() {
    	LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getClass ().getSimpleName() + ":onPause ();");
        super.onPause();
    }
    
    
    
    
    @Override
    public void onStop() {
    	LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getClass ().getSimpleName() + ":onStop ();");
        super.onStop();
    }
    
    @Override
    public void onDestroyView() {
    	LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getClass ().getSimpleName() + ":onDestroyView ();");
        isViewDestroyed = true;

        super.onDestroyView();


        if (mRootView != null) unbindDrawables(mRootView);
    }

    private void unbindDrawables(View view){
        if (view.getBackground() != null){
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)){
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++){
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
    
    @Override
    public void onDestroy() {
    	LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getClass ().getSimpleName() + ":onDestroy ();");
        super.onDestroy();

        //Runtime.getRuntime().gc();
    }

    @Override
    public void onDetach() {
    	LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getClass ().getSimpleName() + ":onDetach ();");
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    	LogUtil.I (DebugTags.TAG_LIFE_CYCLE, getClass ().getSimpleName() + ":onSaveInstanceState ();");
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
