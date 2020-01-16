package com.netmania.checklod.tnt.service;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.netmania.checklod.general.activities.IBaseJobActivity;
import com.netmania.checklod.general.data.DBHelper;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.JsonUtils;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.tnt.BaseApplication;
import com.netmania.checklod.tnt.dto.FcmMessageItemDto;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by hansangcheol on 2017. 11. 13..
 */

public class FbMessagingservice extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        LogUtil.I(DebugTags.TAG_FCM, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            LogUtil.I(DebugTags.TAG_FCM, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            LogUtil.I(DebugTags.TAG_FCM, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            LogUtil.I(DebugTags.TAG_FCM, "data --> " + remoteMessage.getData());

            Map<String, String> params = remoteMessage.getData();
            JSONObject object = new JSONObject(params);
            FcmMessageItemDto fcm = new FcmMessageItemDto();
            try {
                JsonUtils.autoMappingJsonToObject (object, fcm);
            } catch (Exception e) {
                //nothing;
            }

            LogUtil.I(DebugTags.TAG_FCM, "FcmMessageItemDto.cmd = " + fcm.cmd);
            LogUtil.I(DebugTags.TAG_FCM, "FcmMessageItemDto.info = " + fcm.info);

            if (fcm.cmd.equals("takeover")) {
                updateTakeOver (fcm.info);
            } else if (fcm.cmd.equals("wakeup")) {
                BaseApplication.getInstance().restartApplication();
            }

        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }


    /** 정보 업데이트
     *
     * @param mac
     */
    private void updateTakeOver (String mac) {
        String qry = "UPDATE " + BeaconItemDto.getInstance().getTblName() + " SET "
                + "islive=" + BeaconItemDto.INT_FALSE + ", "
                + "delivery_step=" + BeaconItemDto.DELEVERY_STEP_HANDOVER
                + " WHERE MAC='" + mac + "';";
        new DBHelper(BaseApplication.getInstance()).update(qry);

        if (BaseApplication.getInstance().getTaskOnTop() != null
                && BaseApplication.getInstance().getTaskOnTop() instanceof IBaseJobActivity) {
            ((IBaseJobActivity) BaseApplication.getInstance().getTaskOnTop()).doDisableMe(mac);
        }
    }


    /** 유후
     *
     */
    private void scheduleJob () {
        //nothing yet;
    }

    /** 유후
     *
     */
    private void handleNow () {
        //nothing yet;
    }

}
