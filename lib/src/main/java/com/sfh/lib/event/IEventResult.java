package com.sfh.lib.event;

import io.reactivex.disposables.Disposable;

/**
 * 功能描述: 消息监听接口
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/8/8
 */
public interface IEventResult<T> {

    /***
     * 数据监听回调
     * @param t
     * @throws Exception
     */
    void onEventSuccess(T t) throws Exception;

    /***
     * 任务
     * @param d
     */
    void onSubscribe(Disposable d);
}
