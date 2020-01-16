package com.netmania.checklod.general.data;

import android.content.ContentValues;
import android.content.Context;

import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.dto.AuthorizedDeviceItemDto;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.TemperatureTrackingDto;
import com.netmania.checklod.general.manage.Constants;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.LogUtil;
import com.netmania.checklod.general.utils.StringUtils;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import lib.netmania.ble.model.BeaconDataModel;
import lib.netmania.data.utils.DataUtils;

/**
 * Created by hansangcheol on 2017. 5. 31..
 */

public class DataProviderUtil {

    public static DataProviderUtil dataProviderUtil;

    private Context mContext;
    private DBHelper mDBHelper;


    public DataProviderUtil(Context context) {

        mContext = context;
        mDBHelper = new DBHelper(mContext.getApplicationContext());
    }


    /** 싱글톤 고고싱
     *
     * @param context
     * @return
     */
    public static DataProviderUtil getInstance (Context context) {
        if (dataProviderUtil == null) {
            dataProviderUtil = new DataProviderUtil(context);
        }

        return dataProviderUtil;
    }


    /** 등록 장비 리스트 고고
     *
     * @return
     */
    public ArrayList<AuthorizedDeviceItemDto> getAuthorizedBeaconList () {
        String qry = AuthorizedDeviceItemDto.getInstance().getListQuery(0, 0, "", "", false);
        ArrayList<AuthorizedDeviceItemDto> resultList
                = (ArrayList<AuthorizedDeviceItemDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(qry), AuthorizedDeviceItemDto.class);

        if (resultList == null) {
            resultList = new ArrayList<>();
        }

        return resultList;
    }


    /** 등록장비 중 맥으로 찾아 반환
     *
     * @param mac
     * @return
     */
    public AuthorizedDeviceItemDto getAutorizedBeaconWithId (String mac) {
        String qry = AuthorizedDeviceItemDto.getInstance().getListQuery(1, 1, "WHERE id='" + mac + "'", "", true);
        ArrayList<AuthorizedDeviceItemDto> resultList
                = (ArrayList<AuthorizedDeviceItemDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(qry), AuthorizedDeviceItemDto.class);

        AuthorizedDeviceItemDto result = (resultList == null || resultList.size() == 0) ? null : resultList.get(0);

        return result;
    }


    /** 등록장비 중 맥으로 찾아 반환
     *
     * @param stickerNo
     * @return
     */
    public AuthorizedDeviceItemDto getAutorizedBeaconWithAlias (String stickerNo) {
        String qry = AuthorizedDeviceItemDto.getInstance().getListQuery(1, 1, "WHERE alias='" + stickerNo + "'", "", true);
        ArrayList<AuthorizedDeviceItemDto> resultList
                = (ArrayList<AuthorizedDeviceItemDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(qry), AuthorizedDeviceItemDto.class);

        AuthorizedDeviceItemDto result = (resultList == null || resultList.size() == 0) ? null : resultList.get(0);

        return result;
    }


    /** 등록 되어있는 비콘인가??
     *
     * @param mac
     * @return
     */
    public boolean isTrakingData (String mac) {
        String qry = "SELECT COUNT(*) FROM " + BeaconItemDto.getInstance().getTblName() + " WHERE MAC='" + mac + "';";
        ArrayList<ContentValues> resultList = mDBHelper.getDataList(qry);
        int count = (resultList != null) ? (int) resultList.get(0).get("COUNT(*)") : 0;
        return (count > 0);
    }


    /** 동록된 비콘리스트 */
    public ArrayList<BeaconItemDto> getTrackingDeviceList () {
        String qry = BeaconItemDto.getInstance().getListQuery(0, 0, "", "", false);

        ArrayList<BeaconItemDto> resultList = (ArrayList<BeaconItemDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(qry), BeaconItemDto.class);

        if (resultList == null) {
            resultList = new ArrayList<>();
        }

        return resultList;
    }


    /** 등록된 비콘 리스트중 하나
     *
     * @param mac
     * @return
     */
    public BeaconItemDto getTrackingDevice (String mac) {
        BeaconItemDto tItem = new BeaconItemDto();
        String qry = tItem.getListQuery(1, 1, "WHERE MAC='" + mac + "'", "", false);
        //LogUtil.I("qry =====> " + qry);

        ArrayList<BeaconItemDto> resultList
                = (ArrayList<BeaconItemDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(qry), BeaconItemDto.class);

        return (resultList.size() > 0) ? resultList.get(0) : null;
    }


