package com.sfh.lib.event;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultDisFuture implements FutureEvent {

    private List<IEventListener> mResults;
    private IEventListener mEventResult;
    private AtomicBoolean mAtomicBoolean = new AtomicBoolean(false);

    public DefaultDisFuture(IEventListener eventResult, List<IEventListener> results) {
        super();
        this.mEventResult = eventResult;
        this.mResults = results;
    }

    @Override
    public boolean cancel() {
        // 移除监听
        if (this.mAtomicBoolean.get()) {
            return true;
        }
        if (this.mResults.remove(mEventResult)) {
            mAtomicBoolean.set(true);
        }
        return this.mAtomicBoolean.get();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {

        return this.cancel();
    }

    @Override
    public boolean isCancelled() {
        return this.mAtomicBoolean.get();
    }

    @Override
    public boolean isDone() {
        return this.mAtomicBoolean.get();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return this.mEventResult;
    }

    @Override
    public Object get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.mEventResult;
    }
}
