package com.sfh.lib.event;

import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.rx.EmptyResult;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * 功能描述: 注入RxEvent 监听
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/30
 */
public class RxBusRegistry implements Function<Object, Boolean>, IEventResult {

    private final static String TAG = RxBusRegistry.class.getName();

    private Object observe;

    private Map<Class<?>, Method> mMethod;

    private RetrofitManager mRetrofit;

    public RxBusRegistry() {
        this.mRetrofit = new RetrofitManager();
        this.mMethod = new HashMap<>(5);
    }

    public void registry(Object observe) {
        this.observe = observe;
        this.mRetrofit.execute(Flowable.just(observe).map(this), new EmptyResult<Boolean>());
    }


    public Method getMethod(Map<Class<?>, Method> map, Object data) {

        for (Map.Entry<Class<?>, Method> entry : map.entrySet()) {
            Class<?> type = entry.getKey();
            if (type.isInstance(data)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public Boolean apply(Object observe) throws Exception {

        final Method[] methods = observe.getClass().getDeclaredMethods();
        for (Method method : methods) {
            // 注册RxBus监听
            RxBusEvent event = method.getAnnotation(RxBusEvent.class);
            if (event != null) {

                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<?> dataClass;
                if (parameterTypes != null && (dataClass = parameterTypes[0]) != null) {
                    this.mMethod.put(dataClass, method);
                    RxBusEventManager.register(dataClass, this);
                }
            }
        }
        return true;
    }

    public void onCleared() {
        if (mMethod != null) {
            mMethod.clear();
            mMethod = null;
        }
        if (mRetrofit != null) {
            mRetrofit.clearAll();
            mRetrofit = null;
        }
        observe = null;
    }

    @Override
    public void onEventSuccess(Object data) throws Exception {
        // RxBus 消息监听
        if (data == null) {
            return;
        }
        Method method = this.getMethod(this.mMethod, data);
        if (method != null) {
            method.invoke(this.observe, data);
        }
    }

    @Override
    public void onSubscribe(Disposable d) {
        this.mRetrofit.put(d);
    }
}

