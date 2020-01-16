package com.netmania.checklod.general.utils;

import android.content.Context;
import android.util.Log;

import com.netmania.checklod.general.manage.Constants;


public class LogUtil {

	public static boolean IS_LOG = Constants.IS_LOG;
	public static final String TAG = "NETMANIA";

	public static void D(String msg) {
		D(null, msg);
	}

	public static void D(String tag, String msg) {
		if (IS_LOG) {
			if (tag == null) {
				Log.d(TAG, msg);
			} else {
				Log.d(tag, msg);
			}
		}
	}

	public static void I(String msg) {
		I(null, msg);
	}

	public static void I(String tag, String msg) {
		if (IS_LOG) {
			if (tag == null) {
				Log.i(TAG, msg);
			} else {
				Log.i(tag, msg);
			}
		}
	}

	public static void W(String msg) {
		W(null, msg);
	}

	public static void W(String tag, String msg) {
		if (IS_LOG) {
			if (tag == null) {
				Log.w(TAG, msg);
			} else {
				Log.w(tag, msg);
			}
		}
	}
	
	public static void W(String tag, String msg, Throwable e) {
        if (IS_LOG) {
            if (tag == null) {
                Log.w(TAG, msg, e);
            } else {
                Log.w(tag, msg, e);
            }
        }
    }

	public static void E(String msg) {
		E(null, msg);
	}
	
	public static void E(Throwable e) {
        E(null, e.getMessage(), e);
    }

	public static void E(String tag, String msg) {
		if (IS_LOG) {
			if (tag == null) {
				Log.e(TAG, msg);
			} else {
				Log.e(tag, msg);
			}
		}
	}
	
	public static void E(String tag, String msg, Throwable e) {
        if (IS_LOG) {
            if (tag == null) {
                Log.e(TAG, msg, e);
            } else {
                Log.e(tag, msg, e);
            }
        }
    }

	
	/** 염병.. 긴거.
	 * 
	 * @param context
	 * @param message
	 */
	public static void showLog (Context context, String message ) {
		if (IS_LOG) {
	        int maxLogSize = 1000;
	        for(int i = 0; i <= message.length() / maxLogSize; i++) {
	            int start = i * maxLogSize;
	            int end = (i+1) * maxLogSize;
	            end = end > message.length() ? message.length() : end;
	            Log.i("[JAndroidUtil|showLog()]", message.substring(start, end));
	        }
	    }
	}




}
