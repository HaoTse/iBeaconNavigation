package com.example.lai.project3;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NavigationFragment extends Fragment {
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_navigation, container, false);
        getActivity().setTitle(R.string.navigation_name);

        findView();

        return view;
    }

    private void findView(){

    }
}
