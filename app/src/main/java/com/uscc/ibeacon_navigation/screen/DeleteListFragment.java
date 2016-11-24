package com.uscc.ibeacon_navigation.screen;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.uscc.ibeacon_navigation.aid.SQLiteManager;

import java.util.ArrayList;

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
                ft.replace(R.id.layout_fragment, fragment, "FavoriteFragment");
                ft.commit();
            }
        });

        return view;
    }

    private void findView(){
        mList = (ListView)view.findViewById(R.id.list_view);
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
        for(int i = 0; i < checkedIds.size(); i++){
            DB.delete(DB.getWritableDatabase(), checkedIds.get(i));
        }
        show();
        myAdapter.notifyDataSetChanged();
        cursor.close();
    }

    private void show(){
        mNames = new ArrayList<>();
        mIds = new ArrayList<>();
        checkedIds = new ArrayList<>();
        Cursor cursor = DB.getInfo(DB.getReadableDatabase());

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            do{
                mNames.add(cursor.getString(2));
                mIds.add(cursor.getString(1));
            }
            while (cursor.moveToNext());
            myAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, mNames);

            // setup list view
            mList.setAdapter(myAdapter);
            mList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

            mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                    AbsListView list = (AbsListView)adapterView;
                    SparseBooleanArray array = list.getCheckedItemPositions();
                    checkedIds.clear();

                    for (int i = 0; i < array.size(); i++) {
                        int key = array.keyAt(i);
                        if (array.get(key)) {
                            /* record id*/
                            checkedIds.add(mIds.get(key));
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
