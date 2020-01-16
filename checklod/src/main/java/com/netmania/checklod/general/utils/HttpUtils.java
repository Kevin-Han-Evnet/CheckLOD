package com.netmania.checklod.general.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.R;
import com.netmania.checklod.general.dto.HttpResultDto;
import com.netmania.checklod.general.http.HttpListener;
import com.netmania.checklod.general.manage.DebugTags;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

public class HttpUtils {
	public FrameLayout mProgressContainer;
	private Context mContext;
	private FrameLayout mLayout;
	static public final int PROGRESS_ID = 98314736;
	private HttpListener mHttpObjectListener;

	//타임체크
	private long ts = 0;
	private long lab = 0;


	public ProgressBar mProgressBar;
	/**
	 * 액티비티에서 호출
	 * @param activity
	 */
	public HttpUtils(Activity activity) {
		mContext = activity;
		mLayout = (FrameLayout) ((Activity) mContext).findViewById(android.R.id.content);
		progressAdd();
	}

	/**
	 * 액티비티가 없는 곳에서 호출
	 * @param context
	 */
	public HttpUtils(Context context) {
		mContext = context;
	}

	public void progressHide() {
		try {
			if(mProgressContainer != null) {
				//Util.disableEnableControls(true, mLayout);
				mProgressContainer.setVisibility(View.GONE);
			}
		} catch (Exception e) {}
	}

