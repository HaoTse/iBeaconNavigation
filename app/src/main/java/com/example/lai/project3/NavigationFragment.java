package com.example.lai.project3;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by lai on 2016/10/15.
 **/

public class NavigationFragment extends Fragment {
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_navigation, container, false);

        return view;
    }

    private void findView(){

    }
}
