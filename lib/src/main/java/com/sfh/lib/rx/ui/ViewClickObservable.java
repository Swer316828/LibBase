package com.sfh.lib.rx.ui;

import android.os.Looper;
import android.view.View;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;
import io.reactivex.disposables.Disposables;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/4/8
 */
public class ViewClickObservable extends Observable<Object> {

    private final View view;

    ViewClickObservable(View view) {

        this.view = view;
    }

    @Override
    protected void subscribeActual(Observer<? super Object> observer) {

        if (!checkMainThread (observer)) {
            return;
        }
        Listener listener = new Listener (view, observer);
        observer.onSubscribe (listener);
        view.setOnClickListener (listener);
    }

    private  boolean checkMainThread(Observer<?> observer) {

        if (Looper.myLooper () != Looper.getMainLooper ()) {
            observer.onSubscribe (Disposables.empty ());
            observer.onError (new IllegalStateException (
                    "Expected to be called on the main thread but was " + Thread.currentThread ().getName ()));
            return false;
        }
        return true;
    }
    static final class Listener extends MainThreadDisposable implements View.OnClickListener {

        private final View view;

        private final Observer<? super Object> observer;

        Listener(View view, Observer<? super Object> observer) {

            this.view = view;
            this.observer = observer;
        }

        @Override
        public void onClick(View v) {

            if (!isDisposed ()) {
                observer.onNext(v);
            }
        }

        @Override
        protected void onDispose() {

            view.setOnClickListener (null);
        }
    }
}
