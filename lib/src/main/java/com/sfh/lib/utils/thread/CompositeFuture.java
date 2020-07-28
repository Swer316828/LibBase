package com.sfh.lib.utils.thread;
/*=============================================================================================
 * 功能描述:
 *---------------------------------------------------------------------------------------------
 *  涉及业务 & 更新说明:
 *
 *
 *--------------------------------------------------------------------------------------------
 *  @Author     SunFeihu 孙飞虎  on  2020/7/28
 *=============================================================================================*/

import android.support.annotation.NonNull;

import com.sfh.lib.utils.OpenHashSet;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class CompositeFuture implements Future  {

    OpenHashSet<Future> resources;

    volatile boolean disposed;

    public CompositeFuture() {
    }

    public boolean add(@NonNull Future future) {
        if (future == null) {
            return true;
        }
        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    OpenHashSet<Future> set = resources;
                    if (set == null) {
                        set = new OpenHashSet<Future>();
                        resources = set;
                    }
                    set.add(future);
                    return true;
                }
            }
        }
        future.cancel(true);
        return false;
    }

    public boolean remove(@NonNull Future disposable) {
        if (delete(disposable)) {
            disposable.cancel(true);
            return true;
        }
        return false;
    }

    public boolean delete(@NonNull Future disposable) {
        if (disposable == null) {
            return true;
        }
        if (disposed) {
            return false;
        }
        synchronized (this) {
            if (disposed) {
                return false;
            }

            OpenHashSet<Future> set = resources;
            if (set == null || !set.remove(disposable)) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        if (disposed) {
            return;
        }
        OpenHashSet<Future> set;
        synchronized (this) {
            if (disposed) {
                return;
            }

            set = resources;
            resources = null;
        }

        dispose(set);
    }

    public int size() {
        if (disposed) {
            return 0;
        }
        synchronized (this) {
            if (disposed) {
                return 0;
            }
            OpenHashSet<Future> set = resources;
            return set != null ? set.size() : 0;
        }
    }

    void dispose(OpenHashSet<Future> set) {
        if (set == null) {
            return;
        }
        Object[] array = set.keys();
        for (Object o : array) {
            if (o instanceof Future) {
                ((Future) o).cancel(true);
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (disposed) {
            return true;
        }
        OpenHashSet<Future> set;
        synchronized (this) {
            if (disposed) {
                return true;
            }
            disposed = true;
            set = resources;
            resources = null;
        }

        dispose(set);
        return true;
    }

    @Override
    public boolean isCancelled() {
        return disposed;
    }

    @Override
    public boolean isDone() {
        return disposed;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return resources;
    }

    @Override
    public Object get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return resources;
    }
}
