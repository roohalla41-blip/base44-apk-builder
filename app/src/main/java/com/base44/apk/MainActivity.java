package com.base44.apk;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.widget.FrameLayout;

import androidx.browser.customtabs.CustomTabsIntent;

public class MainActivity extends Activity {

    private static final String APP_URL =
            "https://motherapp.base44.app/dashboard";

    private WebView webView;
    private FrameLayout rootLayout;

    private boolean googleLoginStarted = false;

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

        rootLayout = new FrameLayout(this);

        webView = new WebView(this);

        rootLayout.addView(
                webView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(rootLayout);

        configureWebView();

        if (savedInstanceState == null) {
            webView.loadUrl(APP_URL);
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private void configureWebView() {

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

        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            cookieManager.setAcceptThirdPartyCookies(
                    webView,
                    true
            );

            settings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_NEVER_ALLOW
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
         * فقط شروع Google OAuth را به Custom Tab می‌فرستیم.
         */
        if (!googleLoginStarted
                && url.startsWith("https://accounts.google.com/")) {

            googleLoginStarted = true;

            CookieManager.getInstance().flush();

            CustomTabsIntent.Builder builder =
                    new CustomTabsIntent.Builder();

            CustomTabsIntent customTabsIntent =
                    builder.build();

            customTabsIntent.launchUrl(this, uri);

            return true;
        }

        /*
         * وقتی OAuth شروع شده، دیگر هیچ URL وبی
         * دوباره به Google فرستاده نمی‌شود.
         *
         * این قسمت جلوی حلقه Google → Google را می‌گیرد.
         */
        if ("http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme())) {

            return false;
        }

        /*
         * لینک‌های خارجی مثل tel:// و mailto://
         */
        try {
            Intent intent =
                    new Intent(Intent.ACTION_VIEW, uri);

            startActivity(intent);

        } catch (Exception ignored) {
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * بعد از برگشت از Custom Tab،
         * کوکی‌های WebView را تازه‌سازی می‌کنیم.
         */
        if (webView != null && googleLoginStarted) {

            CookieManager.getInstance().flush();

            webView.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (webView != null) {

                        webView.loadUrl(
                                "https://motherapp.base44.app/dashboard"
                        );
                    }
                }
            }, 500);

            googleLoginStarted = false;
        }
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
    protected void onSaveInstanceState(Bundle outState) {

        if (webView != null) {
            webView.saveState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (webView != null) {
            CookieManager.getInstance().flush();
        }
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
}
