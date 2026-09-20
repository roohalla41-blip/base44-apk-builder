package com.base44.apk;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    private WebView webView;
    private FrameLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        // اجازه می‌دهد برنامه فضای نوارهای سیستمی گوشی را رعایت کند
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);

            window.setStatusBarColor(Color.WHITE);
            window.setNavigationBarColor(Color.WHITE);

            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        }

        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.WHITE);

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();

        // JavaScript برای اجرای کامل Base44
        settings.setJavaScriptEnabled(true);

        // ذخیره اطلاعات سایت
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        // اجازه پخش رسانه
        settings.setMediaPlaybackRequiresUserGesture(false);

        // جلوگیری از زوم ناخواسته
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // نمایش صحیح محتوا
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);

        // لینک‌ها داخل همین WebView باز شوند
        webView.setWebViewClient(new WebViewClient());

        // فاصله امن برای نوار وضعیت و نوار ناوبری گوشی
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

            webView.setOnApplyWindowInsetsListener((view, insets) -> {

                android.graphics.Insets systemBars =
                        insets.getInsets(WindowInsets.Type.systemBars());

                view.setPadding(
                        0,
                        systemBars.top,
                        0,
                        systemBars.bottom
                );

                return insets;
            });

        } else {

            webView.setPadding(0, 0, 0, 0);
        }

        rootLayout.addView(
                webView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(rootLayout);

        // آدرس واقعی برنامه قرآن
        webView.loadUrl(
                "https://practical-pure-quran-path.base44.app"
        );
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
        }

        super.onDestroy();
    }
}
