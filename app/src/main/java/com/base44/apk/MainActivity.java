package com.base44.apk;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity {

    private static final String APP_URL =
            "https://motherapp.base44.app/dashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // باز کردن سایت در مرورگر سیستم
        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(APP_URL)
        );

        startActivity(browserIntent);

        // این Activity فقط نقش Launcher را دارد.
        // بعد از باز شدن مرورگر، خود برنامه بسته می‌شود.
        finish();
    }
}
