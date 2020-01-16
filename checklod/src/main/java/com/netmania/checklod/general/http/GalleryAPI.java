package com.netmania.checklod.general.http;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.dto.APIResultDto;
import com.netmania.checklod.general.dto.ApiContextInfoDto;
import com.netmania.checklod.general.dto.PhotoCompositinoItemDto;
import com.netmania.checklod.general.utils.BitmapUtils;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.JsonUtils;
import com.netmania.checklod.general.utils.LogUtil;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by KevinHan on 2016. 4. 17..
 */
public class GalleryAPI extends BaseAPI {


    /** 파일 업로드
     *
     * @param context
     * @param showProgress
     * @param file
     * @param logerId
     * @param listener
     */
    public static void fileupload (Context context,
                                   boolean showProgress,
                                   File file,
                                   final String logerId,
                                   final ApiMapListenerWithFailedForFiles listener) {


        final ApiContextInfoDto contextDto = getContextInfo (context);



        //파라미터
        RequestParams params = new RequestParams();
        try {
            params.put("loggerId", logerId);
            params.put("phoneNo", "01026658956");
            params.put("invoiceId", "");
            params.put("fileToUpload", GeneralUtils.getFile (file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        addCommonParam(context.getApplicationContext(), params);

        String url = getV2UploadUrl(V2_API_HANDOUT);
        LogUtil.I("업로드 --> " + url);

        SendAndReceiveMessage.post(
                url,
                params,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {

                        super.onProgress(bytesWritten, totalSize);

                        listener.onProgress (bytesWritten, totalSize);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                        HashMap<String, Object> map = JsonUtils.parse(json);

                        //결과값 코드 및 메시지
                        APIResultDto resultDto = new APIResultDto();
                        try {
                            JsonUtils.autoMappingJsonToObject(json.optJSONObject("result"), resultDto);
                        } catch (Exception e) {
                            //nothing;
                        }
                        map.put(RESULT_DTO, resultDto);

                        listener.onComplete(map);

                        sendFuck(logerId, "송정 전송 성공 --> " + statusCode + " : " + json.toString());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                        if (statusCode == REST_SUCCESS) {
                            listener.onComplete(statusCode, headers, throwable, errorResponse);

                            sendFuck(logerId, "송정 전송 성공 --> " + statusCode + " :: " + errorResponse.toString());
                        } else {
                            listener.onFailed(statusCode);

                            sendFuck(logerId, "송정 전송 실패 --> " + statusCode + " :: " + errorResponse.toString());
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                        if (statusCode == REST_SUCCESS) {
                            listener.onComplete(statusCode, headers, throwable);

                            sendFuck(logerId, "송정 전송 성공 --> " + statusCode + " : " + responseString);
                        } else {
                            listener.onFailed(statusCode);

                            sendFuck(logerId, "송정 전송 실패 --> " + statusCode + " : " + responseString);
                        }
                    }

                });

    }


    /** 파일 업로드
     *
     * @param context
     * @param showProgress
     * @param files
     * @param logerId
     * @param listener
     */
    public static void fileupload (Context context,
                                   boolean showProgress,
                                   final ArrayList<PhotoCompositinoItemDto> files,
                                   final String logerId,
                                   final ApiMapListenerWithFailedForFiles listener) {


        final ApiContextInfoDto contextDto = getContextInfo (context);



        //파라미터
        RequestParams params = new RequestParams();
        try {
            params.put("loggerId", logerId);
            params.put("phoneNo", "01026658956");
            params.put("invoiceId", "");

            String types = "";
            for (int i = 0; i < files.size(); i++) {
                params.put("fileToUpload[" + i + "]", GeneralUtils.getFile (files.get(i).file));
                types += (i > 0) ? "," : "";
                types += files.get(i).type;
            }
            params.put("image_types", types);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        addCommonParam(context.getApplicationContext(), params);

        String url = getV2UploadUrl(V2_API_HANDOUT);
        LogUtil.I("업로드 --> " + url);

        SendAndReceiveMessage.post(
                url,
                params,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {

                        super.onProgress(bytesWritten, totalSize);

                        listener.onProgress (bytesWritten, totalSize);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                        HashMap<String, Object> map = JsonUtils.parse(json);

                        //결과값 코드 및 메시지
                        APIResultDto resultDto = new APIResultDto();
                        try {
                            JsonUtils.autoMappingJsonToObject(json.optJSONObject("result"), resultDto);
                        } catch (Exception e) {
                            //nothing;
                        }
                        map.put(RESULT_DTO, resultDto);

                        listener.onComplete(map);
                        GeneralUtils.deleteTempFiles (files);

                        sendFuck(logerId, "송정 전송 성공 --> " + statusCode + " : " + json.toString());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                        if (statusCode == REST_SUCCESS) {
                            listener.onComplete(statusCode, headers, throwable, errorResponse);

                            try {
                                sendFuck(logerId, "송정 전송 성공 --> " + statusCode + " :: " + errorResponse.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            listener.onFailed(statusCode);

                            try {
                                sendFuck(logerId, "송정 전송 실패 --> " + statusCode + " :: " + errorResponse.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        GeneralUtils.deleteTempFiles (files);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                        if (statusCode == REST_SUCCESS) {
                            listener.onComplete(statusCode, headers, throwable);

                            sendFuck(logerId, "송정 전송 성공 --> " + statusCode + " : " + responseString);
                        } else {
                            listener.onFailed(statusCode);

                            sendFuck(logerId, "송정 전송 실패 --> " + statusCode + " : " + responseString);
                        }
                        GeneralUtils.deleteTempFiles (files);
                    }

                });

    }

}
