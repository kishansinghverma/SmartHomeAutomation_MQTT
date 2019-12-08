package com.example.smarthomeautomation_mqtt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class VideoActivity extends AppCompatActivity {

    String url="http://192.168.43.1:8080/browserfs.html";
    TextView local, global;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        local=findViewById(R.id.local);
        global=findViewById(R.id.global);

        webView=findViewById(R.id.video_view);
        webView.setWebViewClient(new mBrowser());
        webView.loadUrl(url);
    }

    public void setUrl(View v){
        if(v==local){
            url="http://192.168.43.1:8080/browserfs.html";
            local.setText("Local IP \uf058");
            global.setText("Public IP \uf111");
            webView.loadUrl(url);
        }
        else {
            url="http://klinux.tk/browserfs.html";
            local.setText("Local IP \uf111");
            global.setText("Public IP \uf058");
            webView.loadUrl(url);
        }
    }
}
class mBrowser extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}
