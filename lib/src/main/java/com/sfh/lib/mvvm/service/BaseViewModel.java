package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.sfh.lib.event.RxBusEventManager;
import com.sfh.lib.event.RxBusRegistry;
import com.sfh.lib.exception.HandleException;
import com.sfh.lib.mvvm.IViewModel;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.rx.IResultSuccess;
import com.sfh.lib.rx.IResultSuccessNoFail;
import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.ui.dialog.DialogBuilder;
import com.sfh.lib.utils.UtilLog;

import java.lang.reflect.Method;

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

    private volatile SparseArray<Method> mLiveDataMethod;

    private RetrofitManager mRetrofit;

    private RxBusRegistry mRxBus;

    private final MutableLiveData mLiveData = new MutableLiveData();

    public BaseViewModel() {

        this.mLiveDataMethod = new SparseArray<>(3);
        this.mRetrofit = new RetrofitManager();
        // 注入ViewModel层之间数据通信
        if (this.eventOnOff()) {
            this.mRxBus = new RxBusRegistry();
            this.mRxBus.registry(this);
        }
    }

    /***
     * 消息监听开关 【默认关闭】
     * @return
     */
    public boolean eventOnOff() {
        return false;
    }

    @Override
    public MutableLiveData getLiveData() {
        return this.mLiveData;
    }


    /***
     * 刷新UI 数据
     * @param action
     * @param data
     */
    @MainThread
    public void setValue(String action, Object... data) {

        Method method = this.mLiveDataMethod.get(action.hashCode());
        if (method == null) {
            return;
        }
        this.mLiveData.setValue(new UIData(method, data));
    }


    @MainThread
    public void setValue(String action) {
        Method method = this.mLiveDataMethod.get(action.hashCode());
        if (method == null) {
            return;
        }
        this.mLiveData.setValue(new UIData(method));
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        if (this.mRxBus != null) {
            this.mRxBus.onCleared();
            this.mRxBus = null;
        }
        this.mRetrofit.clearAll();
    }

    @Override
    public void putLiveDataMethod(Method method) {
        this.mLiveDataMethod.put(method.getName().hashCode(), method);
    }

    public final void putDisposable(Disposable disposable) {
        this.mRetrofit.put(disposable);
    }


    /***
     * 执行异步任务 【无执行对话框】【需处理成功,异常】
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(@NonNull Observable<T> observable, @Nullable IResult<T> observer) {
        this.mRetrofit.execute(observable, observer);
    }

    /**
     * 执行异步任务【无遮挡对话框】【只处理成功结果，异常以对话框形式提示】
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(@NonNull Observable<T> observable, @Nullable final IResultSuccess<T> observer) {
        this.mRetrofit.execute(observable, new IResult<T>() {
            @Override
            public void onSuccess(T t) throws Exception {
                if (observer != null){
                    observer.onSuccess(t);
                }
            }

            @Override
            public void onFail(HandleException e) {
                showDialogToast(e.getMsg());
            }
        });
    }

    /***
     *  执行异步任务 【无遮挡对话框】【只处理成功结果，异常不提示】
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(@NonNull Observable<T> observable, @Nullable final IResultSuccessNoFail<T> observer) {
        this.mRetrofit.execute(observable, new IResult<T>() {
            @Override
            public void onSuccess(T t) throws Exception {
                if (observer != null){
                    observer.onSuccess(t);
                }
            }

            @Override
            public void onFail(HandleException e) {
                UtilLog.e(TAG,e.toString());
            }
        });
    }

    /***
     * 执行异步任务 【无遮挡对话框】【需处理成功,异常】
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(@NonNull Flowable<T> observable, @Nullable IResult<T> observer) {
        this.mRetrofit.execute(observable, observer);
    }

    /***
     * 执行异步任务【无遮挡对话框】【只处理成功结果，异常以对话框形式提示】
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(@NonNull Flowable<T> observable, @Nullable final IResultSuccess<T> observer) {
        this.mRetrofit.execute(observable,  new IResult<T>() {
            @Override
            public void onSuccess(T t) throws Exception {
                if (observer != null){
                    observer.onSuccess(t);
                }
            }

            @Override
            public void onFail(HandleException e) {
                showDialogToast(e.getMsg());
            }
        });
    }


    /***
     * 执行异步任务 【无遮挡对话框】【只处理成功结果，异常不提示】
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(@NonNull Flowable<T> observable, @Nullable final IResultSuccessNoFail<T> observer) {
        this.mRetrofit.execute(observable,  new IResult<T>() {
            @Override
            public void onSuccess(T t) throws Exception {
                if (observer != null){
                    observer.onSuccess(t);
                }
            }

            @Override
            public void onFail(HandleException e) {
                UtilLog.e(TAG,e.toString());
            }
        });
    }

    /***
     * 执行异步任务【有遮挡对话框】【需处理成功,异常】
     * @param cancelDialog true 遮挡对话框可取消，false 遮挡对话框不可取消
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Observable<T> observable, @Nullable final IResult<T> observer) {
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
     * 执行异步任务 【有遮挡对话框】【只处理成功结果，异常以对话框形式提示】
     * @param cancelDialog true 遮挡对话框可取消，false 遮挡对话框不可取消
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Observable<T> observable, @Nullable final IResultSuccess<T> observer) {
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
                showDialogToast(e.getMsg());
            }
        });
    }

    /***
     * 执行异步任务 【有遮挡对话框】【只处理成功结果，异常不提示】
     * @param cancelDialog true 遮挡对话框可取消，false 遮挡对话框不可取消
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Observable<T> observable, @Nullable final IResultSuccessNoFail<T> observer) {
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
                UtilLog.e(TAG,e.toString());
            }
        });
    }

    /***
     * 执行异步任务【有遮挡对话框】【需处理成功,异常】
     * @param cancelDialog true 遮挡对话框可取消，false 遮挡对话框不可取消
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Flowable<T> observable, @Nullable final IResult<T> observer) {
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
     * 执行异步任务【有遮挡对话框】【只处理成功结果，异常以对话框形式提示】
     * @param cancelDialog true 遮挡对话框可取消，false 遮挡对话框不可取消
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Flowable<T> observable, @Nullable final IResultSuccess<T> observer) {
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
                showDialogToast(e.getMsg());
            }
        });
    }

    /***
     * 执行异步任务 【有遮挡对话框】【只处理成功结果，异常不提示】
     * @param cancelDialog true 遮挡对话框可取消，false 遮挡对话框不可取消
     * @param observable
     * @param observer
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Flowable<T> observable, @Nullable final IResultSuccessNoFail<T> observer) {
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
                UtilLog.e(TAG,e.toString());
            }
        });
    }

    /***
     * 显示等待对话框
     * @param cancel true 可以取消默认值 false 不可以取消
     */
    public final void showLoading(boolean cancel) {
        this.mLiveData.setValue(NetWorkState.showLoading(cancel));
    }

    /***
     *隐藏等待对话框
     */
    public final void hideLoading() {
        this.mLiveData.setValue(NetWorkState.hideLoading());
    }

    /***
     * 显示提示对话框
     * @param dialog 提示信息
     */
    public final void showDialog(DialogBuilder dialog) {
        this.mLiveData.setValue(NetWorkState.showDialog(dialog));
    }


    /***
     * Toast提示(正常提示)
     */
    public final void showToast(CharSequence msg) {
        this.mLiveData.setValue(NetWorkState.showToast(msg));
    }

    /***
     * Toast提示(正常提示)
     */
    public final void showDialogToast(CharSequence msg) {

        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setMessage(msg);
        dialogBuilder.setHideCancel(true);
        showDialog(dialogBuilder);
    }

    /***
     * 发送Rx消息通知
     * @param t
     * @param <T>
     */
    public final <T> void postEvent(T t) {
        RxBusEventManager.postEvent(t);
    }
}
