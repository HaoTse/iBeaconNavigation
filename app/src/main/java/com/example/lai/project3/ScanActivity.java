/*
 * Copyright (C) 2013 youten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.lai.project3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.support.v4.content.ContextCompat;
import static java.security.AccessController.getContext;

public class ScanActivity extends Activity implements BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private Handler mHandler;
    private boolean mIsScanning;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    //timer
    Timer tmr;
    //JSON URL
    public static final String DATA_URL = "http://192.168.209.12/beacon_connect/getBeaconLocation.php";

    private WebView mWebViewMap;
    //net.macdidi.webviewtest.JavaScriptInterface mJavaScriptInterface;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 8 seconds.
    private static final long SCAN_PERIOD = 8000;
    private static int count = 0;
    private double[] x_array = new double[21];
    private double[] y_array = new double[21];
    private double[] r = new double[21];
    double x;
    double y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_scan);
        //check SDK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
        init();
        //setContentView(R.layout.activity_locate);
        //fetchDataFromMysqlToSQLite();
        //tmr = new Timer();
        //tmr.schedule(new show_coordinate(),5000,3000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if ((mBTAdapter != null) && (!mBTAdapter.isEnabled())) {
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE_BT);
            //Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
        }
        invalidateOptionsMenu();
        startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopScan();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mIsScanning) {
            menu.findItem(R.id.action_scan).setVisible(false);
            menu.findItem(R.id.action_stop).setVisible(true);
        } else {
            menu.findItem(R.id.action_scan).setEnabled(true);
            menu.findItem(R.id.action_scan).setVisible(true);
            menu.findItem(R.id.action_stop).setVisible(false);
        }
        if ((mBTAdapter == null) || (!mBTAdapter.isEnabled())) {
            menu.findItem(R.id.action_scan).setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }*/

    @SuppressWarnings("unchecked")
    /*@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // ignore
            return true;
        } else if (itemId == R.id.action_scan) {
            startScan();
            return true;
        } else if (itemId == R.id.action_stop) {
            stopScan();
            return true;
        } else if (itemId == R.id.action_clear) {
            if ((mDeviceAdapter != null) && (mDeviceAdapter.getCount() > 0)) {
                mDeviceAdapter.clear();
                mDeviceAdapter.notifyDataSetChanged();
                getActionBar().setSubtitle("");
            }
            return true;
        } else if (itemId == R.id.action_locate) {
            setContentView(R.layout.activity_locate);
            //fetchDataFromMysqlToSQLite();
            test_locate();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }*/

    @Override
    public void onLeScan(final BluetoothDevice newDeivce, final int newRssi,
            final byte[] newScanRecord) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String summary = mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
                if (summary != null) {
                    getActionBar().setSubtitle(summary);
                }
            }
        });
    }

    private void init() {
        // BLE check
        if(!getPackageManager().hasSystemFeature(getPackageManager().FEATURE_BLUETOOTH_LE)){
            Toast.makeText(getBaseContext(),R.string.ble_not_supported,Toast.LENGTH_SHORT).show();
            finish();
        }//利用getPackageManager().hasSystemFeature()檢查手機是否支援BLE設備，否則利用finish()關閉程式。

        // BT check
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBTAdapter.isEnabled())
            mBTAdapter.enable();

        // init listview
        ListView deviceListView = (ListView) findViewById(R.id.list);
        mDeviceAdapter = new DeviceAdapter(this, R.layout.listitem_device,
                new ArrayList<ScannedDevice>());
        deviceListView.setAdapter(mDeviceAdapter);
        mHandler = new Handler();
        fetchDataFromMysqlToSQLite();
        startScan();
    }

    private void fetchDataFromMysqlToSQLite(){
        //Creating a string request
        Log.i("wtf","wtf");
        StringRequest stringRequest = new StringRequest(DATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("wtf","wtf");
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            Log.i("wtf","wtf");
                            Log.i("json",jsonArray.toString());
                            //開啟手機資料庫
                            SQLiteDB mSQLiteDB = new SQLiteDB(ScanActivity.this);
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int name1 = jsonObject.getInt("E0:E5:CF:32:29:EC");
                                int name2 = jsonObject.getInt("F4:B8:5E:B2:E8:27");
                                int name3 = jsonObject.getInt("F4:B8:5E:B2:E8:05");
                                int name4 = jsonObject.getInt("12:3B:6A:1A:7E:0A");
                                int name5 = jsonObject.getInt("12:3B:6A:1A:7D:E7");
                                int name6 = jsonObject.getInt("12:3B:6A:1A:7C:6F");
                                double x_coordinate = jsonObject.getDouble("x");
                                double y_coordinate = jsonObject.getDouble("y");
                                Log.i("ss",String.valueOf(x_coordinate));
                                Log.i("ss",String.valueOf(y_coordinate));
                                mSQLiteDB.insert(name1,name2,name3,name4,name5,name6,x_coordinate,y_coordinate);
                            }
                            mSQLiteDB.close();
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        //Creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Adding request to the queue
        requestQueue.add(stringRequest);

    }

    private void test_locate(){
        stopScan();
        Double[] l = new Double[21];
        HashMap<Double,Integer> hashMap = new HashMap<Double, Integer>();
        SQLiteDB mSQLiteDB = new SQLiteDB(ScanActivity.this);
        SQLiteDatabase db = mSQLiteDB.getReadableDatabase();
        Cursor mCursor;
        int i,j;
        for(i=0;i<21;i++)
            l[i] = 0.0;
        for(i=1;i<=20;i++){
            for(j=0;j<6;j++){
                int rssi = mDeviceAdapter.getItem(j).getRssi();
                String dev = "B";
                String beacon = dev.concat(mDeviceAdapter.getItem(j).getDevice().getAddress().replace(":",""));
                mCursor = db.rawQuery("SELECT" + "`" + beacon + "`" + "FROM detect_beacon WHERE _id=" + Integer.toString(i) ,null);
                Log.i("88",beacon);
                if(mCursor.moveToFirst()){
                    do{
                        l[i] += Math.pow(Math.abs(Integer.parseInt(mCursor.getString(0))-rssi),2);
                    }while (mCursor.moveToNext());
                }
                mCursor.close();
            }
            mCursor = db.rawQuery("SELECT x,y FROM detect_beacon WHERE _id=" + Integer.toString(i),null);
            if(mCursor.moveToFirst()){
                do{
                    x_array[i] = Double.parseDouble(mCursor.getString(0));
                    y_array[i] = Double.parseDouble(mCursor.getString(1));
                }while (mCursor.moveToNext());
            }
            mCursor.close();
            l[i] = Math.sqrt(l[i]);
            Log.i("dis",Double.toString(l[i]));
            hashMap.put(l[i],i);
        }
        Arrays.sort(l, Collections.reverseOrder());
        int x=0,y=0;
        for(int k=0;k<4;k++){
            x += x_array[hashMap.get(l[k])];
            y += y_array[hashMap.get(l[k])];
            Log.i("num",Integer.toString(hashMap.get(l[k])));
        }
        double new_x = x / 4;
        double new_y = y / 4;
        Log.i("newx",Double.toString(new_x));
        Log.i("newy",Double.toString(new_y));
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            count++;
            SQLiteDB mSQLiteDB = new SQLiteDB(ScanActivity.this);
            SQLiteDatabase db = mSQLiteDB.getReadableDatabase();

            Log.i("data",msg.obj.toString());
            Log.i("data.i", String.valueOf(msg.what));
            //取得資料庫的指標
            Cursor mCursor = db.rawQuery("SELECT" + "'" + msg.obj.toString() + "'" + "FROM detect_beacon",null);
            int cc = 0;
            if(mCursor.moveToFirst()){
                do{
                    //Log.i("xx",mCursor.getString(1));

//                    x_array[msg.what] = Double.parseDouble(mCursor.getString(1));
//
//                    y_array[msg.what] = Double.parseDouble(mCursor.getString(2));
//
//                    r[msg.what] = mDeviceAdapter.getItem(msg.what).getIBeacon().getDis();

                    r[cc++] = Math.pow(Math.abs(Integer.parseInt(mCursor.getString(1)) - mDeviceAdapter.getItem(msg.what).getRssi()),2);

                }while (mCursor.moveToNext());
            }
            mCursor.close();

            //計算座標
            if(count==3) {
                double r1 = Math.round(r[0] * 100) / 100.0;
                double r2 = Math.round(r[1] * 100) / 100.0;
                double r3 = Math.round(r[2] * 100) / 100.0;
                x = r3/(r2+r3);
                y = r1*0.1;

                Log.i("newx", Double.toString(x));
                Log.i("newy", Double.toString(y));
                count = 0;
                mWebViewMap = (WebView) findViewById(R.id.wvMap);
                readHtmlFormAssets();

                mWebViewMap.setWebViewClient(new WebViewClient(){
                    public void onPageFinished(WebView view, String url){
                        mWebViewMap.loadUrl("javascript:refreshPoint(" + x*100 + ", " + y*100 + ")");
                    }
                });
            }
        }
    };

    public class  show_coordinate extends TimerTask {
        @Override
        public void run() {
            int i;
            for(i=0;i<6;i++){
                Message message = new Message();
                message.obj = mDeviceAdapter.getItem(i).getDevice().getAddress();
                message.what = i;
                Log.i("dev",message.obj.toString());
                handler.sendMessage(message);
            }
            i = 0;
        }
    };

    private void startScan() {
        if ((mBTAdapter != null) && (!mIsScanning)) {
            // Stops scanning after a pre-defined scan period.
            /*mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsScanning = false;
                    mBTAdapter.stopLeScan(ScanActivity.this);
                    setProgressBarIndeterminateVisibility(false);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);*/
            mBTAdapter.startLeScan(this);
            mIsScanning = true;
            //tmr = new Timer();
            //tmr.schedule(new show_coordinate(),5000,3000);
            setProgressBarIndeterminateVisibility(true);
            invalidateOptionsMenu();

        }
    }

    private void stopScan() {
        if (mBTAdapter != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mBTAdapter.stopLeScan(this);
        }
        mIsScanning = false;
        //tmr.cancel();
        //tmr.purge();
        setProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();

    }

    // 读取SVG文件方法
    private void readHtmlFormAssets() {
        mWebViewMap.setWebChromeClient(new WebChromeClient());
        mWebViewMap.setWebViewClient(new WebViewClient());
        //mJavaScriptInterface = new net.macdidi.webviewtest.JavaScriptInterface(ScanActivity.this);
        //mWebViewMap.addJavascriptInterface(mJavaScriptInterface, "Android");
        mWebViewMap.setHorizontalScrollBarEnabled(false);
        mWebViewMap.setVerticalScrollBarEnabled(false);
        mWebViewMap.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWebViewMap.setBackgroundColor(Color.TRANSPARENT);

        WebSettings websettings = mWebViewMap.getSettings();
        websettings.setJavaScriptEnabled(true);
        websettings.setSupportZoom(false);  // do not remove this
        websettings.setAllowFileAccessFromFileURLs(true); // do not remove this
        websettings.setSupportMultipleWindows(false);
        websettings.setJavaScriptCanOpenWindowsAutomatically(false);
        websettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        websettings.setLoadWithOverviewMode(true);
        websettings.setUseWideViewPort(true);

        String aURL = "file:///android_asset/index.html";
        mWebViewMap.loadUrl(aURL);

    }

}