    /** DB에 저장된 맥스 온도
     *
     * @param mac
     * @return
     */
    public double getSavedMaxTemp (String mac) {
        return getSavedMaxTemp (mac, true);
    }
    public double getSavedMaxTemp (String mac, boolean is_probe) {
        double result = 0.0f;

        String column = is_probe ? "temp" : "outside_temp";
        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(1, 1, "WHERE MAC=\'" + mac + "\'", "ORDER BY " + column + " DESC", true);
        ArrayList<TemperatureTrackingDto> tTemperatureItems
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        if (tTemperatureItems != null && tTemperatureItems.size() > 0)  {
            result = is_probe ? Double.valueOf(tTemperatureItems.get(0).temp) : Double.valueOf(tTemperatureItems.get(0).outside_temp);
        }

        return result;
    }

    /** DB에 저장된 마지막 데이타 리턴
     *
     * @param mac
     * @return
     */
    public double getSavedMinTemp (String mac) {
        return getSavedMinTemp (mac, true);
    }
    public double getSavedMinTemp (String mac, boolean is_probe) {
        double result = 0.0f;

        String column = is_probe ? "temp" : "outside_temp";
        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(1, 1, "WHERE MAC=\'" + mac + "\'", "ORDER BY " + column + " ASC", true);
        ArrayList<TemperatureTrackingDto> tTemperatureItems
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        if (tTemperatureItems != null && tTemperatureItems.size() > 0)  {
            result = is_probe ? Double.valueOf(tTemperatureItems.get(0).temp) : Double.valueOf(tTemperatureItems.get(0).outside_temp);
        }

        return result;
    }

    /** DB에 저장된 전송 성공한 데이타 리턴
     *
     * @param mac
     * @return
     */
    public ArrayList<TemperatureTrackingDto> getReportedTemperatures (String mac) {
        return getReportedTemperatures (mac, false, 20, 1, true);
    }
    public ArrayList<TemperatureTrackingDto> getReportedTemperatures (String mac, boolean reverse, int count, int page, boolean limit) {
        String k = (reverse) ? "DESC" : "ASC";
        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(count, page, "WHERE MAC=\'" + mac + "\'", "ORDER BY seq " + k, limit);
        ArrayList<TemperatureTrackingDto> tTemperatureItems
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        if (tTemperatureItems == null) {
            tTemperatureItems = new ArrayList<>();
        }

        return tTemperatureItems;
    }


    /** 미전송 데이타 셋
     *
     * @param mac
     * @return
     */
    public ArrayList<TemperatureTrackingDto> getNotReportedData (String mac) {
        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(0, 0, "WHERE MAC=\'" + mac + "\' and sent=0", "", false);
        ArrayList<TemperatureTrackingDto> tTemperatureItems
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        if (tTemperatureItems == null) {
            tTemperatureItems = new ArrayList<>();
        }

        return tTemperatureItems;
    }

    /** DB에 저장된 전송 실패한 데이타 리턴
     *
     * @param mac
     * @return
     */
    public ArrayList<TemperatureTrackingDto> getNotReportedCount (String mac) {

        String where = StringUtils.isEmpty(mac) ? "WHERE sent=0" : "WHERE MAC=\'" + mac + "\' and sent=0";

        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(0, 0, where, "", false);
        ArrayList<TemperatureTrackingDto> tTemperatureItems
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        return tTemperatureItems;
    }



    /** 키값으로 온도 정보 조회
     *
     * @param idx
     * @return
     */
    public TemperatureTrackingDto getTemperatureDataWithIdx (String idx) {
        TemperatureTrackingDto result = TemperatureTrackingDto.getInstance();

        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(1, 1, "WHERE idx=" + idx, "", false);
        ArrayList<TemperatureTrackingDto> tTemperatureItems
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        if (tTemperatureItems != null && tTemperatureItems.size() > 0)  {
            result = tTemperatureItems.get(0);
        }

        return result;

    }


    /** 키값으로 특정 온도 정보 전송되었음을 업데이트
     *
     * @param idx
     */
    public void updateTemperatureData (String idx) {

        String updateQRY = "UPDATE " + TemperatureTrackingDto.getInstance().getTblName() + " SET sent=1 WHERE idx=" + idx;
        mDBHelper.update(updateQRY);

    }

    /** 키값으로 온도 정보 조회
     *
     * @param mac
     * @param seq
     * @return
     */
    public TemperatureTrackingDto getTemperatureDataWithSeq (String mac, int seq) {
        TemperatureTrackingDto result = TemperatureTrackingDto.getInstance();

        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(1, 1, "WHERE MAC='" + mac + "' and seq='" + seq + "'", "", false);
        ArrayList<TemperatureTrackingDto> tTemperatureItems
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        if (tTemperatureItems != null && tTemperatureItems.size() > 0)  {
            result = tTemperatureItems.get(0);
        }

        return result;

    }

