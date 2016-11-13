package com.example.lai.project3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by lai on 2016/10/16.
 **/

public class SQLiteManager extends SQLiteOpenHelper {
    private final static int DB_VERSION = 1; // 資料庫版本
    private final static String DB_NAME = "MySQLite.db"; //資料庫名稱，附檔名為db
    private final static String TABLE1 = "favorite_table";
    private final static String TABLE2 = "rated_table";
    private final static String _ID = "_id"; //欄位名稱
    private final static String PROJ_ID = "proj_id"; //專題id
    private final static String NAME = "name"; //欄位名稱
    private final static String IFRATED = "ifRated"; //欄位名稱

    public SQLiteManager (Context context) {

        /*
        *   SQLiteOpenHelper
        * (Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
        * 通常我們只會傳回context, DB_NAME, DB_VERSION;
        * factory 我也不大了解作用是什麼。
        */

        super(context, DB_NAME, null, DB_VERSION);
    }

    // 每次使用將會檢查是否有無資料表，若無，則會建立資料表

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable1 = "CREATE TABLE " + TABLE1 + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PROJ_ID + " INT(11), "
                + NAME + " VARCHAR(50))";
        String createTable2 = "CREATE TABLE " + TABLE2 + " ("
                + IFRATED + " INT(11))";

        db.execSQL(createTable1);
        db.execSQL(createTable2);
        initRate(db);

        Log.i("sqlite", "create success");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE1);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE2);

        onCreate(db);
    }

    public Cursor getInfo(SQLiteDatabase db) {
        return  db.query(TABLE1, new String[] {_ID, PROJ_ID, NAME}, null, null, null, null, null);
    }

    public Cursor ifRated(SQLiteDatabase db) {
        return  db.query(TABLE2, new String[] {IFRATED}, null, null, null, null, null);
    }

    public void insert(SQLiteDatabase db, String id, String name){
        Log.i("insert", id);
        db.execSQL("INSERT INTO favorite_table (proj_id, name) VALUES(" + id + ", '" + name + "')");
    }

    public void delete(SQLiteDatabase db, String id){
        Log.i("delete", id);
        db.execSQL("DELETE FROM favorite_table WHERE proj_id="+id);
    }

    public void initRate(SQLiteDatabase db){
        db.execSQL("INSERT INTO rated_table (ifRated) VALUES (0)");
    }

    public void rated(SQLiteDatabase db){
        ContentValues cv = new ContentValues();
        cv.put("ifRated", "1");
        db.update(TABLE2, cv, "ifRated = 0", null);
        //db.execSQL("Update rated_table (ifRated) VALUES(" + val + ")");
    }

}
