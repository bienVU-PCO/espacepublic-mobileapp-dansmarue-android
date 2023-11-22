package com.accenture.dansmarue.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.accenture.dansmarue.R;

import butterknife.OnClick;

public class InternalWebViewActivity extends BaseActivity{


    public static final String WEBSITE_ADDRESS = "website_address";
    public static final String SCREEN_TITLE = "screen_title";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url  = getIntent().getStringExtra(WEBSITE_ADDRESS);
        String screenTitle = getIntent().getStringExtra(SCREEN_TITLE);
        if (url == null || url.isEmpty()) finish();

        ((TextView)findViewById(R.id.text_title_fa)).setText(screenTitle);
        WebView webView = (WebView) findViewById(R.id.dmr_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient());

        ImageView imageArrowBack = (ImageView) findViewById(R.id.arrow_back_fa);
        imageArrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.clearHistory();
                webView.clearCache(true);
                onBackPressed();
            }
        });
    }


    @Override
    protected int getContentView() {
        return R.layout.internal_web_view_activity_layout;
    }

    @OnClick(R.id.arrow_back_fa)
    public void backType() {
        finish();
    }
}