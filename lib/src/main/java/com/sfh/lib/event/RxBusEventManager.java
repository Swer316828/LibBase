package com.sfh.lib.event;


import android.support.annotation.NonNull;


import io.reactivex.android.schedulers.AndroidSchedulers;
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
        if (data == null) {
            return;
        }
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
    public static <T> void register(@NonNull final Class<T> eventClass, @NonNull final IEventResult<T> onNext) {
        if (eventClass == null) {
            throw new NullPointerException("Class<T> eventClass is null");
        }
        if (onNext == null) {
            throw new NullPointerException("IEventResult<T> onNext is null");
        }
        Hondler.EVENT.bus.ofType(eventClass).observeOn(AndroidSchedulers.mainThread()).subscribe(new RxEventObserver<>(onNext));
    }

}
