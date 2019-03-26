package com.sfh.lib.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.sfh.lib.event.RxBusEventManager;
import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.mvvm.service.BaseViewModel;
import com.sfh.lib.mvvm.service.LiveDataRegistry;
import com.sfh.lib.mvvm.service.NetWorkState;
import com.sfh.lib.ui.dialog.DialogBuilder;
import com.sfh.lib.utils.ViewModelProviders;

import io.reactivex.disposables.Disposable;


/**
 * 功能描述: MVVM 自定义View
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/9/6
 */
public abstract class AbstractLifecycleView<VM extends BaseViewModel> extends FrameLayout implements IView, Observer {

    protected VM mViewModel;

    private LiveDataRegistry mLiveDataRegistry;

    private LiveData mLiveData;

    public AbstractLifecycleView(Context context) {

        super (context);
        this.setContentView ();
    }

    public AbstractLifecycleView(Context context, @Nullable AttributeSet attrs) {

        super (context, attrs);
        this.setContentView ();
    }

    public AbstractLifecycleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super (context, attrs, defStyleAttr);
        this.setContentView ();
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

        if (this.layout () <= 0) {
            return;
        }
        inflate (this.getContext (), this.layout (), this);
        this.initData ();

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

    /***
     * 解除响应
     */
    public void onDestoryObserver() {

        if (this.mLiveData != null) {
            this.mLiveData.removeObserver (this);
        }
    }

    @Override
    protected void onAttachedToWindow() {

        super.onAttachedToWindow ();
        if (this.mLiveDataRegistry == null) {
            this.mLiveDataRegistry = ViewModelProviders.of ((FragmentActivity) this.getContext ()).get (LiveDataRegistry.class);
            this.mLiveDataRegistry.handerMethod (this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {

        super.onDetachedFromWindow ();
        if (this.mLiveData != null) {
            if (this.isForever ()) {
                return;
            }
            this.mLiveData.removeObserver (this);
            this.mViewModel = null;
            this.mLiveDataRegistry = null;
            this.mLiveData = null;
        }
    }

    @Nullable
    @Override
    final public VM getViewModel() {

        if (this.mViewModel == null) {
            this.mViewModel = LiveDataRegistry.getViewModel (this);
            if (this.mViewModel != null){
                this.mViewModel.getLiveData ().observe ((FragmentActivity) this.getContext (), this);
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
    public <T extends BaseViewModel> T getViewModel(Class<T> cls) {

        T t = ViewModelProviders.of ((FragmentActivity) this.getContext ()).get (cls);
        if (t != null) {
            t.getLiveData ().observe ((FragmentActivity) this.getContext (), this);
        }
        return t;
    }


    @Override
    public final <T> void observer(LiveData<T> liveData) {

        this.mLiveData = liveData;
        this.mLiveData.observe ((FragmentActivity) this.getContext (), this);
    }

    @Override
    public final void onChanged(@Nullable Object data) {

        if (data instanceof NetWorkState) {
            this.setNetWorkState ((NetWorkState) data);
        } else if (data instanceof UIData) {
            this.mLiveDataRegistry.showLiveData (this, (UIData) data);
        } else {
            this.setNetWorkState (NetWorkState.showToast ("数据类型不匹配"));
        }
    }

    public final void showDialogToast(CharSequence msg) {

        DialogBuilder dialog = new DialogBuilder ();
        dialog.setTitle ("提示");
        dialog.setHideCancel (true);
        dialog.setMessage (msg);
        this.showDialog (dialog);
    }

    public final void showDialog(DialogBuilder dialog) {

        this.setNetWorkState (NetWorkState.showDialog (dialog));
    }

    public final void showToast(CharSequence msg) {

        this.setNetWorkState (NetWorkState.showToast (msg));
    }

    private final void setNetWorkState(NetWorkState netWorkState) {

        Context activity = this.getContext ();
        if (activity == null || !(activity instanceof AbstractLifecycleActivity)) {
            return;
        }
        AbstractLifecycleActivity lifecycleActivity = (AbstractLifecycleActivity) activity;
        lifecycleActivity.setNetWorkState (netWorkState);
    }

    /***
     * 添加RxJava监听
     * @param disposable
     */
    public final void putDisposable(Disposable disposable) {

        this.mLiveDataRegistry.putDisposable (disposable);
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
