package com.uplink.selfstore.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static DbOpenHelper instance;

	private static final String TRIPMSG_TABLE_CREATE = "CREATE TABLE "
			+ TripMsgDao.TABLE_NAME + " ("
			+ TripMsgDao.COLUMN_NAME_MSG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
			+ TripMsgDao.COLUMN_NAME_CONTENT + " TEXT , "
			+ TripMsgDao.COLUMN_NAME_POST_URL + " TEXT , "
			+ TripMsgDao.COLUMN_NAME_STATUS + " INTEGER );";


	private DbOpenHelper(Context context) {
		super(context, "selfstore.db", null, DATABASE_VERSION);
	}
	
	public static DbOpenHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DbOpenHelper(context.getApplicationContext());
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TRIPMSG_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


	}
	
	public void closeDB() {
	    if (instance != null) {
	        try {
	            SQLiteDatabase db = instance.getWritableDatabase();
	            db.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        instance = null;
	    }
	}
	
}
