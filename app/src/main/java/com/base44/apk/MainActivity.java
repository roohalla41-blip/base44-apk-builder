package com.base44.apk;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;

import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private static final String APP_URL =
            "https://practical-pure-quran-path.base44.app/";

    // ایمیل پشتیبانی را بعداً اینجا وارد می‌کنیم
    private static final String SUPPORT_EMAIL =
            "support@example.com";

    private WebView webView;
    private FrameLayout rootLayout;

    private LinearLayout loadingLayout;
    private TextView progressText;
    private TextView statusText;

    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean pageFinished = false;
    private boolean pageError = false;

    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        window.setStatusBarColor(Color.WHITE);
        window.setNavigationBarColor(Color.WHITE);

        if (Build.VERSION.SDK_INT >= 23) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        createRootLayout();
        createLoadingScreen();
        createWebView();

        setContentView(rootLayout);

        startLoading();
    }

    private void createRootLayout() {

        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.WHITE);

        rootLayout.setOnApplyWindowInsetsListener((view, insets) -> {

            if (Build.VERSION.SDK_INT >= 30) {

                android.graphics.Insets systemBars =
                        insets.getInsets(WindowInsets.Type.systemBars());

                FrameLayout.LayoutParams params =
                        (FrameLayout.LayoutParams) webView.getLayoutParams();

                params.leftMargin = 0;
                params.topMargin = systemBars.top;
                params.rightMargin = 0;
                params.bottomMargin = systemBars.bottom;

                webView.setLayoutParams(params);

            } else if (Build.VERSION.SDK_INT >= 23) {

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
    }

    private void createLoadingScreen() {

        loadingLayout = new LinearLayout(this);
        loadingLayout.setOrientation(LinearLayout.VERTICAL);
        loadingLayout.setGravity(Gravity.CENTER);
        loadingLayout.setPadding(40, 40, 40, 40);
        loadingLayout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("به برنامه خوش آمدید");
        title.setTextSize(24);
        title.setTextColor(Color.rgb(15, 81, 50));
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        loadingLayout.addView(
                title,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        TextView subtitle = new TextView(this);
        subtitle.setText("در حال آماده‌سازی برنامه...");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 20, 0, 30);

        loadingLayout.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        progressText = new TextView(this);
        progressText.setText("1%");
        progressText.setTextSize(28);
        progressText.setTextColor(Color.rgb(15, 81, 50));
        progressText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        progressText.setGravity(Gravity.CENTER);

        loadingLayout.addView(
                progressText,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        statusText = new TextView(this);
        statusText.setText("در حال اتصال به برنامه...");
        statusText.setTextSize(14);
        statusText.setTextColor(Color.GRAY);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 20, 0, 0);

        loadingLayout.addView(
                statusText,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        rootLayout.addView(
                loadingLayout,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );
    }

    private void createWebView() {

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setMediaPlaybackRequiresUserGesture(false);

        // حفظ قابلیت استفاده از اطلاعات ذخیره‌شده WebView
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    android.graphics.Bitmap favicon) {

                pageFinished = false;
                pageError = false;

                updateStatus("در حال اتصال به برنامه...");
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url) {

                pageFinished = true;

                updateProgress(100);
                updateStatus("برنامه آماده است");

                handler.postDelayed(() -> {

                    if (pageFinished) {
                        hideLoadingScreen();
                    }

                }, 300);
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error) {

                if (request.isForMainFrame()) {
                    pageError = true;
                }

                super.onReceivedError(view, request, error);
            }
        });

        webView.setDownloadListener(
                (url, userAgent, contentDisposition, mimetype, contentLength) -> {

                    try {

                        Intent intent =
                                new Intent(Intent.ACTION_VIEW);

                        intent.setData(Uri.parse(url));

                        startActivity(intent);

                    } catch (Exception ignored) {
                    }
                }
        );

        FrameLayout.LayoutParams webParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        rootLayout.addView(webView, webParams);
    }

    private void startLoading() {

        showLoadingScreen();

        updateProgress(1);

        updateStatus("در حال بررسی اتصال اینترنت...");

        if (!isInternetAvailable()) {

            updateStatus("اتصال اینترنت برقرار نیست");

            showErrorAfterDelay(
                    "اتصال اینترنت برقرار نیست",
                    "لطفاً اتصال اینترنت خود را بررسی کنید."
            );

            return;
        }

        updateStatus("اتصال اینترنت برقرار است");
        updateProgress(5);

        webView.loadUrl(APP_URL);

        timeoutRunnable = () -> {

            if (!pageFinished) {

                checkServerStatus();
            }

        };

        handler.postDelayed(timeoutRunnable, 12000);
    }

    private void checkServerStatus() {

        updateStatus("در حال بررسی سرویس...");

        new Thread(() -> {

            boolean serverAvailable = false;

            HttpURLConnection connection = null;

            try {

                URL url = new URL(APP_URL);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("HEAD");

                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                connection.setInstanceFollowRedirects(true);

                int responseCode =
                        connection.getResponseCode();

                serverAvailable =
                        responseCode >= 200 &&
                        responseCode < 500;

            } catch (Exception ignored) {

                serverAvailable = false;

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

            boolean finalServerAvailable = serverAvailable;

            runOnUiThread(() -> {

                if (pageFinished) {
                    return;
                }

                if (finalServerAvailable) {

                    updateStatus(
                            "سرویس در دسترس است، در حال بارگذاری..."
                    );

                } else {

                    showErrorScreen(
                            "برنامه با مشکل فنی مواجه شده است",
                            "امکان اتصال به سرویس برنامه وجود ندارد."
                    );
                }
            });

        }).start();
    }

    private boolean isInternetAvailable() {

        ConnectivityManager cm =
                (ConnectivityManager)
                        getSystemService(CONNECTIVITY_SERVICE);

        if (cm == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            Network network =
                    cm.getActiveNetwork();

            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities =
                    cm.getNetworkCapabilities(network);

            if (capabilities == null) {
                return false;
            }

            return capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
            );

        } else {

            android.net.NetworkInfo info =
                    cm.getActiveNetworkInfo();

            return info != null && info.isConnected();
        }
    }

    private void updateProgress(int progress) {

        if (progress < 1) {
            progress = 1;
        }

        if (progress > 100) {
            progress = 100;
        }

        final int finalProgress = progress;

        runOnUiThread(() -> {

            if (progressText != null) {

                progressText.setText(
                        finalProgress + "%"
                );
            }
        });
    }

    private void updateStatus(String text) {

        runOnUiThread(() -> {

            if (statusText != null) {
                statusText.setText(text);
            }
        });
    }

    private void showLoadingScreen() {

        runOnUiThread(() -> {

            if (loadingLayout != null) {
                loadingLayout.setVisibility(View.VISIBLE);
            }

            if (webView != null) {
                webView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void hideLoadingScreen() {

        runOnUiThread(() -> {

            if (loadingLayout != null) {
                loadingLayout.setVisibility(View.GONE);
            }

            if (webView != null) {
                webView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showErrorAfterDelay(
            String title,
            String message) {

        handler.postDelayed(() -> {

            if (!pageFinished) {

                showErrorScreen(title, message);
            }

        }, 1000);
    }

    private void showErrorScreen(
            String title,
            String message) {

        runOnUiThread(() -> {

            loadingLayout.removeAllViews();

            TextView titleView = new TextView(this);

            titleView.setText(title);
            titleView.setTextSize(21);
            titleView.setTextColor(
                    Color.rgb(15, 81, 50)
            );
            titleView.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
            titleView.setGravity(Gravity.CENTER);

            loadingLayout.addView(
                    titleView,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );

            TextView messageView = new TextView(this);

            messageView.setText(message);
            messageView.setTextSize(16);
            messageView.setTextColor(Color.DKGRAY);
            messageView.setGravity(Gravity.CENTER);
            messageView.setPadding(0, 20, 0, 30);

            loadingLayout.addView(
                    messageView,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );

            Button retryButton = new Button(this);

            retryButton.setText("تلاش مجدد");

            retryButton.setOnClickListener(
                    view -> {

                        pageFinished = false;
                        pageError = false;

                        recreate();
                    }
            );

            loadingLayout.addView(
                    retryButton,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );

            Button supportButton = new Button(this);

            supportButton.setText("گزارش مشکل به پشتیبانی");

            supportButton.setOnClickListener(
                    view -> openSupportEmail()
            );

            LinearLayout.LayoutParams supportParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            supportParams.topMargin = 15;

            loadingLayout.addView(
                    supportButton,
                    supportParams
            );

            loadingLayout.setVisibility(View.VISIBLE);

            if (webView != null) {
                webView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void openSupportEmail() {

        try {

            Intent emailIntent =
                    new Intent(Intent.ACTION_SENDTO);

            emailIntent.setData(
                    Uri.parse("mailto:" + SUPPORT_EMAIL)
            );

            emailIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "گزارش مشکل برنامه"
            );

            emailIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "سلام، در استفاده از برنامه با مشکل مواجه شدم."
            );

            startActivity(emailIntent);

        } catch (Exception ignored) {

            Intent browserIntent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "mailto:" + SUPPORT_EMAIL
                            )
                    );

            startActivity(browserIntent);
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
    protected void onDestroy() {

        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable);
        }

        if (webView != null) {

            webView.stopLoading();
            webView.destroy();
        }

        super.onDestroy();
    }
            }
