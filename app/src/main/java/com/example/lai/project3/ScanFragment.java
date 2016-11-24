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
    private SQLiteManager DB = null;
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private boolean mIsScanning;
    private Button locate_btn;
    private WebView mWebViewMap;

    private Timer tmr;
    private static final long DELAY_TIME = 5000;
    private static final long PERIOD_TIME = 2000;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 1;

    //JSON URL
    public static final String DATA_URL = "http://140.116.82.52/iBeaconNavigationApp/getBeaconLocation.php";

    private double[] x_array = new double[21];
    private double[] y_array = new double[21];
    double x;
    double y;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_scan, container, false);
        getActivity().setTitle(R.string.map_name);

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
                startScan();
                tmr = new Timer();
                tmr.schedule(new locate_task(), DELAY_TIME, PERIOD_TIME);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if ((mBTAdapter != null) && (!mBTAdapter.isEnabled())) {
            //pop dialog and open Bluetooth
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE_BT);
        }

        startScan();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), R.string.permission_deny, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void findView(){
        locate_btn = (Button)view.findViewById(R.id.locate_btn);
        mWebViewMap = (WebView) view.findViewById(R.id.wvMap);
        readHtmlFormAssets();
    }

    private void openDB(){
        DB = new SQLiteManager(getActivity());
    }

    private void closeDB(){
        DB.close();
    }

    private void init() {
        //利用 getPackageManager().hasSystemFeature() 檢查手機是否支援BLE設備，否則利用 finish() 關閉程式。
        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(getActivity().getBaseContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        // BT check
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBTAdapter.isEnabled())
            mBTAdapter.enable();

        // init listview
        ListView deviceListView = (ListView) view.findViewById(R.id.list);
        mDeviceAdapter = new DeviceAdapter(getActivity(), R.layout.listitem_device,
                new ArrayList<ScannedDevice>());
        deviceListView.setAdapter(mDeviceAdapter);
    }

    private void fetchDataFromMysqlToSQLite(){
        //Creating a string request
        StringRequest stringRequest = new StringRequest(DATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray ibeacon_data = json.getJSONArray("ibeacon");
                            JSONArray detect_point_data = json.getJSONArray("detect_point");
                            JSONArray point_info_data = json.getJSONArray("point_info");

                            openDB();

                            for(int i = 0; i < ibeacon_data.length(); i++){
                                JSONObject jsonObject = ibeacon_data.getJSONObject(i);

                                int beacon_id = jsonObject.getInt("beacon_id");
                                String mac_addr = jsonObject.getString("mac_addr");
                                String name = jsonObject.getString("name");
                                double x_coordinate = jsonObject.getDouble("x");
                                double y_coordinate = jsonObject.getDouble("y");
                                DB.insert_ibeacon_data(beacon_id, mac_addr, name, x_coordinate, y_coordinate);
                            }

                            for(int i = 0; i < point_info_data.length(); i++){
                                JSONObject jsonObject = point_info_data.getJSONObject(i);

                                int point_id = jsonObject.getInt("point_id");
                                int beacon_id = jsonObject.getInt("beacon_id");
                                int rssi = jsonObject.getInt("rssi");
                                DB.insert_point_info_data(point_id,beacon_id,rssi);
                            }

                            for(int i = 0; i < detect_point_data.length(); i++){
                                JSONObject jsonObject = detect_point_data.getJSONObject(i);

                                int point_id = jsonObject.getInt("point_id");
                                double x = jsonObject.getDouble("x");
                                double y = jsonObject.getDouble("y");
                                DB.insert_detect_point_data(point_id,x,y);
                            }

                            closeDB();
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

    public class locate_task extends TimerTask {
        @Override
        public void run () {
            HashMap<String, Integer> map = new HashMap<>();
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
            HashMap<Double, Integer> hashMap = new HashMap<>();

            openDB();
            SQLiteDatabase db = DB.getReadableDatabase();
            Cursor mCursor;

            for (int i = 0; i < 21; i++)
                l[i] = 10000.0;

            //場域內已知點數量
            for (int i = 10; i <= 15; i++) {
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
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            String str = msg.obj.toString();
            String[] ss = str.split("@");
            x = Double.parseDouble(ss[0]);
            y = Double.parseDouble(ss[1]);

            mWebViewMap.loadUrl("javascript:refreshPoint(" + x + ", " + y + ")");
        }
    };


    @Override
    public void onLeScan(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
            }
        });
    }

    private void startScan() {
        if ((mBTAdapter != null) && (!mIsScanning)) {
            // Stops scanning after a pre-defined scan period.
            mBTAdapter.startLeScan(this);
            mIsScanning = true;
        }
    }

    public void stopScan() {
        if (mBTAdapter != null) {
            mBTAdapter.stopLeScan(this);
        }
        mIsScanning = false;
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
