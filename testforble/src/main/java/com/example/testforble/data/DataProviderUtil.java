package com.example.testforble.data;

import android.content.Context;

import com.example.testforble.BaseApplication;
import com.example.testforble.dto.BeaconItemDto;
import com.example.testforble.dto.SystemLogDto;
import com.example.testforble.dto.TemperatureTrackingDto;
import com.example.testforble.manage.Constants;
import com.example.testforble.manage.DebugTags;
import com.example.testforble.utils.LogUtil;
import com.example.testforble.utils.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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




    /** DB에 저장된 마지막 데이타 리턴
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




    /** 비밀스런 기능 한번 만들어 보자... 만들어 냈다.. ㅎㄷㄷ; 난 내가 놀라워 ㅎㅎㅎ
     *
     * @param mac --- 맥어드레스
     * @param offset --- n-1, n-2 건너띄고 체크하도록 오프셋 주기
     */
    public void updateLostData (String mac, int offset) {
        updateLostData (mac, offset, true);
    }

    public void updateLostData (String mac, int offset, boolean confirmed) {

        LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "보정 --> " + mac);

        int lastSeq = getSavedFinalData(mac).seq;
        double temp_gab;
        double temp_outside_gab;
        TemperatureTrackingDto fakeData;
        int curretnSeq = -1;
        int tk = 0;

        //처음 저장된 데이타를 토대로 계산식 생성
        TemperatureTrackingDto myFirst = DataProviderUtil.getInstance(BaseApplication.getInstance()).getSavedFirstData(mac);

        String getTemperatureItemQry = TemperatureTrackingDto.getInstance().getListQuery(100, 1, "WHERE MAC=\'" + mac + "\' and seq < " + (lastSeq - offset), "ORDER BY seq DESC", true);
        ArrayList<TemperatureTrackingDto> tTemperatureItems = (ArrayList<TemperatureTrackingDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getTemperatureItemQry), TemperatureTrackingDto.class);

        //LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[체크] ---------------------------------------------------> " + tTemperatureItems.size());
        for (int i = 0; i < tTemperatureItems.size() - 1; i++) {
            int gab = tTemperatureItems.get(i).seq - tTemperatureItems.get(i + 1).seq - 1;
            //LogUtil.I(DebugTags.TAG_PROCESS_CHECK,"이런 씹어먹을... --> " + tTemperatureItems.get(i).seq + " - " +tTemperatureItems.get(i + 1).seq + " --> " + gab);
            if (gab > 0) {

                LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[체크] " + tTemperatureItems.get(i).seq + " 이전에 --> " + gab + "개 없다...");
                temp_gab =  (Double.valueOf(tTemperatureItems.get(i + 1).temp) -  Double.valueOf(tTemperatureItems.get(i).temp));
                temp_outside_gab =  (Double.valueOf(tTemperatureItems.get(i + 1).outside_temp) -  Double.valueOf(tTemperatureItems.get(i).outside_temp));

                LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[체크] 총 온도차이는 --> " + temp_gab);

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
                    fakeData.hum = "0";
                    fakeData.outside_hum = "0";


                    LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[체크] " + curretnSeq+ " --- " + fakeData.temp);


                    fakeData.rtc = "";
                    fakeData.timestamp = Calendar.getInstance().getTimeInMillis();


                    fakeData.sent = TemperatureTrackingDto.NOT_REPORTED;
                    fakeData.ble_status = BeaconDataModel.BCN_STATUS_RUN;
                    fakeData.is_first = 0;
                    fakeData.$failed_count = 0;
                    fakeData.is_adjusted = TemperatureTrackingDto.ADJUSTED_DATA;
                    mDBHelper.insertWithAsset(TemperatureTrackingDto.getInstance().getTblName(), fakeData.getInsertAsset());

                    tk -= 1;
                }

                break;

            }
        }
        //LogUtil.I(DebugTags.TAG_PROCESS_CHECK, "[체크] ---------------------------------------------------");

    }


    /** 시스템 로그 얻기
     *
     */
    public ArrayList<SystemLogDto> getSystemLog () {
        String getItemQry = SystemLogDto.getInstance().getListQuery(0, 0, "", "ORDER BY timestamp ASC", false);
        ArrayList<SystemLogDto> tTemperatureItems
                = (ArrayList<SystemLogDto>) DataUtils.parseContentValuesToDto (mDBHelper.getDataList(getItemQry), SystemLogDto.class);

        if (tTemperatureItems == null) {
            tTemperatureItems = new ArrayList<>();
        }

        return tTemperatureItems;
    }

}
