package com.sfh.lib.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.sfh.lib.event.RxBusEventManager;
import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.mvvm.service.BaseViewModel;
import com.sfh.lib.mvvm.service.LiveDataRegistry;
import com.sfh.lib.mvvm.service.NetWorkState;
import com.sfh.lib.ui.dialog.AppDialog;
import com.sfh.lib.ui.dialog.DialogBuilder;
import com.sfh.lib.ui.dialog.IDialog;
import com.sfh.lib.utils.ViewModelProviders;

import io.reactivex.disposables.Disposable;


/**
 * 功能描述:UI【不存在任何业务逻辑代码】
 *
 * @author SunFeihu 孙飞虎
 * @date 2017/7/5
 */
public abstract class AbstractLifecycleActivity<VM extends BaseViewModel> extends AppCompatActivity implements IView, Observer {

    /***
     * 对话框句柄【基础操作】
     */
    private IDialog mDialogBridge;

    protected VM mViewModel;

    private LiveDataRegistry mLiveDataRegistry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.mLiveDataRegistry = ViewModelProviders.of(this).get(LiveDataRegistry.class);
        this.mLiveDataRegistry.observe(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mDialogBridge != null) {
            this.mDialogBridge.onDestory();
        }
        this.mDialogBridge = null;
    }


    @Override
    @Nullable
    public VM getViewModel() {
        if (this.mViewModel == null) {
            this.mViewModel = (VM) this.mLiveDataRegistry.getViewModel(this);
        }
        return this.mViewModel;
    }

    /***
     * 使用其他ViewModel
     * @param cls
     * @param <T>
     * @return
     */
    public <T extends BaseViewModel> T getViewModel(Class<T> cls) {
        T t = ViewModelProviders.of(this).get(cls);
        if (t != null) {
            t.getLiveData().observe(this, this);
        }
        return t;
    }

    @Override
    public <T> void observer(LiveData<T> liveData) {
        liveData.observe(this, this);
    }

    @Override
    public void onChanged(@Nullable Object data) {
        if (data instanceof NetWorkState) {
            this.setNetWorkState((NetWorkState) data);
        } else if (data instanceof UIData) {
            this.mLiveDataRegistry.showLiveData(this, (UIData) data);
        } else {
            this.setNetWorkState(NetWorkState.showToast("数据类型不匹配"));
        }
    }


    /***
     * 创建对话框句柄【可自定义对话框】
     * @return
     */
    protected IDialog onCreateDialog() {
        return new AppDialog(this);
    }


    final protected void setNetWorkState(NetWorkState state) {
        if (this.isLifeCycle()) {
            return;
        }
        switch (state.getType()) {
            case NetWorkState.TYPE_SHOW_LOADING: {
                this.mDialogBridge.showLoading(true);
                break;
            }
            case NetWorkState.TYPE_SHOW_LOADING_NO_CANCEL: {
                this.mDialogBridge.showLoading(false);
                break;
            }
            case NetWorkState.TYPE_HIDE_LOADING: {
                this.mDialogBridge.hideLoading();
                break;
            }
            case NetWorkState.TYPE_SHOW_TOAST: {
                this.mDialogBridge.showToast(state.getShowToast());
                break;
            }
            case NetWorkState.TYPE_SHOW_DIALOG: {
                this.mDialogBridge.showDialog(state.getBuilder());
                break;
            }
            default:
                break;
        }
    }

    final public void showDialog(DialogBuilder dialog) {
        this.setNetWorkState(NetWorkState.showDialog(dialog));
    }
    final public void showDialogToast(CharSequence msg){
        DialogBuilder dialog = new DialogBuilder();
        dialog.setTitle("提示");
        dialog.setHideCancel(true);
        dialog.setMessage(msg);
        this.setNetWorkState(NetWorkState.showDialog(dialog));
    }
    final public void showToast(CharSequence msg) {
        this.setNetWorkState(NetWorkState.showToast(msg));
    }


    final private boolean isLifeCycle() {
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

    /***
     * 添加RxJava监听
     * @param disposable
     */
    final public void putDisposable(Disposable disposable) {

        this.mLiveDataRegistry.putDisposable(disposable);
    }


    /***
     * 发送Rx消息通知
     * @param t
     * @param <T>
     */
    public final <T> void postEvent(T t) {
        RxBusEventManager.postEvent(t);
    }
}

