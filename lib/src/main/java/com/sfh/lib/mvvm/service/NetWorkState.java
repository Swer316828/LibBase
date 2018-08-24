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

    /**显示加载等待对话框-可取消*/
    public final static NetWorkState SHOW_LOADING = new NetWorkState(TYPE_SHOW_LOADING);
    /**显示加载等待对话框-不可取消*/
    public final static NetWorkState SHOW_LOADING_NO_CANCEL = new NetWorkState(TYPE_SHOW_LOADING_NO_CANCEL);
    /**取消加载等待对话框*/
    public final static NetWorkState HIDE_LOADING = new NetWorkState(TYPE_HIDE_LOADING);

    /***
     * 显示提示信息
     * @param toast
     * @return
     */
    public static NetWorkState showToast(CharSequence toast){
        return new NetWorkState(TYPE_SHOW_TOAST,toast);
    }


    /***
     * 显示提示对话框
     * @return
     */
    public static NetWorkState showDialog(DialogBuilder builder){
        return new NetWorkState(TYPE_SHOW_DIALOG,builder);
    }


    private  int type;
    private  CharSequence showToast;
    private DialogBuilder builder;

    public NetWorkState(int type) {
        this.type = type;
    }

    public NetWorkState(int type, CharSequence toast) {
        this.type = type;
        this.showToast = toast;
    }
    public NetWorkState(int type, DialogBuilder toast) {
        this.type = type;
        this.builder = toast;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public CharSequence getShowToast() {
        return showToast;
    }


    public DialogBuilder getBuilder() {
        return builder;
    }
}
