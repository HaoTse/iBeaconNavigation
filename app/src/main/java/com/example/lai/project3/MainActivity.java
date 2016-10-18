package com.example.lai.project3;

import android.app.*;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private BottomBar bottomBar;
    private FragmentManager fm;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();

        bottomBar.setDefaultTab(R.id.tab_map);

        fm = getFragmentManager();
        fragment = fm.findFragmentById(R.id.layout_fragment);

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if(tabId == R.id.tab_favorites){
                    fragment = new FavoriteFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment);
                    ft.addToBackStack(null);
                    ft.commit();
                }else if (tabId == R.id.tab_navigation) {
                    fragment = new NavigationFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment);
                    ft.addToBackStack(null);
                    ft.commit();
                }else if(tabId == R.id.tab_map){
                    fragment = new MapFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment);
                    ft.addToBackStack(null);
                    ft.commit();
                } else if(tabId == R.id.tab_company){
                    fragment = new CompanyFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment);
                    ft.addToBackStack(null);
                    ft.commit();
                }else if(tabId == R.id.tab_list){
                    fragment = new ListFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment);
                    ft.addToBackStack(null);
                    ft.commit();
                }

            }
        });

    }



    @Override
    public void onBackPressed() {
        fm = this.getFragmentManager();

        if (fm.getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            Log.i("tag", fm.getBackStackEntryAt(fm.getBackStackEntryCount()-1).toString());
            fm.popBackStack();
        }
    }


    protected void findView(){
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
    }
}
