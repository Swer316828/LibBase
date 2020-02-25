package com.sfh.lib.mvvm;

import android.arch.lifecycle.ViewModel;
import android.util.LruCache;


import com.sfh.lib.event.BusEvent;
import com.sfh.lib.event.BusEventManager;
import com.sfh.lib.event.IEventListener;
import com.sfh.lib.utils.ZLog;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public abstract class AbstractVM extends ViewModel implements IEventListener, java.util.concurrent.Callable {

    private static final String TAG = AbstractVM.class.getName();


    protected volatile LruCache<String, Method> mMethods = new LruCache<String, Method>(10) {
        @Override
        protected int sizeOf(String key, Method value) {
            return 1;
        }
    };

    private List<Future> mFutureTasks = new ArrayList<>(7);

    protected volatile boolean mActive = true;

    /***
     *  消息通知回调
     * @param method
     * @param data
     */
    public abstract void setEventSuccess(Method method, Object data);

    @Override
    protected void onCleared() {
        super.onCleared();
        for (Future future : mFutureTasks) {
            if (!future.isDone() && !future.isCancelled()) {
                future.cancel(true);
            }
        }
        this.mFutureTasks.clear();
        this.mActive = false;
        this.mMethods.evictAll();
    }

    /***
     * 载入LiveDataMatch 与  BusEvent 方法
     * @param target
     */
    protected void loadMethods(Object target) {

        final Method[] methods = target.getClass().getDeclaredMethods();
        for (Method method : methods) {

            final int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers)
                    || Modifier.isFinal(modifiers)
                    || Modifier.isAbstract(modifiers)
                    || Modifier.isStatic(modifiers)) {
                continue;
            }

            // 1.注册LiveData监听
            LiveDataMatch liveEvent = method.getAnnotation(LiveDataMatch.class);
            if (liveEvent != null) {
                this.mMethods.put(method.getName(), method);
            }

            // 2.注册BusEvent 消息通知监听
            BusEvent busEvent = method.getAnnotation(BusEvent.class);
            if (busEvent != null) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<?> eventClass;
                if (parameterTypes != null && (eventClass = parameterTypes[0]) != null) {

                    Future future = BusEventManager.register(eventClass, this);
                    this.putFuture(future);
                    //已监听的类类型作为key
                    this.mMethods.put(String.format("BusEvent_%s", eventClass.getName()), method);
                }
            }
        }
    }


    public void putFuture(Future future) {
        if (future == null) {
            return;
        }
        this.mFutureTasks.add(future);
    }


    @Override
    public void onEventSuccess(Object data) {

        ZLog.d(TAG, "AbstractVM onEventSuccess() start");
        if (!this.mActive) {
            ZLog.d(TAG, "AbstractVM onEventSuccess() mActive:" + this.mActive);
            return;
        }

        //接收到消息通知
        String eventKey = String.format("BusEvent_%s", data.getClass().getName());
        Method method = this.mMethods.get(eventKey);
        if (method == null) {
            ZLog.d(TAG, "AbstractVM onEventSuccess() Method is null, ClassName:%s", data.getClass().getName());
            return;
        }

        //消息监听方法同一个参数
        try {
            this.setEventSuccess(method, data);
        } catch (Exception e) {
            ZLog.d(TAG, "LiveDataManger onEventSuccess() Exception:" + e);
        }
        ZLog.d(TAG, "LiveDataManger onEventSuccess() end");

    }

}
