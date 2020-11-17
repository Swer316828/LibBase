package com.sfh.lib.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sfh.lib.MVCache;
import com.sfh.lib.event.EventManager;
import com.sfh.lib.mvvm.BaseViewModel;
import com.sfh.lib.mvvm.IDialog;
import com.sfh.lib.mvvm.IUIListener;
import com.sfh.lib.mvvm.ViewModelFactoty;
import com.sfh.lib.mvvm.UIRegistry;

import java.util.concurrent.Future;

public abstract class MVVMFragment extends Fragment implements IDialog {

    protected final UIRegistry mUIRegistry = new UIRegistry(this);

    protected ViewModelProvider viewModelProvider;

    protected View rootView;

    protected IDialog dialog;

    public abstract int getLayout();

    public abstract void initData(View view);

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean initCreateView = false;
        if (this.rootView != null) {
            ViewGroup parent = (ViewGroup) this.rootView.getParent();
            if (parent != null) {
                parent.removeView(this.rootView);
            }

            initCreateView = false;
        } else if (this.getLayout() > 0) {
            this.rootView = inflater.inflate(this.getLayout(), container, false);
            initCreateView = true;
        }

        if (initCreateView) {
            mUIRegistry.observe(this, mUIListener);
            this.initData(this.rootView);
        }

        return this.rootView;
    }

    public void onDestroy() {
        super.onDestroy();
        this.viewModelProvider = null;
        this.dialog = null;
        this.rootView = null;
    }


    public final void activateLifecycleEvent() {
        Lifecycle lifecycle = this.getLifecycle();
        if (lifecycle instanceof LifecycleRegistry) {

            ((LifecycleRegistry) lifecycle).handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        }
    }


    @Nullable
    public Context getContext() {
        Context context = super.getContext();
        if (context == null) {
            context = super.getActivity();
            if (context == null) {
                context = MVCache.getApplication();
            }
        }
        return context;
    }

    @Nullable
    public <T extends BaseViewModel> T getViewModel(@NonNull Class<T> cls) {

        if (viewModelProvider == null) {
            viewModelProvider = new ViewModelProvider(this, new ViewModelFactoty(mUIRegistry.getLiveData()));
        }
        return viewModelProvider.get(cls);
    }

    protected IUIListener mUIListener = new IUIListener() {

        @Override
        public void call(String method, Object... args) {

            mUIRegistry.call(MVVMFragment.this, method, args);
        }
    };

    @Override
    public void showLoading(boolean cancel) {
        if (!this.isAdded()) {
            return;
        }
        this.getDialog().showLoading(cancel);
    }

    @Override
    public void hideLoading() {
        if (!this.isAdded()) {
            return;
        }
        this.getDialog().hideLoading();
    }

    @Override
    public void showDialog(DialogBuilder dialog) {
        if (!this.isAdded()) {
            return;
        }
        this.getDialog().showDialog(dialog);
    }

    @Override
    public void showDialogToast(CharSequence msg) {
        if (!this.isAdded()) {
            return;
        }
        this.getDialog().showDialogToast(msg);
    }

    @Override
    public void showToast(CharSequence msg) {
        if (!this.isAdded()) {
            return;
        }
        this.getDialog().showToast(msg);
    }

    public void putFuture(Future future) {
        this.mUIRegistry.putFuture(future);
    }

    public <T> boolean postEvent(T t) {
        return EventManager.postEvent(t);
    }


    public IDialog getDialog() {

        if (dialog == null) {
            FragmentActivity fragmentActivity = this.getActivity();

            if (fragmentActivity != null && fragmentActivity instanceof MVVMActivity) {

                MVVMActivity mvvmActivity = (MVVMActivity) fragmentActivity;
                dialog = mvvmActivity.getDialog();

            } else {

                AppDialog appDialog = new AppDialog(getActivity());
                this.getLifecycle().addObserver(appDialog);
                dialog = appDialog;
            }
        }
        return dialog;
    }
}
