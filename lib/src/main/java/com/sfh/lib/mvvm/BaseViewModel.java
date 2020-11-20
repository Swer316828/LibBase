package com.sfh.lib.mvvm;


import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.util.SparseArray;


import com.sfh.lib.annotation.EventMatch;
import com.sfh.lib.exception.HandleException;
import com.sfh.lib.event.EventManager;
import com.sfh.lib.event.IEventListener;
import com.sfh.lib.utils.ThreadTaskUtils;
import com.sfh.lib.utils.ZLog;
import com.sfh.lib.ui.DialogBuilder;
import com.sfh.lib.utils.thread.CompositeFuture;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


/**
 * 功能描述: 业务Model
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/30
 */
public class BaseViewModel extends ViewModel implements Callable<Boolean>, IEventListener {

    public final static String TAG = BaseViewModel.class.getName();
    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC;

    //任务管理
    protected final CompositeFuture mCompositeFuture = new CompositeFuture();

    protected final UILiveData mLiveData;

    private SparseArray<Method> mEventMethods;

    public BaseViewModel(UILiveData liveData) {

        if (liveData == null) {
            throw new IllegalArgumentException("BaseViewModel() UILiveData is NULL !");
        }
        mLiveData = liveData;

        if (this.eventOnOff()) {
            mEventMethods = new SparseArray<>(7);
            // 线程处理
            Future futureTask = ThreadTaskUtils.execute(this);
            mCompositeFuture.add(futureTask);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeFuture.clear();
        if (mEventMethods != null) {
            mEventMethods.clear();
        }
        ZLog.d("%s: onCleared() ", this.getClass().getSimpleName());
    }


    /* ---------------------------------------------------------------- 消息监听处理 start------------------------------------------------------------------ */

    /***
     * 消息监听开关 【默认关闭】
     * true 表示当前VM 接收信息通知
     * @return
     */
    public boolean eventOnOff() {

        return false;
    }

    @Override
    public void onEventSuccess(Object event) {
        if (mLiveData != null) {
            try {
                Method method = mEventMethods.get(event.getClass().getName().hashCode());
                method.invoke(this, event);
            } catch (Exception e) {
                ZLog.d("setEventSuccess() Exception:%s", e);
            }
        }
    }


    @Override
    public Boolean call() throws Exception {


        Method[] methods = this.getClass().getDeclaredMethods();

        for (Method method : methods) {

            int modifiers = method.getModifiers();

            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {

                Class<?>[] parameterTypes = method.getParameterTypes();

                if (parameterTypes.length == 1) {

                    EventMatch busEvent = method.getAnnotation(EventMatch.class);

                    if (busEvent != null) {
                        Class<?> eventType = parameterTypes[0];
                        mEventMethods.put(eventType.getName().hashCode(), method);

                        Future future = EventManager.register(eventType, this);
                        mCompositeFuture.add(future);
                    }
                }
            } else if (method.isAnnotationPresent(EventMatch.class)) {
                String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                throw new RuntimeException(methodName +
                        " is a illegal @BusEvent method: must be public, non-static, and non-abstract");
            }
        }

        return Boolean.TRUE;
    }

    /***
     * 发送Rx消息通知
     * @param t
     */
    public boolean postEvent(Object t) {

        return EventManager.postEvent(t);
    }

    /*------------------------------------任务执行 start-------------------------------------------------------------------------------*/


    public <T> FutureTask<T> execute(Callable<T> request) {

        return this.execute(new Task<>(request));
    }

    public <T> FutureTask<T> execute(final Callable<T> request, IResultSuccess<T> listener) {

        return this.execute(new Task<>(request, listener));
    }

    public <T> FutureTask<T> execute(boolean cancelDialog, final Callable<T> request, IResultSuccess<T> listener) {
        return this.execute(new TaskLoading<>(cancelDialog, request, listener));
    }

    private <T> FutureTask<T> execute(Task<T> task) {

        ThreadTaskUtils.execute(task);
        return task;
    }


    public <T> FutureTask<List<T>> executeTasks(Callable<T>... taskList) {

        FutureTask<List<T>> futureTask = new Task<>(new MulitTask<>(taskList));
        ThreadTaskUtils.execute(futureTask);
        return futureTask;
    }

    public <T> FutureTask<List<T>> executeTasks(IResultSuccess<List<T>> complete, Callable<T>... taskList) {

        FutureTask<List<T>> futureTask = new Task<>(new MulitTask<>(taskList),complete);
        ThreadTaskUtils.execute(futureTask);
        return futureTask;
    }

    public <T> FutureTask<List<T>> executeTasks(boolean cancelDialog, IResultSuccess<List<T>> complete, Callable<T>... taskList) {

        FutureTask<List<T>> futureTask = new TaskLoading<>(cancelDialog, new MulitTask<>(taskList), complete);
        ThreadTaskUtils.execute(futureTask);
        return futureTask;
    }


    /***
     * 多任务
     * @param <T>
     */
    class MulitTask<T> implements Callable<List<T>> {
        Callable<T>[] taskList;

        public MulitTask(Callable<T>... callables) {
            taskList = callables;
        }

        @Override
        public List<T> call() throws Exception {

            Vector<T> resultList = new Vector<>(taskList.length);

            CountDownLatch countDownLatch = new CountDownLatch(taskList.length);
            for (Callable<T> callable : taskList) {
                ThreadTaskUtils.execute(() -> {
                    T t = null;
                    try {
                        t = callable.call();
                        resultList.add(t);
                    } finally {
                        countDownLatch.countDown();
                    }
                    return t;
                });
            }
            countDownLatch.await();
            return resultList;
        }

    }

    /***
     * 交互处理
     * @param <T>
     */
    class TaskLoading<T> extends Task<T> {

        public TaskLoading(boolean cancelDialog, Callable<T> callable, IResultSuccess<T> listener) {

            super(callable, listener);
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

    /***
     * 无交互处理
     * @param <T>
     */
    class Task<T> extends FutureTask<T> implements IResult<T> {

        IResultSuccess<T> listener;

        volatile boolean isAdd;

        public Task(@NonNull Callable<T> callable) {
            super(callable);
        }


        public Task(@NonNull Callable<T> callable, IResultSuccess<T> listener) {
            super(callable);
            this.listener = listener;
            isAdd = true;
            BaseViewModel.this.mCompositeFuture.add(this);
        }

        @Override
        public void run() {
            super.run();
        }

        @Override
        protected void done() {

            try {
                if (this.isCancelled()) {
                    ZLog.w(TAG, " Task is isCancelled() " + this.isCancelled());
                    return;
                }

                this.onSuccess(get());

            } catch (Exception e) {

                if (e instanceof CancellationException) {
                    ZLog.e(TAG, " Task is done()  CancellationException : " + e);
                } else {
                    this.onFail(HandleException.handleException(e));
                }

            } finally {
                this.removeFuture();
            }
        }

        @Override
        public void onFail(HandleException e) {
            ZLog.e(TAG, "TaskLinstener Task " + this + " onFail() e:" + e);
            if (listener instanceof IResult) {
                //回调处理异常失败
                ((IResult) listener).onFail(e);

            } else if (listener instanceof IResultNoFailSuccess) {
                //不处理异常失败
                ZLog.e(TAG, "onFail:" + e.toString());

            } else if (listener instanceof IResultSuccess) {
                //对话框形式提示异常失败
                BaseViewModel.this.showDialogToast(e.getMessage());
            }

        }

        @Override
        public void onSuccess(T result) throws Exception {
            if (listener != null) {
                listener.onSuccess(result);
            }
        }

        protected void removeFuture(){
            if (isAdd){
                isAdd = false;
                BaseViewModel.this.mCompositeFuture.remove(this);
            }
        }

    }


    /* ---------------------------------------------------------------- 消息监听处理 刷新UI 数据管理------------------------------------------------------------------ */

    public void setValue(String action, Object... args) {

        this.mLiveData.call(action, args);
    }

    public void showLoading(boolean cancel) {
        this.mLiveData.call("showLoading", cancel);
    }

    public void hideLoading() {
        this.mLiveData.call("hideLoading");
    }


    public void showDialog(DialogBuilder builder) {
        this.mLiveData.call("showDialog", builder);
    }


    public void showToast(CharSequence msg) {

        this.mLiveData.call("showToast", msg);
    }

    public void showToast(CharSequence msg, int duration) {
        this.mLiveData.call("showToast", msg, duration);
    }

    public void showDialogToast(CharSequence msg) {
        this.mLiveData.call("showDialogToast", msg);
    }

    public boolean putFuture(Future future) {
        return mCompositeFuture.add(future);
    }

}
