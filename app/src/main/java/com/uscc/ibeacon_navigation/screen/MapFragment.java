package com.uscc.ibeacon_navigation.screen;

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
import com.uscc.ibeacon_navigation.aid.SQLiteManager;
import com.uscc.ibeacon_navigation.algorithm.AStar;
import com.uscc.ibeacon_navigation.ibeacon_detect.DeviceAdapter;
import com.uscc.ibeacon_navigation.ibeacon_detect.ScannedDevice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
class ibeacon{
    String name;
    String mac_addr;
}
class point{
    double known_x;
    double known_y;
    HashMap<String, Integer> rssi = new HashMap<>();
}
public class MapFragment extends Fragment implements BluetoothAdapter.LeScanCallback{

    private View view;
    private AStar star;
    private SQLiteManager DB = null;
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private boolean mIsScanning;
    private Button locate_btn;
    private Button navigationButton;
    private WebView mWebViewMap;

    private Timer tmr;
    private static final long DELAY_TIME = 5000;
    private static final long PERIOD_TIME = 500;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 1;

    //JSON URL
    public static final String DATA_URL = "http://140.116.82.52/iBeaconNavigationApp/getBeaconLocation.php";

    private double[] x_array = new double[46];
    private double[] y_array = new double[46];
    private ArrayList<Integer> detect_point;
    private static double x;
    private static double y;
    private static double previous_x;
    private static double previous_y;
    public static double currentX;
    public static double currentY;

