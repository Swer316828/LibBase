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
public class TextViewBeforeTextChangeEventObservable extends Observable<TextViewBeforeTextChangeEvent> {


    private final TextView view;

    TextViewBeforeTextChangeEventObservable(TextView view) {

        this.view = view;
    }

    @Override
    protected void subscribeActual(Observer<? super TextViewBeforeTextChangeEvent> observer) {

        Listener listener = new Listener (view, observer);
        observer.onSubscribe (listener);
        view.addTextChangedListener (listener);
    }

    static final class Listener extends MainThreadDisposable implements TextWatcher {

        private final TextView view;

        private final Observer<? super TextViewBeforeTextChangeEvent> observer;

        Listener(TextView view, Observer<? super TextViewBeforeTextChangeEvent> observer) {

            this.view = view;
            this.observer = observer;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            if (!isDisposed ()) {
                observer.onNext (new TextViewBeforeTextChangeEvent (view, s, start, count, after));
            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        protected void onDispose() {

            view.removeTextChangedListener (this);
        }
    }
}
