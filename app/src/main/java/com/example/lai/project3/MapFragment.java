package com.example.lai.project3;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * Created by lai on 2016/10/15.
 **/

public class MapFragment extends Fragment{
    private View view;
    private Button locate_btn;
    private WebView mWebViewMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);
        getActivity().setTitle(R.string.map_name);

        findView();
        readHtmlFormAssets();

        mWebViewMap.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                mWebViewMap.loadUrl("javascript:refreshPoint(10, 10)");
            }
        });

        locate_btn = (Button)view.findViewById(R.id.locate_btn);

        locate_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                // TODO Auto-generated method stub
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment fragment = new ScanFragment();
                ft.replace(R.id.layout_fragment, fragment);
                ft.commit();


            }
        });

        return view;
    }

    private void findView(){
        mWebViewMap = (WebView) view.findViewById(R.id.wvMap);
    }

    // read svg
    private void readHtmlFormAssets() {
        mWebViewMap.setWebChromeClient(new WebChromeClient());
        mWebViewMap.setWebViewClient(new WebViewClient());
        mWebViewMap.setHorizontalScrollBarEnabled(false);
        mWebViewMap.setVerticalScrollBarEnabled(false);
        mWebViewMap.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWebViewMap.setBackgroundColor(Color.TRANSPARENT);

        WebSettings websettings = mWebViewMap.getSettings();
        websettings.setJavaScriptEnabled(true);
        websettings.setSupportZoom(true);
        websettings.setBuiltInZoomControls(true);
        websettings.setDisplayZoomControls(false);
        websettings.setAllowFileAccessFromFileURLs(true);
        websettings.setSupportMultipleWindows(false);
        websettings.setJavaScriptCanOpenWindowsAutomatically(false);
        websettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        websettings.setLoadWithOverviewMode(true);
        websettings.setUseWideViewPort(true);


        String aURL = "file:///android_asset/index.html";
        mWebViewMap.loadUrl(aURL);


    }
}
