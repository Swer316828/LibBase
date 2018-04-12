package com.sfh.lib.mvp.service;

import android.support.annotation.Nullable;


import com.sfh.lib.RxBusEventManager;
import com.sfh.lib.mvp.ILifeCycle;
import com.sfh.lib.mvp.IPresenter;
import com.sfh.lib.mvp.IView;
import com.sfh.lib.mvp.annotation.CacheMethod;
import com.sfh.lib.mvp.annotation.RxBusEvent;
import com.sfh.lib.utils.UtilLog;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

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
public class ViewProxy<T extends IView> implements InvocationHandler, ILifeCycle<T>, Consumer {


    /**
     * 运行内存kB 1/50 大概10M以下
     */
//    private final static LruCache<String, Object[]> VIEWCACHES = new LruCache<String, Object[]>((int) Runtime.getRuntime().maxMemory() / 1024 / 50) {
//        @Override
//        protected int sizeOf(String key, Object[] value) {
//            //KB
//            return String.valueOf(value).getBytes().length / 1024;
//        }
//    };

    /***
     * 清除缓存数据
     */
    public static void onDertory() {
//        VIEWCACHES.evictAll();
    }

    /**
     * 生命周期状态值 默认 ON_CREATE
     */
    private int lifecycleEvent = ILifeCycle.EVENT_ON_CREATE;

    /**
     * 视图回调
     */
    private SoftReference<T> viewHolder;

    /***
     * 管理任务-防止界面无IPresenter 有需要消息监听
     */
    private  RetrofitManager retrofitManager;

    /***
     * 当前V层方法中进行消息事件监听
     */
    private  Map<String, Method> eventMethod = new HashMap<>(2);

    @Override
    public void onEvent(T listener, int event) {

        this.lifecycleEvent = event;
        switch (event) {

            case EVENT_ON_CREATE: {
                // 绑定View
                this.bindView(listener);
                IPresenter<T> presenter = listener.getPresenter();
                if (presenter != null) {
                    presenter.onCreate(this.proxy(listener));
                }
                this.registerEvent(listener);
                break;
            }
            case EVENT_ON_FINISH: {
                // 销毁资源
                this.unBindView();
                IPresenter<T> presenter = listener.getPresenter();
                if (presenter != null) {
                    presenter.onDestory();
                }
                this.unregisterEvent();
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void bindToLifecycle(@Nullable ILifeCycle<T> lifeCycle) {
        // 加入生命周期管理
        lifeCycle.bindToLifecycle(this);
    }

    /**
     * 获取代理对象
     *
     * @param t
     * @return
     */
    private T proxy(T t) {

        Class<?> clz = t.getClass();
        if (clz == null) {
            throw new NullPointerException("proxy class is NULL class:" + clz.getClass().getName());
        }
        return (T) Proxy.newProxyInstance(clz.getClassLoader(), clz.getInterfaces(), this);
    }

    private boolean isLife() {

        return this.lifecycleEvent != ILifeCycle.EVENT_ON_DESTROY || ILifeCycle.EVENT_ON_FINISH != this.lifecycleEvent;
    }

    private void bindView(T view) {
        if (view == null) {
            return;
        }
        this.unBindView();
        this.viewHolder = new SoftReference(view);

    }

    /***
     * 解除视图
     */
    private void unBindView() {
        if (viewHolder != null) {
            viewHolder.clear();
            viewHolder = null;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (this.isCacheMethod(method)) {
            this.cacheMethod(method, args);
        }
        if (this.isBind() && this.isLife()) {
            UtilLog.d(ViewProxy.class, "代理类缓存数据:" + viewHolder.get().getClass().getName());
            return invokeMethod(viewHolder.get(), method, args);
        }
        return null;
    }

    private boolean isBind() {
        return viewHolder != null && viewHolder.get() != null;
    }

    private boolean isCacheMethod(Method method) {
        CacheMethod cacheMethod = method.getAnnotation(CacheMethod.class);
        return cacheMethod != null && cacheMethod.isCached();
    }

    private void cacheMethod(Method method, Object[] args) {
        //VIEWCACHES.put(method.getName(), args);
    }

    private Object invokeMethod(Object view, Method method, Object... args) {
        if (view == null || method == null) {
            return null;
        }
        try {
            return method.invoke(view, args);
        } catch (Exception e) {
            e.printStackTrace();
            UtilLog.d(ViewProxy.class, "IView 方法回调异常 Exception:" + e);
        }
        return null;
    }




    private void registerEvent(T t) {

        Disposable disposable = Flowable.just(t).map(new Function<T, Boolean>() {
            @Override
            public Boolean apply(T t) throws Exception {
                Method[] methods = t.getClass().getMethods();
                for (Method method : methods) {
                    RxBusEvent event = method.getAnnotation(RxBusEvent.class);
                    if (event == null) {
                        continue;
                    }
                    Class<?> clz = event.eventClass();
                    if (clz != null && event.taskId() != -1) {
                        eventMethod.put(clz.getName(), method);
                        Disposable disposable = RxBusEventManager.register(clz, ViewProxy.this);
                        IPresenter presenter = t.getPresenter();
                        if (presenter == null) {
                            retrofitManager = new RetrofitManager();
                            retrofitManager.put(event.taskId(), disposable);
                        } else {
                            presenter.putDisposable(event.taskId(), disposable);
                        }
                    }
                }
                return true;
            }
        }).onBackpressureLatest().subscribeOn(Schedulers.io()).subscribe();
        IPresenter presenter = t.getPresenter();
        if (presenter == null) {
            this.retrofitManager = new RetrofitManager();
            this.retrofitManager.put(0x100001, disposable);
        } else {
            presenter.putDisposable(0x100001, disposable);
        }
    }

    private void unregisterEvent() {
        if (this.retrofitManager != null) {
            this.retrofitManager.clearAll();
        }
        if (this.eventMethod != null) {
            this.eventMethod.clear();
        }
    }

    @Override
    public void accept(Object o) throws Exception {
        // 消息监听
        Method method = eventMethod.get(o.getClass().getName());
        if (method != null && this.isBind() && this.isLife()) {
            this.invokeMethod(viewHolder.get(), method, o);
        }
    }
}
