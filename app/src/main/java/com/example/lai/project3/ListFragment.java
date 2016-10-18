package com.example.lai.project3;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by lai on 2016/10/15.
 **/

public class ListFragment extends Fragment {
    private ListView mList;
    private ToggleButton toggleButton;
    private View view;
    private JSONArray result;
    private ArrayList<String> mNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list, container, false);

        mNames = new ArrayList<>();

        findView();
        getData(0);
        toggleButton.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if(isChecked){
                    mNames.clear();
                    getData(1);
                } else{
                    mNames.clear();
                    getData(0);
                }
            }
        }) ;

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment fragment = new ProjectFragment();
                ft.replace(R.id.layout_fragment, fragment);
                Bundle bundle = new Bundle();
                bundle.putInt("id", position);
                fragment.setArguments(bundle);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return view;
    }

    private void findView(){
        mList = (ListView)view.findViewById(R.id.list_view);
        toggleButton = (ToggleButton)view.findViewById(R.id.togglebutton);
    }

    private void getData(int option){
        String src = "";
        if(option == 0)
            src = "connectDB.php";
        else
            src = "sortByRate.php";
        //Creating a string request
        StringRequest stringRequest = new StringRequest("http://140.116.82.52/" + src,
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
        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);

                //Adding the name of the student to array list
                mNames.add(json.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mNames));
    }
}
