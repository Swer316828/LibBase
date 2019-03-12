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
import com.sfh.lib.http.IRxHttpClient;
import com.sfh.lib.http.transaction.OutreachRequest;
import com.sfh.lib.mvvm.IViewModel;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.rx.EmptyResult;
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

    private final static String TAG = BaseViewModel.class.getName ();

    private volatile SparseArray<Method> mLiveDataMethod;

    private RetrofitManager mRetrofit;

    private RxBusRegistry mRxBus;

    private final ObjectMutableLiveData mLiveData = new ObjectMutableLiveData ();

    public BaseViewModel() {

        this.mLiveDataMethod = new SparseArray<> (5);
        this.mRetrofit = new RetrofitManager ();
        // 注入ViewModel层之间数据通信
        if (this.eventOnOff ()) {
            this.mRxBus = new RxBusRegistry ();
            this.mRxBus.registry (this);
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

        Method method = this.mLiveDataMethod.get (action.hashCode ());
        if (method == null) {
            return;
        }
        this.mLiveData.setValue (new UIData (method, data));
    }


    @MainThread
    public void setValue(String action) {

        Method method = this.mLiveDataMethod.get (action.hashCode ());
        if (method == null) {
            return;
        }
        this.mLiveData.setValue (new UIData (method));
    }


    @Override
    protected void onCleared() {

        super.onCleared ();
        if (this.mRxBus != null) {
            this.mRxBus.onCleared ();
            this.mRxBus = null;
        }
        this.mRetrofit.clearAll ();
    }

    @Override
    public void putLiveDataMethod(Method method) {

        this.mLiveDataMethod.put (method.getName ().hashCode (), method);
    }

    public final void putDisposable(Disposable disposable) {

        this.mRetrofit.put (disposable);
    }


    /***
     * 创建Retrofit 请求接口
     * @param client 网络连接服务
     * @param cls 接口类
     * @param <T>
     * @return
     */
    public <T> T builderRetrofitHttpClient(IRxHttpClient client, Class<T> cls) {

        return client.getRxHttpService (cls);
    }

    /***
     * 执行异步任务，任务执行无回调
     * @param task
     * @param <T>
     */
    public final <T> void execute(@NonNull Observable<T> task) {

        this.mRetrofit.execute (task, new EmptyResult ());
    }

    /***
     * 执行异步任务，任务回调成功/异常方法
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(@NonNull Observable<T> task, @Nullable IResult<T> listener) {

        this.mRetrofit.execute (task, listener);
    }

    /**
     * 执行异步任务，任务回调成功方法，异常以对话框形式提示
     *
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(@NonNull Observable<T> task, @Nullable final IResultSuccess<T> listener) {

        this.executeTask(task, new Task<> (listener));
    }

    /***
     *  执行异步任务，任务回调成功方法，异常丢弃
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(@NonNull Observable<T> task, @Nullable final IResultSuccessNoFail<T> listener) {

        this.executeTask(task, new Task<> (listener));
    }

    /***
     * 执行异步等待任务，任务回调成功/异常方法
     * @param cancelDialog true 可取消，false 不可取消
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Observable<T> task, @Nullable final IResult<T> listener) {

        this.executeTask(task, new TaskLoading<> (cancelDialog,listener));
    }

    /***
     * 执行异步等待任务，任务回调成功方法，异常以对话框形式提示
     * @param cancelDialog true 可取消，false 不可取消
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Observable<T> task, @Nullable final IResultSuccess<T> listener) {

        this.executeTask(task, new TaskLoading<> (cancelDialog,listener));
    }

    /***
     * 执行异步等待任务 任务回调成功方法，异常丢弃
     * @param cancelDialog true 可取消，false 不可取消
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Observable<T> task, @Nullable final IResultSuccessNoFail<T> listener) {

        this.executeTask(task, new TaskLoading<> (cancelDialog,listener));
    }

    private <T> void executeTask( @NonNull Observable<T> observable, Task<T> listener) {

        this.mRetrofit.execute (observable, listener);
    }

    /*------------------------------------Flowable 模式任务 start-------------------------------------------------------------------------------*/
    /***
     * 执行背压异步任务，任务回调成功/异常方法
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(@NonNull Flowable<T> task, @Nullable IResult<T> listener) {

        this.mRetrofit.execute (task, listener);
    }

    /***
     * 执行背压异步任务，任务回调成功方法，异常以对话框形式提示
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(@NonNull Flowable<T> task, @Nullable final IResultSuccess<T> listener) {

        this.executeTask(task, new Task<> (listener));
    }


    /***
     * 执行背压异步任务，任务回调成功方法，异常丢弃
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(@NonNull Flowable<T> task, @Nullable final IResultSuccessNoFail<T> listener) {

        this.executeTask(task, new Task<> (listener));
    }

    /***
     * 执行背压异步等待任务，任务回调成功/异常方法
     * @param cancelDialog true 可取消，false 不可取消
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Flowable<T> task, @Nullable final IResult<T> listener) {

        this.executeTask(task, new TaskLoading<> (cancelDialog,listener));
    }

    /***
     * 执行背压异步等待任务，任务回调成功方法，异常以对话框形式提示
     * @param cancelDialog true 可取消，false 不可取消
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Flowable<T> task, @Nullable final IResultSuccess<T> listener) {

        this.executeTask(task, new TaskLoading<> (cancelDialog,listener));
    }

    /***
     * 执行背压异步等待任务，任务回调成功方法，异常丢弃
     * @param cancelDialog true 可取消，false 不可取消
     * @param task
     * @param listener
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Flowable<T> task, @Nullable final IResultSuccessNoFail<T> listener) {

        this.executeTask(task, new TaskLoading<> (cancelDialog,listener));
    }

    private <T> void executeTask( @NonNull Flowable<T> flowable, Task<T> listener) {

        this.mRetrofit.execute (flowable, listener);
    }
    /*------------------------------------新的请求方式 start-------------------------------------------------------------------------------*/
    /***
     * 执行异步任务，任务执行无回调
     * @param <T>
     */
    public final <T> void execute(@NonNull OutreachRequest<T> request) {

        request.sendRequest (new EmptyResult<T> ());
    }

    /***
     * 执行异步任务 任务回调成功/异常方法
     * @param listener
     * @param <T>
     */
    public final <T> void execute(@NonNull OutreachRequest<T> request, @Nullable IResult<T> listener) {

        request.sendRequest (listener);
    }

    /**
     * 执行异步任务，任务回调成功方法，异常以对话框形式提示
     *
     * @param request
     * @param listener
     * @param <T>
     */
    public final <T> void execute(@NonNull OutreachRequest<T> request, @Nullable final IResultSuccess<T> listener) {
        this.executeTask (request,new Task<> (listener));
    }

    /***
     *  执行异步任务，任务回调成功方法，异常丢弃
     * @param request
     * @param listener
     * @param <T>
     */
    public final <T> void execute(@NonNull OutreachRequest<T> request, @Nullable final IResultSuccessNoFail<T> listener) {

        this.executeTask (request,new Task<> (listener));
    }

    /***
     * 执行异步等待任务，任务回调成功/异常方法
     * @param cancelDialog true 可取消，false 不可取消
     * @param request
     * @param listener
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull OutreachRequest<T> request, @Nullable final IResult<T> listener) {

        this.executeTask (request,new TaskLoading<> (cancelDialog,listener));
    }

    /***
     * 执行异步等待任务，任务回调成功方法，异常以对话框形式提示
     * @param cancelDialog true 可取消，false 不可取消
     * @param request
     * @param listener
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull OutreachRequest<T> request, @Nullable final IResultSuccess<T> listener) {

        this.executeTask (request,new TaskLoading<> (cancelDialog,listener));
    }

    /***
     * 执行异步等待任务，任务回调成功方法，异常丢弃
     * @param cancelDialog true 可取消，false 不可取消
     * @param request
     * @param listener
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull OutreachRequest<T> request, @Nullable final IResultSuccessNoFail<T> listener) {
        this.executeTask (request,new TaskLoading<> (cancelDialog,listener));
    }


   private <T> void executeTask(@NonNull OutreachRequest<T> request, Task<T> task) {

        this.putDisposable (request.sendRequest (task));
    }


    class TaskLoading<T> extends Task<T>  {

        public TaskLoading(boolean cancelDialog, IResultSuccess<T> listener) {

            super(listener);
            BaseViewModel.this.showLoading (cancelDialog);
        }

        @Override
        public void onFail(HandleException e) {
            BaseViewModel.this.hideLoading ();
            super.onFail (e);
        }

        @Override
        public void onSuccess(T t) throws Exception {

            BaseViewModel.this.hideLoading ();
            super.onSuccess (t);
        }
    }

    class Task<T> implements IResult<T> {

        IResultSuccess<T> listener;

        public Task(IResultSuccess<T> listener) {

            this.listener = listener;
        }

        @Override
        public void onFail(HandleException e) {

            UtilLog.e (TAG, e.toString ());
            if (this.listener != null) {
                if (listener instanceof IResult) {
                    //回调处理异常失败
                    ((IResult) listener).onFail (e);
                } else if (listener instanceof IResultSuccessNoFail) {
                    //不处理异常失败
                } else {
                    //对话框形式提示异常失败
                    BaseViewModel.this.showDialogToast (e.getMsg ());
                }
            }
        }

        @Override
        public void onSuccess(T t) throws Exception {
            if (this.listener != null) {
                this.listener.onSuccess (t);
            }
        }
    }
    /***
     * 显示等待对话框
     * @param cancel true 可以取消默认值 false 不可以取消
     */
    public final void showLoading(boolean cancel) {

        this.mLiveData.setValue (NetWorkState.showLoading (cancel));
    }

    /***
     *隐藏等待对话框
     */
    public final void hideLoading() {

        this.mLiveData.setValue (NetWorkState.hideLoading ());
    }

    /***
     * 显示提示对话框
     * @param dialog 提示信息
     */
    public final void showDialog(DialogBuilder dialog) {

        this.mLiveData.setValue (NetWorkState.showDialog (dialog));
    }


    /***
     * Toast提示(正常提示)
     */
    public final void showToast(CharSequence msg) {

        this.mLiveData.setValue (NetWorkState.showToast (msg));
    }

    /***
     * Toast提示(正常提示)
     */
    public final void showDialogToast(CharSequence msg) {

        DialogBuilder dialogBuilder = new DialogBuilder ();
        dialogBuilder.setMessage (msg);
        dialogBuilder.setHideCancel (true);
        showDialog (dialogBuilder);
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
