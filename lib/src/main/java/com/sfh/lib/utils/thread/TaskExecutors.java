package com.sfh.lib.utils.thread;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

public class TaskExecutors extends com.sfh.lib.utils.thread.TaskExecutor {
    private static volatile TaskExecutors sInstance;
    @NonNull
    private TaskExecutor mDelegate;
    @NonNull
    private TaskExecutor mDefaultTaskExecutor = new DefaultTaskExecutor();
    @NonNull
    private static final Executor sMainThreadExecutor = new Executor() {
        public void execute(Runnable command) {
            TaskExecutors.getInstance().postToMainThread(command);
        }
    };
    @NonNull
    private static final Executor sIOThreadExecutor = new Executor() {
        public void execute(Runnable command) {
            TaskExecutors.getInstance().executeOnDiskIO(command);
        }
    };

    private TaskExecutors() {
        this.mDelegate = this.mDefaultTaskExecutor;
    }

    @NonNull
    public static TaskExecutors getInstance() {
        if (sInstance != null) {
            return sInstance;
        } else {
            Class var0 = TaskExecutors.class;
            synchronized(TaskExecutors.class) {
                if (sInstance == null) {
                    sInstance = new TaskExecutors();
                }
            }

            return sInstance;
        }
    }

    public void setDelegate(@Nullable TaskExecutor taskExecutor) {
        this.mDelegate = taskExecutor == null ? this.mDefaultTaskExecutor : taskExecutor;
    }

    public void executeOnDiskIO(Runnable runnable) {
        this.mDelegate.executeOnDiskIO(runnable);
    }

    public void postToMainThread(Runnable runnable) {
        this.mDelegate.postToMainThread(runnable);
    }

    @NonNull
    public static Executor getMainThreadExecutor() {
        return sMainThreadExecutor;
    }

    @NonNull
    public static Executor getIOThreadExecutor() {
        return sIOThreadExecutor;
    }

    public boolean isMainThread() {
        return this.mDelegate.isMainThread();
    }
}
