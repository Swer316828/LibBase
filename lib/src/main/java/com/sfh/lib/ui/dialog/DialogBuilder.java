package com.sfh.lib.ui.dialog;

import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.support.annotation.IntegerRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;

/**
 * 功能描述:提示对话框接口
 *
 * @author Sunfehu
 * @date 2016/11/14
 */

public class DialogBuilder {

    public interface DialogInterface {

        /***
         * 对话框取消
         */
        void dismiss();

        interface OnClickListener {
            /***
             * 对话框按钮点击
             * @param dialog
             * @param which
             */
            void onClick(DialogInterface dialog, int which);
        }
    }

    private CharSequence title;
    @ColorRes
    private int titleTextColor;
    @DimenRes
    private int titleTextSize;

    private CharSequence cancelText;
    @ColorRes
    private int cancelTextColor;
    @DimenRes
    private int cancelTextSize;

    private CharSequence okText;
    @ColorRes
    private int okTextColor;
    @DimenRes
    private int okTextSize;


    private CharSequence message;
    @ColorRes
    private int messageTextColor;
    @DimenRes
    private int messageTextSize;

    private DialogInterface.OnClickListener cancelListener;
    private DialogInterface.OnClickListener okListener;

    private boolean cancelable = true;

    private int gravity = Gravity.LEFT;

    private View view = null;

    private boolean hideCancel;


    public DialogBuilder() {
        this.title = "提示";
        this.cancelText = "取消";
        this.okText = "确定";
    }

    public DialogBuilder setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    public DialogBuilder setTitleColor(CharSequence title, @ColorRes int titleTextColor) {
        this.title = title;
        this.titleTextColor = titleTextColor;
        return this;
    }

    public DialogBuilder setTitleSize(CharSequence title, @DimenRes int titleTextSize) {
        this.title = title;
        this.titleTextSize = titleTextSize;
        return this;
    }

    public DialogBuilder setTitleSizeColor(CharSequence title, @ColorRes int titleTextColor, @DimenRes int titleTextSize) {
        this.title = title;
        this.titleTextColor = titleTextColor;
        this.titleTextSize = titleTextSize;
        return this;
    }

    public DialogBuilder setCancelText(CharSequence cancelText) {
        this.cancelText = cancelText;
        return this;
    }

    public DialogBuilder setCancelTextColor(CharSequence cancelText, @ColorRes int cancelTextColor) {
        this.cancelText = cancelText;
        this.cancelTextColor = cancelTextColor;
        return this;
    }

    public DialogBuilder setCancelTextSize(CharSequence cancelText, @DimenRes int cancelTextSize) {
        this.cancelText = cancelText;
        this.cancelTextSize = cancelTextSize;
        return this;
    }

    public DialogBuilder setCancelTextColorSize(CharSequence cancelText, @ColorRes int cancelTextColor, @DimenRes int cancelTextSize) {
        this.cancelText = cancelText;
        this.cancelTextColor = cancelTextColor;
        this.cancelTextSize = cancelTextSize;
        return this;
    }


    public DialogBuilder setOKText(CharSequence okText) {
        this.okText = okText;
        return this;
    }

    public DialogBuilder setOKTextColor(CharSequence okText, @ColorRes int okTextColor) {
        this.okText = okText;
        this.okTextColor = okTextColor;
        return this;
    }

    public DialogBuilder setOKTextSize(CharSequence okText, @DimenRes int okTextSize) {
        this.okText = okText;
        this.okTextSize = okTextSize;
        return this;
    }

    public DialogBuilder setOKTextColorSize(CharSequence okText, @ColorRes int okTextColor, @DimenRes int okTextSize) {
        this.okText = okText;
        this.okTextColor = okTextColor;
        this.okTextSize = okTextSize;
        return this;
    }

    public DialogBuilder setMessage(CharSequence message) {
        this.message = message;
        return this;
    }

    public DialogBuilder setMessageGravity(CharSequence message, int gravity) {
        this.message = message;
        this.gravity = gravity;
        return this;
    }

    public DialogBuilder setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public DialogBuilder setMessageTextColor(CharSequence message, @ColorRes int messageTextColor) {
        this.message = message;
        this.messageTextColor = messageTextColor;
        return this;
    }

    public DialogBuilder setMessageTextSize(CharSequence message, @DimenRes int messageTextSize) {
        this.message = message;
        this.messageTextSize = messageTextSize;
        return this;
    }

    public DialogBuilder setMessageTextColorSize(CharSequence message, @ColorRes int messageTextColor, @DimenRes int messageTextSize) {
        this.message = message;
        this.messageTextColor = messageTextColor;
        this.messageTextSize = messageTextSize;
        return this;
    }

    public DialogBuilder setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public DialogBuilder setView(View view) {
        this.view = view;
        return this;
    }

    public DialogBuilder setHideCancel(boolean hideCancel) {
        this.hideCancel = hideCancel;
        return this;
    }

    public DialogBuilder setCancelListener(DialogInterface.OnClickListener cancelListener) {
        this.cancelListener = cancelListener;
        return this;
    }

    public DialogBuilder setOkListener(DialogInterface.OnClickListener okListener) {
        this.okListener = okListener;
        return this;
    }

    public int getGravity() {
        return gravity;
    }

    public CharSequence getTitle() {
        return title;
    }

    public int getTitleTextColor() {
        return titleTextColor;
    }

    public int getTitleTextSize() {
        return titleTextSize;
    }

    public CharSequence getCancelText() {
        return cancelText;
    }

    public int getCancelTextColor() {
        return cancelTextColor;
    }

    public int getCancelTextSize() {
        return cancelTextSize;
    }

    public CharSequence getOkText() {
        return okText;
    }

    public int getOkTextColor() {
        return okTextColor;
    }

    public int getOkTextSize() {
        return okTextSize;
    }

    public CharSequence getMessage() {
        return message;
    }

    public int getMessageTextColor() {
        return messageTextColor;
    }

    public int getMessageTextSize() {
        return messageTextSize;
    }

    public DialogInterface.OnClickListener getCancelListener() {
        return cancelListener;
    }

    public DialogInterface.OnClickListener getOkListener() {
        return okListener;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public View getView() {
        return view;
    }

    public boolean isHideCancel() {
        return hideCancel;
    }
}
