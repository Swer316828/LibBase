package com.sfh.lib.ui.dialog;

import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.support.annotation.IntegerRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;

/**
 * 功能描述:提示对话框接口
 *
 * @date 2016/11/14
 */

public class DialogBuilder {

    interface DialogInterface {

        void dismiss();

        interface OnClickListener {
            void onClick(DialogFragment dialog, int which);
        }
    }

    public CharSequence title;
    @ColorRes
    public int titleTextColor;
    @DimenRes
    public int titleTextSize;

    public CharSequence leftText;
    @ColorRes
    public int leftTextColor;
    @DimenRes
    public int leftTextSize;

    public CharSequence rightText;
    @ColorRes
    public int rightTextColor;
    @DimenRes
    public int rightTextSize;


    public CharSequence message;
    @ColorRes
    public int messageTextColor;
    @DimenRes
    public int messageTextSize;

    public DialogInterface.OnClickListener leftListener;
    public DialogInterface.OnClickListener rightListener;

    /**
     * 是否可以取消
     */
    public boolean isCancelable = true;

    /**
     * 内容对齐方式
     */
    public int gravity = Gravity.LEFT;

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public void setTitleColor(CharSequence title, @ColorRes int titleTextColor) {
        this.title = title;
        this.titleTextColor = titleTextColor;
    }

    public void setTitleSize(CharSequence title, @DimenRes int titleTextSize) {
        this.title = title;
        this.titleTextSize = titleTextSize;
    }

    public void setTitleSizeColor(CharSequence title, @ColorRes int titleTextColor, @DimenRes int titleTextSize) {
        this.title = title;
        this.titleTextColor = titleTextColor;
        this.titleTextSize = titleTextSize;
    }

    public void setLeftText(CharSequence leftText) {
        this.leftText = leftText;
    }

    public void setLeftTextColor(CharSequence leftText, @ColorRes int leftTextColor) {
        this.leftText = leftText;
        this.leftTextColor = leftTextColor;
    }

    public void setLeftTextSize(CharSequence leftText, @DimenRes int leftTextSize) {
        this.leftText = leftText;
        this.leftTextSize = leftTextSize;
    }

    public void setLeftTextColorSize(CharSequence leftText, @ColorRes int leftTextColor, @DimenRes int leftTextSize) {
        this.leftText = leftText;
        this.leftTextColor = leftTextColor;
        this.leftTextSize = leftTextSize;
    }


    public void setRightText(CharSequence rightText) {
        this.rightText = rightText;
    }

    public void setRightTextColor(CharSequence rightText, @ColorRes int rightTextColor) {
        this.rightText = rightText;
        this.rightTextColor = rightTextColor;
    }

    public void setRightTextSize(CharSequence rightText, @DimenRes int rightTextSize) {
        this.rightText = rightText;
        this.rightTextSize = rightTextSize;
    }

    public void setRightTextColorSize(CharSequence rightText, @ColorRes int rightTextColor, @DimenRes int rightTextSize) {
        this.rightText = rightText;
        this.rightTextColor = rightTextColor;
        this.rightTextSize = rightTextSize;
    }

    public void setMessage(CharSequence message) {
        this.message = message;
    }
    public void setMessageGravity(CharSequence message,int gravity) {
        this.message = message;
        this.gravity = gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public void setMessageTextColor(CharSequence message, @ColorRes int messageTextColor) {
        this.message = message;
        this.messageTextColor = messageTextColor;
    }

    public void setMessageTextSize(CharSequence message, @DimenRes int messageTextSize) {
        this.message = message;
        this.messageTextSize = messageTextSize;
    }

    public void setMessageTextColorSize(CharSequence message, @ColorRes int messageTextColor, @DimenRes int messageTextSize) {
        this.message = message;
        this.messageTextColor = messageTextColor;
        this.messageTextSize = messageTextSize;
    }

    public void setLeftListener(DialogInterface.OnClickListener leftListener) {
        this.leftListener = leftListener;
    }

    public void setRightListener(DialogInterface.OnClickListener rightListener) {
        this.rightListener = rightListener;
    }

    public void setCancelable(boolean cancelable) {
        this.isCancelable = cancelable;
    }


    public void build(FragmentActivity activity) {
        ToastDialog toastDialog = ToastDialog.newToastDialog();
        toastDialog.setData(this);
        toastDialog.show(activity);
    }
}
