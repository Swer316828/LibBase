package com.sfh.lib.ui.dialog;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.sfh.lib.R;
import com.sfh.lib.mvvm.IDialog;

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
        this.showToast(view, msg, type, Snackbar.LENGTH_SHORT);
    }


    private void showToast(View view, CharSequence msg, int type, int duration) {
        Snackbar snackbar = Snackbar.make(view, msg, duration);
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

        snackbar.getView().setBackgroundColor(color);

        TextView tvContent = snackbar.getView().findViewById(R.id.snackbar_text);
        tvContent.setTextColor(colorTxt);
        tvContent.setText(msg);
        tvContent.setCompoundDrawablePadding(15);
        tvContent.setGravity(Gravity.CENTER | Gravity.LEFT);
        tvContent.setCompoundDrawablesWithIntrinsicBounds(res, 0, 0, 0);
        snackbar.show();
    }


    @Override
    public void onDestory()  {
        this.hideDialog();
        this.hideLoading();
        this.mActivity.clear();
        this.mActivity = null;
        this.mToastDialog = null;
        this.mWaitDialog = null;
    }
}
