package com.sfh.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sfh.lib.ui.AbstractLifecycleActivity;

import java.io.File;

public class Main2Activity extends AbstractLifecycleActivity {

    public static void statUI(Context context,String path){
        Intent intent = new Intent(context,Main2Activity.class);
        intent.putExtra("path",path);
        context.startActivity(intent);
    }
    WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        webView = findViewById(R.id.web);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showLoading(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideLoading();
            }
        });
        String path = getIntent().getStringExtra("path");
        webView.loadUrl(Uri.fromFile(new File(path)).toString());
    }
}
