package com.example.lai.project3;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
    private int size = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favorite, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                if(size > 0) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Fragment fragment = new DeleteListFragment();
                    ft.replace(R.id.layout_fragment, fragment);
                    ft.commit();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        size = cursor.getCount();
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            do{
                mNames.add(cursor.getString(2));
                /* record id */
                mIds.add(cursor.getString(1));
            }
            while (cursor.moveToNext());
            mList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mNames));
        }
    }

}
