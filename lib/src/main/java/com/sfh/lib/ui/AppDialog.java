package com.sfh.lib.ui;

import android.app.Activity;
import android.view.Gravity;
import android.widget.Toast;

import com.sfh.lib.mvvm.IDialog;

import java.lang.ref.WeakReference;

public class AppDialog implements IDialog {

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
    public synchronized void showLoading(boolean cancel) {
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
    public synchronized void showDialog(DialogBuilder dialog) {
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
    public void onDestory() {
        this.hideLoading();
        mActivity.clear();
        if (mToastDialog != null) {
            mToastDialog.dismiss();
        }
        mToastDialog = null;
        mWaitDialog = null;
    }
}
