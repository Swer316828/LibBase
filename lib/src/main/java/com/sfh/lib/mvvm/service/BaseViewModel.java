package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sfh.lib.event.RxBusRegistry;
import com.sfh.lib.mvvm.IViewModel;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.utils.UtilLog;

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

    private RetrofitManager mRetrofit;

    private RxBusRegistry mRxBus;

    private final MutableLiveData mLiveData = new MutableLiveData();

    public BaseViewModel() {

        this.mRetrofit = new RetrofitManager();
        // 注入ViewModel层之间数据通信
        this.mRxBus = new RxBusRegistry();
        this.mRxBus.registry(this);
    }

    @Override
    public MutableLiveData getLiveData() {
        return this.mLiveData;
    }

    @MainThread
    public <T> void setValue(T t) {
        this.mLiveData.setValue(t);
    }


    @MainThread
    public <T> void setValue(String action, T t) {
        this.mLiveData.setValue(new UIData(action, t));
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
