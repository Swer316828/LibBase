package com.sfh.lib.ui;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.sfh.lib.event.EventManager;
import com.sfh.lib.event.IEventListener;
import com.sfh.lib.mvvm.BaseViewModel;
import com.sfh.lib.mvvm.IDialog;
import com.sfh.lib.mvvm.IUIListener;
import com.sfh.lib.mvvm.UIRegistry;
import com.sfh.lib.mvvm.ViewModelFactoty;

import java.util.List;

public class MVVMActivity extends FragmentActivity implements IDialog {

    private static final String BUNDLE_FRAGMENTS_KEY = "android:support:fragments";

    protected final UIRegistry mUIRegistry = new UIRegistry(this);

    private ViewModelProvider mViewModelProvider;

    protected IDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null && this.clearFragmentsTag()) {
            savedInstanceState.remove(BUNDLE_FRAGMENTS_KEY);
        }
        super.onCreate(savedInstanceState);
        this.getLifecycle().addObserver(mUIRegistry);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null && this.clearFragmentsTag()) {
            outState.remove(BUNDLE_FRAGMENTS_KEY);
        }
    }

    public boolean clearFragmentsTag() {
        return true;
    }



    @Nullable
    public <T extends BaseViewModel> T getViewModel(@NonNull Class<T> cls) {

        if (mViewModelProvider == null) {
            mViewModelProvider = new ViewModelProvider(this, new ViewModelFactoty(mUIListener));
        }
        return mViewModelProvider.get(cls);
    }

    protected IUIListener mUIListener = new IUIListener() {

        @Override
        public void call(String method, Object... args) {

            mUIRegistry.call(MVVMActivity.this, method, args);
        }

        @Override
        public LifecycleOwner getLifecycleOwner() {
            return MVVMActivity.this;
        }

        @Override
        public IDialog getDialog() {
            return MVVMActivity.this.getDialog();
        }
    };

    @Override
    public void showLoading(boolean cancel) {
        if (this.isFinishing()) {
            return;
        }
        this.getDialog().showLoading(cancel);
    }

    @Override
    public void hideLoading() {
        if (this.isFinishing()) {
            return;
        }
        this.getDialog().hideLoading();
    }

    @Override
    public void showDialog(DialogBuilder dialog) {
        if (this.isFinishing()) {
            return;
        }
        this.getDialog().showDialog(dialog);
    }

    @Override
    public void showDialogToast(CharSequence msg) {
        if (this.isFinishing()) {
            return;
        }
        this.getDialog().showDialogToast(msg);
    }

    @Override
    public void showToast(CharSequence msg) {
        if (this.isFinishing()) {
            return;
        }
        this.getDialog().showToast(msg);
    }

    @Override
    public void showToast(CharSequence msg, int duration) {
        if (this.isFinishing()) {
            return;
        }
        this.getDialog().showToast(msg, duration);
    }


    public <T> boolean postEvent(T t) {
        return EventManager.postEvent(t);
    }

    public IDialog getDialog() {
        if (dialog == null) {
            dialog = new AppDialog(this);
            this.getLifecycle().addObserver(dialog);
        }
        return dialog;
    }


}
