package com.uscc.ibeacon_navigation.aid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteManager extends SQLiteOpenHelper {

    private final static int DB_VERSION = 1; // 資料庫版本
    private final static String DB_NAME = "MySQLite.db"; //資料庫名稱，附檔名為db

    private final static String FAVORITE_TABLE = "favorite_table";
    private final static String RATED_TABLE = "rated_table";

    private final static String _ID = "_id"; //欄位名稱
    private final static String PROJ_ID = "proj_id"; //專題id
    private final static String NAME = "name"; //欄位名稱
    private final static String IFRATED = "ifRated"; //欄位名稱

    public SQLiteManager (Context context) {

        /*
        *   SQLiteOpenHelper /
        *   (Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
        *   通常我們只會傳回context, DB_NAME, DB_VERSION;
        */

        super(context, DB_NAME, null, DB_VERSION);
    }

    // 每次使用將會檢查是否有無資料表，若無，則會建立資料表
    @Override
    public void onCreate(SQLiteDatabase db) {

        String create_FAVORITE_TABLE = "CREATE TABLE " + FAVORITE_TABLE + " ("
                            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + PROJ_ID + " INT(11), "
                            + NAME + " VARCHAR(50))";
        String create_RATED_TABLE = "CREATE TABLE " + RATED_TABLE + " ("
                            + IFRATED + " INT(11))";

        db.execSQL(create_FAVORITE_TABLE);
        db.execSQL(create_RATED_TABLE);
        initRate(db);

        // create ibeacon table
        String ibeacon_table = "CREATE TABLE " + "ibeacon" + " ("
                            + "beacon_id"  + " INT, "
                            + "mac_addr" + " VARCHAR(21) , "
                            + "name" + " VARCHAR(21) , "
                            + "x" + " DOUBLE , "
                            + "y" + " DOUBLE "
                            + ")";
        db.execSQL(ibeacon_table);

        String detect_point_table = "CREATE TABLE " + "detect_point" + " ("
                            + "point_id"  + " INT, "
                            + "x" + " DOUBLE , "
                            + "y" + " DOUBLE "
                            + ")";
        db.execSQL(detect_point_table);

        String point_info_table = "CREATE TABLE " + "point_info" + " ("
                + "point_id" + " INT , "
                + "beacon_id" + " INT , "
                + "rssi" + " INT "
                + ")";
        db.execSQL(point_info_table);

        Log.i("sqlite", "create success");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + FAVORITE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS" + RATED_TABLE);

        onCreate(db);
    }

    private void initRate(SQLiteDatabase db){
        db.execSQL("INSERT INTO rated_table (ifRated) VALUES (0)");
    }

    public Cursor getInfo(SQLiteDatabase db) {
        return  db.query(FAVORITE_TABLE, new String[] {_ID, PROJ_ID, NAME}, null, null, null, null, null);
    }

    public Cursor ifRated(SQLiteDatabase db) {
        return  db.query(RATED_TABLE, new String[] {IFRATED}, null, null, null, null, null);
    }

    public void insert(SQLiteDatabase db, String id, String name){
        Log.i("insert id", id);
        db.execSQL("INSERT INTO favorite_table (proj_id, name) VALUES(" + id + ", '" + name + "')");
    }

    public void delete(SQLiteDatabase db, String id){
        Log.i("delete id", id);
        db.execSQL("DELETE FROM favorite_table WHERE proj_id="+id);
    }

    public void rated(SQLiteDatabase db){
        ContentValues cv = new ContentValues();
        cv.put("ifRated", "1");
        db.update(RATED_TABLE, cv, "ifRated = 0", null);
    }

    /* 新增 ibeacon Table內容
     *
     * return id
     */
    public long insert_ibeacon_data(int beacon_id, String mac_addr, String name,
                       double x, double y)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("beacon_id",beacon_id);
        cv.put("mac_addr", mac_addr);
        cv.put("name", name);
        cv.put("x", x);
        cv.put("y", y);

        return db.insert("ibeacon", null, cv);
    }

    public long insert_detect_point_data(int point_id, double x, double y)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("point_id",point_id);
        cv.put("x", x);
        cv.put("y", y);

        return db.insert("detect_point", null, cv);
    }

    public long insert_point_info_data(int point_id, int beacon_id, int rssi)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("point_id", point_id);
        cv.put("beacon_id", beacon_id);
        cv.put("rssi", rssi);

        return db.insert("point_info", null, cv);
    }
}
