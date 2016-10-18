package com.example.lai.project3;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by lai on 2016/10/15.
 **/

public class MapFragment extends Fragment{
    private View view;
    private WebView mWebViewMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);

        findView();

        readHtmlFormAssets();
        mWebViewMap.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                mWebViewMap.loadUrl("javascript:refreshPoint(10, 10)");
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
        //mJavaScriptInterface = new net.macdidi.webviewtest.JavaScriptInterface(ScanActivity.this);
        //mWebViewMap.addJavascriptInterface(mJavaScriptInterface, "Android");
        mWebViewMap.setHorizontalScrollBarEnabled(false);
        mWebViewMap.setVerticalScrollBarEnabled(false);
        mWebViewMap.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWebViewMap.setBackgroundColor(Color.TRANSPARENT);

        WebSettings websettings = mWebViewMap.getSettings();
        websettings.setJavaScriptEnabled(true);
        websettings.setSupportZoom(false);  // do not remove this
        websettings.setAllowFileAccessFromFileURLs(true); // do not remove this
        websettings.setSupportMultipleWindows(false);
        websettings.setJavaScriptCanOpenWindowsAutomatically(false);
        websettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        websettings.setLoadWithOverviewMode(true);
        websettings.setUseWideViewPort(true);


        String aURL = "file:///android_asset/index.html";
        mWebViewMap.loadUrl(aURL);


    }
}
