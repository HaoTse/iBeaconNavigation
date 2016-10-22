//package youten.redo.ble.ibeacondetector;
package com.example.lai.project3;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        String TABLE = "CREATE TABLE " + "beacon_location" + " ("
                + "_id"  + " INTEGER primary key autoincrement, "
                + "name" + " VARCHAR(20) , "
                + "mac_addr" + " VARCHAR(21) , "
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
        Cursor cursor = db.query("beacon_location", null, null, null, null, null, null);
        return cursor;
    }

    //新增db Table內容，帶入裝置名稱、mac_addr、x座標、y座標
    public long insert(String name, String mac_addr, double x, double y)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("mac_addr", mac_addr);
        cv.put("x", x);
        cv.put("y", y);
        long row = db.insert("beacon_location", null, cv);
        return row;
    }

    //刪除Table單筆資料，帶入id
    public void delete(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "id" + " = " + Integer.toString(id) ;
        db.delete("beacon_location", where, null);
    }

    //刪除Table全部資料
    public void deleteAll()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + " beacon_location" );
    }
}
