package com.coinomi.wallet.util;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

public class ScreenshotPreventHelper {

    public void setScreenshotPrevent(Activity activity) {
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                );
            }
        }
    }

    public void removeScreenshotPrevent(Activity activity) {
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
        }
    }
}
