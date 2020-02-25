package com.sfh.lib.mvvm;


import android.support.annotation.NonNull;


import com.sfh.lib.HandleException;
import com.sfh.lib.IResult;
import com.sfh.lib.IResultComplete;
import com.sfh.lib.IResultSuccess;
import com.sfh.lib.IResultSuccessNoFail;
import com.sfh.lib.event.BusEventManager;
import com.sfh.lib.utils.ThreadTaskUtils;
import com.sfh.lib.utils.ZLog;
import com.sfh.lib.ui.DialogBuilder;

import java.lang.reflect.Method;
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
public class BaseViewModel extends AbstractVM {

    private final static String TAG = BaseViewModel.class.getName();

    /***监听任务*/
    protected IShowDataListener mShowDataListener;

    public BaseViewModel() {
        if (this.eventOnOff()) {
            // 线程处理
            Future futureTask = ThreadTaskUtils.execute(this);
            this.putFuture(futureTask);
        }
    }

    public void setShowDataListener(IShowDataListener mShowDataListener) {
        this.mShowDataListener = mShowDataListener;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.mShowDataListener = null;
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
    public Object call() throws Exception {
        this.loadMethods(this);
        return true;
    }

    @Override
    public void setEventSuccess(Method method, Object data) {
        try {
            method.invoke(this, data);
        } catch (Exception e) {
            ZLog.d("setEventSuccess() Exception:%s", e);
        }
    }

    /***
     * 发送Rx消息通知
     * @param t
     */
    public boolean postEvent(Object t) {

        return BusEventManager.postEvent(t);
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
    public <T> void execute(final Callable<T> request, IResultSuccess<T> listener) {

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
    public <T> void executeTasks(final IResultSuccess<T> listener, final IResultComplete complete, final Callable<T>... taskList) {

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
    public <T> void execute(boolean cancelDialog, final Callable<T> request, IResultSuccess<T> listener) {
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
    public <T> void executeTasks(boolean cancelDialog, final IResultSuccess<T> listener, final IResultComplete complete, final Callable<T>... taskList) {

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
    private class Task<T> extends FutureTask<T> implements IResult<T> {

        IResultSuccess<T> listener;

        public Task(@NonNull Callable<T> callable) {
            super(callable);
        }


        public Task(@NonNull Callable<T> callable, IResultSuccess<T> listener) {
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
            if (listener instanceof IResult) {
                //回调处理异常失败
                ((IResult) listener).onFail(e);

            } else if (listener instanceof IResultSuccessNoFail) {
                //不处理异常失败
                ZLog.d(TAG, "onFail:" + e.toString());

            } else if (listener instanceof IResultSuccess) {
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

    /***
     * 刷新UI 数据
     * @param action
     * @param data
     */
    public void setValue(String action, Object... data) {

        if (this.mShowDataListener != null && this.mActive) {
            this.mShowDataListener.call(action, data);
        } else {
            ZLog.d("setValue() mShowDataListener is NULL, methodName:%s", action);
        }
    }

    /***
     * 显示等待对话框
     * @param cancel true 可以取消默认值 false 不可以取消
     */
    public void showLoading(boolean cancel) {
        if (this.mShowDataListener != null && this.mActive) {
            this.mShowDataListener.showLoading(cancel);
        }
    }

    /***
     *隐藏等待对话框
     */
    public void hideLoading() {

        if (this.mShowDataListener != null && this.mActive) {
            this.mShowDataListener.hideLoading();
        }
    }

    /***
     * 显示提示对话框
     * @param dialog 提示信息
     */
    public void showDialog(DialogBuilder dialog) {
        if (this.mShowDataListener != null && this.mActive) {
            this.mShowDataListener.showDialog(dialog);
        }
    }


    /***
     * Toast提示(正常提示)
     */
    public void showToast(CharSequence msg) {

        if (this.mShowDataListener != null && this.mActive) {
            this.mShowDataListener.showToast(msg);
        }
    }

    /***
     * Toast提示(正常提示)
     */
    public void showDialogToast(CharSequence msg) {
        if (this.mShowDataListener != null && this.mActive) {
            this.mShowDataListener.showDialogToast(msg);
        }
    }


}
