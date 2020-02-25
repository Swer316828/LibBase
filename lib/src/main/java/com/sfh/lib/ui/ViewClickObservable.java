package com.sfh.lib.ui;

import android.support.annotation.NonNull;
import android.view.View;


import com.sfh.lib.utils.ThreadUIUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/4/8
 */
class ViewClickObservable implements Future<View>, View.OnClickListener, Runnable {

    private View view;
    private View.OnClickListener listener;
    private long mDuration;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private final AtomicBoolean mCancelBoolean = new AtomicBoolean(false);


    public ViewClickObservable(View view,long duration, View.OnClickListener listener) {
        this.view = view;
        this.listener = listener;
        this.mDuration = duration;
        this.view.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        if (!this.isCancelled() && !this.mRunning.get()) {
            this.mRunning.set(true);
            ThreadUIUtils.postDelayed(this, this.mDuration, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void run() {
        if (listener != null) {
            this.mRunning.set(false);
            this.listener.onClick(this.view);
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.view.setOnClickListener(null);
        this.mCancelBoolean.set(true);
        this.view = null;
        this.listener = null;
        return true;
    }

    @Override
    public boolean isCancelled() {
        return this.mCancelBoolean.get();
    }

    @Override
    public boolean isDone() {
        return this.mCancelBoolean.get();
    }


    @Override
    public View get() throws InterruptedException, ExecutionException {
        return this.view;
    }

    @Override
    public View get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.view;
    }

}
