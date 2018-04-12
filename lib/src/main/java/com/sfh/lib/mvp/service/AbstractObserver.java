package com.sfh.lib.mvp.service;


import com.sfh.lib.http.service.HandleException;

import io.reactivex.functions.Consumer;

/**
 * 功能描述:结果处理
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public abstract class AbstractObserver<T> implements Consumer<T>{

    /**
     * 处理成功
     * @param result
     * @throws Exception
     */
    public  abstract void onSuccess(T result) throws Exception;

    /**
     * 处理失败
     * @param e
     */
    public  abstract void onError(HandleException e);


    @Override
    public void accept(T o) throws Exception {
        this.onSuccess(o);
    }

    public Consumer<Throwable> throwable() {
        return new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                AbstractObserver.this.onError((HandleException) throwable);
            }
        };
    }


}
