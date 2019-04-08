package com.sfh.lib.ui.dialog;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.sfh.lib.AppCacheManager;
import com.sfh.lib.utils.UtilsToast;

import java.lang.ref.WeakReference;

/**
 * 功能描述:提示对话框接口
 *
 * @date 2016/11/14
 */

public class AppDialog implements IDialog {


    private ToastDialog mToastDialog;

    private WaitDialog mWaitDialog;

    private WeakReference<FragmentActivity> mActivity;

    /**
     * 不属于当前类，防止
     */
    private static Toast mToast;

    public AppDialog(FragmentActivity activity) {
        this.mActivity = new WeakReference<>(activity);
    }


    @Override
    public synchronized void showLoading(boolean cancel) {
        if (this.mActivity == null || this.mActivity.get() == null) {
            return;
        }
        if (this.mWaitDialog == null) {
            this.mWaitDialog = WaitDialog.newToastDialog(this.mActivity.get());
        }
        this.mWaitDialog.setCancelable(cancel);
        this.mWaitDialog.show();
    }

    @Override
    public void hideLoading() {
        if (this.mActivity == null || this.mActivity.get() == null) {
            return;
        }
        if (this.mWaitDialog == null || !this.mWaitDialog.isShowing()) {
            return;
        }
        this.mWaitDialog.dismiss();
    }

    @Override
    public synchronized void showDialog(DialogBuilder dialog) {
        if (this.mActivity == null || this.mActivity.get() == null) {
            return;
        }
        if (this.mToastDialog == null) {
            this.mToastDialog = ToastDialog.newToastDialog(this.mActivity.get());
        }
        this.mToastDialog.show(dialog);
    }

    @Override
    public void showToast(CharSequence msg, int... duration) {
        if (this.mActivity == null || this.mActivity.get() == null) {
            return;
        }

        int time = (duration == null || duration.length == 0) ? Toast.LENGTH_SHORT : duration[0];
        if (mToast == null) {

            mToast = Toast.makeText(AppCacheManager.getApplication(), msg, time);
            UtilsToast.hook(mToast);
        }

        mToast.setText(msg);
        mToast.setDuration(time);
        mToast.show();
    }


    @Override
    public void onDestory() {
        this.hideLoading();
        this.mActivity.clear();
        this.mActivity = null;

        if (this.mToastDialog != null) {
            this.mToastDialog.dismiss();
            this.mToastDialog = null;
        }
        this.mWaitDialog = null;
    }
}