    /** DB에 저장된 마지막 데이타 리턴
     *
     * @param mac
     * @return
     */
    public TemperatureTrackingDto getSavedFinalData (String mac) {

        TemperatureTrackingDto result = TemperatureTrackingDto.getInstance();

        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(1, 1, "WHERE MAC=\'" + mac + "\'", "ORDER BY seq DESC", true);
        ArrayList<TemperatureTrackingDto> tTemperatureItems
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        if (tTemperatureItems != null && tTemperatureItems.size() > 0)  {
            result = tTemperatureItems.get(0);
        } else {
            result.temp = "0.0";
            result.timestamp = 0;
            result.ble_status = BeaconDataModel.BCN_STATUS_OFF;
            result.MAC = mac;
        }

        return result;
    }




    /** DB에 저장된 첫번째 데이타 리턴
     *
     * @param mac
     * @return
     */
    public TemperatureTrackingDto getSavedFirstData (String mac) {

        TemperatureTrackingDto result = TemperatureTrackingDto.getInstance();

        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(1, 1, "WHERE MAC=\'" + mac + "\'", "ORDER BY seq ASC", true);
        ArrayList<TemperatureTrackingDto> tTemperatureItems
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        if (tTemperatureItems != null && tTemperatureItems.size() > 0)  {
            result = tTemperatureItems.get(0);
        } else {
            result.seq = -1;
            result.temp = "0.0";
            result.timestamp = 0;
            result.ble_status = BeaconDataModel.BCN_STATUS_OFF;
            result.MAC = mac;
        }

        return result;
    }


    /** 나의 특정 거시기
     *
     */
    public TemperatureTrackingDto getSavedDataWithSeq (String mac, String seq) {
        TemperatureTrackingDto result = null;

        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(1, 1, "WHERE MAC=\'" + mac + "\' and seq=" + seq, "", true);
        ArrayList<TemperatureTrackingDto> tTemperatureItems
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        if (tTemperatureItems != null && tTemperatureItems.size() > 0)  {
            result = tTemperatureItems.get(0);
        }

        return result;
    }


