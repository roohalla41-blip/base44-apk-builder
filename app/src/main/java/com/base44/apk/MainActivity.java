package com.base44.apk;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.net.Uri;
import android.content.Intent;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    private static final String APP_URL =
            "https://motherapp.base44.app/dashboard";

    private WebView webView;

    private static final String CHROME_UA =
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) "
            + "Chrome/124.0.0.0 Mobile Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);

        FrameLayout root = new FrameLayout(this);

        webView = new WebView(this);

        root.addView(
                webView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(root);

        setupWebView();

        if (savedInstanceState == null) {
            webView.loadUrl(APP_URL);
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private void setupWebView() {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setUserAgentString(CHROME_UA);

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);

        CookieManager cookies =
                CookieManager.getInstance();

        cookies.setAcceptCookie(true);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            cookies.setAcceptThirdPartyCookies(
                    webView,
                    true
            );
        }

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                return handleUrl(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url
            ) {
                return handleUrl(Uri.parse(url));
            }
        });
    }

    private boolean handleUrl(Uri uri) {

        if (uri == null) {
            return false;
        }

        String url = uri.toString();

        /*
         * لینک‌های خود Base44 و برنامه داخل WebView
         * باز می‌شوند.
         */
        if (url.startsWith("https://motherapp.base44.app/")
                || url.startsWith("https://app.base44.com/")) {

            return false;
        }

        /*
         * لینک‌های Google را مستقیماً در WebView باز نمی‌کنیم.
         * این قسمت عمداً فعلاً دست‌کاری نمی‌شود تا
         * State مربوط به OAuth خراب نشود.
         */
        if (url.startsWith("https://accounts.google.com/")) {

            Intent intent =
                    new Intent(Intent.ACTION_VIEW, uri);

            try {
                startActivity(intent);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (webView != null) {
            webView.saveState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {

        if (webView != null) {
            CookieManager.getInstance().flush();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {
            webView.stopLoading();
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
