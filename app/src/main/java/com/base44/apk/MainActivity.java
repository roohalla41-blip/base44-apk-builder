package com.base44.apk;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    private static final String APP_URL =
            "https://motherapp.base44.app/dashboard";

    private WebView webView;
    private FrameLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        // رنگ نوارهای سیستم
        window.setStatusBarColor(Color.WHITE);
        window.setNavigationBarColor(Color.WHITE);

        // فقط برای Android 6 به بالا
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        // ساخت صفحه اصلی
        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.WHITE);

        // ساخت WebView
        webView = new WebView(this);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // تنظیمات سازگاری WebView
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);

        // پشتیبانی از محتوای ترکیبی در نسخه‌های جدید
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            );
        }

        // جلوگیری از باز شدن لینک‌ها در مرورگر خارجی
        webView.setWebViewClient(new WebViewClient());

        // قرار دادن WebView در صفحه
        rootLayout.addView(
                webView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(rootLayout);

        // باز کردن برنامه
        webView.loadUrl(APP_URL);
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}
