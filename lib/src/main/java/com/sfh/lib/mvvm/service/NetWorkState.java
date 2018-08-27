package com.sfh.lib.mvvm.service;

import com.sfh.lib.ui.dialog.DialogBuilder;

/**
 * 功能描述: UI提示信息 【通用】
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/19
 */
public class NetWorkState {

    public static final int TYPE_SHOW_LOADING = 0x1;
    public static final int TYPE_SHOW_LOADING_NO_CANCEL = 0x2;
    public static final int TYPE_HIDE_LOADING = 0x3;
    public static final int TYPE_SHOW_TOAST = 0x4;
    public static final int TYPE_SHOW_DIALOG = 0x5;

    private final static NetWorkState SHOW_LOADING = new NetWorkState(TYPE_SHOW_LOADING);
    private final static NetWorkState SHOW_LOADING_NO_CANCEL = new NetWorkState(TYPE_SHOW_LOADING_NO_CANCEL);
    private final static NetWorkState HIDE_LOADING = new NetWorkState(TYPE_HIDE_LOADING);
    private final static NetWorkState TOAST = new NetWorkState(TYPE_SHOW_TOAST);
    private final static NetWorkState DIALOG = new NetWorkState(TYPE_SHOW_DIALOG);


    public static NetWorkState showLoading(boolean cancle) {
        return cancle ? SHOW_LOADING : SHOW_LOADING_NO_CANCEL;
    }


    public static NetWorkState hideLoading() {
        return HIDE_LOADING;
    }

    public static NetWorkState showToast(CharSequence toast) {
        TOAST.showToast = toast;
        return TOAST;
    }

    public static NetWorkState showDialog(DialogBuilder builder) {
        DIALOG.builder = builder;
        return DIALOG;
    }


    private int type;
    private CharSequence showToast;
    private DialogBuilder builder;

    private NetWorkState(int type) {
        this.type = type;
    }



    public int getType() {
        return type;
    }

    public CharSequence getShowToast() {
        return showToast;
    }


    public DialogBuilder getBuilder() {
        return builder;
    }
}
