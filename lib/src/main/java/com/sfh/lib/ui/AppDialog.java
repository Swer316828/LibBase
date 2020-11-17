package com.sfh.lib.ui;

import android.app.Activity;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.view.Gravity;
import android.widget.Toast;

import com.sfh.lib.mvvm.IDialog;

import java.lang.ref.WeakReference;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;

public class AppDialog implements IDialog, GenericLifecycleObserver {

    private ToastDialog mToastDialog;

    private WaitDialog mWaitDialog;

    private WeakReference<Activity> mActivity;

    /**
     * 不属于当前类，防止
     */
    private static Toast mToast;

    public AppDialog(Activity activity) {
        this.mActivity = new WeakReference<>(activity);
    }


    @Override
    public void showLoading(boolean cancel) {
        Activity activity = this.mActivity.get();

        if (activity == null) {
            return;
        }
        if (mWaitDialog == null) {
            mWaitDialog = WaitDialog.newToastDialog(activity);
        }

        mWaitDialog.setCancelable(cancel);
        mWaitDialog.show();
    }

    @Override
    public void hideLoading() {
        Activity activity = this.mActivity.get();

        if (activity == null) {
            return;
        }
        if (mWaitDialog == null || !mWaitDialog.isShowing()) {
            return;
        }
        mWaitDialog.dismiss();
    }

    @Override
    public void showDialog(DialogBuilder dialog) {
        Activity activity = this.mActivity.get();

        if (activity == null) {
            return;
        }
        if (mToastDialog == null) {
            mToastDialog = ToastDialog.newToastDialog(activity);
        }
        mToastDialog.show(dialog);
    }

    @Override
    public void showDialogToast(CharSequence msg) {

    }

    @Override
    public void showToast(CharSequence msg) {

    }


    @Override
    public void showToast(CharSequence msg, int duration) {
        Activity activity = this.mActivity.get();

        if (activity == null) {
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(activity, msg, duration);
            mToast.setGravity(Gravity.CENTER, 0, 0);
        }

        mToast.setText(msg);
        mToast.setDuration(duration);
        mToast.show();
    }


    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        if (source.getLifecycle().getCurrentState() == DESTROYED) {
            this.hideLoading();
            mActivity.clear();
            if (mToastDialog != null) {
                mToastDialog.dismiss();
            }
            mToastDialog = null;
            mWaitDialog = null;
        }

    }
}