    private ibeacon beacon[] = new ibeacon[16];
    private point fieldPoint[] = new point[85];


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);
        getActivity().setTitle(R.string.map_name);

        findView();
        openDB();

        // navigation
        star = new AStar(135, 110);

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

        navigationButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do navigation first
                executeAStar((int)currentX, (int)currentY, 100, 100);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mIsScanning)
            stopScan();
        if(tmr != null)
            tmr.cancel();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mIsScanning)
            stopScan();
        if(tmr != null)
            tmr.cancel();
        closeDB();
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
        navigationButton = (Button) view.findViewById(R.id.navigationButton);
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

        // init predicted location
        previous_x = 601.61;
        previous_y = 481.30 ;

        //init deteced point ID
        detect_point = new ArrayList<>();

        //init point_info
        for(int i=0;i<16;i++){
            beacon[i] = new ibeacon();
        }
        for(int i=0;i<85;i++){
            fieldPoint[i] = new point();
        }
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

                            for(int i = 0; i < ibeacon_data.length(); i++){
                                JSONObject jsonObject = ibeacon_data.getJSONObject(i);

                                int beacon_id = jsonObject.getInt("beacon_id");
                                String mac_addr = jsonObject.getString("mac_addr");
                                String name = jsonObject.getString("name");
                                double x_coordinate = jsonObject.getDouble("x");
                                double y_coordinate = jsonObject.getDouble("y");
                                DB.insert_ibeacon_data(beacon_id, mac_addr, name, x_coordinate, y_coordinate);

                                beacon[i].mac_addr = mac_addr;
                                beacon[i].name = name;
                            }

                            for(int i = 0; i < point_info_data.length(); i++){
                                JSONObject jsonObject = point_info_data.getJSONObject(i);

                                /*int point_id = jsonObject.getInt("point_id");
                                int beacon_id = jsonObject.getInt("beacon_id");
                                int rssi = jsonObject.getInt("rssi");
                                DB.insert_point_info_data(point_id,beacon_id,rssi);*/

                                String beaconName = jsonObject.getString("mac_addr");
                                int point_rssi = jsonObject.getInt("rssi");

                                fieldPoint[i/10].rssi.put(beaconName,point_rssi);
                            }

                            for(int i = 0; i < detect_point_data.length(); i++){
                                JSONObject jsonObject = detect_point_data.getJSONObject(i);

                                int point_id = jsonObject.getInt("point_id");
                                // 順便存現在有哪些已知點ID
                                detect_point.add(point_id);
                                double x = jsonObject.getDouble("x");
                                double y = jsonObject.getDouble("y");
                                DB.insert_detect_point_data(point_id,x,y);

                                fieldPoint[i].known_x = x;
                                fieldPoint[i].known_y = y;

                            }

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

            Double[] l = new Double[detect_point.size()];
            HashMap<Double, Integer> hashMap = new HashMap<>();

            SQLiteDatabase db = DB.getReadableDatabase();
            Cursor mCursor;

            for (int i = 0; i < detect_point.size(); i++)
                l[i] = 10000.0;

            //場域內已知點數量
            for (int i = 0; i < detect_point.size(); i++) {
                Set<String> keys = map.keySet();// 得到全部的key
                Iterator<String> iter = keys.iterator() ;

                while (iter.hasNext()) {
                    String beacon = iter.next();
                    if(fieldPoint[i].rssi.get(beacon) == null)
                        continue;
                    int rssi = map.get(beacon) / 50;
                    /*mCursor = db.rawQuery("SELECT ibeacon.beacon_id, point_info.rssi FROM ibeacon JOIN point_info ON ibeacon.beacon_id=point_info.beacon_id " +
                            "WHERE ibeacon.mac_addr='" + beacon + "' and point_info.point_id=" + Integer.toString(detect_point.get(i)), null);
                    if (mCursor.moveToFirst()) {
                        do {
                            l[i] += Math.pow(Math.abs(Integer.parseInt(mCursor.getString(1)) - rssi), 2);
                        } while (mCursor.moveToNext());
                    }
                    mCursor.close();*/
                    Log.i("err",beacon);
                    l[i] += Math.pow(Math.abs(fieldPoint[i].rssi.get(beacon) - rssi),2);
                }

                /*mCursor = db.rawQuery("SELECT x,y FROM detect_point WHERE point_id=" + Integer.toString(detect_point.get(i)), null);
                if (mCursor.moveToFirst()) {
                    do {
                        x_array[i] = Double.parseDouble(mCursor.getString(0));
                        y_array[i] = Double.parseDouble(mCursor.getString(1));
                    } while (mCursor.moveToNext());
                }
                mCursor.close();*/

                l[i] = Math.sqrt(l[i]);
                Log.i("dis", Double.toString(l[i]));
                hashMap.put(l[i], i);
            }

            Arrays.sort(l);
            double x = 0, y = 0;
            for (int k = 0; k < 8; k++) {
                x += fieldPoint[hashMap.get(l[k])].known_x;
                y += fieldPoint[hashMap.get(l[k])].known_y;
                Log.i("num", Integer.toString(hashMap.get(l[k])));
            }
            double new_x = x / 8;
            double new_y = y / 8;
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

            currentX = x;
            currentY = y;

            if(Math.abs(x-previous_x) < 30 && Math.abs(y-previous_y) < 30){
                previous_x = x;
                previous_y = y;
            }

            mWebViewMap.loadUrl("javascript:refreshPoint(" + previous_x + ", " + previous_y + ")");
        }
    };


    @Override
    public void onLeScan(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {
        if(mIsScanning) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
                }
            });
        }
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

    // return current x & y;
    public static double getCurrentX() {
        return currentX;
    }

    public static double getCurrentY() {
        return currentY;
    }

    // navigation
    private void receivcePosition() {
        // need to know my current position: call function() ?
        this.x = MapFragment.getCurrentX();
        this.y = MapFragment.getCurrentY();
    }

    private String executeAStar(int startX, int startY, int endX, int endY){
        // graph size 80 * 80
        //block_graph = new int[][]{{4, 1}, {0, 4}, {3, 1}, {2, 2}, {2, 1}, {4, 3}};
        //block_graph = new int[][]{{45, 0}, {45, 1}, {45, 2}, {45, 3}, {45, 4}, {45, 5}, {45, 6}, {45, 7}};
        // grid, starting point, ending point, blocked point
        Map<Integer, Integer> result = AStar.executeAStar(135, 110, startX, startY, endX, endY);
        Map<Integer, Integer> real_result = new HashMap<Integer, Integer>();
        // iterate through and add all points: 145, 178
        Iterator<Map.Entry<Integer, Integer>> iter = result.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Integer> pair = (Map.Entry)iter.next();
            Integer keyy = pair.getKey();
            Integer valuee = pair.getValue();
            real_result.put(keyy*5 + 145, valuee*3 + 178);
            int tmp_x = keyy*5 + 145;
            int tmp_y = valuee*3 + 178;
            mWebViewMap.loadUrl("javascript:disPoint(" + tmp_x + ", " + tmp_y + ")");
        }
        Log.e("original result", printMap(result));
        Log.e("original result", printMap(real_result));

        // trace back the path
        return printMap(real_result);
        //Toast.makeText(this.getContext(), printMap(result), Toast.LENGTH_LONG);
    }

    public String printMap(Map<Integer, Integer> map) {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<Integer, Integer>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Integer> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=').append('"');
            sb.append(entry.getValue());
            sb.append('"');
            if (iter.hasNext()) {
                sb.append(',').append(' ');
            }
        }
        return sb.toString();
    }

}