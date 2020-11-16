package com.sfh.lib.event;



import com.sfh.lib.utils.ZLog;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 功能描述:总线事件[简单]
 * 1.消息通知
 * 2.数据回传
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/3/29
 */
public final class EventManager {

    private static final String TAG = EventManager.class.getName();
    private final ReentrantLock mLock = new ReentrantLock();
    private final LinkedHashMap<Class, LinkedList<IEventListener>> linkedHashMap;

    private final static class Hondler {

        private static final EventManager EVENT = new EventManager();
    }

    private EventManager() {
        linkedHashMap = new LinkedHashMap<>(17);
    }


    private synchronized  <T> FutureEvent put(Class<T> eventClass, final IEventListener<T> listener) {

//        mLock.tryLock();
//        try {
//
//        }finally {
//            mLock.unlock();
//        }

        LinkedList<IEventListener> lits = this.linkedHashMap.get(eventClass);
        if (null == lits) {
            lits = new LinkedList<>();
            this.linkedHashMap.put(eventClass, lits);
        }

        FutureEvent future = null;
        if (lits.isEmpty() || !lits.contains(listener)) {
            if (lits.add(listener)) {
                future = new DefaultDisFuture(listener, lits);
            }
            ZLog.d(TAG, "Event success, Class:%s,EventResult:%s", eventClass, listener);

        } else {

            ZLog.d(TAG, "Event fail, Evnt is exits. Class:%s,EventResult:%s", eventClass, listener);
        }
        return future;
    }

    private <T> boolean event(T data) {

        final LinkedList<IEventListener> listEventResult = this.linkedHashMap.get(data.getClass());
        if (null == listEventResult || listEventResult.isEmpty()) {
            ZLog.d(TAG, "Event fail, Evnt is not exits.Class:%s ", data.getClass());
            return false;
        }

        for (IEventListener eventResult : listEventResult) {
            eventResult.onEventSuccess(data);
        }
        return true;
    }

    private void removeListener(IEventListener listener){

        Collection<LinkedList<IEventListener>>   linkedLists = linkedHashMap.values();
        for (LinkedList<IEventListener>  linkedList :linkedLists){
            if (linkedList.isEmpty()){
                continue;
            }
            if (linkedList.remove(listener)){
                break;
            }
        }

    }

    /***
     * 发送一个新的事件
     * @param data 数据对象
     */
    public static <T> boolean postEvent(T data) {
        if (data == null) {
            return false;
        }
        return Hondler.EVENT.event(data);
    }


    /***
     * 注册【根据传递的 eventType 类型返回特定类型(eventType)的 被观察者 】
     * 对应的注销借口
     * @Link unRegisterRxBus()
     * @param eventClass  监听class类型
     * @param eventResult 回调接口
     * @param <T> class类型
     */
    public static <T> Future register(Class<T> eventClass, IEventListener<T> eventResult) {
        if (eventClass == null) {
            throw new NullPointerException("Class<T> eventClass is null");
        }
        if (eventResult == null) {
            throw new NullPointerException("IEventListener<T> onNext is null");
        }
        return  Hondler.EVENT.put(eventClass, eventResult);
    }

    public static void unRegister(IEventListener listener){
            if (listener == null){
                return;
            }

            Hondler.EVENT.removeListener(listener);
    }

}
