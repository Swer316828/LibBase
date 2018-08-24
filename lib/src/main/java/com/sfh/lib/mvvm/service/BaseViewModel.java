package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sfh.lib.event.RxBusRegistry;
import com.sfh.lib.mvvm.IViewModel;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.rx.IHanderLoading;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.ui.dialog.DialogBuilder;
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
    private  <T> void setValue(T t) {
        this.mLiveData.setValue(t);
    }


    @MainThread
    public <T> void setValue(String action, T t) {
        this.setValue(new UIData(action, t));
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


    public <T> void execute(final boolean cancelDialog, @NonNull Observable<T> observable, @NonNull IResult<T> observer) {
        this.showLoading(cancelDialog);
        this.mRetrofit.execute(observable, observer, new IHanderLoading() {
            @Override
            public void hideLoading() {
                BaseViewModel.this.hideLoading();
            }
        });
    }


    /***
     * 显示等待对话框
     * @param cancel true 可以取消默认值 false 不可以取消
     */
    public void showLoading(boolean cancel){
        this.setValue(cancel?NetWorkState.SHOW_LOADING:NetWorkState.SHOW_LOADING_NO_CANCEL);
    }

    /***
     *隐藏等待对话框
     */
    public void hideLoading(){
        this.setValue(NetWorkState.HIDE_LOADING);
    }

    /***
     * 显示提示对话框
     * @param dialog 提示信息
     */
    public  void showDialog(DialogBuilder dialog){
        this.setValue(NetWorkState.showDialog(dialog));
    }


    /***
     * Toast提示(正常提示)
     */
    public void showToast(CharSequence msg){
        this.setValue(NetWorkState.showToast(msg));
    }


}
