package com.sfh.lib.utils.thread;

import android.support.annotation.NonNull;

public abstract class TaskExecutor {

    public abstract void executeOnDiskIO(@NonNull Runnable runnable);


    public abstract void postToMainThread(@NonNull Runnable runnable);


    public void executeOnMainThread(@NonNull Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            postToMainThread(runnable);
        }
    }

    public abstract boolean isMainThread();
}
