package com.sfh.lib.utils.ui;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;


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
public class TextViewTextObservable implements Future<CharSequence>, TextWatcher {

    private final TextView view;
    private final AtomicBoolean mCancelBoolean = new AtomicBoolean(true);
    private long mDuration;
    private TextChangedListener textChanged;


    TextViewTextObservable(TextView view, long timeout, TextChangedListener textChanged) {
        this.view = view;
        this.textChanged = textChanged;
        this.mDuration = timeout;
        this.view.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        view.removeCallbacks(runCharSequence);
        runCharSequence.setData(s);
        view.postDelayed(runCharSequence, this.mDuration);
    }

    private final RunCharSequence runCharSequence = new RunCharSequence();

    class RunCharSequence implements Runnable {
        private CharSequence content;

        public void setData(CharSequence content) {
            this.content = content;
        }

        @Override
        public void run() {
            if (!isCancelled()) {
                return;
            }
            if (ThreadUIUtils.isInUiThread()) {
                if (textChanged != null) {
                    textChanged.textChanged(content);
                }
            } else {
                ThreadUIUtils.onUiThread(this);
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        mCancelBoolean.set(false);
        view.removeTextChangedListener(this);
        return false;
    }

    @Override
    public boolean isCancelled() {
        return mCancelBoolean.get();
    }

    @Override
    public boolean isDone() {
        return mCancelBoolean.get();
    }

    @Override
    public CharSequence get() throws InterruptedException, ExecutionException {
        return view.getText();
    }

    @Override
    public CharSequence get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return  view.getText();
    }

}

