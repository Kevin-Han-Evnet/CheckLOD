package com.netmania.checklod.general.http;

/**
 * Created by KevinHan on 2016. 4. 17..
 */

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.netmania.checklod.general.utils.LogUtil;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * http://loopj.com/android-async-http/
 */

public class SendAndReceiveMessage {

    static AsyncHttpClient client = new AsyncHttpClient();

    public SendAndReceiveMessage() {
        //nothing;
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        LogUtil.W ("DATA_CONNECTION", "url : " + url);
        LogUtil.W("DATA_CONNECTION", "http params ===>  " + params.toString());


        client.get(url, params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        LogUtil.W ("DATA_CONNECTION", "url : " + url);
        LogUtil.W("DATA_CONNECTION", "http params ===>  " + params.toString());

        client.post(url, params, responseHandler);

    }

    public static void save(String url, FileAsyncHttpResponseHandler responseHandler) {
        client.get(url, responseHandler);
    }


    public static void post(Context context, String url, StringEntity entity, AsyncHttpResponseHandler responseHandler){
        client.post(context, url, entity, "application/json", responseHandler);
    }

    public static void post(Context context, String url, JSONObject json, AsyncHttpResponseHandler responseHandler){
        StringEntity entity = null;
        try{
            entity = new StringEntity(json.toString(), "utf-8");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        client.post(context, url, entity, "application/json", responseHandler);
    }

    public static void post(Context context, String url, JSONArray json, AsyncHttpResponseHandler responseHandler){
        StringEntity entity = null;
        try{
            entity = new StringEntity(json.toString(), "utf-8");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        client.post(context, url, entity, "application/json", responseHandler);
    }


}
