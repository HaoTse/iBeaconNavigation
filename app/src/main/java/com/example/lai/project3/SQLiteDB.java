package com.example.lai.project3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Dennis on 2016/10/6.
 */
public class SQLiteDB extends SQLiteOpenHelper{
    //DB 名稱
    public final static String db_name = "beacon_test";
    //DB 版本
    public final static int db_version = 1;
    public SQLiteDB(Context context)
    {
        super(context, db_name, null, db_version);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //建立db的Table與Table裡的欄位項目
        String TABLE = "CREATE TABLE " + "detect_beacon" + " ("
                + "_id"  + " INTEGER primary key autoincrement, "
                + "BE0E5CF3229EC" + " INT , "
                + "BF4B85EB2E827" + " INT , "
                + "BF4B85EB2E805" + " INT , "
                + "B123B6A1A7E0A" + " INT , "
                + "B123B6A1A7DE7" + " INT , "
                + "B123B6A1A7C6F" + " INT , "
                + "x" + " DOUBLE , "
                + "y" + " DOUBLE "
                + ")";

        db.execSQL(TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }

    //指標，db指向sqldb的Table
    public Cursor select()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("detect_beacon", null, null, null, null, null, null);
        return cursor;
    }

    //新增db Table內容
    public long insert(int name1, int name2, int name3,
                       int name4, int name5, int name6,
                       double x, double y)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("BE0E5CF3229EC", name1);
        cv.put("BF4B85EB2E827", name2);
        cv.put("BF4B85EB2E805", name3);
        cv.put("B123B6A1A7E0A", name4);
        cv.put("B123B6A1A7DE7", name5);
        cv.put("B123B6A1A7C6F", name6);
        Log.i("name3",Integer.toString(name3));
        cv.put("x", x);
        cv.put("y", y);
        long row = db.insert("detect_beacon", null, cv);
        Log.i("cv",cv.toString());
        return row;
    }

    //刪除Table單筆資料，帶入id
    public void delete(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "id" + " = " + Integer.toString(id);
        db.delete("detect_beacon", where, null);
    }

    //刪除Table全部資料
    public void deleteAll()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + " detect_beacon" );
    }
}
