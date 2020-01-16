package lib.netmania.data.dtos;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Created by hansangcheol on 2017. 4. 12..
 */

public class DataFormatDto extends BaseDto {
    protected String tbl_name;
    protected final String C_ID = "idx";
    protected ArrayList<Scheme> schemes = new ArrayList<>();


    /**
     *
     */
    public DataFormatDto () {
        tbl_name = "tbl_" + getClass().getSimpleName();
        initialize ();
    }


    /**
     *
     */
    public void initialize () {

        for(Field f : getClass().getDeclaredFields()) {
            if (!f.getName().equals("serialVersionUID")
                    && !f.getName().startsWith("$")
                    && (f.getModifiers() & java.lang.reflect.Modifier.FINAL) != Modifier.FINAL
                    && !f.getName().equals(C_ID)) {
                schemes.add(new Scheme(f.getName() , f.getType(), false, null));
            }
        }
    }


    /** 테이블 드럅 쿼리
     *
     * @return
     */
    public String getDropTableQuery () {
        return "DROP TABLE IF EXISTS " + tbl_name + ";";
    }


    /** 데이타 베이스 테이블 크리에이션 쿼리 득
     *
     * @return
     *
     *
     */
    public String getCreateTableQuery () {
        String qry = "CREATE TABLE IF NOT EXISTS " + tbl_name + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT";

                /*+ AuthorizedDeviceItemDto.C_NO + " INTEGER NOT NULL , "
                + AuthorizedDeviceItemDto.C_NAME + " TEXT NOT NULL , "
                + AuthorizedDeviceItemDto.C_STICKER + " TEXT NOT NULL , "
                + AuthorizedDeviceItemDto.C_CREATED_AT + " VARCHAR(255) NOT NULL , "
                + AuthorizedDeviceItemDto.C_MAC + " VARCHAR(255) NOT NULL UNIQUE"*/

        for (int i = 0; i < schemes.size(); i++) {
            qry += ", " + schemes.get(i).name + " " + queryString (schemes.get(i).dataFormat, schemes.get(i).nullable, schemes.get(i).default_value);
        }

        qry += ");";


        return qry;
    }

    /** 데이타 베이스 테이블 크리에이션 쿼리 득
     *
     * @return
     *
     *
     */
    public String getCreateTableQuery (String[] uniq_keys) {
        String qry = "CREATE TABLE IF NOT EXISTS " + tbl_name + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT";

                /*+ AuthorizedDeviceItemDto.C_NO + " INTEGER NOT NULL , "
                + AuthorizedDeviceItemDto.C_NAME + " TEXT NOT NULL , "
                + AuthorizedDeviceItemDto.C_STICKER + " TEXT NOT NULL , "
                + AuthorizedDeviceItemDto.C_CREATED_AT + " VARCHAR(255) NOT NULL , "
                + AuthorizedDeviceItemDto.C_MAC + " VARCHAR(255) NOT NULL UNIQUE"*/

        for (int i = 0; i < schemes.size(); i++) {
            qry += ", " + schemes.get(i).name + " " + queryString (schemes.get(i).dataFormat, schemes.get(i).nullable, schemes.get(i).default_value);
        }

        if (uniq_keys != null && uniq_keys.length > 0) {
            qry += ", CONSTRAINT name_unique UNIQUE (";

            for (int i = 0; i < uniq_keys.length; i++) {
                if (i > 0) qry += ", ";
                qry += uniq_keys[i];
            }

            qry += ")";
        }

        qry += ");";


        return qry;
    }


    /** 데이타 베이스 인서트 쿼리
     *
     * @return
     */
    public ContentValues getInsertAsset () {
        ContentValues insertValues = new ContentValues();

        for(Field f : getClass().getDeclaredFields()) {


            if (f.getName().equals("serialVersionUID")
                    || f.getName().startsWith("$")
                    || (f.getModifiers() & java.lang.reflect.Modifier.FINAL) == Modifier.FINAL
                    || f.getName().equals(C_ID)) {

                continue;
            }


            Object tValue = null;
            try {
                tValue = f.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


            if (tValue instanceof Integer) {
                insertValues.put(f.getName(), (Integer) tValue);
            } else if (tValue instanceof String) {
                insertValues.put(f.getName(), (String) tValue);
            } else if (tValue instanceof Float) {
                insertValues.put(f.getName(), (Float) tValue);
            } else if (tValue instanceof Double) {
                insertValues.put(f.getName(), (Double) tValue);
            } else if (tValue instanceof Long) {
                insertValues.put(f.getName(), (Long) tValue);
            }
        }

        return insertValues;
    }


    /** 일반 셀렉트 문 조합
     *
     * @param pageSize
     * @param pageNum
     * @param where
     * @param orderBy
     * @param limit
     * @return
     */
    public String getListQuery (int pageSize, int pageNum, String where, String orderBy, boolean limit) {
        String whereK = (where == null || where == "") ? "" : " " + where;
        String orderByK = (orderBy == null || orderBy == "") ? "" : " " + orderBy;
        String limitK = limit ? " LIMIT " + pageSize + " OFFSET " + ((pageNum - 1) * pageSize) : "";
        return  "SELECT * FROM " + getTblName() + whereK + orderByK + limitK + ";";
    }


    /** 데이타 타입 쿼리
     *
     * @param cls
     * @return
     */
    private String queryString (Class<?> cls, boolean nullable, String default_value) {
        String result = "";

        //integer
        if (int.class.equals(cls) || Integer.class.equals(cls)) {
            result = "INTEGER";
            result += nullable ? "" : " NOT NULL";
            result += (default_value == null || default_value.equals("")) ? "" : " DEFAULT " + default_value;
        }
        //string
        else if (String.class.equals(cls)) {

            result = "TEXT";
            result += nullable ? "" : " NOT NULL";
            result += (default_value == null || default_value.equals("")) ? "" : " DEFAULT '" + default_value + "';";
        }
        //real
        else if (String.class.equals(cls)
                || double.class.equals(cls)
                || Double.class.equals(cls)
                || float.class.equals(cls)
                || Float.class.equals(cls)
                || Long.class.equals(cls)
                || long.class.equals(cls)) {

            result = "REAL";
            result += nullable ? "" : " NOT NULL";
            result += (default_value == null || default_value.equals("")) ? "" : " DEFAULT " + default_value + ";";
        }
        //blob
        else {
            result = "BLOB";
            result += nullable ? "" : " NOT NULL";
        }

        return result;
    }


    /** 테이블 이름 반환
     *
     * @return
     */
    public String getTblName () {
        return tbl_name;
    }






    /** 데이타 어셋 출출
     *
     * @param cursor
     * @return
     */
    public synchronized DataFormatDto getDataFromCursor (Cursor cursor){
        return new DataFormatDto();
    }

}
