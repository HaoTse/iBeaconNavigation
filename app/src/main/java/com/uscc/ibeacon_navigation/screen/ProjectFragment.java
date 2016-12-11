package com.uscc.ibeacon_navigation.screen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.uscc.ibeacon_navigation.aid.SQLiteManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

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
    private JSONObject rateResult;
    private SQLiteManager DB = null;
    private int checkRate = 0;

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
            getData(id);
        }

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
                add_favorite(String.valueOf(id), name);
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
        popDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            // Button OK
            public void onClick(DialogInterface dialog, int which) {
                double origin = Double.parseDouble(origin_rate);
                if(origin == -1) {
                    updateRate(id, String.valueOf(rating.getProgress()));
                } else{
                    /*calculate the average rate*/
                    double result = (origin + (double)rating.getProgress()) / 2;
                    updateRate(id, String.format(Locale.getDefault(), "%.1f", result));
                }
                Rated();
                dialog.dismiss();
                toast = Toast.makeText(getActivity(),
                        "成功評分", Toast.LENGTH_LONG);
                toast.show();
            }}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
        StringRequest stringRequest = new StringRequest("http://140.116.82.52/iBeaconNavigationApp/updateRate.php?id=" + proj_id + "&rate=" + proj_rate,
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
        StringRequest stringRequest = new StringRequest("http://140.116.82.52/iBeaconNavigationApp/findById.php?id=" + proj_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j;
                        try {
                            j = new JSONObject(response);

                            result = j.getJSONObject("exhibition");
                            result2 = j.getJSONArray("student");

                            getNames(result);
                            getStudents(result2);
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
            getActivity().setTitle(name);

            textTeacherTitle.setText(R.string.proj_teacher);
            textTeacher.setText(j.getString("teacher"));

            textIntroTitle.setText(R.string.proj_intro);
            textIntroduction.setText(j.getString("introduction"));

            String pic = j.getString("img_path").replace("..", "http://140.116.82.52/iBeaconNavigation");
            getBitmapFromURL(pic);

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
                if (i == 0) {
                    student += json.getString("name");
                } else {
                    student += "、" + json.getString("name");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        textStudentTitle.setText(R.string.proj_student);
        textStudents.setText(student);
    }

    public void getBitmapFromURL(String src) {
        ImageRequest imageRequest = new ImageRequest(
                src,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        imgView.setImageBitmap(response);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {}
            });
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(imageRequest);
    }

    private void getRate(String proj_id){
        //Creating a string request
        StringRequest stringRequest = new StringRequest("http://140.116.82.52/iBeaconNavigationApp/findById.php?id=" + proj_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j;
                        try {
                            j = new JSONObject(response);
                            rateResult = j.getJSONObject("exhibition");
                            origin_rate = rateResult.getString("rate");
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

    private void add_favorite(String id, String name){
        Cursor cursor = DB.getInfo(DB.getReadableDatabase());
        int repeat = 0;
        if(cursor.getCount() > 0) {
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
            //顯示Toast
            toast = Toast.makeText(getActivity(),
                    "成功加入我的最愛", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void checkIfRated() {
        Cursor cursor = DB.ifRated(DB.getReadableDatabase());
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            if(cursor.getString(0).equals("0")){
                checkRate = 0;
            } else{
                checkRate = 1;
            }
        }
        cursor.close();
    }

    private void Rated(){
        SQLiteDatabase db = DB.getWritableDatabase();
        DB.rated(db);
    }

}
