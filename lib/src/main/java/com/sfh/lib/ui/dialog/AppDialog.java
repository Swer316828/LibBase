package com.sfh.lib.ui.dialog;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;


import com.sfh.lib.R;
import com.sfh.lib.mvp.IPresenter;
import com.sfh.lib.mvp.IView;
import com.sfh.lib.ui.DialogView;
import com.sfh.lib.ui.IDialog;

import java.lang.ref.WeakReference;

/**
 * 功能描述:提示对话框接口
 *
 * @date 2016/11/14
 */

public class AppDialog implements IDialog {


    private ToastDialog toastDialog;

    private WaitDialog waitDialog;

    private WeakReference<FragmentActivity> activity;

    public AppDialog(FragmentActivity activity) {
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public <T extends IView> IPresenter<T> getPresenter() {
        return null;
    }

    @Override
    public void showLoading(boolean cancel) {
        if (this.activity == null || this.activity.get() == null) {
            return;
        }
        if (this.waitDialog == null) {
            this.waitDialog = WaitDialog.newToastDialog();
        }
        this.waitDialog.setCancelable(cancel);
        this.waitDialog.show(this.activity.get());
    }

    @Override
    public void hideLoading() {
        if (this.activity == null || this.activity.get() == null) {
            return;
        }
        if (this.waitDialog == null || !this.waitDialog.isVisible()) {
            return;
        }
        this.waitDialog.dismiss();
    }

    @Override
    public void showDialog(DialogView dialog) {
        if (this.activity == null || this.activity.get() == null) {
            return;
        }
        if (this.toastDialog == null) {
            this.toastDialog = ToastDialog.newToastDialog();
        }

        this.toastDialog.setData(dialog);
        this.toastDialog.show(this.activity.get());
    }

    @Override
    public void hideDialog() {
        if (this.toastDialog == null) {
            return;
        }
        this.toastDialog.dismiss();
    }

    @Override
    public void showToast(CharSequence msg) {
        if (this.activity == null || this.activity.get() == null) {
            return;
        }
        View view = this.activity.get().getCurrentFocus();
        this.showToast(view, msg, 0, Snackbar.LENGTH_SHORT);
    }

    @Override
    public void showToast(CharSequence msg, int type) {
        if (this.activity == null || this.activity.get() == null) {
            return;
        }
        View view = this.activity.get().getWindow().getDecorView();
        this.showToast(view, msg, type, Snackbar.LENGTH_SHORT);
    }


    private void showToast(View view, CharSequence msg, int type, int duration) {
        Snackbar mSnackbar = Snackbar.make(view, msg, duration);
        int color = type == 1 ? Color.YELLOW :type == 2 ? Color.RED : Color.WHITE;
        int res = type == 1 ? R.drawable.base_warring : type ==2 ? R.drawable.base_fail : R.drawable.base_info;

        mSnackbar.getView().setBackgroundColor(color);
        mSnackbar.setText(msg);

        TextView tvContent = mSnackbar.getView().findViewById(R.id.snackbar_text);
        tvContent.setTextColor(Color.BLACK);
        tvContent.setCompoundDrawablePadding(15);
        tvContent.setGravity(Gravity.CENTER | Gravity.LEFT);
        tvContent.setCompoundDrawablesWithIntrinsicBounds(res, 0, 0, 0);
        mSnackbar.show();
    }


    @Override
    public void onDestroy() {
        this.hideDialog();
        this.hideLoading();
        this.activity.clear();
        this.activity = null;
        this.toastDialog = null;
    }
}
