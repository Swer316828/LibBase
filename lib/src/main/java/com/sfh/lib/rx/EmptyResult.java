package com.sfh.lib.rx;

import com.sfh.lib.exception.HandleException;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.utils.UtilLog;

import io.reactivex.disposables.Disposable;

/**
 * 功能描述:空任务
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/6/11
 */
public class EmptyResult<T> implements IResult<T> {

    Disposable disposable;

    public void addDisposable(Disposable disposable) {

        this.disposable = disposable;
        if (this.disposable != null) {
            RxJavaDisposableThrowableHandler.put(this, this.disposable);
        }
    }

    @Override
    public void onSuccess(T t) throws Exception {
        if (this.disposable != null) {
            RxJavaDisposableThrowableHandler.onRemove(this, this.disposable);
        }
    }

    @Override
    public void onFail(HandleException e) {

        UtilLog.w(EmptyResult.class.getName(), "RxJava EmptyResult.class onFail:" + e);
        if (this.disposable != null) {
            RxJavaDisposableThrowableHandler.onRemove(this, this.disposable);
        }
    }
}
