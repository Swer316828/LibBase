package com.sfh.lib.mvp.service;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sfh.lib.mvp.IPresenter;
import com.sfh.lib.mvp.IResult;
import com.sfh.lib.mvp.IView;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * 功能描述:Mode 与 View 中间层
 * 1.通过Model层业务逻辑获取数据，在调用V层代理对象把数据传给真正V层对象
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public abstract class AbstractPresenter<V extends IView> implements IPresenter<V> {


    /**
     * 视图回调代理对象
     */
    private V proxy;

    /***
     * 管理操作
     */
    private  volatile RetrofitManager retrofit;

    @Override
    public V getView() {

        return this.proxy;
    }

    @Override
    public void onBindProxy(V proxy) {
        if (this.retrofit == null) {
            this.retrofit = new RetrofitManager();
        }
        this.proxy = proxy;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void disconnectListener() {
        //取消业务层监听
        if (retrofit != null) {
            this.retrofit.clearAll();
        }
    }

    @Override
    public void putDisposable(Disposable disposable) {

        if (this.retrofit == null) {
            this.retrofit = new RetrofitManager();
        }
        this.retrofit.put(disposable);
    }

    @Override
    public <T> void execute( @NonNull Observable<T> observable, @NonNull IResult<T> observer) {
        if (this.retrofit == null) {
            this.retrofit = new RetrofitManager();
        }
         this.retrofit.execute(observable, observer);
    }

    /***
     * 操作处理
     * @param observable
     * @param observer
     * @return
     */
    @Override
    public <T> void execute(@NonNull Flowable<T> observable, @Nullable final IResult<T> observer){
        if (this.retrofit == null) {
            this.retrofit = new RetrofitManager();
        }
        this.retrofit.execute(observable, observer);
    }
}
