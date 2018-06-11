package com.sfh.lib.mvp.service;

import android.support.annotation.NonNull;
import android.util.SparseArray;


import com.sfh.lib.http.service.HandleException;
import com.sfh.lib.mvp.IResult;
import com.sfh.lib.utils.UtilLog;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 功能描述: Retrofit 处理
 *
 * @author sunfeihu
 * @date 2017/7/19
 */
final class RetrofitManager {


    private volatile SparseArray<Disposable> serverList = new SparseArray<>(2);


    /***
     * 添加业务层控制监听
     * @param taskId 任务ID
     * @param disposable
     */
    public void put(int taskId, Disposable disposable) {

        this.remove(taskId);
        this.serverList.put(taskId, disposable);
    }

    /***
     *  取消业务层控制监听
     * @param taskId
     */
    public void remove(int taskId) {

        // 没有任务
        if (this.serverList.size() == 0) {
            return;
        }

        Disposable subscription = this.serverList.get(taskId);
        if (subscription != null) {
            subscription.dispose();
        }
        this.serverList.remove(taskId);
    }

    /***
     * 取消全部业务层监听
     */
    public void clearAll() {

        if (this.serverList.size() == 0) {
            return;
        }
        final int size = this.serverList.size();
        for (int i = 0; i <= size; i--) {
            Disposable subscription = this.serverList.valueAt(i);
            if (null != subscription) {
                subscription.dispose();
            }
        }
        this.serverList.clear();

    }

    /***
     * 异步请求操作
     * @param observable
     * @param <T>
     * @return
     */
    public <T> int execute(@NonNull Observable<T> observable, @NonNull final IResult<T> result) {

        Observer<T> subscribe = new Observer(result);
        final int taskId = subscribe.hashCode();
        Disposable disposable = observable.compose(new ObservableTransformer<T, T>() {

            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorResumeNext(new ThrowableFunc());
            }
        }).subscribe(subscribe, subscribe.onError(), new Action() {
            @Override
            public void run() throws Exception {
                remove(taskId);
            }
        });
        put(taskId, disposable);
        return taskId;
    }
    /***
     * [背压]异步请求操作
     * @param observable
     * @param <T>
     * @return
     */
    public <T> int execute(@NonNull Flowable<T> observable, @NonNull final IResult<T> result) {

        Observer<T> subscribe = new Observer(result);
        final int taskId = subscribe.hashCode();
        Disposable disposable = observable.compose(new FlowableTransformer<T, T>() {

            @Override
            public Publisher<T> apply(Flowable<T> upstream) {
               return upstream.subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorResumeNext(new ThrowableFunc());
            }
        }).subscribe(subscribe, subscribe.onError(), new Action() {
            @Override
            public void run() throws Exception {
                remove(taskId);
            }
        });
        put(taskId, disposable);
        return taskId;
    }


    /***
     * 异常处理类
     * @param <E>
     */
    class ThrowableFunc<E extends Throwable> implements Function<E, Observable<HandleException>> {
        @Override
        public Observable<HandleException> apply(E throwable) throws Exception {
            // 封装成自定义异常对象
            return Observable.error(HandleException.handleException(throwable));
        }
    }
}
