package com.base44.apk;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;
import android.net.Uri;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    private static final String APP_URL =
            "https://motherapp.base44.app/dashboard";

    private WebView webView;
    private FrameLayout rootLayout;

    private static final String CHROME_UA =
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) "
            + "Chrome/124.0.0.0 Mobile Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        window.setStatusBarColor(Color.WHITE);
        window.setNavigationBarColor(Color.WHITE);

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.WHITE);

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();

        // JavaScript
        settings.setJavaScriptEnabled(true);

        // Storage
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        // Cookies
        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            cookieManager.setAcceptThirdPartyCookies(
                    webView,
                    true
            );
        }

        // Chrome-like User-Agent
        settings.setUserAgentString(CHROME_UA);

        // View
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);

        // HTTPS
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_NEVER_ALLOW
            );
        }

        /*
         * اجازه مدیریت پنجره‌ها و رفتارهای Web App
         */
        webView.setWebChromeClient(new WebChromeClient());

        /*
         * مدیریت لینک‌ها
         */
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                Uri uri = request.getUrl();

                if (uri == null) {
                    return false;
                }

                String scheme = uri.getScheme();

                /*
                 * لینک‌های معمولی HTTP/HTTPS
                 * داخل WebView باقی می‌مانند.
                 */
                if ("http".equalsIgnoreCase(scheme)
                        || "https".equalsIgnoreCase(scheme)) {

                    return false;
                }

                /*
                 * لینک‌های intent:// ، tel:// ، mailto://
                 * و سایر Schemeهای خارجی
                 */
                try {
                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            uri
                    );

                    startActivity(intent);
                    return true;

                } catch (Exception e) {
                    return true;
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url
            ) {
                if (url == null) {
                    return false;
                }

                if (url.startsWith("http://")
                        || url.startsWith("https://")) {

                    return false;
                }

                try {
                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

                    startActivity(intent);
                    return true;

                } catch (Exception e) {
                    return true;
                }
            }
        });

        rootLayout.addView(
                webView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(rootLayout);

        webView.loadUrl(APP_URL);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * بعد از برگشت از Google یا برنامه خارجی،
         * وضعیت صفحه دوباره بررسی می‌شود.
         */
        if (webView != null) {
            webView.onResume();
            webView.reload();
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
    protected void onPause() {
        super.onPause();

        if (webView != null) {
            webView.onPause();
        }

        CookieManager.getInstance().flush();
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
