package com.sfh.lib.rx;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import com.sfh.lib.exception.HandleException;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 功能描述: Retrofit 处理
 *
 * @author sunfeihu
 * @date 2017/7/19
 */
public final class RetrofitManager {


    private volatile CompositeDisposable serverList = new CompositeDisposable ();

    /***
     * 添加业务层控制监听
     * @param disposable
     */
    @Deprecated
    public void put(Disposable disposable) {

        this.serverList.add (disposable);
    }

    /***
     *  取消业务层控制监听
     */
    @Deprecated
    public void remove(Disposable disposable) {

        this.serverList.remove (disposable);
    }

    /***
     * 取消全部业务层监听
     */
    @Deprecated
    public void clearAll() {

        this.serverList.clear ();

    }

    /***
     * 异步请求操作
     * @param observable
     * @param <T>
     * @return
     */
    @Deprecated
    public <T> void execute(@NonNull Observable<T> observable, @Nullable IResult<T> result) {

        this.put (executeSigin (observable, result));
    }

    /***
     * [背压]异步请求操作
     * @param flowable
     * @param <T>
     * @return
     */
    @Deprecated
    public <T> void execute(@NonNull Flowable<T> flowable, @Nullable IResult<T> result) {

        this.put (executeSigin(flowable,result));
    }

    /***
     * 异步请求操作
     * @param observable
     * @param <T>
     * @return
     */
    public static <T> Disposable executeSigin(@NonNull Observable<T> observable, @Nullable IResult<T> result) {

        RxObserver<T> subscribe = new RxObserver (result);
        return compose (observable).subscribe (subscribe, subscribe.getOnError ());
    }


    /***
     * [背压]异步请求操作
     * @param flowable
     * @param <T>
     * @return
     */
    public static <T> Disposable executeSigin(@NonNull Flowable<T> flowable, @Nullable IResult<T> result) {

        RxObserver<T> subscribe = new RxObserver (result);
        return compose (flowable).subscribe (subscribe, subscribe.getOnError ());
    }

    /***
     * 线程调度
     * @param observable
     * @param <T>
     * @return
     */
    public static <T> Observable<T> compose(@NonNull Observable<T> observable) {

        return observable.compose (new ObservableTransformer<T, T> () {

            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {

                return upstream.subscribeOn (Schedulers.io ())
                        .unsubscribeOn (Schedulers.io ())
                        .observeOn (AndroidSchedulers.mainThread ())
                        .onErrorResumeNext (new ThrowableFunc ());
            }
        });
    }


    /***
     * 线程调度
     * @param observable
     * @param <T>
     * @return
     */
    public static <T> Flowable<T> compose(@NonNull Flowable<T> observable) {

        return observable.compose (new FlowableTransformer<T, T> () {

            @Override
            public Publisher<T> apply(Flowable<T> upstream) {

                return upstream.subscribeOn (Schedulers.io ())
                        .unsubscribeOn (Schedulers.io ())
                        .observeOn (AndroidSchedulers.mainThread ())
                        .onErrorResumeNext (new ThrowableFunc ());
            }
        });
    }

    /***
     * 异常处理类
     * @param <E>
     */
    public static class ThrowableFunc<E extends Throwable> implements Function<E, Observable<HandleException>> {

        @Override
        public Observable<HandleException> apply(E throwable) throws Exception {
            // 封装成自定义异常对象
            return Observable.error (HandleException.handleException (throwable));
        }
    }
}
