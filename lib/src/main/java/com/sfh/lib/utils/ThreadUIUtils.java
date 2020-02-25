package com.sfh.lib.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.TimeUnit;

public class ThreadUIUtils {
    private static Handler sMainHandler = new Handler(Looper.getMainLooper());
    private static long sMainThreadId = sMainHandler.getLooper().getThread().getId();

    private ThreadUIUtils() {
    }

    public static void runOnUiThread(Runnable action) {
        if (isInUiThread()) {
            action.run();
        } else {
            sMainHandler.post(action);
        }
    }

    public static void onUiThread(Runnable action) {
        sMainHandler.post(action);
    }

    public static boolean isInUiThread() {
        return Thread.currentThread().getId() == sMainThreadId;
    }

    public static boolean postDelayed(Runnable action, long duration, TimeUnit unit) {
        long delayMillis = unit == TimeUnit.MILLISECONDS ? duration
                : unit == TimeUnit.SECONDS ? duration * 1000
                : unit == TimeUnit.MINUTES ? duration * 1000 * 60
                : unit == TimeUnit.HOURS ? duration * 1000 * 60 * 60
                : duration * 1000 * 60 * 60 * 24;
        return sMainHandler.postDelayed(action, delayMillis);
    }
}
