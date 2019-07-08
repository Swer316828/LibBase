package com.sfh.lib.event;


import com.sfh.lib.utils.UtilLog;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 功能描述:结果处理[需要把异常处理成自定义异常]
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
class RxEventObserver<T> implements Observer<T> {

    private IEventResult<T> result;

    public RxEventObserver(IEventResult<T> result) {
        this.result = result;
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (result != null) {
            result.onSubscribe(d);
        }
    }

    @Override
    public void onNext(T t) {

        if (result != null) {
            try {
                result.onEventSuccess(t);
            } catch (Exception e) {
                UtilLog.w(RxEventObserver.class, "RxJava RxEventObserver.class onNext() Exception:" + e);
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        UtilLog.w(RxEventObserver.class, "RxJava RxEventObserver.class onError:" + e);
    }

    @Override
    public void onComplete() {

    }
}
