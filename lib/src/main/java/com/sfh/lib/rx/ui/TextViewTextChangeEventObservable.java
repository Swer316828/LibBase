package com.sfh.lib.rx.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/4/8
 */
public class TextViewTextChangeEventObservable
        extends Observable<TextViewTextChangeEvent> {
    private final TextView view;

    TextViewTextChangeEventObservable(TextView view) {
        this.view = view;
    }

    @Override
    protected void subscribeActual(Observer<? super TextViewTextChangeEvent> observer) {
        Listener listener = new Listener(view, observer);
        observer.onSubscribe(listener);
        view.addTextChangedListener(listener);
    }

    final static class Listener extends MainThreadDisposable implements TextWatcher {
        private final TextView view;
        private final Observer<? super TextViewTextChangeEvent> observer;

        Listener(TextView view, Observer<? super TextViewTextChangeEvent> observer) {
            this.view = view;
            this.observer = observer;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!isDisposed()) {
                observer.onNext(new TextViewTextChangeEvent(view, s, start, before, count));
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }

        @Override
        protected void onDispose() {
            view.removeTextChangedListener(this);
        }
    }
}
