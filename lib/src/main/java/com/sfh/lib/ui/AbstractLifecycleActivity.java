package com.sfh.lib.ui;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.BaseAdapter;

import com.sfh.lib.mvvm.IDialog;
import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.service.BaseViewModel;
import com.sfh.lib.mvvm.service.LiveDataUIRegistry;
import com.sfh.lib.mvvm.service.NetWorkState;
import com.sfh.lib.ui.dialog.AppDialog;
import com.sfh.lib.ui.dialog.DialogBuilder;
import com.sfh.lib.utils.ViewModelProviders;

import java.lang.reflect.ParameterizedType;


/**
 * 功能描述:MVP【不存在任何业务逻辑代码】
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

    private LiveDataUIRegistry mLiveDataRegistry;

    @Override
    @Nullable
    public VM getViewModel() {
        if (this.mViewModel == null) {
            Class<VM> cls = (Class<VM>) ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            this.mViewModel = ViewModelProviders.of(this).get(cls);
        }
        return this.mViewModel;
    }


    @Override
    @NonNull
    public LifecycleOwner getLifecycleOwner() {
        return this;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.mLiveDataRegistry = ViewModelProviders.of(this).get(LiveDataUIRegistry.class);
        this.mLiveDataRegistry.observe(this);

    }

    public void showLoading(boolean cancelAble) {

        if (this.isLifeCycle()) {
            return;
        }

        this.mDialogBridge.showLoading(cancelAble);
    }

    public void hideLoading() {

        if (this.isLifeCycle()) {
            return;
        }
        this.mDialogBridge.hideLoading();
    }

    public void showDialog(DialogBuilder dialog) {

        if (this.isLifeCycle()) {
            return;
        }
        this.mDialogBridge.showDialog(dialog);
    }

    public void hideDialog() {

        if (this.isLifeCycle()) {
            return;
        }
        this.mDialogBridge.hideDialog();
    }

    public void showToast(CharSequence message) {
        if (this.isLifeCycle()) {
            return;
        }
        this.mDialogBridge.showToast(message);
    }

    public void showToast(CharSequence message, int type) {
        if (this.isLifeCycle()) {
            return;
        }
        this.mDialogBridge.showToast(message, type);
    }


    /***
     * 获取LiveData 监听
     * @return
     */
    @Override
    @NonNull
    public Observer getObserver() {
        return this;
    }

    @Override
    public void onChanged(@Nullable Object data) {
        if (data instanceof NetWorkState) {
            this.setNetWorkState((NetWorkState) data);
        } else {
            this.mLiveDataRegistry.showLiveData(this, data);
        }
    }

    public void setNetWorkState(NetWorkState state) {
        switch (state.getType()) {
            case NetWorkState.TYPE_SHOW_LOADING: {
                this.showLoading(true);
                break;
            }
            case NetWorkState.TYPE_SHOW_LOADING_NO_CANCEL: {
                this.showLoading(false);
                break;
            }
            case NetWorkState.TYPE_HIDE_LOADING: {
                this.hideLoading();
                break;
            }
            case NetWorkState.TYPE_SHOW_TOAST: {
                this.showToast(state.getShowToast());
                break;
            }
            case NetWorkState.TYPE_SHOW_DIALOG: {
                // TODO 显示对话框
                break;
            }
            default:
                break;
        }
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
            this.mDialogBridge.onDestory();
        }
        this.mDialogBridge = null;
    }
}
