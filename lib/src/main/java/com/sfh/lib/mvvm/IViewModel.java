package com.sfh.lib.mvvm;


import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sfh.lib.mvvm.service.BaseLiveData;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;


/**
 * 功能描述:基础中间控制层
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public interface IViewModel {

    /***
     * 数据持有者
     * @return
     */
    BaseLiveData getLiveData();

    /***
     * 刷新数据
     * @param t
     * @param <T>
     */
    @MainThread
    <T> void setValue(T t);


    /***
     * 任务加入管理中
     * @param disposable
     */
    void putDisposable(Disposable disposable);

    /***
     * 异步处理任务
     * @param observable
     * @param observer
     * @param <T>
     */
    <T> void execute(@NonNull Observable<T> observable, @NonNull IResult<T> observer);

    /***
     * 异步处理任务
     * @param observable
     * @param observer
     * @return
     */
    <T> void execute(@NonNull Flowable<T> observable, @Nullable final IResult<T> observer);
}
