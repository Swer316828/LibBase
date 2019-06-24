package com.sfh.lib.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sfh.lib.event.RxBusEventManager;
import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.mvvm.service.BaseViewModel;
import com.sfh.lib.mvvm.service.LiveDataRegistry;
import com.sfh.lib.mvvm.service.NetWorkState;
import com.sfh.lib.mvvm.service.ObjectMutableLiveData;
import com.sfh.lib.ui.dialog.DialogBuilder;
import com.sfh.lib.utils.ViewModelProviders;

import io.reactivex.disposables.Disposable;


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
            ViewGroup parent = (ViewGroup) mRoot.getParent ();
            if (parent != null) {
                parent.removeView (mRoot);
            }
            initCreateView = false;
        } else {
            this.mRoot = inflater.inflate (this.getLayout (), container, false);
            initCreateView = true;
        }

        if (initCreateView) {
            // 视图代理类-ViewModel
            this.mLiveDataRegistry = new LiveDataRegistry ();
            this.mLiveDataRegistry.register (this);
            this.initData (this.mRoot);
        }
        return this.mRoot;
    }

    @Override
    public void onDestroy() {

        super.onDestroy ();
        if (this.mLiveDataRegistry != null) {
            this.mLiveDataRegistry.onDestroy ();
        }
        this.mRoot = null;
    }

    /**
     * 激活一次生命周期监听(指定)
     *
     * @param event
     */
    public final void handleLifecycleEvent(Lifecycle.Event event) {

        Lifecycle lifecycle = this.getLifecycle ();
        if (lifecycle instanceof LifecycleRegistry) {
            ((LifecycleRegistry) lifecycle).handleLifecycleEvent (event);
        }
    }
    /**
     * 激活一次生命周期监听
     *
     */
    public final void activateLifecycleEvent() {

        Lifecycle lifecycle = this.getLifecycle ();
        if (lifecycle instanceof LifecycleRegistry) {
            ((LifecycleRegistry) lifecycle).handleLifecycleEvent (Lifecycle.Event.ON_RESUME);
        }
    }
    @Override
    @Nullable
    public final VM getViewModel() {

        if (this.mViewModel == null) {
            this.mViewModel = LiveDataRegistry.getViewModel (this);
            if (this.mViewModel != null && this.mLiveDataRegistry != null) {
                this.mViewModel.putLiveData (this.mLiveDataRegistry.getLiveData ());
            }
        }
        return this.mViewModel;
    }

    /***
     * 使用其他ViewModel
     * @param cls
     * @param <T>
     * @return
     */
    public final <T extends BaseViewModel> T getViewModel(Class<T> cls) {

        T t = ViewModelProviders.of (this).get (cls);
        if (t != null) {
            t.putLiveData (this.mLiveDataRegistry.getLiveData ());
        }
        return t;
    }

    @Override
    public final void onChanged(@Nullable Object data) {

        if (data instanceof NetWorkState) {
            this.setNetWorkState ((NetWorkState) data);
        } else if (this.mLiveDataRegistry != null && data instanceof UIData) {
            this.mLiveDataRegistry.showLiveData (this, (UIData) data);
        } else {
            this.setNetWorkState (NetWorkState.showToast ("数据类型不匹配"));
        }
    }

    public String getName() {

        return "";
    }

    public final void showLoading(boolean cancel) {

      this.setNetWorkState(NetWorkState.showLoading(cancel));
    }

    public final void hideLoading() {

        this.setNetWorkState(NetWorkState.hideLoading());

    }

    public final void showDialog(DialogBuilder dialog) {

        this.setNetWorkState (NetWorkState.showDialog (dialog));
    }

    public final void showDialogToast(CharSequence msg) {

        DialogBuilder dialog = new DialogBuilder ();
        dialog.setTitle ("提示");
        dialog.setHideCancel (false);
        dialog.setMessage (msg);
        this.setNetWorkState (NetWorkState.showDialog (dialog));
    }

    public final void showToast(CharSequence msg) {

        this.setNetWorkState (NetWorkState.showToast (msg));
    }

    /***
     * 显示UI 基础操作
     * @param state
     */
    private final void setNetWorkState(NetWorkState state) {

        FragmentActivity activity = getActivity ();
        if (activity == null || activity.isFinishing () || !(activity instanceof AbstractLifecycleActivity)) {
            return;
        }
        AbstractLifecycleActivity abstractLifecycleActivity = (AbstractLifecycleActivity) activity;
        abstractLifecycleActivity.setNetWorkState (state);
    }

    /***
     * 添加RxJava监听
     * @param disposable
     */
    public final void putDisposable(Disposable disposable) {

        if (this.mLiveDataRegistry != null) {
            this.mLiveDataRegistry.putDisposable (disposable);
        }
    }

    /***
     * 发送Rx消息通知
     * @param t
     * @param <T>
     */
    public final <T> void postEvent(T t) {

        RxBusEventManager.postEvent (t);
    }
}
