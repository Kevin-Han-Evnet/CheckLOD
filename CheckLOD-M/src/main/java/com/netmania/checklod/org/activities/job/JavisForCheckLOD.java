package com.netmania.checklod.org.activities.job;

import android.view.View;
import android.view.ViewGroup;

import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.org.R;
import com.netmania.checklod.org.cui.SpeechRecognizerManager;

import java.util.ArrayList;

/**
 * Created by hansangcheol on 2018. 6. 28..
 */

public class JavisForCheckLOD {

    private static final String TAG = "JAVIS";
    private static JavisForCheckLOD myJavis;
    private BaseActivity mActivity;
    private SpeechRecognizerManager mSpeechManager;
    private ViewGroup mRootView;
    private View mJavisView;

    private boolean javisReady = false;
    private OnJavisCallback mCallback;


    private static final String myNAME = "체크로드";


    public JavisForCheckLOD () {
        //nothign yet;
    }


    //make singleton
    public static JavisForCheckLOD getInstance (BaseActivity mActivity, OnJavisCallback mCallback) {
        if (myJavis == null) myJavis = new JavisForCheckLOD();
        myJavis.mActivity = mActivity;
        myJavis.mCallback = mCallback;
        myJavis.readyToListener ();

        myJavis.mRootView = ((ViewGroup) mActivity.findViewById(android.R.id.content));
        if (myJavis.mJavisView == null) myJavis.mJavisView = mActivity.mInflater.inflate(R.layout.layout_for_javis, null);

        return myJavis;
    }


    /** 자비스 초기화
     *
     */
    private void setSpeechListener() {

        mSpeechManager=new SpeechRecognizerManager(mActivity, mSpeechRecogNaigeListener);
    }


    /** 자비스 대기모드
     *
     */
    public void readyToListener () {
        if(mSpeechManager==null) {
            setSpeechListener();
        } else if(!mSpeechManager.ismIsListening()) {
            mSpeechManager.destroy();
            setSpeechListener();
        }
        LogUtil.I(TAG, "말하숑..");
    }


    /** 액티비티에서 온리쥼 시 불러라
     *
     */
    public void onResume () {
        readyToListener ();
    }


    /** 액티비티 pause
     *
     */
    public void onPause () {
        if(mSpeechManager!=null) {
            mSpeechManager.destroy();
            mSpeechManager=null;
        }
    }


    /** 액티비티 터미네이션시 불러라
     *
     */
    public void onDestroy () {
        if(mSpeechManager != null) {
            mSpeechManager.destroy();
        }
    }


    /** 네 듣고 있어용
     *
     */
    private void javisListening () {
        if (!javisReady) {
            mRootView.addView(mJavisView);
            javisReady = true;
            mCallback.onJavisReady();
        }
    }


    public void releaseJavis () {
        if (javisReady) {
            mRootView.removeView(mJavisView);
            javisReady = false;
        }
    }


    //리스너 ---------------------------------------------------------------------------------------------------------------------------------------
    private SpeechRecognizerManager.onResultsReady mSpeechRecogNaigeListener = new SpeechRecognizerManager.onResultsReady() {
        @Override
        public void onResults(ArrayList<String> results) {

            if(results!=null && results.size()>0) {
                if(results.size()==1) {
                    mSpeechManager.destroy();
                    mSpeechManager = null;
                    LogUtil.I(TAG, results.get(0));



                    if (myNAME.equals(results.get(0))) {
                        javisListening ();
                    }
                    //비콘 등록
                    else if (javisReady && results.get(0).contains("비콘") && results.get(0).contains("등록")) {
                        mCallback.onBeaconAdd();
                    }


                } else {
                    StringBuilder sb = new StringBuilder();


                    if (results.size() > 5) {
                        results = (ArrayList<String>) results.subList(0, 5);
                    }



                    for (String result : results) {
                        sb.append(result).append("\n");

                        if (myNAME.equals(result)) {
                            javisListening ();
                            break;
                        }
                        //비콘 등록
                        else if (javisReady && results.get(0).contains("비콘") && results.get(0).contains("등록")) {
                            mCallback.onBeaconAdd();
                            break;
                        }
                    }
                    LogUtil.I(TAG,sb.toString());
                }

            } else {
                LogUtil.I(TAG, "nothing matchs...");
                releaseJavis ();
            }
        }
    };


    /** 나는 자비스다!!
     *
     */
    public interface OnJavisCallback {
        void onJavisReady ();
        void onBeaconAdd ();
    }
}
