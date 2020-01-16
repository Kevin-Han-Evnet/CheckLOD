package com.netmania.checklod.org.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.netmania.checklod.general.data.AppSharedData;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.LogUtil;

/**
 * Created by hansangcheol on 2017. 11. 13..
 */

public class FbInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        LogUtil.I(DebugTags.TAG_FCM, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }


    /** 신규 토큰 전송 -- 서버가 셋팅 되었다면 말이지..
     *
     * @param fbc_token
     */
    private void sendRegistrationToServer (String fbc_token) {
        LogUtil.I(DebugTags.TAG_FCM, fbc_token);
        AppSharedData.put(
                AppSharedData.DNAME_FMC_PREFER,
                AppSharedData.KEY_FCM_TOKEN,
                fbc_token
        );
    }

}
