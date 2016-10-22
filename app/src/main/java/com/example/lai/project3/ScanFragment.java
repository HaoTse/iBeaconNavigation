package com.example.lai.project3;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by Dennis on 2016/10/23.
 **/
public class ScanFragment extends Fragment implements BluetoothAdapter.LeScanCallback{
    private View view;
    private Button locate_btn;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //getActivity().requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        view = inflater.inflate(R.layout.activity_scan, container, false);
        getActivity().setTitle(R.string.navigation_name);

        init();

        return view;
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
