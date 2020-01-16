package lib.netmania.data.utils;

import android.content.ContentValues;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import lib.netmania.data.dtos.DataFormatDto;

/**
 * Created by hansangcheol on 2017. 5. 24..
 */

public class DataUtils {

    /**
     *
     * @param t
     * @return
     */
    public static ArrayList<?> parseContentValuesToDto (ArrayList<ContentValues> t, Class<?> tDataObj) {

        ArrayList<DataFormatDto> deviceList = new ArrayList<>();
        DataFormatDto tItem = null;

        if (t != null && t.size() > 0) {

            for (int i = 0; i < t.size(); i++) {
                Constructor<?> ctor = tDataObj.getConstructors()[0];
                try {
                    tItem = (DataFormatDto) ctor.newInstance();
                } catch (java.lang.InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

                for (String key : t.get(i).keySet()) {

                    try {
                        tItem.setValue(key, t.get(i).get(key));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                deviceList.add(tItem);
            }
        }

        return deviceList;
    }

}
