package com.uscc.ibeacon_navigation.screen;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uscc.ibeacon_navigation.algorithm.AStar;

import java.util.Iterator;
import java.util.Map;

public class NavigationFragment extends Fragment {

    private View view;
    AStar star;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_navigation, container, false);
        getActivity().setTitle(R.string.navigation_name);

        findView();
        executeAStar();
        return view;
    }

    private void findView(){

    }


    private void executeAStar(){
        this.star = new AStar();
        // grid, starting point, ending point, blocked point
        Map<String, String> result = AStar.executeAStar(5, 5, 0, 0, 4, 4, new int[][]{{4, 1}, {0, 4}, {3, 1}, {2, 2}, {2, 1}, {4, 3}});
        // trace back the path
        printMap(result);
    }

    public String printMap(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=').append('"');
            sb.append(entry.getValue());
            sb.append('"');
            if (iter.hasNext()) {
                sb.append(',').append(' ');
            }
        }
        return sb.toString();
    }

}
