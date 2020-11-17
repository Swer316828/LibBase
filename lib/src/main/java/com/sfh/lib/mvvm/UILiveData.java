package com.sfh.lib.mvvm;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sfh.lib.utils.SafeIterableMap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;
import static android.arch.lifecycle.Lifecycle.State.STARTED;

 class UILiveData {

    private SafeIterableMap<IUIListener, ObserverWrapper> mObservers =
            new SafeIterableMap<>();
    private LinkedHashMap<String, Object[]> mData = new LinkedHashMap<>(7);
    private boolean mDispatchingValue;
    private boolean mDispatchInvalidated;

    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull IUIListener observer) {
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
        ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
        if (existing != null && !existing.isAttachedTo(owner)) {
            throw new IllegalArgumentException("Cannot add the same observer"
                    + " with different lifecycles");
        }
        if (existing != null) {
            return;
        }
        owner.getLifecycle().addObserver(wrapper);
    }

    class LifecycleBoundObserver extends ObserverWrapper implements GenericLifecycleObserver {
        @NonNull
        final LifecycleOwner mOwner;

        LifecycleBoundObserver(@NonNull LifecycleOwner owner, IUIListener observer) {
            super(observer);
            mOwner = owner;
        }

        @Override
        boolean shouldBeActive() {
            return mOwner.getLifecycle().getCurrentState().isAtLeast(STARTED);
        }

        @Override
        public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
            if (mOwner.getLifecycle().getCurrentState() == DESTROYED) {
                removeObserver(mObserver);
                return;
            }
            activeStateChanged(shouldBeActive());
        }

        @Override
        boolean isAttachedTo(LifecycleOwner owner) {
            return mOwner == owner;
        }

        @Override
        void detachObserver() {
            mOwner.getLifecycle().removeObserver(this);
        }
    }

    private abstract class ObserverWrapper {
        final IUIListener mObserver;
        boolean mActive;

        ObserverWrapper(IUIListener observer) {
            mObserver = observer;
        }

        abstract boolean shouldBeActive();

        boolean isAttachedTo(LifecycleOwner owner) {
            return false;
        }

        void detachObserver() {
        }

        void activeStateChanged(boolean newActive) {
            if (newActive == mActive) {
                return;
            }
            // immediately set active state, so we'd never dispatch anything to inactive
            // owner
            mActive = newActive;
            if (mActive) {
                onActive();
            }
            if (!mActive) {
                onInactive();
            }

            if (mActive) {
                dispatchingValue(this);
            }
        }
    }

    public void onActive() {
    }

    public void onInactive() {
    }


    private void dispatchingValue(@Nullable ObserverWrapper initiator) {
        if (mDispatchingValue) {
            mDispatchInvalidated = true;
            return;
        }
        mDispatchingValue = true;
        do {
            mDispatchInvalidated = false;
            if (initiator != null) {
                considerNotify(initiator);
                initiator = null;
            } else {
                for (Iterator<Map.Entry<IUIListener, ObserverWrapper>> iterator =
                     mObservers.iteratorWithAdditions(); iterator.hasNext(); ) {
                    considerNotify(iterator.next().getValue());
                    if (mDispatchInvalidated) {
                        break;
                    }
                }
            }
        } while (mDispatchInvalidated);
        mDispatchingValue = false;
    }

    private void considerNotify(ObserverWrapper observer) {
        if (!observer.mActive) {
            return;
        }

        if (!observer.shouldBeActive()) {
            observer.activeStateChanged(false);
            return;
        }
        if (mData.isEmpty()){
            return;
        }
        for (Iterator<Map.Entry<String, Object[]>> iterator =
             mData.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Object[]> entry = iterator.next();
            iterator.remove();
            observer.mObserver.call(entry.getKey(), entry.getValue());
        }
    }

    public void removeObserver(@NonNull final IUIListener observer) {

        ObserverWrapper removed = mObservers.remove(observer);
        if (removed == null) {
            return;
        }
        removed.detachObserver();
        removed.activeStateChanged(false);
    }

    public void call(String method, Object... args) {
        mData.put(method, args);
        dispatchingValue(null);
    }

}
