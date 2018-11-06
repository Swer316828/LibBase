package com.sfh.lib.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.mvvm.service.BaseViewModel;
import com.sfh.lib.mvvm.service.LiveDataRegistry;
import com.sfh.lib.mvvm.service.NetWorkState;
import com.sfh.lib.ui.dialog.DialogBuilder;
import com.sfh.lib.utils.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;


/**
 * 功能描述: TODO
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/9/6
 */
public abstract class AbstractLifecycleView<VM extends BaseViewModel> extends FrameLayout implements IView, Observer {


    protected VM mViewModel;

    private LiveDataRegistry mLiveDataRegistry;

    private LiveData mLiveData;

    public AbstractLifecycleView(Context context) {
        super(context);
        this.setContentView();
    }

    public AbstractLifecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setContentView();
    }

    public AbstractLifecycleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setContentView();
    }

    /***
     * 获取布局ID
     * @return
     */
    public abstract int layout();

    /***
     * 初始化
     */
    public abstract void initData();

    private void setContentView() {
        inflate(this.getContext(), this.layout(), this);
        this.initData();
    }

    /***
     * 不关联 View onDetachedFromWindow()事件,需要使用手动调用onDestoryObserver()解除
     * 默认 关联
     * @return true 不关联 View onDetachedFromWindow()事件, false 关联
     *
     */
    public boolean isForever() {
        return false;
    }

    public void onDestoryObserver() {
        if (this.mLiveData != null) {
            this.mLiveData.removeObserver(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mLiveDataRegistry == null) {
            this.mLiveDataRegistry = ViewModelProviders.of((FragmentActivity) this.getContext()).get(LiveDataRegistry.class);
            this.mLiveDataRegistry.observe(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mLiveData != null) {
            if (this.isForever()) {
                return;
            }
            this.mLiveData.removeObserver(this);
        }
    }

    @Nullable
    @Override
    final public VM getViewModel() {
        if (this.mViewModel == null) {
            this.mViewModel = (VM) this.mLiveDataRegistry.getViewModel(this.getManagerKey(), this);
        }
        return this.mViewModel;
    }

    /***
     * 使用当前ViewModel 独立，如共享数据设置相同key
     * 不建议重写该方法
     * @return
     */
    protected String getManagerKey() {

        return ViewModelProviders.createKey();
    }

    @Override
    final public <T> void observer(LiveData<T> liveData) {
        this.mLiveData = liveData;
        if (this.mLiveData != null) {
            this.mLiveData.observe((FragmentActivity) this.getContext(), this);
        }
    }

    @Override
    final public void onChanged(@Nullable Object data) {

        if (data instanceof NetWorkState) {
            this.setNetWorkState((NetWorkState) data);
        } else if (data instanceof UIData) {
            this.mLiveDataRegistry.showLiveData(this, (UIData) data);
        } else {
            this.setNetWorkState(NetWorkState.showToast("数据类型不匹配"));
        }
    }

    final public void showDialog(DialogBuilder dialog) {
        this.setNetWorkState(NetWorkState.showDialog(dialog));
    }

    final public void showToast(CharSequence msg) {
        this.setNetWorkState(NetWorkState.showToast(msg));
    }

    final public void setNetWorkState(NetWorkState netWorkState) {
        Context activity = this.getContext();
        if (activity == null || !(activity instanceof AbstractLifecycleActivity)) {
            return;
        }
        AbstractLifecycleActivity lifecycleActivity = (AbstractLifecycleActivity) activity;
        lifecycleActivity.setNetWorkState(netWorkState);
    }

    /***
     * 添加RxJava监听
     * @param disposable
     */
    final public void putDisposable(Disposable disposable) {

        this.mLiveDataRegistry.putDisposable(disposable);
    }

}
