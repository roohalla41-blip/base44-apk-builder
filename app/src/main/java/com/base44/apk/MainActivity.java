package com.base44.apk;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.browser.customtabs.CustomTabsIntent;

public class MainActivity extends Activity {

private static final String APP_URL =
        "https://motherapp.base44.app/dashboard";

private WebView webView;

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
    settings.setJavaScriptCanOpenWindowsAutomatically(true);
    settings.setSupportMultipleWindows(false);

    settings.setLoadWithOverviewMode(false);
    settings.setUseWideViewPort(false);

    settings.setAllowFileAccess(true);
    settings.setAllowContentAccess(true);

    CookieManager cookieManager = CookieManager.getInstance();
    cookieManager.setAcceptCookie(true);
    cookieManager.setAcceptThirdPartyCookies(webView, true);

    webView.setBackgroundColor(Color.WHITE);

    webView.setWebChromeClient(new WebChromeClient());

    webView.setWebViewClient(new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view,
                WebResourceRequest request
        ) {
            return handleUrl(request.getUrl().toString());
        }

        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view,
                String url
        ) {
            return handleUrl(url);
        }
    });
}

private boolean handleUrl(String url) {

    if (url == null || url.isEmpty()) {
        return false;
    }

    Uri uri = Uri.parse(url);
    String scheme = uri.getScheme();

    if (scheme == null) {
        return false;
    }

    /*
     * لینک‌های عادی خود برنامه باید داخل WebView باز شوند.
     */
    if (scheme.equals("http") || scheme.equals("https")) {

        String host = uri.getHost();

        if (host != null &&
                (host.equals("motherapp.base44.app")
                        || host.endsWith(".base44.app")
                        || host.contains("supabase.co")
                        || host.contains("google.com")
                        || host.contains("accounts.google.com"))) {

            webView.loadUrl(url);
            return true;
        }

        /*
         * سایر لینک‌های HTTPS را نیز فعلاً داخل WebView نگه می‌داریم
         * تا فرآیند ورود Google از برنامه خارج نشود.
         */
        webView.loadUrl(url);
        return true;
    }

    /*
     * Deep Link / OAuth callback
     */
    if (scheme.equals("intent")
            || scheme.equals("app")
            || scheme.equals("base44")
            || scheme.equals("com.base44.apk")) {

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (Exception ignored) {
        }

        return true;
    }

    return false;
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
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.destroy();
        webView = null;
    }

    super.onDestroy();
}

}
