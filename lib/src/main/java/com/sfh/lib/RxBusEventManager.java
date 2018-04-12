package com.sfh.lib;


import android.support.annotation.NonNull;


import com.sfh.lib.utils.UtilLog;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * 功能描述:总线事件
 * 1.消息通知
 * 2.数据回传
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/3/29
 */
public final class RxBusEventManager {

    private final static class Hondler {

        private static final RxBusEventManager EVENT = new RxBusEventManager();
    }

    RxBusEventManager() {
    }

    /***
     * 用RxJava实现事件总线
     */
    private Subject<Object> bus = PublishSubject.create().toSerialized();

    /***
     * 发送一个新的事件
     * @param data 数据对象
     */
    public static <T> void postEvent(@NonNull T data) {
        Hondler.EVENT.bus.onNext(data);
    }

    /***
     * 注册【根据传递的 eventType 类型返回特定类型(eventType)的 被观察者 】
     * 对应的注销借口
     * @Link unRegisterRxBus()
     * @param eventClass  监听class类型
     * @param onNext 回调接口
     * @param <T> class类型
     * @return Disposable 需要手动销毁
     */
    public static  <T> Disposable register(@NonNull final Class<T> eventClass, @NonNull final Consumer<T> onNext) {

        return Hondler.EVENT.bus.ofType(eventClass).observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable throwable) throws Exception {
                //onError()被调用，订阅者和被订阅者的订阅关系就解除,需要重新注册
                register(eventClass, onNext);
                UtilLog.e(eventClass, "监听回调操作异常：" + throwable.toString());
            }
        });
    }

}
