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
        String TABLE = "CREATE TABLE " + "ibeacon" + " ("
                + "beacon_id"  + " INT, "
                + "mac_addr" + " VARCHAR(21) , "
                + "name" + " VARCHAR(21) , "
                + "x" + " DOUBLE , "
                + "y" + " DOUBLE "
                + ")";

        db.execSQL(TABLE);

        String TABLE2 = "CREATE TABLE " + "detect_point" + " ("
                + "point_id"  + " INT, "
                + "x" + " DOUBLE , "
                + "y" + " DOUBLE "
                + ")";

        db.execSQL(TABLE2);

        String TABLE3 = "CREATE TABLE " + "point_info" + " ("
                + "point_id" + " INT , "
                + "beacon_id" + " INT , "
                + "rssi" + " INT "
                + ")";

        db.execSQL(TABLE3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }

    //新增db Table內容
    public long insert(int beacon_id,String mac_addr, String name,
                       double x, double y)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("beacon_id",beacon_id);
        cv.put("mac_addr", mac_addr);
        cv.put("name", name);
        cv.put("x", x);
        cv.put("y", y);
        long row = db.insert("ibeacon", null, cv);
        Log.i("cv",cv.toString());
        return row;
    }
    public long insert2(int point_id,double x, double y)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("point_id",point_id);
        cv.put("x", x);
        cv.put("y", y);
        long row = db.insert("detect_point", null, cv);
        Log.i("cv",cv.toString());
        return row;
    }
    public long insert3(int point_id, int beacon_id, int rssi)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("point_id", point_id);
        cv.put("beacon_id", beacon_id);
        cv.put("rssi", rssi);
        long row = db.insert("point_info", null, cv);
        Log.i("cv",cv.toString());
        return row;
    }

}
