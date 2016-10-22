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
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

public class ScanActivity extends Activity implements BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private Handler mHandler;
    private boolean mIsScanning;

    //timer
    Timer tmr;
    //JSON URL
    public static final String DATA_URL = "http://192.168.1.23/beacon_connect/getBeaconLocation.php";

    private WebView mWebViewMap;
    //net.macdidi.webviewtest.JavaScriptInterface mJavaScriptInterface;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 8 seconds.
    private static final long SCAN_PERIOD = 8000;
    private static int count = 0;
    private double[] x_array = new double[3];
    private double[] y_array = new double[3];
    private double[] r = new double[3];
    double x;
    double y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_scan);

        init();
        //setContentView(R.layout.activity_locate);
        //fetchDataFromMysqlToSQLite();
        //tmr = new Timer();
        //tmr.schedule(new show_coordinate(),5000,3000);
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

    /*@SuppressWarnings("unchecked")
    @Override
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
            fetchDataFromMysqlToSQLite();

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
        startScan();
    }

    private void fetchDataFromMysqlToSQLite(){
        //Creating a string request
        StringRequest stringRequest = new StringRequest(DATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            Log.i("json",jsonArray.toString());
                            //開啟手機資料庫
                            SQLiteDB mSQLiteDB = new SQLiteDB(ScanActivity.this);
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String name = jsonObject.getString("name");
                                String mac_addr = jsonObject.getString("mac_addr");
                                double x_coordinate = jsonObject.getDouble("x");
                                double y_coordinate = jsonObject.getDouble("y");
                                Log.i("ss",String.valueOf(x_coordinate));
                                Log.i("ss",String.valueOf(y_coordinate));
                                mSQLiteDB.insert(name,mac_addr,x_coordinate,y_coordinate);
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

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            count++;
            SQLiteDB mSQLiteDB = new SQLiteDB(ScanActivity.this);
            SQLiteDatabase db = mSQLiteDB.getReadableDatabase();

            Log.i("data",msg.obj.toString());
            Log.i("data.i", String.valueOf(msg.what));
            //取得資料庫的指標
            Cursor mCursor = db.rawQuery("SELECT name,x,y FROM beacon_location WHERE mac_addr=" + "'" + msg.obj.toString() + "'",null);
            if(mCursor.moveToFirst()){
                do{
                    Log.i("xx",mCursor.getString(1));

                    x_array[msg.what] = Double.parseDouble(mCursor.getString(1));

                    y_array[msg.what] = Double.parseDouble(mCursor.getString(2));

                    r[msg.what] = mDeviceAdapter.getItem(msg.what).getIBeacon().getDis();

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
            for(i=0;i<3;i++){
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
