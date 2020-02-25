package com.sfh.lib.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.sfh.lib.IViewLinstener;
import com.sfh.lib.event.BusEventManager;
import com.sfh.lib.mvvm.BaseViewModel;
import com.sfh.lib.mvvm.LiveDataManger;

import java.util.concurrent.Future;

public class MVVMActivity extends FragmentActivity implements IViewLinstener {

    private static final String BUNDLE_FRAGMENTS_KEY = "android:support:fragments";
    protected LiveDataManger liveDataManger;
    protected IDialog dialog;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && this.clearFragmentsTag()) {
            savedInstanceState.remove(BUNDLE_FRAGMENTS_KEY);
        }

        super.onCreate(savedInstanceState);
        if (this.liveDataManger == null) {
            this.liveDataManger = new LiveDataManger(this);
        }

    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null && this.clearFragmentsTag()) {
            outState.remove(BUNDLE_FRAGMENTS_KEY);
        }
    }

    protected boolean clearFragmentsTag() {
        return true;
    }


    @MainThread
    public final <T extends BaseViewModel> T getViewModel(@NonNull Class<T> cls) {
        if (this.liveDataManger == null) {
            this.liveDataManger = new LiveDataManger(this);
        }
        return this.liveDataManger.getViewModel(cls);
    }


    protected IDialog onCreateDialog() {
        return new AppDialog(this);
    }


    public final void showDialog(DialogBuilder dialog) {
        if (this.liveDataManger != null) {
            this.liveDataManger.showDialog(dialog);
        }
    }

    public final void showDialogToast(CharSequence msg) {
        DialogBuilder dialog = new DialogBuilder();
        dialog.setTitle("提示");
        dialog.setHideCancel(true);
        dialog.setMessage(msg);
        this.showDialog(dialog);
    }

    public final void showToast(CharSequence msg) {
        if (this.liveDataManger != null) {
            this.liveDataManger.showToast(msg);
        }
    }

    public final void putDisposable(Future future) {
        if (this.liveDataManger != null) {
            this.liveDataManger.putFuture(future);
        }
    }

    public final <T> void postEvent(T t) {
        BusEventManager.postEvent(t);
    }


    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public IDialog getDialog() {
        if (dialog == null) {
            dialog = new AppDialog(this);
        }
        return dialog;
    }

}
