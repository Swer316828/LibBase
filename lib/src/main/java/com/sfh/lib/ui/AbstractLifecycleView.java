package com.sfh.lib.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;

import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.mvvm.service.BaseViewModel;
import com.sfh.lib.mvvm.service.LiveDataRegistry;
import com.sfh.lib.mvvm.service.NetWorkState;
import com.sfh.lib.utils.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;


/**
 * 功能描述: TODO
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/9/6
 */
public abstract class AbstractLifecycleView<VM extends BaseViewModel> extends View implements IView, Observer {


    protected VM mViewModel;

    private LiveDataRegistry mLiveDataRegistry;

    private List<LiveData> mLiveData = new ArrayList<>(3);

    public AbstractLifecycleView(Context context) {
        super(context);
    }

    public AbstractLifecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractLifecycleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mLiveDataRegistry = ViewModelProviders.of((FragmentActivity) this.getContext()).get(LiveDataRegistry.class);
        this.mLiveDataRegistry.observe(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        for (LiveData liveData : this.mLiveData) {
            liveData.removeObserver(this);
        }
        this.mLiveData.clear();
    }


    @Nullable
    @Override
    final public VM getViewModel() {
        if (this.mViewModel == null) {

            this.mViewModel = (VM) this.mLiveDataRegistry.getViewModel(this);
        }
        return this.mViewModel;
    }


    @Override
    final  public <T> void observer(LiveData<T> liveData) {
        liveData.observe((FragmentActivity) this.getContext(), this);
        this.mLiveData.add(liveData);
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

    /***
     * 使用其他ViewModel
     * @param cls
     * @param <T>
     * @return
     */
    final public <T extends BaseViewModel> T getViewModel(Class<T> cls) {
        T t = ViewModelProviders.of((FragmentActivity) this.getContext()).get(cls);

        if (t != null && !mLiveData.contains(t.getLiveData())) {
            LiveData liveData = t.getLiveData();
            liveData.observe((FragmentActivity) this.getContext(), this);
            this.mLiveData.add(liveData);
        }
        return t;
    }

    final public void setNetWorkState(NetWorkState netWorkState) {
        Context activity = this.getContext();
        if (activity == null || !(activity instanceof AbstractLifecycleActivity)) {
            return;
        }
        AbstractLifecycleActivity lifecycleActivity = (AbstractLifecycleActivity) activity;
        lifecycleActivity.setNetWorkState(netWorkState);
    }


}
