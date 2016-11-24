package com.uscc.ibeacon_navigation.screen;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class CompanyFragment extends Fragment {
    private View view;
    private Spinner spinner;
    private EditText mailText;
    private Button button;
    private JSONArray result;
    private ArrayList<String> mNames;
    private ArrayList<String> mEmails;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_company, container, false);

        mNames = new ArrayList<>();
        mEmails = new ArrayList<>();

        getActivity().setTitle(R.string.company_name);

        findView();
        getData();

        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + mEmails.get(spinner.getSelectedItemPosition())));
                intent.putExtra(Intent.EXTRA_SUBJECT, "關於專題");
                intent.putExtra(Intent.EXTRA_TEXT, mailText.getText().toString());
                try {
                    startActivity(Intent.createChooser(intent, "Send mail..."));
                    getActivity().finish();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return view;
    }

    private void findView(){
        spinner = (Spinner)view.findViewById(R.id.spinner);
        mailText = (EditText)view.findViewById(R.id.mailText);
        button = (Button)view.findViewById(R.id.send_button);
    }

    private void getData(){
        //Creating a string request
        StringRequest stringRequest = new StringRequest("http://140.116.82.52/iBeaconNavigationApp/connectDB.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j;
                        try {
                            //Parsing the fetched Json String to JSON Object
                            j = new JSONObject(response);

                            //Storing the Array of JSON String to our JSON Array
                            result = j.getJSONArray("result");

                            //Calling method getStudents to get the students from the JSON Array
                            getDatas(result);
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

    private void getDatas(JSONArray j){
        //Traversing through all the items in the json array
        for(int i = 0; i < j.length(); i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);

                //Adding the name of the student to array list
                mNames.add(json.getString("name"));
                mEmails.add(json.getString("email"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        spinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mNames));
    }

}
