package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.sfh.lib.event.IEventResult;
import com.sfh.lib.event.RxBusEvent;
import com.sfh.lib.event.RxBusEventManager;
import com.sfh.lib.exception.HandleException;
import com.sfh.lib.http.transaction.OutreachRequest;
import com.sfh.lib.mvvm.IViewModel;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.rx.EmptyResult;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.rx.IResultSuccess;
import com.sfh.lib.rx.IResultSuccessNoFail;
import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.rx.RxJavaDisposableThrowableHandler;
import com.sfh.lib.ui.dialog.DialogBuilder;
import com.sfh.lib.utils.UtilLog;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * 功能描述: 业务Model
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/30
 */
public class BaseViewModel extends ViewModel implements IViewModel, IEventResult, Function<String, Boolean> {

    private final static String TAG = BaseViewModel.class.getName();

    protected final CompositeDisposable mDisposableList;

    /*** 消息监听方法*/
    protected final SparseArray<Method> mEventMethod;

    /***监听任务*/
    protected ObjectMutableLiveData mLiveData;

    public BaseViewModel() {
        this.mDisposableList = new CompositeDisposable();
        this.mEventMethod = new SparseArray<>(5);
        // 注入ViewModel层之间数据通信
        if (this.eventOnOff()) {
            this.execute(Observable.just(TAG).map(this));
        }
    }

    @Override
    public final void putLiveData(ObjectMutableLiveData listener) {

        if (this.mLiveData != listener) {
            this.mLiveData = listener;
        }
    }

    @Override
    protected void onCleared() {

        super.onCleared();
        this.mEventMethod.clear();
        this.mDisposableList.clear();
        RxJavaDisposableThrowableHandler.clear(this);
    }

    /* ---------------------------------------------------------------- 消息监听处理 start------------------------------------------------------------------ */

    /***
     * 消息监听开关 【默认关闭】
     * @return
     */
    public boolean eventOnOff() {

        return false;
    }

