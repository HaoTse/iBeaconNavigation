package com.uscc.ibeacon_navigation.screen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import com.uscc.ibeacon_navigation.aid.SQLiteManager;
import com.uscc.ibeacon_navigation.algorithm.AStar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NavigationFragment extends Fragment {

    /*
        ideal graph with 80 * 80 for its height and width value

     */

    private View view;
    private AStar star;
    private SQLiteManager DB = null;
    private double[] x_array = new double[21];
    private double[] y_array = new double[21];
    public double x;
    public double y;
    private WebView mWebViewMap;
    private Button navigationButton;
    private WebView myWebview;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_navigation, container, false);
        getActivity().setTitle(R.string.navigation_name);

        findView();
        receivcePosition();
        this.star = new AStar(135, 110);


        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        navigationButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do navigation first
                executeAStar((int)x, (int)y, 100, 100);

//                // if error: alert
//                new AlertDialog.Builder(getActivity())
//                        .setTitle("navigation route")
//                        .setMessage("nothing")
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // continue with delete
//                            }
//                        })
//                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // do nothing
//                            }
//                        })
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
            }
        });


        return view;
    }

    private void findView() {
        this.navigationButton = (Button) view.findViewById(R.id.navigationButton);
        this.myWebview = (WebView)view.findViewById(R.id.navigationWebview);
    }

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
            real_result.put(keyy + 145, valuee + 178);
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


    public void drawSvgStartingCenter(double x, double y) {

    }


    /* private void fetchDataFromMysqlToSQLite(){
        //Creating a string request
        StringRequest stringRequest = new StringRequest(MapFragment.DATA_URL,
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
                                double y_coordinate = jsonObject.getDouble(d"y");
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

/////
    public class locate_task extends TimerTask {
        @Override
        public void run () {
            HashMap<String, Integer> map = new HashMap<>();
//            for (int i = 0; i < 50; i++) {
//                stopScan();
//                for (int j = 0; j < mDeviceAdapter.getCount(); j++) {
//                    int rssi = mDeviceAdapter.getItem(j).getRssi();
//                    String mac_addr = mDeviceAdapter.getItem(j).getDevice().getAddress().replace(":", "");
//                    if (i != 0)
//                        map.put(mac_addr, map.get(mac_addr) + rssi);
//                    else
//                        map.put(mac_addr, rssi);
//                }
//                startScan();
//            }

            Double[] l = new Double[21];
            HashMap<Double, Integer> hashMap = new HashMap<>();

            SQLiteDatabase db = DB.getReadableDatabase();
            Cursor mCursor;

            for (int i = 0; i < 21; i++)
                l[i] = 10000.0;

            //場域內已知點數量
            for (int i = 1; i <= 9; i++) {
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
    }    */

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

}
