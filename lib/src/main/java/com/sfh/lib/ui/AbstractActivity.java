package com.sfh.lib.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.sfh.lib.mvp.IDialog;
import com.sfh.lib.mvp.IPresenter;
import com.sfh.lib.mvp.IView;
import com.sfh.lib.mvp.service.ViewProxy;
import com.sfh.lib.ui.dialog.AppDialog;
import com.sfh.lib.ui.dialog.DialogBuilder;
import com.sfh.lib.utils.ViewModelProviders;


/**
 * 功能描述:MVP【不存在任何业务逻辑代码】
 *
 * @author SunFeihu 孙飞虎
 * @date 2017/7/5
 */
public abstract class AbstractActivity extends AppCompatActivity implements IView {

    /***
     * 获取控制对象
     * @return
     */
    public abstract  IPresenter getPresenter();

    /***
     * 对话框句柄【基础操作】
     */
    private IDialog mDialogBridge;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // 视图代理类-ViewModel
        ViewProxy viewProxy =  ViewModelProviders.of(this).get(ViewProxy.class);
        viewProxy.register(this);

        //Lifecycle
        IPresenter presenter = this.getPresenter();
        if (presenter != null){

            viewProxy.bindProxy(presenter);
            //绑定生命周期管理
            this.getLifecycle().addObserver(presenter);
        }
    }


    @Override
    public void showLoading(boolean cancelAble) {

        if (this.isLifeCycle()) {
            return;
        }

        this.mDialogBridge.showLoading(cancelAble);
    }

    @Override
    public void hideLoading() {

        if (this.isLifeCycle()) {
            return;
        }
        this.mDialogBridge.hideLoading();
    }

    @Override
    public void showDialog(DialogBuilder dialog) {

        if (this.isLifeCycle()) {
            return;
        }
        this.mDialogBridge.showDialog(dialog);
    }

    @Override
    public void hideDialog() {

        if (this.isLifeCycle()) {
            return;
        }
        this.mDialogBridge.hideDialog();
    }

    @Override
    public void showToast(CharSequence message) {
        if (this.isLifeCycle()) {
            return;
        }
        this.mDialogBridge.showToast(message);
    }

    @Override
    public void showToast(CharSequence message, int type) {
        if (this.isLifeCycle()) {
            return;
        }
        this.mDialogBridge.showToast(message, type);
    }

    /***
     * 创建对话框句柄【可自定义】
     * @return
     */

    protected IDialog onCreateDialog() {
        return new AppDialog(this);
    }

    private boolean isLifeCycle() {
        if (this.isFinishing()) {
            return true;
        }
        if (this.mDialogBridge == null) {
            // 创建统一对话框与等待框，提示框
            this.mDialogBridge = this.onCreateDialog();
        }
        if (this.mDialogBridge == null) {
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mDialogBridge != null) {
            this.mDialogBridge. onDestory();
        }
        this.mDialogBridge = null;
    }
}
