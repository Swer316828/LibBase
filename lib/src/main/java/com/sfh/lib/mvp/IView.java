package com.sfh.lib.mvp;


import com.sfh.lib.ui.dialog.DialogBuilder;

/**
 * 功能描述:【V】视图操作接口
 *
 * @author sunfeihu
 * @date 2016/11/14
 */

public interface IView {

    /***
     * 显示等待对话框
     * @param cancel true 可以取消默认值 false 不可以取消
     */
    void showLoading(boolean cancel);

    /***
     *隐藏等待对话框
     */
    void hideLoading();

    /***
     * 显示提示对话框
     * @param dialog 提示信息
     */
    void showDialog(DialogBuilder dialog);

    /***
     *隐藏提示对话框
     */
    void hideDialog();

    /***
     * Toast提示(正常提示)
     */
    void showToast(CharSequence msg);

    /***
     * Toast提示
     * @param msg
     * @param type 0 正常 1 警告 2错误
     */
    void showToast(CharSequence msg, int type);
}
