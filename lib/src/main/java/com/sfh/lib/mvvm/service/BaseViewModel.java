package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sfh.lib.mvvm.IViewModel;
import com.sfh.lib.mvvm.IResult;
import com.sfh.lib.utils.UtilLog;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * 功能描述: 业务Model
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/30
 */
public class BaseViewModel extends ViewModel implements IViewModel {

    private final static String TAG = BaseViewModel.class.getName();

    private volatile List<Class<?>> mLiveDataClass;

    private RetrofitManager mRetrofit;

    private RxBusRegistry mRxBus;

    private final BaseLiveData mLiveData = new BaseLiveData();

    public BaseViewModel() {

        this.mLiveDataClass = new ArrayList<>(3);
        this.mRetrofit = new RetrofitManager();
        // 注入ViewModel层之间数据通信
        this.mRxBus = new RxBusRegistry();
        this.mRxBus.registry(this);
    }

    @Override
    public BaseLiveData getLiveData() {
        return this.mLiveData;
    }


    @Override
    public void onLiveDataClass(Class<?> clz) {
        this.mLiveDataClass.add(clz);
    }


    @MainThread
    @Override
    public <T> void setValue(T t) {
       if (this.isExitLiveDataClass(t)){
           mLiveData.setValue(t);
       }
    }

    /***
     * 判断数据是否为监听数据类型
     * @param clz
     * @param <T>
     * @return
     */
    public <T> boolean isExitLiveDataClass(T clz) {
        for (Class<?> type : mLiveDataClass) {
            if (type.isInstance(clz)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        UtilLog.d("", "onCleared=========== 资源销毁");
        this.mRxBus.onCleared();
        this.mRetrofit.clearAll();
    }

    @Override
    public void putDisposable(Disposable disposable) {

        this.mRetrofit.put(disposable);
    }


    @Override
    public <T> void execute(@NonNull Observable<T> observable, @NonNull IResult<T> observer) {

        this.mRetrofit.execute(observable, observer);
    }


    @Override
    public <T> void execute(@NonNull Flowable<T> observable, @Nullable final IResult<T> observer) {

        this.mRetrofit.execute(observable, observer);
    }

}
