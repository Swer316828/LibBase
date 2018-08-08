package com.sfh.lib.event;

import io.reactivex.disposables.Disposable;

/**
 * 功能描述: TODO
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/8/8
 */
public interface IEventResult<T> {
    void onEventSuccess(T t) throws Exception;

    void onSubscribe(Disposable d);
}