    /** 바로 전 시퀀스와의 시간차
     *
     */
    public long getRtcGab (TemperatureTrackingDto bcnInfo) {
        TemperatureTrackingDto result = null;

        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(1, 1, "WHERE MAC=\'" + bcnInfo.MAC + "\' and seq<" + bcnInfo.seq, "ORDER BY seq DESC", true);
        ArrayList<TemperatureTrackingDto> tTemperatureItems
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        if (tTemperatureItems != null && tTemperatureItems.size() > 0)  {
            result = tTemperatureItems.get(0);


            try {
                long longRTC = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(bcnInfo.rtc).getTime();
                Date tmpDate = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(result.rtc);
                return longRTC - tmpDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }


    /** 와하하하하
     *
     * @param mac
     * @return
     */
    public long getRtcGab (String mac) {
        String qry = BeaconItemDto.getInstance().getListQuery(1, 1, "WHERE MAC=\'" + mac + "\'", "", true);
        ArrayList<BeaconItemDto> resultList
                = (ArrayList<BeaconItemDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(qry), BeaconItemDto.class);

        if (resultList != null && resultList.size() > 0) {
           return resultList.get(0).ble_signal_cycle;
        }

        return -1;
    }




    /** 나에게 프로브 온도가 있는가??
     *
     * @param mac
     * @return
     */
    public boolean isProbeMode (String mac) {
        String qry = TemperatureTrackingDto.getInstance().getListQuery(1, 1, "WHERE MAC=\'" + mac + "\'", "ORDER BY seq ASC", true);
        ArrayList<TemperatureTrackingDto> resultList
                = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(qry), TemperatureTrackingDto.class);

        if (resultList != null && resultList.size() > 0) {


            if ("0.0".equals(resultList.get(0).hum)) {
                LogUtil.I("뇽뇽 --> " + qry + " --- 프로브 모드가 아닙니다. ---> " + resultList.get(0).hum);
                return false;
            } else {
                LogUtil.I("뇽뇽 --> " + qry + " --- 프로브 모드 입니다. ---> " + resultList.get(0).hum);
                return true;
            }

        }

        LogUtil.I("뇽뇽 --> " + qry + " --- 아직 데이타 없다.");
        return false;
    }




    /** 비밀스런 기능 한번 만들어 보자... 만들어 냈다.. ㅎㄷㄷ; 난 내가 놀라워 ㅎㅎㅎ
     *
     * @param mac --- 맥어드레스
     * @param offset --- n-1, n-2 건너띄고 체크하도록 오프셋 주기
     */
    public void updateLostData (String mac, int offset) {
        updateLostData (mac, offset, true);
    }

    public void updateLostData (String mac, int offset, boolean confirmed) {

        int lastSeq = getSavedFinalData(mac).seq;
        double temp_gab;
        double temp_outside_gab;
        double hum_gab;
        double hum_outside_gab;
        TemperatureTrackingDto fakeData;
        int curretnSeq = -1;
        int tk = 0;

        //처음 저장된 데이타를 토대로 계산식 생성
        TemperatureTrackingDto myFirst = DataProviderUtil.getInstance(BaseApplication.getInstance()).getSavedFirstData(mac);
        Date madeRTC = new Date ();
        try {
            madeRTC = new SimpleDateFormat(Constants.RTC_DATE_FORMAT, Locale.KOREA).parse(myFirst.rtc);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(200, 1, "WHERE MAC=\'" + mac + "\' and seq < " + (lastSeq - offset), "ORDER BY seq DESC", true);
        ArrayList<TemperatureTrackingDto> tTemperatureItems = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        //LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[체크] ---------------------------------------------------");
        for (int i = 0; i < tTemperatureItems.size(); i++) {
            int gab = (i < tTemperatureItems.size() - 2) ? tTemperatureItems.get(i).seq - tTemperatureItems.get(i + 1).seq - 1 : -1;
            if (gab > 0) {

                LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[체크] " + tTemperatureItems.get(i).seq + " 이전에 --> " + gab + "개 없다...");
                temp_gab = (Double.valueOf(tTemperatureItems.get(i + 1).temp) -  Double.valueOf(tTemperatureItems.get(i).temp));
                temp_outside_gab =  (Double.valueOf(tTemperatureItems.get(i + 1).outside_temp) -  Double.valueOf(tTemperatureItems.get(i).outside_temp));

                hum_gab = (Double.valueOf(tTemperatureItems.get(i + 1).hum) -  Double.valueOf(tTemperatureItems.get(i).hum));
                hum_outside_gab = (Double.valueOf(tTemperatureItems.get(i + 1).outside_hum) -  Double.valueOf(tTemperatureItems.get(i).outside_hum));

                LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[체크] 총 온도차이는 --> " + temp_gab);
                LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[체크] 총 습도차이는 --> " + hum_gab);

                /** 일정 시간 이상의 텀이면 채우지 말고 건너뛸것. */
                if (gab > Constants.ADJUSTABLE_GAB) continue;

                fakeData = new TemperatureTrackingDto();
                tk = gab - 1;
                for (int j = 0; j < gab; j++) {

                    curretnSeq = (tTemperatureItems.get(i).seq - gab + j);

                    fakeData.seq = tTemperatureItems.get(i).seq - gab + j;
                    fakeData.MAC = mac;
                    fakeData.temp = String.format("%.1f", Double.valueOf(tTemperatureItems.get(i).temp) + ((temp_gab / (double) gab) * tk));
                    fakeData.outside_temp = String.format("%.1f", Double.valueOf(tTemperatureItems.get(i).outside_temp) + ((temp_outside_gab / (double) gab) * tk));
                    fakeData.hum = String.format("%.1f", Double.valueOf(tTemperatureItems.get(i).hum) + ((hum_gab / (double) gab) * tk));
                    fakeData.outside_hum = String.format("%.1f", Double.valueOf(tTemperatureItems.get(i).outside_hum) + ((hum_outside_gab / (double) gab) * tk));;


                    LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[체크] " + curretnSeq+ " --- " + fakeData.temp);


                    fakeData.rtc = new SimpleDateFormat(Constants.RTC_DATE_FORMAT).format(new Date(madeRTC.getTime() + (getRtcGab (mac) * (Integer.valueOf(curretnSeq) - myFirst.seq))));;
                    fakeData.timestamp = myFirst.timestamp + (getRtcGab (mac)  * (Integer.valueOf(curretnSeq) - myFirst.seq));


                    fakeData.sent = TemperatureTrackingDto.NOT_REPORTED;
                    fakeData.ble_status = BeaconDataModel.BCN_STATUS_RUN;
                    fakeData.is_first = 0;
                    fakeData.is_adjusted = TemperatureTrackingDto.ADJUSTED_DATA;
                    fakeData.$failed_count = 0;
                    if (fakeData.timestamp > 0) mDBHelper.insertWithAsset(TemperatureTrackingDto.getInstance().getTblName(), fakeData.getInsertAsset());

                    tk -= 1;
                }

                break;

            }
        }
        //LLogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[체크] ---------------------------------------------------");

    }

}
