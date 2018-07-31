package com.sfh.lib.mvvm.service;

/**
 * 功能描述: UI 通用
 *
 * @author SunFeihu 孙飞虎
 * @company 中储南京智慧物流科技有限公司
 * @copyright （版权）中储南京智慧物流科技有限公司所有
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
    public static NetWorkState showToast(String toast){
        return new NetWorkState(TYPE_SHOW_TOAST,toast);
    }
    /***
     * 显示提示对话框
     * @param toast
     * @return
     */
    public static NetWorkState showDialog(String toast){
        return new NetWorkState(TYPE_SHOW_DIALOG,toast);
    }


    private  int type;
    private  String showToast;

    public NetWorkState(int type) {
        this.type = type;
    }

    public NetWorkState(int type, String toast) {
        this.type = type;
        this.showToast = showToast;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getShowToast() {
        return showToast;
    }

    public void setShowToast(String showToast) {
        this.showToast = showToast;
    }

}
