package com.sfh.lib.mvp.service;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.util.SparseArray;


import com.sfh.lib.RxBusEventManager;
import com.sfh.lib.mvp.IView;
import com.sfh.lib.mvp.annotation.RxBusEvent;
import com.sfh.lib.mvp.service.empty.EmptyResult;
import com.sfh.lib.utils.UtilLog;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * 功能描述:视图代理类
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
public class ViewProxy<V extends IView> implements InvocationHandler, LifecycleObserver, Consumer {

    /**
     * 视图回调
     */
    private SoftReference<V> mViewHolder;

    /***
     * 管理任务消息监听
     */
    private RetrofitManager mRetrofitManager;

    /***
     * 当前V层方法中进行消息事件监听
     */
    private SparseArray<Method> mEventMethod;

    public ViewProxy(@NonNull V listener) {
        // 绑定View
        this.bindView(listener);
    }

    /**
     * 获取代理对象
     *
     * @return
     */
    public V getProxy(@NonNull V listener) {

        Class<?> clz = listener.getClass();

        return (V) Proxy.newProxyInstance(clz.getClassLoader(), clz.getInterfaces(), this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void connectListener() {

        V listener = this.getView();
        if (listener != null) {
            this.registerEvent(listener);
        }
    }

    private V getView() {
        if (mViewHolder == null) {
            return null;
        }
        return mViewHolder.get();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void disconnectListener() {
        // 销毁资源
        this.unBindView();
        this.unregisterEvent();
    }

    private void unregisterEvent() {
        if (this.mRetrofitManager != null) {
            this.mRetrofitManager.clearAll();
        }
        if (this.mEventMethod != null) {
            this.mEventMethod.clear();
        }
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        V listener = this.getView();
        if (listener != null) {
            UtilLog.d(ViewProxy.class, "代理类缓存数据:" + listener.getClass().getName());
            return invokeMethod(listener, method, args);
        }
        return null;
    }

    /***
     * 绑定视图
     * @param view
     */
    private void bindView(V view) {
        if (view == null) {
            return;
        }
        this.unBindView();
        this.mViewHolder = new SoftReference(view);
    }

    /***
     * 解除视图
     */
    private void unBindView() {
        if (this.mViewHolder != null) {
            this.mViewHolder.clear();
            this.mViewHolder = null;
        }
    }

    /***
     * 调用被代理对象的方法
     * @param view
     * @param method
     * @param args
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object invokeMethod(Object view, Method method, Object... args) throws InvocationTargetException, IllegalAccessException {
        if (view == null || method == null) {
            return null;
        }
        return method.invoke(view, args);
    }

    /***
     * 检查view 视图中是否存在消息通知监听的方法
     * @param t
     */
    private void registerEvent(V t) {

        Observer emptyObserver = new Observer(new EmptyResult());

        Disposable disposable = Flowable.just(t).map(new Function<V, Boolean>() {
            @Override
            public Boolean apply(V t) throws Exception {
                Method[] methods = t.getClass().getMethods();
                for (Method method : methods) {
                    RxBusEvent event = method.getAnnotation(RxBusEvent.class);
                    if (event == null) {
                        continue;
                    }
                    Class<?> clz = event.eventClass();
                    if (clz != null && event.taskId() != -1) {
                        if (mEventMethod == null) {
                            mEventMethod = new SparseArray<>(2);
                        }
                        mEventMethod.put(clz.getName().hashCode(), method);
                        Disposable disposable = RxBusEventManager.register(clz, ViewProxy.this);
                        putDisposable(event.taskId(), disposable);
                    }
                }
                return true;
            }
        }).onBackpressureLatest().subscribeOn(Schedulers.io()).subscribe(emptyObserver, emptyObserver.onError());

        putDisposable(0x100001, disposable);
    }

    private void putDisposable(int taskId, Disposable disposable) {

        if (this.mRetrofitManager == null) {
            this.mRetrofitManager = new RetrofitManager();
        }
        this.mRetrofitManager.put(taskId, disposable);
    }


    @Override
    public void accept(Object o) throws Exception {
        if (mEventMethod == null || mEventMethod.size() == 0) {
            return;
        }
        // 消息监听
        Method method = mEventMethod.get(o.getClass().getName().hashCode());
        if (method != null) {
            V listener = this.getView();
            if (listener != null) {
                this.invokeMethod(listener, method, o);
            }
        }
    }
}
