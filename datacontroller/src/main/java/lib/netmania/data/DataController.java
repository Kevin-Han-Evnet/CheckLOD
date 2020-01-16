package lib.netmania.data;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import lib.netmania.data.callback.DataControlListener;
import lib.netmania.data.dtos.BaseDto;
import lib.netmania.data.dtos.DataFormatDto;

/**
 * Created by hansangcheol on 2017. 4. 12..
 */

public class DataController {


    private Context mContext;
    private ArrayList<DataFormatDto> dataFormatList;
    private DataControlListener mListener;

    public DataController (Context mContext) {
        this.mContext = mContext;
    }

    public DataController (Context mContext, DataControlListener mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
        this.dataFormatList = new ArrayList<>();
    }


    /** 데이타 이니셜라이징
     *
     * @param tDataFormat
     */
    public void addDataFormat (Class<?> tDataFormat) {

        Constructor<?> ctor = tDataFormat.getConstructors()[0];
        DataFormatDto tmp = null;
        try {
            tmp = (DataFormatDto) ctor.newInstance(); //new Object[]{mContext} --> 파라미터가 있다면. 침고
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (!dataFormatList.contains(tmp)) dataFormatList.add(tmp);

        mListener.onDataFormatAdd(tmp);
    }


    /** 데이타 포맷 삭제
     *
     * @param tDataFormat
     */
    public void removeDataFormat (Class<?> tDataFormat) {

        for (int i = 0; i < dataFormatList.size(); i++) {
            if (tDataFormat.getClass().equals(dataFormatList.get(i).getClass())) {
                mListener.onDataFormatRemove(dataFormatList.get(i));
                dataFormatList.remove(i);
                break;
            }
        }
    }

}
