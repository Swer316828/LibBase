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
public class TextViewAfterTextChangeEventObservable extends Observable<CharSequence> {

    private final TextView view;

    TextViewAfterTextChangeEventObservable(TextView view) {

        this.view = view;
    }

    @Override
    protected void subscribeActual(Observer<? super CharSequence> observer) {

        Listener listener = new Listener (this.view, observer);
        observer.onSubscribe (listener);
        this.view.addTextChangedListener (listener);
    }

    static final class Listener extends MainThreadDisposable implements TextWatcher {

        private final TextView view;

        private final Observer<? super CharSequence> observer;

        Listener(TextView view, Observer<? super CharSequence> observer) {

            this.view = view;
            this.observer = observer;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            if (!isDisposed ()) {
                observer.onNext (s);
            }
        }

        @Override
        protected void onDispose() {

            view.removeTextChangedListener (this);
        }
    }
}
