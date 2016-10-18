package com.example.lai.project3;

import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by lai on 2016/10/15.
 **/

public class ProjectFragment extends Fragment {
    private View view;
    private TextView textTitle;
    private TextView textStudents;
    private TextView textIntroduction;
    private ImageView imgView;
    private int id;
    private String name;
    private JSONArray result;
    private SQLiteManager DB = null;
    private ImageManager Img = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_project, container, false);
        findView();
        openDB();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            id = bundle.getInt("id") + 1;
        }
        getData(String.valueOf(id));

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

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findView(){
        textTitle = (TextView)view.findViewById(R.id.textView2);
        textStudents = (TextView)view.findViewById(R.id.textView3);
        textIntroduction = (TextView)view.findViewById(R.id.textView4);
        imgView = (ImageView)view.findViewById(R.id.imageView);
    }

    private void getData(String src){
        //Creating a string request
        StringRequest stringRequest = new StringRequest("http://140.116.82.52/findById.php?id=" + src,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j = null;
                        try {
                            //Parsing the fetched Json String to JSON Object
                            j = new JSONObject(response);

                            //Storing the Array of JSON String to our JSON Array
                            result = j.getJSONArray("result");

                            //Calling method getStudents to get the students from the JSON Array
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

    private void getNames(JSONArray j){
        //Traversing through all the items in the json array
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(0);

                name = json.getString("name");
                textTitle.setText(name);
                textIntroduction.setText(json.getString("introduction"));

                textStudents.setText("鄭皓澤、馮禹德");
                int img = getResources().getIdentifier("com.example.lai.project3:drawable/" + json.getString("img_path"), null, null);
                //Img.decodeSampledBitmapFromResource(getResources(), img, 100, 100);
                imgView.setImageBitmap(Img.decodeSampledBitmapFromResource(getResources(), img, 100, 100));
                /*
                listItem.add(json.getString("img_path"));
                */
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        Log.i("show", "111");
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                if(cursor.getString(1).equals(id)){
                    repeat = 1;
                    break;
                }
            }
        }
        if(repeat == 0) {
            SQLiteDatabase db = DB.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("proj_id", id);
            values.put("name", name);
            db.insert("favorite_table", null, values);
        }
    }

}
