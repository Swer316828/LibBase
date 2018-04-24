package com.sfh.lib.ui;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.sfh.lib.mvp.ILifeCycle;
import com.sfh.lib.mvp.IView;
import com.sfh.lib.mvp.service.ViewProxy;
import com.sfh.lib.ui.dialog.AppDialog;
import com.sfh.lib.utils.UtilTool;


/**
 * 功能描述:MVP【不存在任何业务逻辑代码】
 *
 * @author SunFeihu 孙飞虎
 * @date 2017/7/5
 */
public abstract class AbstractActivity extends AppCompatActivity implements IView {


    /***
     * 对话框句柄【基础操作】
     */
    private IDialog dialogProxy;

    /***
     * 生命周期管理
     */
    private final ILifeCycle lifeCycle = AndroidLifecycle.createLifecycleProvider();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // 视图代理类
        ViewProxy viewProxy = new ViewProxy();
        //绑定生命周期管理
        viewProxy.bindToLifecycle(this.lifeCycle);

        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_CREATE);
    }

    @Override
    protected void onStart() {

        super.onStart();
        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_START);
    }


    @Override
    protected void onResume() {

        super.onResume();
        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_RESUME);
    }

    @Override
    protected void onPause() {

        super.onPause();
        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_PAUSE);
    }

    @Override
    protected void onStop() {

        super.onStop();
        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_STOP);
    }

    @Override
    public void finish() {

        super.finish();
        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_FINISH);
        if (this.dialogProxy != null) {
            this.dialogProxy.onDestroy();
            this.dialogProxy = null;
        }
    }

    @Override
    protected void onDestroy() {

        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_DESTROY);
        super.onDestroy();
    }

    @Override
    public void showLoading(boolean cancelAble) {

        if (this.isLifeCycle()) {
            return;
        }

        this.dialogProxy.showLoading(cancelAble);
    }

    @Override
    public void hideLoading() {

        if (this.isLifeCycle()) {
            return;
        }
        this.dialogProxy.hideLoading();
    }

    @Override
    public void showDialog(DialogView dialog) {

        if (this.isLifeCycle()) {
            return;
        }
        this.dialogProxy.showDialog(dialog);
    }

    @Override
    public void hideDialog() {

        if (this.isLifeCycle()) {
            return;
        }
        this.dialogProxy.hideDialog();
    }

    @Override
    public void showToast(CharSequence message) {
        if (this.isLifeCycle()) {
            return;
        }
        this.dialogProxy.showToast(message);
    }

    @Override
    public void showToast(CharSequence message, int type) {
        if (this.isLifeCycle()) {
            return;
        }
        this.dialogProxy.showToast(message, type);
    }

    /***
     * 创建对话框句柄
     * @return
     */
    protected   IDialog onCreateDialog(){
        return  new AppDialog(this);
    }

    private boolean isLifeCycle() {
        if (this.isFinishing()) {
            return true;
        }
        if (this.dialogProxy == null) {
            // 创建统一对话框与等待框，提示框
            this.dialogProxy = this.onCreateDialog();
        }
        if (this.dialogProxy == null) {
            return true;
        }
        return false;
    }

    /***
     *
     * @param resId
     * @param <T>
     * @return
     */
    public <T extends View> T findView(@IdRes int resId) {
        return (T) super.findViewById(resId);
    }

}