    @Override
    public final Boolean apply(String data) throws Exception {

        final Method[] methods = this.getClass().getDeclaredMethods();

        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers)
                    || Modifier.isFinal(modifiers)
                    || Modifier.isAbstract(modifiers)
                    || Modifier.isStatic(modifiers)) {
                continue;
            }
            // 注册RxBus监听
            RxBusEvent event = method.getAnnotation(RxBusEvent.class);
            if (event == null) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> dataClass;
            if (parameterTypes != null && (dataClass = parameterTypes[0]) != null) {
                this.mEventMethod.put(dataClass.getSimpleName().hashCode(), method);
                RxBusEventManager.register(dataClass, this);
            }
        }
        return true;
    }

    @Override
    public final void onEventSuccess(Object data) throws Exception {
        // RxBus 消息监听
        Method eventMethod = this.mEventMethod.get(data.getClass().getSimpleName().hashCode());
        if (eventMethod != null) {
            //响应方法：当前ViewModel 监听方法
            eventMethod.invoke(this, data);
        }
    }

    @Override
    public final void onSubscribe(Disposable d) {

        this.mDisposableList.add(d);
    }

    /***
     * 发送Rx消息通知
     * @param t
     * @param <T>
     */
    public final <T> void postEvent(T t) {

        RxBusEventManager.postEvent(t);
    }

    public final void putDisposable(Disposable disposable) {

        this.mDisposableList.add(disposable);
    }

    /* ---------------------------------------------------------------- 任务执行 start------------------------------------------------------------------ */

    /***
     * 执行异步任务，任务执行无回调
     * <p>*【属于单次任务】:任务执行结果回调结束后，当前任务监听会自动注销释放</p>
     *
     * @param task 需执行任务对象
     * @param <T>
     */
    public final <T> void execute(@NonNull Observable<T> task) {

        EmptyResult result = new EmptyResult<T>();
        Disposable disposable = RetrofitManager.executeSigin(task, result);
        result.addDisposable(disposable);
    }

    /**
     * 执行异步任务，任务回调成功方法，异常以对话框形式提示
     * <p>*【属于单次任务】:任务执行结果回调结束后，当前任务监听会自动注销释放</p>
     *
     * @param task     需执行任务对象
     * @param listener 接口为1:【IResultSuccess,错误信息以对话框形式进行提示】2:【 IResultSuccessNoFail时，错误信息在日志输出】3:【IResult时，需处理错误信息】
     * @param <T>
     */
    public final <T> void execute(@NonNull Observable<T> task, @Nullable final IResultSuccess<T> listener) {

        this.executeTask(task, new Task<T>(listener));
    }

    /***
     * 执行异步等待任务，任务回调成功方法，异常以对话框形式提示
     * <p>*【属于单次任务】:任务执行结果回调结束后，当前任务监听会自动注销释放</p>
     *
     * @param cancelDialog true 可取消，false 不可取消
     * @param task 需执行任务对象
     * @param listener 接口为1:【IResultSuccess,错误信息以对话框形式进行提示】2:【 IResultSuccessNoFail时，错误信息在日志输出】3:【IResult时，需处理错误信息】
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Observable<T> task, @Nullable final IResultSuccess<T> listener) {

        this.executeTask(task, new TaskLoading<T>(cancelDialog, listener));
    }

    private final <T> void executeTask(@NonNull Observable<T> observable, Task<T> listener) {

        Disposable disposable = RetrofitManager.executeSigin(observable, listener);
        listener.addDisposable(disposable);
    }
    /*------------------------------------Flowable 模式任务 start-------------------------------------------------------------------------------*/

    /***
     * 执行背压异步任务，任务回调成功方法，异常以对话框形式提示
     * <p>*【属于单次任务】:任务执行结果回调结束后，当前任务监听会自动注销释放</p>
     *
     * @param task 需执行任务对象
     * @param <T>
     */
    public final <T> void execute(@NonNull Flowable<T> task) {

        EmptyResult result = new EmptyResult<T>();
        Disposable disposable = RetrofitManager.executeSigin(task, result);
        result.addDisposable(disposable);
    }

    /***
     * 执行背压异步任务，任务回调成功方法，异常以对话框形式提示
     *  <p>*【属于单次任务】:任务执行结果回调结束后，当前任务监听会自动注销释放</p>
     *
     * @param task 需执行任务对象
     * @param listener 接口为1:【IResultSuccess,错误信息以对话框形式进行提示】2:【 IResultSuccessNoFail时，错误信息在日志输出】3:【IResult时，需处理错误信息】
     * @param <T>
     */
    public final <T> void execute(@NonNull Flowable<T> task, @Nullable final IResultSuccess<T> listener) {

        this.executeTask(task, new Task<T>(listener));
    }

    /***
     * 执行背压异步等待任务，任务回调成功方法，异常以对话框形式提示
     *  <p>*【属于单次任务】:任务执行结果回调结束后，当前任务监听会自动注销释放</p>
     *
     * @param cancelDialog true 可取消，false 不可取消
     * @param task 需执行任务对象
     * @param listener 接口为1:【IResultSuccess,错误信息以对话框形式进行提示】2:【 IResultSuccessNoFail时，错误信息在日志输出】3:【IResult时，需处理错误信息】
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull Flowable<T> task, @Nullable final IResultSuccess<T> listener) {

        this.executeTask(task, new TaskLoading<T>(cancelDialog, listener));
    }

    private <T> void executeTask(@NonNull Flowable<T> flowable, Task<T> listener) {

        Disposable disposable = RetrofitManager.executeSigin(flowable, listener);
        listener.addDisposable(disposable);
    }
    /*------------------------------------新的请求方式 start-------------------------------------------------------------------------------*/

    /***
     * 执行异步任务，任务执行无回调
     *  <p>*任务执行结果回调结束后，当前任务监听会自动注销，属于单次任务</p>
     *
     * @param <T>
     */
    public final <T> void execute(@NonNull OutreachRequest<T> request) {

        EmptyResult result = new EmptyResult<T>();
        Disposable disposable = request.sendRequest(result);
        result.addDisposable(disposable);
    }

    /**
     * 执行异步任务，任务回调成功方法，异常以对话框形式提示
     * <p>*【属于单次任务】:任务执行结果回调结束后，当前任务监听会自动注销释放</p>
     *
     * @param request  需执行任务对象
     * @param listener 接口为1:【IResultSuccess,错误信息以对话框形式进行提示】2:【 IResultSuccessNoFail时，错误信息在日志输出】3:【IResult时，需处理错误信息】
     * @param <T>
     */
    public final <T> void execute(@NonNull OutreachRequest<T> request, @Nullable IResultSuccess<T> listener) {

        this.executeTask(request, new Task<T>(listener));
    }

    /***
     * 执行异步等待任务，任务回调成功方法，异常以对话框形式提示
     *  <p>*【属于单次任务】:任务执行结果回调结束后，当前任务监听会自动注销释放</p>
     *
     * @param cancelDialog true 可取消，false 不可取消
     * @param request 需执行任务对象
     * @param listener 接口为1:【IResultSuccess,错误信息以对话框形式进行提示】2:【 IResultSuccessNoFail时，错误信息在日志输出】3:【IResult时，需处理错误信息】
     * @param <T>
     */
    public final <T> void execute(boolean cancelDialog, @NonNull OutreachRequest<T> request, @Nullable IResultSuccess<T> listener) {

        this.executeTask(request, new TaskLoading<T>(cancelDialog, listener));
    }

    private <T> void executeTask(@NonNull OutreachRequest<T> request, Task<T> task) {

        Disposable disposable = request.sendRequest(task);
        task.addDisposable(disposable);
    }

    class TaskLoading<T> extends Task<T> {

        public TaskLoading(boolean cancelDialog, IResultSuccess<T> listener) {

            super(listener);
            BaseViewModel.this.showLoading(cancelDialog);
        }

        @Override
        public void onFail(HandleException e) {

            BaseViewModel.this.hideLoading();
            super.onFail(e);
        }

        @Override
        public void onSuccess(T t) throws Exception {

            BaseViewModel.this.hideLoading();
            super.onSuccess(t);
        }
    }

    class Task<T> implements IResult<T> {

        Disposable disposable;

        IResultSuccess<T> listener;

        public Task(IResultSuccess<T> listener) {
            this.listener = listener;
        }

        public void addDisposable(Disposable disposable) {

            this.disposable = disposable;
            if (this.disposable != null) {
                RxJavaDisposableThrowableHandler.put(this, this.disposable);
            }
        }

        @Override
        public void onFail(HandleException e) {

            if (this.listener != null) {
                if (listener instanceof IResult) {
                    //回调处理异常失败
                    ((IResult) listener).onFail(e);
                } else if (listener instanceof IResultSuccessNoFail) {
                    //不处理异常失败
                    UtilLog.i(TAG, "onFail:" + e.toString());
                } else {
                    //对话框形式提示异常失败
                    BaseViewModel.this.showDialogToast(e.getMsg());
                }
            }

            if (this.disposable != null) {
                RxJavaDisposableThrowableHandler.remove(this, this.disposable);
            }
        }

        @Override
        public void onSuccess(T t) throws Exception {

            if (this.listener != null) {
                this.listener.onSuccess(t);
            }
            if (this.disposable != null) {
                RxJavaDisposableThrowableHandler.remove(this, this.disposable);
            }
        }
    }

    /* ---------------------------------------------------------------- 消息监听处理 刷新UI 数据管理------------------------------------------------------------------ */

    /***
     * 刷新UI 数据
     * @param action
     * @param data
     */
    @MainThread
    public final void setValue(String action, Object... data) {

        if (this.mLiveData != null) {
            this.mLiveData.setValue(new UIData(action, data));
        }
    }


    @MainThread
    public final void setValue(String action) {

        if (this.mLiveData != null) {
            this.mLiveData.setValue(new UIData(action));
        }
    }


    /***
     * 显示等待对话框
     * @param cancel true 可以取消默认值 false 不可以取消
     */
    @MainThread
    public final void showLoading(boolean cancel) {

        if (this.mLiveData != null) {
            this.mLiveData.setValue(NetWorkState.showLoading(cancel));
        }
    }

    /***
     *隐藏等待对话框
     */
    @MainThread
    public final void hideLoading() {

        if (this.mLiveData != null) {
            this.mLiveData.setValue(NetWorkState.hideLoading());
        }

    }

    /***
     * 显示提示对话框
     * @param dialog 提示信息
     */
    @MainThread
    public final void showDialog(DialogBuilder dialog) {

        if (this.mLiveData != null) {
            this.mLiveData.setValue(NetWorkState.showDialog(dialog));
        }

    }


    /***
     * Toast提示(正常提示)
     */
    @MainThread
    public final void showToast(CharSequence msg) {

        if (this.mLiveData != null) {
            this.mLiveData.setValue(NetWorkState.showToast(msg));
        }

    }

    /***
     * Toast提示(正常提示)
     */
    @MainThread
    public final void showDialogToast(CharSequence msg) {

        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setMessage(msg);
        dialogBuilder.setHideCancel(true);
        this.showDialog(dialogBuilder);
    }


}
