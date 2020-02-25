package com.sfh.lib.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sfh.lib.AppCacheManager;
import com.sfh.lib.IViewLinstener;
import com.sfh.lib.event.BusEventManager;
import com.sfh.lib.mvvm.BaseViewModel;
import com.sfh.lib.mvvm.LiveDataManger;

import java.util.concurrent.Future;

public abstract class MVVMFragment extends Fragment implements IViewLinstener {

    protected LiveDataManger liveDataManger;

    protected View mRoot;

    public abstract int getLayout();

    public abstract void initData(View var1);

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean initCreateView = false;
        if (this.mRoot != null) {
            ViewGroup parent = (ViewGroup) this.mRoot.getParent();
            if (parent != null) {
                parent.removeView(this.mRoot);
            }

            initCreateView = false;
        } else if (this.getLayout() > 0) {
            this.mRoot = inflater.inflate(this.getLayout(), container, false);
            initCreateView = true;
        }

        if (this.liveDataManger == null) {
            this.liveDataManger = new LiveDataManger(this);
        }

        if (initCreateView) {
            this.initData(this.mRoot);
        }

        return this.mRoot;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mRoot = null;
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
                context = AppCacheManager.getInitialization().getApplication();
            }
        }
        return context;
    }

    public final <T extends BaseViewModel> T getViewModel(Class<T> cls) {

        if (this.liveDataManger == null) {
            this.liveDataManger = new LiveDataManger(this);
        }

        return this.liveDataManger.getViewModel(cls);
    }


    public final void showDialog(DialogBuilder dialog) {
        if (this.liveDataManger != null) {
            this.liveDataManger.showDialog(dialog);
        }
    }

    public final void showDialogToast(CharSequence msg) {
        DialogBuilder dialog = new DialogBuilder();
        dialog.setTitle("提示");
        dialog.setHideCancel(false);
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
    public IDialog getDialog() {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null && fragmentActivity instanceof MVVMActivity) {
            MVVMActivity mvvmActivity = (MVVMActivity) fragmentActivity;
            return mvvmActivity.getDialog();
        }
        return null;
    }
}
