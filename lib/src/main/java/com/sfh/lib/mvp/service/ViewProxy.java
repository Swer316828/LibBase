package com.sfh.lib.mvp.service;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.sfh.lib.RxBusEventManager;
import com.sfh.lib.mvp.IPresenter;
import com.sfh.lib.mvp.IView;
import com.sfh.lib.mvp.annotation.RxBusEvent;
import com.sfh.lib.mvp.service.empty.EmptyResult;
import com.sfh.lib.utils.UtilLog;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;


/**
 * 功能描述:视图代理类 ViewModel
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
public class ViewProxy<V extends IView> extends ViewModel implements InvocationHandler, Consumer, Function<V, Boolean> {

    /**
     * 视图回调
     */
    private WeakReference<V> mViewHolder;

    /***
     * 管理任务消息监听
     */
    private RetrofitManager mRetrofitManager;

    /***
     * 当前V层方法中进行消息事件监听
     */
    private SparseArray<Method> mEventMethod;

    public ViewProxy() {
        this.mRetrofitManager = new RetrofitManager();
        this.mEventMethod = new SparseArray<>(2);
    }

    public void register(@NonNull V listener) {
        // 绑定View
        this.bindView(listener);
        this.registerEvent(listener);
    }

    /***
     * 绑定视图
     * @param view
     */
    private void bindView(@NonNull V view) {
        this.unBindView();
        this.mViewHolder = new WeakReference(view);
    }


    /**
     * 获取代理对象
     *
     * @return
     */
    public void bindProxy(@NonNull IPresenter presenter) {
        V listener = this.getView();
        if (listener != null) {
            V proxy = (V) Proxy.newProxyInstance(listener.getClass().getClassLoader(), listener.getClass().getInterfaces(), this);
            presenter.onBindProxy(proxy);
        }
    }


    @Nullable
    private V getView() {
        if (mViewHolder == null) {
            return null;
        }
        return mViewHolder.get();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 销毁资源
        this.unBindView();
        this.unregisterEvent();
    }

    private void unBindView() {
        if (this.mViewHolder != null) {
            this.mViewHolder.clear();
            this.mViewHolder = null;
        }
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
    public Object invoke(Object proxy, Method method, Object[] args) {
        return this.invokeMethod(this.getView(), method, args);
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
    private Object invokeMethod(@Nullable Object view, @Nullable Method method, Object... args) {

        if (view == null || method == null) {
            return null;
        }

        UtilLog.d(ViewProxy.class, "代理类缓存方法 :view" + view + " method:" + method);

        try {
            return method.invoke(view, args);
        } catch (Exception e) {
            UtilLog.e(ViewProxy.class, "代理类缓存方法: method:" + method + " e:" + e);
        }
        return null;

    }

    /***
     * 检查view 视图中是否存在消息通知监听的方法
     * @param t
     */
    private void registerEvent(V t) {

        this.mRetrofitManager.execute(Flowable.just(t).map(this).onBackpressureLatest(), new EmptyResult());
    }

    @Override
    public Boolean apply(V t) throws Exception {
        // 处理消息监听
        Method[] methods = t.getClass().getMethods();
        for (Method method : methods) {
            RxBusEvent event = method.getAnnotation(RxBusEvent.class);
            if (event == null) {
                continue;
            }
            Class<?> clz = event.eventClass();
            if (event == null) {
                continue;
            }

            this.mEventMethod.put(clz.getName().hashCode(), method);
            Disposable disposable = RxBusEventManager.register(clz, ViewProxy.this);
            this.mRetrofitManager.put(disposable);
        }
        return true;
    }

    @Override
    public void accept(Object o) throws Exception{
        if (this.mEventMethod == null || this.mEventMethod.size() == 0) {
            return;
        }
        // 消息监听
        Method method = this.mEventMethod.get(o.getClass().getName().hashCode());
        this.invokeMethod(this.getView(), method, o);
    }
}
