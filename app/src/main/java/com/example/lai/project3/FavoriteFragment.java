package com.example.lai.project3;

import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by lai on 2016/10/15.
 **/

public class FavoriteFragment extends Fragment {
    private View view;
    private SQLiteManager DB = null;
    private ArrayList<String> mNames;
    private ListView mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favorite, container, false);
        mNames = new ArrayList<>();

        findView();
        openDB();
        show();

        return view;
    }

    private void findView(){
        mList = (ListView)view.findViewById(R.id.list_view2);
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

    private void show(){
        Cursor cursor = DB.getInfo(DB.getReadableDatabase());
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                mNames.add(cursor.getString(2));
            }
            mList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mNames));
        }

        //textView.setText(resultData);
    }

}
