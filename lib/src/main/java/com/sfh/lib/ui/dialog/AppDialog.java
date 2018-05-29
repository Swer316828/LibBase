package com.sfh.lib.ui.dialog;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;


import com.sfh.lib.R;
import com.sfh.lib.mvp.IDialog;
import com.sfh.lib.mvp.IPresenter;

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
    public IPresenter getPresenter() {
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
        if (this.waitDialog == null || !this.waitDialog.isShowing()) {
            return;
        }
        this.waitDialog.dismiss();
    }

    @Override
    public void showDialog(DialogBuilder dialog) {
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
        this.showToast(msg, 0);
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
        int color = Color.WHITE;
        int res = R.drawable.base_info;
        int colorTxt =  Color.BLACK;

        switch (type) {
            case 0: {
                color = Color.WHITE;
                colorTxt =  Color.BLACK;
                res = R.drawable.base_info;
                break;
            }
            case 1: {
                color = Color.parseColor("#a8a809");
                colorTxt =  Color.BLACK;
                res = R.drawable.base_warring;
                break;
            }
            case 2: {
                color = Color.RED;
                colorTxt =  Color.WHITE;
                res = R.drawable.base_fail;
                break;
            }
            default:
                break;
        }

        mSnackbar.getView().setBackgroundColor(color);

        TextView tvContent = mSnackbar.getView().findViewById(R.id.snackbar_text);
        tvContent.setTextColor(colorTxt);
        tvContent.setText(msg);
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
