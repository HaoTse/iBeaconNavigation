package com.example.lai.project3;

import android.Manifest;
import android.app.Fragment;
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
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;

/**
 * Created by Dennis on 2016/10/23.
 **/

public class ScanFragment extends Fragment implements BluetoothAdapter.LeScanCallback{
    private View view;
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private Handler mHandler;
    private boolean mIsScanning;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    //timer
    Timer tmr;

    //JSON URL
    public static final String DATA_URL = "http://192.168.1.23/beacon_connect/getBeaconLocation.php";

    private WebView mWebViewMap;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //getActivity().requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        view = inflater.inflate(R.layout.activity_scan, container, false);
        getActivity().setTitle(R.string.navigation_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        init();

        return view;
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
    public void onResume() {
        super.onResume();

        if ((mBTAdapter != null) && (!mBTAdapter.isEnabled())) {
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE_BT);
            //Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
        }
        //getActivity().invalidateOptionsMenu();
        startScan();
    }

    @Override
    public void onPause() {
        super.onPause();

        stopScan();
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
                            SQLiteDB mSQLiteDB = new SQLiteDB(getActivity());
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
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        //Adding request to the queue
        requestQueue.add(stringRequest);

    }

    private void test_locate(){
        stopScan();
        Double[] l = new Double[21];
        HashMap<Double,Integer> hashMap = new HashMap<Double, Integer>();
        SQLiteDB mSQLiteDB = new SQLiteDB(getActivity());
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
            SQLiteDB mSQLiteDB = new SQLiteDB(getActivity());
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
                mWebViewMap = (WebView) view.findViewById(R.id.wvMap);
                readHtmlFormAssets();

                mWebViewMap.setWebViewClient(new WebViewClient(){
                    public void onPageFinished(WebView view, String url){
                        mWebViewMap.loadUrl("javascript:refreshPoint(" + x*100 + ", " + y*100 + ")");
                    }
                });
            }
        }
    };


    @Override
    public void onLeScan(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String summary = mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
                /*if (summary != null) {
                    getActivity().getActionBar().setSubtitle(summary);
                }*/
            }
        });
    }

    private void findView(){

    }

    private void init() {
        // BLE check
        if(!getActivity().getPackageManager().hasSystemFeature(getActivity().getPackageManager().FEATURE_BLUETOOTH_LE)){
            Toast.makeText(getActivity().getBaseContext(),R.string.ble_not_supported,Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }//利用getPackageManager().hasSystemFeature()檢查手機是否支援BLE設備，否則利用finish()關閉程式。

        // BT check
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBTAdapter.isEnabled())
            mBTAdapter.enable();

        // init listview
        ListView deviceListView = (ListView) view.findViewById(R.id.list);
        mDeviceAdapter = new DeviceAdapter(getActivity(), R.layout.listitem_device,
                new ArrayList<ScannedDevice>());
        deviceListView.setAdapter(mDeviceAdapter);
        mHandler = new Handler();

        startScan();
    }

    private void startScan() {
        if ((mBTAdapter != null) && (!mIsScanning)) {
            // Stops scanning after a pre-defined scan period.
            mBTAdapter.startLeScan(this);
            mIsScanning = true;
            //tmr = new Timer();
            //tmr.schedule(new show_coordinate(),5000,3000);
            //getActivity().setProgressBarIndeterminateVisibility(true);
            //getActivity().invalidateOptionsMenu();

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
        //getActivity().setProgressBarIndeterminateVisibility(false);
        //getActivity().invalidateOptionsMenu();

    }

    //read svg
    private void readHtmlFormAssets() {
        mWebViewMap.setWebChromeClient(new WebChromeClient());
        mWebViewMap.setWebViewClient(new WebViewClient());
        mWebViewMap.setHorizontalScrollBarEnabled(false);
        mWebViewMap.setVerticalScrollBarEnabled(false);
        mWebViewMap.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWebViewMap.setBackgroundColor(Color.TRANSPARENT);

        WebSettings websettings = mWebViewMap.getSettings();
        websettings.setJavaScriptEnabled(true);
        websettings.setSupportZoom(true);  // do not remove this
        websettings.setBuiltInZoomControls(true);
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
