package com.example.lai.project3;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lai on 2016/10/17.
 **/

public class ListAdapter extends BaseAdapter {

    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;

    private ItemView itemView;

    private class ItemView {
        TextView ItemName;
        Button ItemButton;
    }

    public ListAdapter(Context c, ArrayList<HashMap<String, Object>> appList, int resource, String[] from, int[] to) {
        mAppList = appList;
        mContext = c;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyString = new String[from.length];
        valueViewID = new int[to.length];
        System.arraycopy(from, 0, keyString, 0, from.length);
        System.arraycopy(to, 0, valueViewID, 0, to.length);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //return 0;
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        //return null;
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        //return 0;
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        //return null;
        if (convertView != null) {
            itemView = (ItemView) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.list_adapter, null);
            itemView = new ItemView();
            itemView.ItemName = (TextView) convertView.findViewById(valueViewID[0]);
            itemView.ItemButton = (Button) convertView.findViewById(valueViewID[1]);
            convertView.setTag(itemView);
        }

        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {
            String name = (String) appInfo.get(keyString[0]);
            itemView.ItemName.setText(name);
            itemView.ItemButton.setBackgroundResource(R.drawable.ic_remove);
            itemView.ItemButton.setOnClickListener(new ItemButton_Click(position));
        }

        return convertView;
    }

    class ItemButton_Click implements View.OnClickListener {
        private int position;

        ItemButton_Click(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View v) {
            int vid = v.getId();
            if (vid == itemView.ItemButton.getId()){
                Log.v("ola_log", String.valueOf(position));
            }
        }
    }
}
