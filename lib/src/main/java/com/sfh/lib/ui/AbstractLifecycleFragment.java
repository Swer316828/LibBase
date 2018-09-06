package com.sfh.lib.ui;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.mvvm.service.BaseViewModel;
import com.sfh.lib.mvvm.service.LiveDataRegistry;
import com.sfh.lib.mvvm.service.NetWorkState;
import com.sfh.lib.utils.ViewModelProviders;


/**
 * 功能描述:UI 不存在任何业务逻辑代码
 *
 * @author SunFeihu 孙飞虎
 * @date 2017/7/5
 */
public abstract class AbstractLifecycleFragment<VM extends BaseViewModel> extends Fragment implements IView, Observer {


    public abstract int getLayout();

    public abstract void initData(View view);

    private LiveDataRegistry mLiveDataRegistry;

    protected VM mViewModel;

    protected View mRoot;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean initCreateView;
        //在Fragment onCreateView方法中缓存View
        //FragmentTab切换Fragment时避免重复加载UI
        if (this.mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
            }
            initCreateView = false;
        } else {
            this.mRoot = inflater.inflate(this.getLayout(), container, false);
            initCreateView = true;
        }

        if (initCreateView) {
            // 视图代理类-ViewModel
            this.mLiveDataRegistry = ViewModelProviders.of(this).get(LiveDataRegistry.class);
            this.mLiveDataRegistry.observe(this);
            this.initData(this.mRoot);
        }
        return this.mRoot;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
    public void onChanged(@Nullable Object data) {

        if (data instanceof NetWorkState) {
            this.setNetWorkState((NetWorkState) data);
        } else if (data instanceof UIData) {
            this.mLiveDataRegistry.showLiveData(this, (UIData) data);
        } else {
            this.setNetWorkState(NetWorkState.showToast("数据类型不匹配"));
        }
    }

    public String getName() {
        return "";
    }


    final protected void setNetWorkState(NetWorkState state) {
        FragmentActivity activity = getActivity();
        if (activity == null || activity.isFinishing() || !(activity instanceof AbstractLifecycleActivity)) {
            return;
        }
        AbstractLifecycleActivity abstractLifecycleActivity = (AbstractLifecycleActivity) activity;
        abstractLifecycleActivity.setNetWorkState(state);
    }


}
