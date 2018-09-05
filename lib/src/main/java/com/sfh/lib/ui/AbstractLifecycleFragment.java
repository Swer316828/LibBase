package com.sfh.lib.ui;

import android.arch.lifecycle.LifecycleOwner;
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
import com.sfh.lib.ui.dialog.DialogBuilder;
import com.sfh.lib.utils.ViewModelProviders;


/**
 * 功能描述:UI 不存在任何业务逻辑代码
 *
 * @author SunFeihu 孙飞虎
 * @date 2017/7/5
 */
public abstract class AbstractLifecycleFragment<VM extends BaseViewModel> extends Fragment implements IView,Observer {

    /***
     * 布局
     * @return
     */
    public abstract int getLayout();

    protected VM mViewModel;

    private LiveDataRegistry mLiveDataRegistry;


    @Override
    @Nullable
    public VM getViewModel() {
        if (this.mViewModel == null) {
            mViewModel = (VM) this.mLiveDataRegistry.getViewModel(this);
        }
        return this.mViewModel;
    }

    @Override
    @NonNull
    public LifecycleOwner getLifecycleOwner() {
        return this;
    }

    /***
     * 视图创建
     * @param view
     */
    public abstract void initData(View view);

    protected View mRoot;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean initCreateView;
        //在Fragment onCreateView方法中缓存View
        //FragmentTab切换Fragment时避免重复加载UI
        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
            }
            initCreateView = false;
        } else {
            mRoot = inflater.inflate(this.getLayout(), container, false);
            initCreateView = true;
        }

        if (initCreateView) {
            // 视图代理类-ViewModel
            mLiveDataRegistry = ViewModelProviders.of(this).get(LiveDataRegistry.class);
            mLiveDataRegistry.observe(this);
            this.initData(mRoot);
        }
        return mRoot;
    }


    public String getName() {
        return "";
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

    public void showLoading(boolean cancel) {
        AbstractLifecycleActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.showLoading(cancel);

    }

    public void hideLoading() {

        AbstractLifecycleActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.hideLoading();
    }

    public void showDialog(DialogBuilder alert) {

        AbstractLifecycleActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.showDialog(alert);
    }

    public void hideDialog() {

        AbstractLifecycleActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.hideDialog();
    }

    public void showToast(CharSequence message) {
        AbstractLifecycleActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.showToast(message);
    }

    public void showToast(CharSequence message, int type) {
        AbstractLifecycleActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.showToast(message, type);
    }

    private AbstractLifecycleActivity lifeCycle() {
        FragmentActivity activity = getActivity();
        if (activity == null || activity.isFinishing() || !(activity instanceof AbstractLifecycleActivity)) {
            return null;
        }
        return (AbstractLifecycleActivity) activity;
    }

    @Override
    public void onChanged(@Nullable Object data){

        if (data instanceof NetWorkState) {
            AbstractLifecycleActivity activity = this.lifeCycle();
            if (activity == null) {
                return;
            }
            activity.setNetWorkState((NetWorkState) data);
        } else if (data instanceof UIData){
            this.mLiveDataRegistry.showLiveData(this, (UIData)data);
        }else{
            this.showToast("数据类型不匹配");
        }

    }
}
