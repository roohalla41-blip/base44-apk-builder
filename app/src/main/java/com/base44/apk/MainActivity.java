package com.base44.apk;

import android.app.Activity;
import android.os.Bundle;

import androidx.browser.customtabs.CustomTabsIntent;

public class MainActivity extends Activity {

    private static final String APP_URL =
            "https://motherapp.base44.app/dashboard";

    private boolean opened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            openApp();
        }
    }

    private void openApp() {

        if (opened) {
            return;
        }

        opened = true;

        CustomTabsIntent.Builder builder =
                new CustomTabsIntent.Builder();

        CustomTabsIntent customTabsIntent =
                builder.build();

        customTabsIntent.launchUrl(
                this,
                android.net.Uri.parse(APP_URL)
        );
    }
}
