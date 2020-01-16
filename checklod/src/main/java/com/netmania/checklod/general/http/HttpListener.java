package com.netmania.checklod.general.http;



import com.androidquery.callback.AjaxStatus;
import com.netmania.checklod.general.dto.HttpResultDto;

import org.json.JSONObject;


public interface HttpListener {
	
	public void onSuccess(JSONObject json, HttpResultDto result);
	public void onFailed(AjaxStatus status);
	
}