	public void progressShow() {
		try {
			if(mProgressContainer != null) {
				//Util.disableEnableControls(false, mLayout);
				mProgressContainer.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {}
	}



	private void progressAdd() {
		try {
			if(mLayout.findViewById(PROGRESS_ID) == null) {
				int wrap = FrameLayout.LayoutParams.WRAP_CONTENT;
				int match = FrameLayout.LayoutParams.MATCH_PARENT;

				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(wrap, wrap);
				lp.gravity = Gravity.CENTER;
				mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleLarge);
				mProgressBar.setLayoutParams(lp);

				mProgressContainer = new FrameLayout(mContext);
				FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(match, match);
				mProgressContainer.setLayoutParams(lp2);
				mProgressContainer.setClickable(true);
				mProgressContainer.setVisibility(View.GONE);
				mProgressContainer.setId(PROGRESS_ID);
				mProgressContainer.setBackgroundColor (Color.parseColor ("#88000000"));

				mLayout.addView(mProgressContainer);
				mProgressContainer.addView(mProgressBar);

			} else {
				mProgressContainer = (FrameLayout) mLayout.findViewById(PROGRESS_ID);
			}
		} catch (Exception e) {}
	}



	@SuppressWarnings("static-access")
	public void httpExecute (String url, final Map<String, String> headers, final Map<String, Object> params, final HttpListener httpObjectListener, final boolean isProgress) {
		httpExecute (url, headers, params, httpObjectListener, isProgress, 10000);
	}

	public void httpExecute (String url, final Map<String, String> headers, final Map<String, Object> params, final HttpListener httpObjectListener, final boolean isProgress, int time_out) {
		mHttpObjectListener = httpObjectListener;
		LogUtil.I(DebugTags.TAG_HTTP, "url:"+url);

		//시작
		ts = Calendar.getInstance().getTimeInMillis();
		lab = 0;

		//헤더 점검
		if(headers != null) {

			Set<String> keys = headers.keySet();
			for(String key : keys) {
				LogUtil.I(DebugTags.TAG_HTTP, "http headers ===>  " + key + " : " + headers.get(key));
			}
		}

		//파라미터 점검
		if(params != null) {

			Set<String> keys = params.keySet();
			for(String key : keys) {
				LogUtil.I(DebugTags.TAG_HTTP, "http params ===>  " + key + " : " + params.get(key));
			}
		}

		//캐시 방지용 타임코드 삽입
		params.put ("issue", GeneralUtils.getTimeCode ());

		if(isProgress) progressShow();
		AQuery aq = new AQuery(mContext);


		//AjaxCallback 선언하고 파라미터를 헤더로 붙이기.
		AjaxCallback<JSONObject> acb = new AjaxCallback<JSONObject>() {
			@SuppressWarnings("unused")
			@Override
			public void callback(final String url, JSONObject json, AjaxStatus status) {


				lab = Calendar.getInstance().getTimeInMillis() - ts;

				LogUtil.W (DebugTags.TAG_HTTP, "[DATA] status : "+ status.getCode() +", " + status.getError());
				LogUtil.W (DebugTags.TAG_HTTP, "[DATA] header.status_code : "+ status.getHeader ("status_code"));
				LogUtil.W (DebugTags.TAG_HTTP, "[DATA] header.auth_access_token : "+ status.getHeader ("auth_access_token"));
				LogUtil.W (DebugTags.TAG_HTTP, "[DATA] header.auth_signature_key : "+ status.getHeader ("auth_signature_key"));
				LogUtil.W (DebugTags.TAG_HTTP, "[DATA] time lab = " + (lab / 1000.00) + "초");

				if (status.getCode() != 200 && status.getCode() != AjaxStatus.TRANSFORM_ERROR) {
					try {
						if(isProgress) progressHide();
						mHttpObjectListener.onFailed (status);
					} catch (Exception e) {}
					return;
				}



				if(isProgress) progressHide();

				if(json == null) {
					HttpResultDto result = new HttpResultDto();
					result.isSuccess = false;
					result.status_code = status.getHeader ("status_code");
					result.auth_access_token = status.getHeader ("auth_access_token");
					result.auth_signature_key = status.getHeader ("auth_signature_key");
					if(mLayout != null) {
						String msg = "";
						int code = status.getCode();
						switch (code) {
							case AjaxStatus.NETWORK_ERROR:
								msg = "NETWORK_ERROR";
								break;
							case AjaxStatus.AUTH_ERROR:
								msg = "AUTH_ERROR";
								break;
							case AjaxStatus.TRANSFORM_ERROR:

								HttpResultDto resultK = new HttpResultDto();
								resultK.isSuccess = true;
								resultK.status_code = "200";
								resultK.auth_access_token = status.getHeader ("auth_access_token");
								resultK.auth_signature_key = status.getHeader ("auth_signature_key");
								try {
									mHttpObjectListener.onSuccess(new JSONObject(), resultK);
								} catch (Exception e) {
									LogUtil.E(e.toString());
									mHttpObjectListener.onFailed (status);
								}
								return;
							default:
								msg = "UNKNOWN_ERROR";
						}


						if (msg.equals("NETWORK_ERROR")) {
							try {

								DialogUtils.alert (mContext, "네트워크 상황이 좋지 않습니다. 확인후 재실행 해 주세요.", new DialogInterface.OnClickListener () {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										BaseApplication cap = (BaseApplication) mContext.getApplicationContext();
										cap.finishAll(); //어플리케이션 종료
									}

								}, false);

							} catch (Exception e) {

							}
						} else {
							try {
								DialogUtils.confirm (mContext, "["+msg+"]\n"+mContext.getString(R.string.confirm_networkfail_retry), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										httpExecute (url, headers, params, mHttpObjectListener, isProgress);
									}
								}, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										BaseApplication cap = (BaseApplication) mContext.getApplicationContext();
										cap.finishAll(); //어플리케이션 종료
									}
								}, false);
							} catch (Exception e) {

							}
						}

					} else {


						try {
							if(isProgress) progressHide();
							mHttpObjectListener.onSuccess(null, result);
						} catch (Exception e) {
							if(isProgress) progressHide();
							mHttpObjectListener.onFailed (status);
						}
					}

				} else {

					LogUtil.W (DebugTags.TAG_HTTP, "body"+ json.toString());

					HttpResultDto result = new HttpResultDto();
					result.isSuccess = true;
					result.status_code = status.getHeader ("status_code");
					result.auth_access_token = status.getHeader ("auth_access_token");
					result.auth_signature_key = status.getHeader ("auth_signature_key");
					try {
						mHttpObjectListener.onSuccess(json, result);
					} catch (Exception e) {
						LogUtil.E(e.toString());
						mHttpObjectListener.onFailed (status);
					}
				}

			}



		};


		acb.params (params);
		acb.headers (headers);
		acb.timeout (time_out);

		aq.ajax (url, params, JSONObject.class, acb);
	}


	private void stringResult(String url, Map<String, Object> params) {
		// TODO Auto-generated method stub
		AQuery aq = new AQuery(mContext);
		aq.ajax(url, params, String.class, new AjaxCallback<String>() {
			@Override
			public void callback(final String url, String json, AjaxStatus status) {
				LogUtil.E("json string:"+ json.toString());
			}
		});
	}


}
