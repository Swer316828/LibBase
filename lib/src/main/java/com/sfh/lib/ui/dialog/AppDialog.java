package com.sfh.lib.ui.dialog;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.sfh.lib.AppCacheManager;
import com.sfh.lib.mvvm.IDialog;
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
    public void showLoading(boolean cancel) {
        if (this.mActivity == null || this.mActivity.get() == null) {
            return;
        }
        if (this.mWaitDialog == null) {
            this.mWaitDialog = WaitDialog.newToastDialog();
        }
        this.mWaitDialog.setCancelable(cancel);
        this.mWaitDialog.show(this.mActivity.get());
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
    public void showDialog(DialogBuilder dialog) {
        if (this.mActivity == null || this.mActivity.get() == null) {
            return;
        }
        if (this.mToastDialog == null) {
            this.mToastDialog = ToastDialog.newToastDialog();
        }

        this.mToastDialog.setData(dialog);
        this.mToastDialog.show(this.mActivity.get());
    }

    @Override
    public void hideDialog() {
        if (this.mToastDialog == null) {
            return;
        }
        this.mToastDialog.dismiss();
    }

    @Override
    public void showToast(CharSequence msg) {
        this.showToast(msg, 0);
    }

    @Override
    public void showToast(CharSequence msg, int type) {
        if (this.mActivity == null || this.mActivity.get() == null) {
            return;
        }
        View view = this.mActivity.get().getWindow().getDecorView();
        this.showToast(view, msg, type, Toast.LENGTH_SHORT);
    }


    private void showToast(View view, CharSequence msg, int type, int duration) {

        if (mToast == null) {
            mToast = Toast.makeText(AppCacheManager.getApplication(), msg, duration);
            UtilsToast.hook(mToast);
        }

        mToast.setText(msg);
        mToast.setDuration(duration);
        mToast.show();
    }


    @Override
    public void onDestory() {
        this.hideDialog();
        this.hideLoading();
        this.mActivity.clear();
        this.mActivity = null;
        this.mToastDialog = null;
        this.mWaitDialog = null;
    }
}
