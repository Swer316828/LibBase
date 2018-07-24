package com.sfh.lib.mvp;


import android.arch.lifecycle.LifecycleObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;


/**
 * 功能描述:基础中间控制层
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public interface IPresenter<V extends IView> extends LifecycleObserver {

    /**
     * 创建代理
     *
     * @param proxy
     */
    void onBindProxy(V proxy);

    /***
     * 获取视图代理类对象
     * @return
     */
    V getView();

    /***
     * 绑定监听
     * @param disposable
     */
    void putDisposable(Disposable disposable);

    /***
     * 操作处理
     * @param observable
     * @param observer
     * @return
     */
    <T> void execute(@NonNull Observable<T> observable,  @Nullable  IResult<T> observer);
    /***
     * 操作处理
     * @param observable
     * @param observer
     * @return
     */
    <T> void execute(@NonNull Flowable<T> observable, @Nullable final IResult<T> observer);
}
