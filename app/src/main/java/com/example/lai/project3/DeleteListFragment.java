package com.example.lai.project3;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lai on 2016/10/15.
 **/

public class DeleteListFragment extends Fragment {
    private View view;
    private Button delete_btn;
    private Button cancel_btn;
    private SQLiteManager DB = null;
    private ArrayAdapter myAdapter;
    private ArrayList<String> mNames;
    private ArrayList<String> mIds;
    private ArrayList<String> checkedIds;
    private ListView mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_delete, container, false);

        getActivity().setTitle(R.string.favorite_name);

        findView();
        openDB();
        show();


        delete_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                delete();
            }
        });

        cancel_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment fragment = new FavoriteFragment();
                ft.replace(R.id.layout_fragment, fragment);
                ft.commit();
            }
        });

        return view;
    }

    private void findView(){
        mList = (ListView)view.findViewById(R.id.list_view2);
        delete_btn = (Button)view.findViewById(R.id.delete_btn);
        cancel_btn = (Button)view.findViewById(R.id.cancel_btn);
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

    private void delete(){
        Cursor cursor = DB.getInfo(DB.getReadableDatabase());
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Log.i("proj_name", cursor.getString(2));
                Log.i("proj_id", cursor.getString(1));
            }
            while (cursor.moveToNext());
        }
        for(int i=0; i<checkedIds.size(); i++){
            Log.i("id", checkedIds.get(i));
            /*mNames.remove(i);
            mIds.remove(i);*/
            DB.delete(DB.getWritableDatabase(), checkedIds.get(i));
        }
        show();
        Log.i("after delete", String.valueOf(checkedIds.size()));
        myAdapter.notifyDataSetChanged();
        cursor.close();
    }

    private void show(){
        mNames = new ArrayList<>();
        mIds = new ArrayList<>();
        checkedIds = new ArrayList<>();
        Cursor cursor = DB.getInfo(DB.getReadableDatabase());
        if(cursor.getCount() > 0) {
            Log.i("cursor", ">0");
            cursor.moveToFirst();
            do{
                mNames.add(cursor.getString(2));
                mIds.add(cursor.getString(1));
            }
            while (cursor.moveToNext());
            myAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, mNames);
            mList.setAdapter(myAdapter);
            mList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                    AbsListView list = (AbsListView)adapterView;
                    Adapter adapter = list.getAdapter();
                    SparseBooleanArray array = list.getCheckedItemPositions();
                    List<String> checked = new ArrayList<>(list.getCheckedItemCount());
                    checkedIds.clear();
                    for (int i = 0; i < array.size(); i++) {
                        int key = array.keyAt(i);
                        if (array.get(key)) {
                            checked.add(String.valueOf(adapter.getItem(key)));
                            /* record id*/
                            checkedIds.add(mIds.get(key));
                            Log.i("checkid", mIds.get(key));
                        }
                    }
                }
            });
        } else{
            myAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, mNames);
            mList.setAdapter(myAdapter);
            mList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        }
        cursor.close();
    }

}
