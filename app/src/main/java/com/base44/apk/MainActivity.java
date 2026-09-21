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

private static final String APP_URL =
        "https://practical-pure-quran-path.base44.app/";

private WebView webView;
private FrameLayout rootLayout;

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

    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    settings.setDatabaseEnabled(true);

    settings.setSupportZoom(false);
    settings.setBuiltInZoomControls(false);
    settings.setDisplayZoomControls(false);

    settings.setMediaPlaybackRequiresUserGesture(false);

    webView.setWebViewClient(new WebViewClient());

    rootLayout.addView(
            webView,
            new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            )
    );

    rootLayout.setOnApplyWindowInsetsListener((view, insets) -> {

        if (android.os.Build.VERSION.SDK_INT >= 30) {

            android.graphics.Insets systemBars =
                    insets.getInsets(WindowInsets.Type.systemBars());

            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) webView.getLayoutParams();

            params.leftMargin = 0;
            params.topMargin = systemBars.top;
            params.rightMargin = 0;
            params.bottomMargin = systemBars.bottom;

            webView.setLayoutParams(params);

        } else if (android.os.Build.VERSION.SDK_INT >= 23) {

            int top = insets.getSystemWindowInsetTop();
            int bottom = insets.getSystemWindowInsetBottom();

            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) webView.getLayoutParams();

            params.leftMargin = 0;
            params.topMargin = top;
            params.rightMargin = 0;
            params.bottomMargin = bottom;

            webView.setLayoutParams(params);
        }

        return insets;
    });

    setContentView(rootLayout);

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
    }

    super.onDestroy();
}

}
