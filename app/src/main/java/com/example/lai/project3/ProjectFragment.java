package com.example.lai.project3;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static java.sql.Types.NULL;

/**
 * Created by lai on 2016/10/15.
 **/

public class ProjectFragment extends Fragment {
    private View view;
    private TextView textTeacherTitle;
    private TextView textStudentTitle;
    private TextView textIntroTitle;
    private TextView textTeacher;
    private TextView textStudents;
    private TextView textIntroduction;
    private ImageView imgView;
    private ProgressBar progress;
    private LinearLayout panel1;
    private LinearLayout panel2;
    private Toast toast;
    private String id;
    private String name;
    private String origin_rate;
    private JSONObject result;
    private JSONArray result2;
    private JSONArray rateResult;
    private SQLiteManager DB = null;
    private ImageManager Img = new ImageManager();
    private int checkRate = 0;
    private TextView ifRate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_project, container, false);
        findView();
        openDB();

        StrictMode
                .setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()   // or .detectAll() for all detectable problems
                        .penaltyLog()
                        .build());
        StrictMode
                .setVmPolicy(new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .penaltyLog()
                        .penaltyDeath()
                        .build());


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            id = bundle.getString("id");
        }
        Log.i("id", id);
        getData(id);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_project, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                add(String.valueOf(id), name);
                return true;
            case R.id.action_rate:
                checkIfRated();
                if(checkRate == 0)
                    showDialog();
                else{
                    toast = Toast.makeText(getActivity(),
                            "您已經評分過了哦", Toast.LENGTH_LONG);
                    toast.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findView(){
        textTeacherTitle = (TextView)view.findViewById(R.id.teacher_title);
        textStudentTitle = (TextView)view.findViewById(R.id.student_title);
        textIntroTitle = (TextView)view.findViewById(R.id.intro_title);
        textTeacher = (TextView)view.findViewById(R.id.teacher);
        textStudents = (TextView)view.findViewById(R.id.students);
        textIntroduction = (TextView)view.findViewById(R.id.introduction);
        imgView = (ImageView)view.findViewById(R.id.imageView);
        progress = (ProgressBar)view.findViewById(R.id.progress);
        ifRate = (TextView)view.findViewById(R.id.ifRate);
        panel1 = (LinearLayout)view.findViewById(R.id.panel1);
        panel2 = (LinearLayout)view.findViewById(R.id.panel2);
    }

    public void showDialog(){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        final RatingBar rating = new RatingBar(getActivity());
        getRate(id);
        rating.setMax(5);
        rating.setNumStars(5);
        rating.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        /*if don't add this part, stars will not be 5*/
        LinearLayout parent = new LinearLayout(getActivity());
        parent.setGravity(Gravity.CENTER);
        parent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        parent.addView(rating);

        popDialog.setTitle("評分");
        popDialog.setView(parent);
        popDialog.setPositiveButton(android.R.string.ok,
        new DialogInterface.OnClickListener() {
            // Button OK
            public void onClick(DialogInterface dialog, int which) {
                Log.i("rating", String.valueOf(rating.getProgress()));
                double origin = Double.parseDouble(origin_rate);
                if(origin == -1) {
                    Log.i("origin", "-1");
                    updateRate(id, String.valueOf(rating.getProgress()));
                } else{
                    /*calculate the average rate*/
                    double result = (origin + (double)rating.getProgress()) / 2;
                    updateRate(id, String.format("%.1f", result));
                }
                Rated();
                dialog.dismiss();
                toast = Toast.makeText(getActivity(),
                        "已成功評分", Toast.LENGTH_LONG);
                toast.show();
            }
        })
    .setNegativeButton("Cancel",
        new DialogInterface.OnClickListener() {
            // Button Cancel
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        popDialog.create();
        popDialog.show();
    }

    private void updateRate(String proj_id, String proj_rate){
        //Creating a string request
        StringRequest stringRequest = new StringRequest("http://140.116.82.52/updateRate.php?id=" + proj_id + "&rate=" + proj_rate,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        //Creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void getData(String proj_id){
        //Creating a string request
        StringRequest stringRequest = new StringRequest("http://140.116.82.52/findById.php?id=" + proj_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j = null;
                        try {
                            //Parsing the fetched Json String to JSON Object
                            j = new JSONObject(response);
                            Log.i("response", response);

                            //Storing the Array of JSON String to our JSON Array
                            result = j.getJSONObject("exhibition");
                            result2 = j.getJSONArray("student");
                            Log.i("json", result.toString());

                            getStudents(result2);
                            getNames(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //Creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void getNames(JSONObject j){
        try {

            name = j.getString("name");
            Log.i("jobj", name);
            getActivity().setTitle(name);

            textTeacherTitle.setText(R.string.proj_teacher);
            textTeacher.setText(j.getString("teacher"));
/*
            textStudentTitle.setText(R.string.proj_student);
            textStudents.setText(json.getString("students"));*/

            textIntroTitle.setText(R.string.proj_intro);
            textIntroduction.setText(j.getString("introduction"));
/*
                int img = getResources().getIdentifier("com.example.lai.project3:drawable/" + json.getString("img_path"), null, null);
                Img.decodeSampledBitmapFromResource(getResources(), img, 100, 100);
                imgView.setImageBitmap(Img.decodeSampledBitmapFromResource(getResources(), img, 100, 100));*/

            String pic = j.getString("img_path").replace("..", "http://140.116.82.52/iBeaconNavigation");
            Log.i("img", pic);
            imgView.setImageBitmap(getBitmapFromURL(pic));

            progress.setVisibility(View.GONE);
            panel1.setVisibility(View.VISIBLE);
            panel2.setVisibility(View.VISIBLE);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getStudents(JSONArray j){
        String student = "";
        try {
            for (int i = 0; i < j.length(); i++) {
                JSONObject json = j.getJSONObject(i);
                if (j.length() == 1) {
                    student = json.getString("name");
                    break;
                } else {
                    if (i == 0) {
                        student += json.getString("name");
                    } else {
                        student += "、" + json.getString("name");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
            Log.i("std", student);
            textStudentTitle.setText(R.string.proj_student);
            textStudents.setText(student);

    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            Log.e("src",src);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.e("Bitmap","returned");
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception",e.getMessage());
            return null;
        }
    }

    private void getRate(String proj_id){
        //Creating a string request
        StringRequest stringRequest = new StringRequest("http://140.116.82.52/findById.php?id=" + proj_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j = null;
                        try {
                            j = new JSONObject(response);
                            rateResult = j.getJSONArray("result");
                            //Getting json object
                            JSONObject json = rateResult.getJSONObject(0);
                            origin_rate = json.getString("rate");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    private void openDB(){
        DB = new SQLiteManager(getActivity());
    }

    private void closeDB(){
        DB.close();
    }

    private void add(String id, String name){
        Cursor cursor = DB.getInfo(DB.getReadableDatabase());
        int repeat = 0;
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Log.i("proj_name", cursor.getString(2));
                Log.i("proj_id", cursor.getString(1));
            }
            while (cursor.moveToNext());
            cursor.moveToFirst();
            do{
                if(cursor.getString(1).equals(id)){
                    repeat = 1;
                    //利用Toast的靜態函式makeText來建立Toast物件
                    toast = Toast.makeText(getActivity(),
                            "這個專題已經在我的最愛了", Toast.LENGTH_LONG);
                    toast.show();

                    break;
                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        if(repeat == 0) {
            SQLiteDatabase db = DB.getWritableDatabase();
            DB.insert(db, id, name);
            Log.i("add", id);
            Log.i("add", name);
            Log.i("add", "ok");
            //顯示Toast
            toast = Toast.makeText(getActivity(),
                    "成功加入我的最愛", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void checkIfRated() {
        checkRate = Integer.parseInt(ifRate.getText().toString());
        Log.i("check", String.valueOf(checkRate));
    }

    private void Rated(){
        ifRate.setText("1");
    }

}
