package com.example.lai.project3;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lai on 2016/10/15.
 **/

public class FavoriteFragment extends Fragment {
    private View view;
    private SQLiteManager DB = null;
    private ArrayList<String> mNames;
    private ArrayList<String> mIds;
    private ListView mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favorite, container, false);
        mNames = new ArrayList<>();
        mIds = new ArrayList<>();
        getActivity().setTitle(R.string.favorite_name);

        findView();
        openDB();
        show();

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment fragment = new ProjectFragment();
                ft.replace(R.id.layout_fragment, fragment);
                Bundle bundle = new Bundle();
                bundle.putString("id", mIds.get((int)id));
                fragment.setArguments(bundle);
                //ft.addToBackStack(null);
                ft.commit();
            }
        });

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
        ArrayList<HashMap<String, Object>> Item = new ArrayList<>();
        Cursor cursor = DB.getInfo(DB.getReadableDatabase());
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("ItemName", cursor.getString(2));
                map.put("ItemButton", R.drawable.ic_remove);
                Item.add(map);
                /* record id */
                mIds.add(cursor.getString(1));
            }
            ListAdapter adapter = new ListAdapter(
                    getActivity(),
                    Item,
                    R.layout.list_adapter,
                    new String[] {"ItemName", "ItemButton"},
                    new int[] {R.id.ItemName,R.id.ItemButton}
            );
            mList.setAdapter(adapter);
        }
    }

}
