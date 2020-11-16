package com.sfh.lib.mvvm;


import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;


import com.sfh.lib.exception.HandleException;
import com.sfh.lib.Result;
import com.sfh.lib.ResultComplete;
import com.sfh.lib.ResultSuccess;
import com.sfh.lib.ResultSuccessNoFail;
import com.sfh.lib.event.EventManager;
import com.sfh.lib.mvvm.hander.EventMethodFinder;
import com.sfh.lib.event.IEventListener;
import com.sfh.lib.mvvm.hander.EventMethod;
import com.sfh.lib.mvvm.hander.LiveEventMethodFinder;
import com.sfh.lib.mvvm.hander.MethodLinkedMap;
import com.sfh.lib.utils.ThreadTaskUtils;
import com.sfh.lib.utils.ThreadUIUtils;
import com.sfh.lib.utils.ZLog;
import com.sfh.lib.ui.DialogBuilder;
import com.sfh.lib.utils.thread.CompositeFuture;

import java.lang.reflect.Method;
import java.util.List;
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

    //任务管理
    protected final  CompositeFuture mCompositeFuture = new CompositeFuture();

    protected UILiveData mLiveData = new UILiveData();

    protected volatile boolean mActive = true;

    protected MethodLinkedMap mEventMethods;

    public BaseViewModel(IUIListener listener) {

        if (listener == null) {
            throw new IllegalArgumentException("BaseViewModel() IUIListener is NULL !");
        }

        mLiveData.observe(listener.getLifecycleOwner(),listener);

        if (this.eventOnOff()) {
            // 线程处理
            Future futureTask = ThreadTaskUtils.execute(this);
            mCompositeFuture.add(futureTask);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeFuture.clear();
        if (mEventMethods != null){
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
    public Boolean call() throws Exception {
        Future<MethodLinkedMap> futureTask = ThreadTaskUtils.execute(new LiveEventMethodFinder(this.getClass()));
        mCompositeFuture.add(futureTask);
        mEventMethods = futureTask.get();

        for (Class eventType : mEventMethods.getEventClass()) {
            if (mActive) {
                Future future = EventManager.register(eventType, this);
                mCompositeFuture.add(future);
            }
        }
        return Boolean.TRUE;
    }


    @Override
    public void onEventSuccess(Object event) {

        if (mLiveData != null && mEventMethods.containsKey(event.getClass().getName())) {
            try {
                Method method = mEventMethods.get(event.getClass().getName());
                method.invoke(this, event);
            } catch (Exception e) {
                ZLog.d("setEventSuccess() Exception:%s", e);
            }
        }

    }


    /***
     * 发送Rx消息通知
     * @param t
     */
    public boolean postEvent(Object t) {

        return EventManager.postEvent(t);
    }

    /*------------------------------------任务执行 start-------------------------------------------------------------------------------*/

    /***
     * 执行异步任务，任务执行无回调
     *
     * @param <T>
     */
    public <T> void execute(Callable<T> request) {
        this.execute(new Task<>(request));
    }

    public <T> void executeTasks(Callable<T>... taskList) {

        for (Callable<T> callable : taskList) {
            Future futureTask = ThreadTaskUtils.execute(callable);
            this.putFuture(futureTask);
        }
    }


    /**
     * 执行异步任务，任务回调成功方法，异常以对话框形式提示
     * <p>*【属于单次任务】:任务执行结果回调结束后，当前任务监听会自动注销释放</p>
     *
     * @param request  需执行任务对象
     * @param listener 接口为1:【IResultSuccess,错误信息以对话框形式进行提示】2:【 IResultSuccessNoFail时，错误信息在日志输出】3:【IResult时，需处理错误信息】
     * @param <T>
     */
    public <T> void execute(final Callable<T> request, ResultSuccess<T> listener) {

        this.execute(new Task<>(request, listener));
    }

    /***
     * 多任务执行，全部执行完成调用IResultComplete，
     *
     * @param listener
     * @param complete
     * @param taskList
     * @param <T>
     */
    public <T> void executeTasks(final ResultSuccess<T> listener, final ResultComplete complete, final Callable<T>... taskList) {

        ThreadTaskUtils.execute(new Runnable() {
            @Override
            public void run() {
                final CountDownLatch downLatch = new CountDownLatch(taskList.length);

                for (final Callable<T> task : taskList) {

                    ThreadTaskUtils.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                listener.onSuccess(task.call());
                            } catch (Exception e) {
                                ZLog.d("executeTasks() in child run() Exception:%s", e);
                            } finally {
                                downLatch.countDown();
                            }
                        }
                    });
                }
                try {
                    downLatch.await();
                } catch (InterruptedException e) {
                    ZLog.d("executeTasks() InterruptedException:%s", e);
                } finally {
                    complete.complete();
                }
            }
        });
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
    public <T> void execute(boolean cancelDialog, final Callable<T> request, ResultSuccess<T> listener) {
        this.execute(new TaskLoading<>(cancelDialog, request, listener));
    }

    /***
     * 多任务执行，全部执行完成调用IResultComplete，
     *
     * @param listener
     * @param complete
     * @param taskList
     * @param <T>
     */
    public <T> void executeTasks(boolean cancelDialog, final ResultSuccess<T> listener, final ResultComplete complete, final Callable<T>... taskList) {

        this.showLoading(cancelDialog);

        ThreadTaskUtils.execute(new Runnable() {
            @Override
            public void run() {
                final CountDownLatch downLatch = new CountDownLatch(taskList.length);

                for (final Callable<T> task : taskList) {

                    ThreadTaskUtils.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                listener.onSuccess(task.call());
                            } catch (Exception e) {
                                ZLog.d("executeTasks() in child run() Exception:%s", e);
                            } finally {
                                downLatch.countDown();
                            }
                        }
                    });
                }
                try {
                    downLatch.await();
                } catch (InterruptedException e) {
                    ZLog.d("executeTasks() InterruptedException:%s", e);
                } finally {
                    hideLoading();
                    complete.complete();
                }
            }
        });
    }

    private <T> void execute(Task<T> task) {

        ThreadTaskUtils.execute(task);
    }


    /***
     * 交互处理
     * @param <T>
     */
    private class TaskLoading<T> extends Task<T> {

        public TaskLoading(boolean cancelDialog, Callable<T> callable, ResultSuccess<T> listener) {

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
    private class Task<T> extends FutureTask<T> implements Result<T> {

        ResultSuccess<T> listener;

        public Task(@NonNull Callable<T> callable) {
            super(callable);
        }


        public Task(@NonNull Callable<T> callable, ResultSuccess<T> listener) {
            super(callable);
            this.listener = listener;
        }

        @Override
        protected void done() {

            if (this.isCancelled()) {
                ZLog.w(TAG, " Task is isCancelled() " + this.isCancelled());
                return;
            }
            try {
                this.onSuccess(get());
            } catch (Exception e) {
                if (e instanceof CancellationException) {
                    ZLog.w(TAG, " Task is done()  CancellationException : " + e);
                } else {
                    this.onFail(HandleException.handleException(e));
                }

            }
        }

        @Override
        public void onFail(HandleException e) {
            ZLog.w(TAG, "TaskLinstener Task " + this + " onFail() e:" + e);
            if (listener instanceof Result) {
                //回调处理异常失败
                ((Result) listener).onFail(e);

            } else if (listener instanceof ResultSuccessNoFail) {
                //不处理异常失败
                ZLog.d(TAG, "onFail:" + e.toString());

            } else if (listener instanceof ResultSuccess) {
                //对话框形式提示异常失败
                BaseViewModel.this.showDialogToast(e.getMessage());
            }
        }

        @Override
        public void onSuccess(T result) throws Exception {
            if (this.listener != null) {
                this.listener.onSuccess(result);
            }
        }


    }



    /* ---------------------------------------------------------------- 消息监听处理 刷新UI 数据管理------------------------------------------------------------------ */

    public  void setValue(String action, Object... args) {

        this.call(action, args);
    }


    public  void call(String method, Object... args) {

        this.mLiveData.call(method,args);

    }

    public void showLoading(boolean cancel) {
        this.mLiveData.call("showLoading",cancel);
    }

    public  void hideLoading() {
        this.mLiveData.call("hideLoading");
    }

    public void showDialog(DialogBuilder builder) {
        this.mLiveData.call("showDialog",builder);
    }

    public void showToast(CharSequence msg) {

        this.mLiveData.call("showToast",msg);
    }

    public void showToast(CharSequence msg, int duration) {
        this.mLiveData.call("showToast",msg,duration);
    }

    public void showDialogToast(CharSequence msg) {
        this.mLiveData.call("showDialogToast",msg);
    }

    public boolean putFuture(Future future) {
        return mCompositeFuture.add(future);
    }

}
