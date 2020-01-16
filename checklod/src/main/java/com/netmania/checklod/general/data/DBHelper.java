package com.netmania.checklod.general.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.dto.AuthorizedDeviceItemDto;
import com.netmania.checklod.general.dto.BeaconItemDto;
import com.netmania.checklod.general.dto.TemperatureTrackingDto;
import com.netmania.checklod.general.manage.DebugTags;
import com.netmania.checklod.general.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DBHelper {
	private final Context mCtx;

	private static final String DATABASE_NAME = "checklod_m.db";
	public static final int DATABASE_VERSION = 30;


	private SQLiteOpenHelper mDbHelper;
	private SQLiteDatabase mDb, mWriteDb;

	public static class DatabaseHelper extends SQLiteOpenHelper {
		private Context mContext;
		private BaseApplication mApp;

		public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
			mApp =(BaseApplication) context.getApplicationContext();
        }
 
        @Override
        public void onCreate(SQLiteDatabase db) {
            LogUtil.I ("Creating Database");
			db.execSQL(TemperatureTrackingDto.getInstance().getCreateTableQuery());
			db.execSQL(AuthorizedDeviceItemDto.getInstance().getCreateTableQuery());
			db.execSQL(BeaconItemDto.getInstance().getCreateTableQuery());
        }
 
        @Override
        public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {

			db.execSQL(TemperatureTrackingDto.getInstance().getDropTableQuery());
			db.execSQL(TemperatureTrackingDto.getInstance().getCreateTableQuery());

			db.execSQL(AuthorizedDeviceItemDto.getInstance().getDropTableQuery());
			db.execSQL(AuthorizedDeviceItemDto.getInstance().getCreateTableQuery());

			db.execSQL(BeaconItemDto.getInstance().getDropTableQuery());
			db.execSQL(BeaconItemDto.getInstance().getCreateTableQuery());

        }
	}
	public static DatabaseHelper newInstance(Context context){
		return new DatabaseHelper(context);
	}
	private static void execSql(SQLiteDatabase db, String query){
		try {
    		db.execSQL(query);
        } catch (SQLException e) {
        	LogUtil.D(query + " --> exec failed...", e.toString ());
        }
	}
	
	public DBHelper(Context ctx) {
		this.mCtx = ctx;
		mDbHelper = new DatabaseHelper(ctx);
	}
	
	public DBHelper open() throws SQLException {
		if(mDbHelper!= null){
			mDb = mDbHelper.getReadableDatabase();
		}
		return this;
	}
	public void startTransaction() {
		BaseApplication globalApplication = BaseApplication.getInstance();
		mWriteDb = globalApplication.getWritableDatabase();
		if (mWriteDb != null){
			mWriteDb.beginTransaction();
		}
	}

	public void endTransaction() {
		if (mWriteDb != null && mWriteDb.inTransaction()) {
			mWriteDb.endTransaction();
		}
	}

	public void commitTransaction() {
		if (mWriteDb != null && mWriteDb.inTransaction()) {
			mWriteDb.setTransactionSuccessful();
		}
	}
	public int delete(String tableName, String whereCase, String[] whereArgs) {

		int result = 0;
		try{
			startTransaction();
			result = mWriteDb.delete(tableName, whereCase, whereArgs);
			commitTransaction();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0;
		}finally{
			endTransaction();
		}
		return result;
		
	}
	public int deleteRow(String tableName, String columnName, long rowId) {
		int result = 0;
		try{
			startTransaction();
			result = mWriteDb.delete(tableName, columnName + "=" + rowId, null);
			commitTransaction();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0;
		}finally{
			endTransaction();
		}
		return result;
		
	}
	public int deleteRow(String tableName, String columnName, String value) {
		int result = 0;
		try{
			startTransaction();
			result = mWriteDb.delete(tableName, columnName + "='" + value + "'", null);
			commitTransaction();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0;
		}finally{
			endTransaction();
		}
		return result;
	}
	public int deleteAll(String tableName) {
		int result = 0;

		try{
			startTransaction();
			result = mWriteDb.delete(tableName, null, null);
			commitTransaction();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return result;
		} finally{
			endTransaction();
		}
		return result;
	}

	public int update(String tableName, String columnName, long rowId, ContentValues updateValues) {
		int result = 0;
		try{
			startTransaction();
			result = mWriteDb.update(tableName, updateValues, columnName + "=" + rowId, null);
			commitTransaction();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return result;
		}finally{
			endTransaction();
		}
		return result;

	}
	public int updateField(String tableName, String columnName, long rowId, String field, String value) {
		int result = 0;
		try{
			ContentValues updateValues = new ContentValues();
			updateValues.put(field, value);
			startTransaction();
			result = mWriteDb.update(tableName, updateValues, columnName + "=" + rowId, null);
			commitTransaction();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return result;
		}finally{
			endTransaction();
		}
		return result;

	}
	public int updateField(String tableName, String columnName, long rowId, ContentValues updateValues) {
		int result = 0;
		try{
			startTransaction();
			result = mWriteDb.update(tableName, updateValues, columnName + "=" + rowId, null);
			commitTransaction();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return result;
		}finally{
			endTransaction();
		}
		return result;

	}
	public List cursor2List(Cursor c){
		List list = new ArrayList();
		LogUtil.I("TEST", c.getCount() + "");
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			do {
				Map map = new HashMap();
				for(int j=0; j<c.getColumnCount(); j++){
					map.put(c.getColumnName(j), c.getString(j));
				}
				list.add(map);
			} while (c.moveToNext());
		}
		return list;
	}
	public Map cursor2Map(Cursor c){
		Map map = null;
		c.moveToFirst();
		if (c != null && c.getCount() > 0) {
			map = new HashMap();
			for(int i=0; i<c.getColumnCount(); i++){
				map.put(c.getColumnName(i), c.getString(i));
			}
		}
		return map;
	}
	public int getCountByTableName(String tableName) {
		int count = 0;
		Cursor cursor = null;
		try{
			open();;
			if(mDb.isOpen()){
				cursor =  mDb.query(tableName, new String[]{
						"count(idx)"
				}, null, null, null, null, null);
			}
			if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return count;
		}finally{
			close(cursor);
		}
		return count;
	}
	public Map<String, String> selectOne(String query) throws SQLException {
		Map map = null;
		Cursor cursor = null;
		try{
			open();;
			if(mDb.isOpen()){
				cursor = mDb.rawQuery(query, null);
			}
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				map = new HashMap();
				for(int i=0; i<cursor.getColumnCount(); i++){
					map.put(cursor.getColumnName(i), cursor.getString(i));
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}finally{
			close(cursor);
		}
		return map;
	}
	public boolean update(String query) throws SQLException {

		try{
			startTransaction();
			mWriteDb.execSQL(query);
			commitTransaction();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}finally{
			endTransaction();
		}
		return true;
	}
	public SQLiteDatabase getDB() {
		return mDb;
	}
	public void close(Cursor cursor){
		try{
			if(cursor != null && cursor.isClosed() == false){
				cursor.close();
				cursor = null;
			}
			if(mDb != null){
				mDb.close();
				mDb = null;
			}
			if(mDbHelper != null){
				mDbHelper.close();
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}




	/**
	 * 워드프레스 따라하기.. 정리해보면 워드프레스는 값 하나 얻는것,
	 * 로우 한줄 얻는것, 로우 리스트를 얻는것 등이 있네. 쿼리는 직접 보내더라..
	 * 뭐 나쁘지 않았어.. 쓰기 편하더군. 따라서 만듭세.
	 *
	 * 안되겠다..ㅎㅎㅎ PHP와는 달리 JAVA는 데이타 타입이 엄격해서.. 쫌만 따라하자..ㅎㅎ
	 *
	 */


	//$wpdb->get_var (); 모방함수 -- String
	public String get_string_var (String strQuery) {
		Cursor c = mWriteDb.rawQuery (strQuery, null);
		String result = "";
		if (c.moveToFirst()) {
			result = c.getString (0);
		}

		c.close ();

		return result;
	}

	//$wpdb->get_var (); 모방함수 -- int --> 이거 동작안함. 나중에 손봐라
	public int get_int_var (String strQuery) {


		int result = 0;

		Cursor cursor = null;
		try{
			open();
			if(mDb.isOpen()){
				cursor =  mDb.rawQuery(strQuery, null);
			}
			if(cursor != null && cursor.getCount() > 0){
				cursor.moveToFirst();
				result = cursor.getInt(0);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0;
		}finally{
			close(cursor);
		}


		return result;
	}


	/** content values 셋으로 등록
	 *
	 * @param data
	 * @return
     */
	public long insertWithAsset (String tblBame, ContentValues data) {
		long result = -1;
		
		try{
			startTransaction();
			result = mWriteDb.insert(tblBame, null, data);
			commitTransaction();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return -1;
		}finally{
			endTransaction();
		}
		return result;
	}


	/** 한꺼번에 넣어보아요
	 *
	 * @param tblBame
	 * @param datas
     */
	public void insertWithAsset (String tblBame, ArrayList<ContentValues> datas) {
		long result = 0;

		try{
			startTransaction();

			for (int i = 0; i < datas.size(); i++) {
				result = mWriteDb.insert(tblBame, null, datas.get(i));
			}

			commitTransaction();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			endTransaction();
		}
	}


	/** 리스트 얻기
	 *
	 * @param query
	 * @return
     */
	public ArrayList<ContentValues> getDataList (String query) {
		ArrayList<ContentValues> datas = new ArrayList<>();
		Cursor cursor = null;
		try{
			open();
			if(mDb.isOpen()){
				cursor =  mDb.rawQuery(query, null);
			}
			if(cursor != null && cursor.getCount() > 0){
				cursor.moveToFirst();
				do {


					ContentValues data = new ContentValues ();

					for (int i = 0; i < cursor.getColumnCount(); i++) {

						switch (cursor.getType(i)) {

							case Cursor.FIELD_TYPE_INTEGER :
								data.put(cursor.getColumnName(i), cursor.getInt(i));
								break;

							case Cursor.FIELD_TYPE_FLOAT :
								data.put(cursor.getColumnName(i), cursor.getLong(i));
								break;

							case Cursor.FIELD_TYPE_STRING :
								data.put(cursor.getColumnName(i), cursor.getString(i));
								break;

							case Cursor.FIELD_TYPE_BLOB :
								data.put(cursor.getColumnName(i), cursor.getBlob(i));
								break;

						}
					}

					datas.add(data);

				} while (cursor.moveToNext());
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}finally{
			close(cursor);
		}
		return datas;
	}
}

