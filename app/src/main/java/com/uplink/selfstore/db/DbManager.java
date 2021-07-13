package com.uplink.selfstore.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.uplink.selfstore.model.TripMsgBean;
import com.uplink.selfstore.own.AppContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DbManager {
    static private DbManager dbMgr = new DbManager();
    private DbOpenHelper dbHelper;

    private DbManager(){
        dbHelper = DbOpenHelper.getInstance(AppContext.getInstance().getApplicationContext());
    }
    
    public static synchronized DbManager getInstance(){
        if(dbMgr == null){
            dbMgr = new DbManager();
        }
        return dbMgr;
    }

    synchronized public void closeDB(){
        if(dbHelper != null){
            dbHelper.closeDB();
        }
        dbMgr = null;
    }

    public void  init(){

    }

    public synchronized Integer saveTripMsg(String post_url, String content){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int id = -1;
        if(db.isOpen()){
            ContentValues values = new ContentValues();
            values.put(TripMsgDao.COLUMN_NAME_POST_URL, post_url);
            values.put(TripMsgDao.COLUMN_NAME_CONTENT, content);
            values.put(TripMsgDao.COLUMN_NAME_STATUS, 0);
            db.insert(TripMsgDao.TABLE_NAME, null, values);

            Cursor cursor = db.rawQuery("select last_insert_rowid() from " + TripMsgDao.TABLE_NAME,null);
            if(cursor.moveToFirst()){
                id = cursor.getInt(0);
            }

            cursor.close();
        }
        return id;
    }

    synchronized public List<TripMsgBean> getTripMsgs(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<TripMsgBean> msgs = new ArrayList<>();
        if(db.isOpen()){
            Cursor cursor = db.rawQuery("select * from " + TripMsgDao.TABLE_NAME,null);
            while(cursor.moveToNext()){
                TripMsgBean msg = new TripMsgBean();
                int msgId = cursor.getInt(cursor.getColumnIndex(TripMsgDao.COLUMN_NAME_MSG_ID));
                String content = cursor.getString(cursor.getColumnIndex(TripMsgDao.COLUMN_NAME_CONTENT));
                int status = cursor.getInt(cursor.getColumnIndex(TripMsgDao.COLUMN_NAME_STATUS));
                String postUrl = cursor.getString(cursor.getColumnIndex(TripMsgDao.COLUMN_NAME_POST_URL));

                msg.setMsgId(msgId);
                msg.setContent(content);
                msg.setStatus(status);
                msg.setPostUrl(postUrl);
                msgs.add(msg);
            }
            cursor.close();
        }
        return msgs;
    }

    synchronized public void deleteTripMsg(int id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(db.isOpen()){
            db.delete(TripMsgDao.TABLE_NAME, TripMsgDao.COLUMN_NAME_MSG_ID + " = ?", new String[]{String.valueOf(id)});
        }
    }

}
