package com.sfh.lib.rx;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sfh.lib.exception.HandleException;
import com.sfh.lib.utils.UtilLog;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 功能描述: Retrofit 处理
 *
 * @author sunfeihu
 * @date 2017/7/19
 */
public final class RetrofitManager {

    /***
     * 异步请求操作
     * @param observable
     * @param <T>
     * @return
     */
    public static <T> Disposable executeSigin(@NonNull Observable<T> observable, @Nullable IResult<T> result) {

        RxObserver<T> subscribe = new RxObserver(result);
        return compose(observable).subscribe(subscribe, subscribe.getOnError(), subscribe);
    }


    /***
     * [背压]异步请求操作
     * @param flowable
     * @param <T>
     * @return
     */
    public static <T> Disposable executeSigin(@NonNull Flowable<T> flowable, @Nullable IResult<T> result) {

        RxObserver<T> subscribe = new RxObserver(result);
        return compose(flowable).subscribe(subscribe, subscribe.getOnError(), subscribe);
    }

    /***
     * 线程调度
     * @param observable
     * @param <T>
     * @return
     */
    public static <T> Observable<T> compose(@NonNull Observable<T> observable) {

        return observable.compose((ObservableTransformer<T, T>) upstream -> upstream.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(new ThrowableFunc()));
    }


    /***
     * 线程调度
     * @param observable
     * @param <T>
     * @return
     */
    public static <T> Flowable<T> compose(@NonNull Flowable<T> observable) {

        return observable.compose(upstream -> upstream.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())).onErrorResumeNext(new ThrowableFunc());
    }

    /***
     * 异常处理类
     */
    public static class ThrowableFunc<E extends Throwable> implements Function<E, Observable<HandleException>> {

        @Override
        public Observable<HandleException> apply(E throwable) {
            // 封装成自定义异常对象
            UtilLog.w("", "RxJava ThrowableFunc.class throwable:" + throwable);
            return Observable.error(HandleException.handleException(throwable));
        }
    }
}
