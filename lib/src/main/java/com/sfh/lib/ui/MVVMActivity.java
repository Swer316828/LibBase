package com.sfh.lib.ui;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.sfh.lib.IViewLinstener;
import com.sfh.lib.event.BusEventManager;
import com.sfh.lib.mvvm.BaseLiveData;
import com.sfh.lib.mvvm.BaseViewModel;
import com.sfh.lib.mvvm.ILiveData;
import com.sfh.lib.mvvm.UIRegistry;
import com.sfh.lib.mvvm.ViewModelFactoty;

import java.util.concurrent.Future;

public abstract class MVVMActivity extends FragmentActivity implements IViewLinstener, Observer {

    private static final String BUNDLE_FRAGMENTS_KEY = "android:support:fragments";
    protected UIRegistry liveDataManger;
    protected IDialog dialog;

    private final ILiveData mLiveData = new BaseLiveData();

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && this.clearFragmentsTag()) {
            savedInstanceState.remove(BUNDLE_FRAGMENTS_KEY);
        }
        super.onCreate(savedInstanceState);
        if (liveDataManger == null) {
            liveDataManger = new UIRegistry(this);
        }

        mLiveData.observe(this,this);

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


    private ViewModelProvider mViewModelProvider;

    @Nullable
    public final <T extends BaseViewModel> T getViewModel(@NonNull Class<T> cls) {

        if (mViewModelProvider == null){
            mViewModelProvider = new ViewModelProvider(this,new ViewModelFactoty(mLiveData));
        }
        return mViewModelProvider.get(cls);
    }



    public IDialog onCreateDialog() {
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

    public final void putFuture(Future future) {
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
