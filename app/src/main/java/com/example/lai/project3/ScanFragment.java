package com.example.lai.project3;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ScanFragment extends Fragment implements BluetoothAdapter.LeScanCallback{
    private View view;
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private Handler mHandler;
    private boolean mIsScanning;
    private Button locate_btn;
    private WebView mWebViewMap;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    //timer
    Timer tmr;

    //JSON URL
    public static final String DATA_URL = "http://140.116.82.52/getBeaconLocation.php";

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 8 seconds.
    private static final long SCAN_PERIOD = 8000;
    private double[] x_array = new double[21];
    private double[] y_array = new double[21];
    private double[] r = new double[21];
    double x;
    double y;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_scan, container, false);
        getActivity().setTitle(R.string.navigation_name);

        findView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        locate_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                init();
                fetchDataFromMysqlToSQLite();
                tmr = new Timer();
                tmr.schedule(new test_locate(),5000,2000);
            }
        });

        return view;
    }

    private void findView(){
        locate_btn = (Button)view.findViewById(R.id.locate_btn);
        mWebViewMap = (WebView) view.findViewById(R.id.wvMap);
        readHtmlFormAssets();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
        StringRequest stringRequest = new StringRequest(DATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray j = json.getJSONArray("output");
                            JSONArray jj = json.getJSONArray("output2");
                            JSONArray jjj = json.getJSONArray("output3");
                            //開啟手機資料庫
                            SQLiteDB mSQLiteDB = new SQLiteDB(getActivity());
                            for(int i=0;i<j.length();i++){
                                JSONObject jsonObject = j.getJSONObject(i);
                                int beacon_id = jsonObject.getInt("beacon_id");
                                String mac_addr = jsonObject.getString("mac_addr");
                                String name = jsonObject.getString("name");
                                double x_coordinate = jsonObject.getDouble("x");
                                double y_coordinate = jsonObject.getDouble("y");
                                mSQLiteDB.insert(beacon_id,mac_addr,name,x_coordinate,y_coordinate);
                            }
                            for(int i=0;i<jj.length();i++){
                                JSONObject jsonObject = jj.getJSONObject(i);
                                int point_id = jsonObject.getInt("point_id");
                                int beacon_id = jsonObject.getInt("beacon_id");
                                int rssi = jsonObject.getInt("rssi");
                                mSQLiteDB.insert3(point_id,beacon_id,rssi);
                            }
                            for(int i=0;i<jjj.length();i++){
                                JSONObject jsonObject = jjj.getJSONObject(i);
                                int point_id = jsonObject.getInt("point_id");
                                double x = jsonObject.getDouble("x");
                                double y = jsonObject.getDouble("y");
                                mSQLiteDB.insert2(point_id,x,y);
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

    public class test_locate extends TimerTask {
        @Override
        public void run () {
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            for (int i = 0; i < 50; i++) {
                stopScan();
                for (int j = 0; j < mDeviceAdapter.getCount(); j++) {
                    int rssi = mDeviceAdapter.getItem(j).getRssi();
                    String mac_addr = mDeviceAdapter.getItem(j).getDevice().getAddress().replace(":", "");
                    if (i != 0)
                        map.put(mac_addr, map.get(mac_addr) + rssi);
                    else
                        map.put(mac_addr, rssi);
                }
                startScan();
            }
            Double[] l = new Double[21];
            HashMap<Double, Integer> hashMap = new HashMap<Double, Integer>();
            SQLiteDB mSQLiteDB = new SQLiteDB(getActivity());
            SQLiteDatabase db = mSQLiteDB.getReadableDatabase();
            Cursor mCursor;
            int i, j;
            for (i = 0; i < 21; i++)
                l[i] = 10000.0;
            //場域內已知點數量
            for (i = 10; i <= 15; i++) {
                //場域內beacon數量
                int numOfBeacon = map.size();
                Set<String> keys = map.keySet();// 得到全部的key
                Iterator<String> iter = keys.iterator() ;
                while (iter.hasNext()) {
                    String beacon = iter.next();
                    int rssi = map.get(beacon) / 50;
                    mCursor = db.rawQuery("SELECT ibeacon.beacon_id, point_info.rssi FROM ibeacon JOIN point_info ON ibeacon.beacon_id=point_info.beacon_id " +
                            "WHERE ibeacon.mac_addr='" + beacon + "' and point_info.point_id=" + Integer.toString(i), null);
                    if (mCursor.moveToFirst()) {
                        do {
                            l[i] += Math.pow(Math.abs(Integer.parseInt(mCursor.getString(1)) - rssi), 2);
                        } while (mCursor.moveToNext());
                    }
                    mCursor.close();
                }
                mCursor = db.rawQuery("SELECT x,y FROM detect_point WHERE point_id=" + Integer.toString(i), null);
                if (mCursor.moveToFirst()) {
                    do {
                        x_array[i] = Double.parseDouble(mCursor.getString(0));
                        y_array[i] = Double.parseDouble(mCursor.getString(1));
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
                l[i] = Math.sqrt(l[i]);
                Log.i("dis", Double.toString(l[i]));
                hashMap.put(l[i], i);
            }
            Arrays.sort(l);
            double x = 0, y = 0;
            for (int k = 0; k < 4; k++) {
                x += x_array[hashMap.get(l[k])];
                y += y_array[hashMap.get(l[k])];
                Log.i("num", Integer.toString(hashMap.get(l[k])));
            }
            double new_x = x / 4;
            double new_y = y / 4;
            Log.i("newx", Double.toString(new_x));
            Log.i("newy", Double.toString(new_y));
            String coordinate = Double.toString(new_x)+"@"+Double.toString(new_y);
            Message message = new Message();
            message.obj = coordinate;
            handler.sendMessage(message);
        }
    };


    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            String str = msg.obj.toString();
            String[] ss = str.split("@");
            x = Double.parseDouble(ss[0]);
            y = Double.parseDouble(ss[1]);
            Log.i("x?",Double.toString(x));

            mWebViewMap.loadUrl("javascript:refreshPoint(" + x + ", " + y + ")");
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

    public void stopScan() {
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
    @SuppressLint("SetJavaScriptEnabled")
    private void readHtmlFormAssets() {
        mWebViewMap.setWebChromeClient(new WebChromeClient());
        mWebViewMap.setWebViewClient(new WebViewClient());
        mWebViewMap.setHorizontalScrollBarEnabled(false);
        mWebViewMap.setVerticalScrollBarEnabled(false);
        mWebViewMap.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWebViewMap.setBackgroundColor(Color.TRANSPARENT);

        WebSettings websettings = mWebViewMap.getSettings();
        websettings.setJavaScriptEnabled(true);

        // Set Zoom control
        websettings.setSupportZoom(true);
        websettings.setBuiltInZoomControls(true);
        websettings.setDisplayZoomControls(false);

        websettings.setAllowFileAccessFromFileURLs(true);
        websettings.setSupportMultipleWindows(false);
        websettings.setJavaScriptCanOpenWindowsAutomatically(false);
        websettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        websettings.setLoadWithOverviewMode(true);
        websettings.setUseWideViewPort(true);

        String aURL = "file:///android_asset/index.html";
        mWebViewMap.loadUrl(aURL);
    }

}
