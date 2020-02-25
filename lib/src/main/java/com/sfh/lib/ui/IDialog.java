package com.sfh.lib.ui;

public interface IDialog {

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
     * Toast提示(正常提示)
     */
    void showToast(CharSequence msg, int duration);

    /***
     * 销毁
     */
    void onDestory();
}
