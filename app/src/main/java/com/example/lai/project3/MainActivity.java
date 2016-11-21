package com.example.lai.project3;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends AppCompatActivity {

    private BottomBar bottomBar;
    private FragmentManager fm;
    private Fragment fragment;
    private int check = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();

        /* set default tab on map */
        bottomBar.setDefaultTab(R.id.tab_map);

        fm = getFragmentManager();
        fragment = fm.findFragmentById(R.id.layout_fragment);

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if(tabId == R.id.tab_favorites){
                    if(check == 0)
                        stop();
                    fragment = new FavoriteFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment, "FavoriteFragment");
                    ft.commit();
                }else if (tabId == R.id.tab_navigation) {
                    if(check == 0)
                        stop();
                    fragment = new NavigationFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment, "NavigationFragment");
                    ft.commit();
                }else if(tabId == R.id.tab_map){
                    fragment = new ScanFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment, "ScanFragment");
                    ft.commit();
                } else if(tabId == R.id.tab_company){
                    if(check == 0)
                        stop();
                    fragment = new CompanyFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment, "CompanyFragment");
                    ft.commit();
                }else if(tabId == R.id.tab_list){
                    if(check == 0)
                        stop();
                    fragment = new ListFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment, "ListFragment");
                    ft.commit();
                }

            }
        });

        /*click on the bottom bar when it is already selected*/
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                if(tabId == R.id.tab_favorites){
                    fragment = new FavoriteFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment, "FavoriteFragment");
                    ft.commit();
                } else if (tabId == R.id.tab_list) {
                    fragment = new ListFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.layout_fragment, fragment, "ListFragment");
                    ft.commit();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        DeleteListFragment deleteListFragment = (DeleteListFragment)getFragmentManager().findFragmentByTag("DeleteListFragment");
        ProjectFragment projectFragment = (ProjectFragment)getFragmentManager().findFragmentByTag("ProjectFragment");

        if(deleteListFragment != null && deleteListFragment.isVisible()){
            Log.v("cur", "1");
            fragment = new FavoriteFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.layout_fragment, fragment, "FavoriteFragment");
            ft.commit();
        }
        else if(projectFragment != null && projectFragment.isVisible()){
            Log.v("cur", "2");
            fragment = new ListFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.layout_fragment, fragment, "ListFragment");
            ft.commit();
        }
        else {
            Log.v("cur", "3");
            //shutdown
            finish();
        }
    }

    protected void findView(){
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
    }

    public void stop(){
        check = 1;
        ScanFragment fm = (ScanFragment) getFragmentManager().findFragmentById(R.id.layout_fragment);
        fm.stopScan();
    }
}
