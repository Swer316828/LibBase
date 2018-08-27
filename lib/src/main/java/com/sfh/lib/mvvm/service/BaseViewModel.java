package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.sfh.lib.event.RxBusRegistry;
import com.sfh.lib.exception.HandleException;
import com.sfh.lib.mvvm.IViewModel;
import com.sfh.lib.mvvm.data.UIData;
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


    @UiThread
    public <T> void setValue(String action, T t) {
        this.setValue(new UIData(action, t));
    }


    @UiThread
    public void setValue(String action) {
        this.setValue(new UIData(action));
    }


    private <T> void setValue(T t) {
        this.mLiveData.setValue(t);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.mRxBus.onCleared();
        this.mRetrofit.clearAll();
    }


    public void putDisposable(Disposable disposable) {
        this.mRetrofit.put(disposable);
    }


    public <T> void execute(@NonNull Observable<T> observable, @Nullable IResult<T> observer) {
        this.mRetrofit.execute(observable, observer);
    }


    public <T> void execute(@NonNull Flowable<T> observable, @Nullable  IResult<T> observer) {

        this.mRetrofit.execute(observable, observer);
    }

    public <T> void execute(boolean cancelDialog, @NonNull Observable<T> observable, @Nullable final IResult<T> observer) {
        this.showLoading(cancelDialog);
        this.mRetrofit.execute(observable, new IResult<T>() {
            @Override
            public void onSuccess(T t) throws Exception {
                BaseViewModel.this.hideLoading();
                if (observer != null) {
                    observer.onSuccess(t);
                }
            }

            @Override
            public void onFail(HandleException e) {
                BaseViewModel.this.hideLoading();
                if (observer != null) {
                    observer.onFail(e);
                }
            }
        });
    }

    public <T> void execute(boolean cancelDialog, @NonNull Flowable<T> observable, @Nullable final IResult<T> observer) {
        this.showLoading(cancelDialog);
        this.mRetrofit.execute(observable, new IResult<T>() {
            @Override
            public void onSuccess(T t) throws Exception {
                BaseViewModel.this.hideLoading();
                if (observer != null) {
                    observer.onSuccess(t);
                }
            }

            @Override
            public void onFail(HandleException e) {
                BaseViewModel.this.hideLoading();
                if (observer != null) {
                    observer.onFail(e);
                }
            }
        });
    }

    /***
     * 显示等待对话框
     * @param cancel true 可以取消默认值 false 不可以取消
     */
    public void showLoading(boolean cancel) {
        this.setValue(NetWorkState.showLoading(cancel));
    }

    /***
     *隐藏等待对话框
     */
    public void hideLoading() {
        this.setValue(NetWorkState.hideLoading());
    }

    /***
     * 显示提示对话框
     * @param dialog 提示信息
     */
    public void showDialog(DialogBuilder dialog) {
        this.setValue(NetWorkState.showDialog(dialog));
    }


    /***
     * Toast提示(正常提示)
     */
    public void showToast(CharSequence msg) {
        this.setValue(NetWorkState.showToast(msg));
    }


}
