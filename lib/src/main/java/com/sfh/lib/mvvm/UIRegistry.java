package com.sfh.lib.mvvm;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;


import com.sfh.lib.annotation.LiveDataMatch;
import com.sfh.lib.annotation.EventMatch;
import com.sfh.lib.event.EventManager;
import com.sfh.lib.event.IEventListener;
import com.sfh.lib.utils.ThreadTaskUtils;
import com.sfh.lib.utils.ThreadUIUtils;
import com.sfh.lib.utils.ZLog;
import com.sfh.lib.utils.thread.CompositeFuture;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;


/**
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
public class UIRegistry implements GenericLifecycleObserver{

    private final static String TAG = UIRegistry.class.getName();

    //任务管理
    private final CompositeFuture mCompositeFuture = new CompositeFuture();

    //UI 数据监听
    private final UILiveData mLiveData = new UILiveData();

    //当前关联Activity,Fragment 注入方法集合
    private LinkedHashMap<String, Method> mMethods  = new LinkedHashMap<>(10);

    //状态
    private volatile boolean mActive = true;


    public UIRegistry(Object tag) {
        Future future = ThreadTaskUtils.execute(new LiveEventHandler(tag.getClass()));
        mCompositeFuture.add(future);
    }

    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull IUIListener observer){

        mLiveData.observe(owner,observer);
        owner.getLifecycle().addObserver(this);
    }

    class LiveEventHandler implements Callable<Boolean>,IEventListener{
        private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC;

        private Class<?> tagClass;
        public LiveEventHandler(Class<?> tagClass){
            this.tagClass = tagClass;
        }

        @Override
        public Boolean call() throws Exception {

            Method[] methods = tagClass.getDeclaredMethods();
            for (Method method : methods) {

                int modifiers = method.getModifiers();

                if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {


                    LiveDataMatch live = method.getAnnotation(LiveDataMatch.class);
                    if (live != null) {
                        mMethods.put(method.getName(), method);
                    }

                    EventMatch event = method.getAnnotation(EventMatch.class);
                    if (event != null) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length == 1) {
                            Class<?> eventType = parameterTypes[0];
                            mMethods.put(eventType.getName(), method);
                            Future future = EventManager.register(eventType, LiveEventHandler.this);
                            mCompositeFuture.add(future);
                        }

                    }

                } else if (method.isAnnotationPresent(LiveDataMatch.class) || method.isAnnotationPresent(EventMatch.class)) {

                    String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                    throw new RuntimeException(methodName +
                            " is a illegal @LiveDataMatch or @Event method: must be public, non-static, and non-abstract");
                }
            }

            //对话框常用方法
            methods = IDialog.class.getDeclaredMethods();
            for (Method method : methods) {
                mMethods.put(method.getName(), method);
            }

            return Boolean.TRUE;
        }

        @Override
        public void onEventSuccess(Object event) {

            mLiveData.call(event.getClass().getName(),event);
            ZLog.d(TAG, "LiveDataManger onEventSuccess() end");
        }
    }


    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {

        ZLog.d(TAG, "LiveDataManger onStateChanged: " + event.name());

        if (source.getLifecycle().getCurrentState() == DESTROYED) {

            source.getLifecycle().removeObserver(this);
            mActive = false;
            mCompositeFuture.clear();

        } else {
            mActive = true;
        }
    }


    public void call(Object ui, String method, Object... args) {

        if (TextUtils.isEmpty(method)) {
            ZLog.d(TAG, "LiveDataManger call()  methodName is null");
            return;
        }
        if (!this.mActive) {
            ZLog.d(TAG, String.format("LiveDataManger call() mActive:%s, method:%s", this.mActive, method));
            return;
        }

        //LiveData
        Method targetMethod = this.mMethods.get(method.trim());

        if (targetMethod == null) {
            ZLog.d(TAG, "LiveDataManger showUILiveData() Method is null, MethodName:%s", method);
            return;
        }

        this.showUI(ui, targetMethod, args);
    }

    private void showUI(Object ui, final Method method, Object... args) {

        if (null == args) {
            args = new Object[0];
        }

        //方法需要参数
        final Class<?>[] parameter = method.getParameterTypes();

        int paramLen = parameter.length;
        int dataLen = args.length;

        final Object[] dyArgs;
        if (paramLen - dataLen > 0) {

            dyArgs = new Object[paramLen];

            for (int i = 0; i < paramLen; i++) {
                if (i < dataLen) {
                    dyArgs[i] = args[i];
                } else {
                    dyArgs[i] = this.getNullObject(parameter[i]);
                }
            }
        } else {
            dyArgs = args;
        }

        if (ThreadUIUtils.isInUiThread()) {
            this.invokeMethod(ui, method, dyArgs);
        } else {
            ThreadUIUtils.onUiThread(() -> invokeMethod(ui, method, dyArgs));

        }
    }

    private void invokeMethod(Object ui, Method method, Object... args) {
        try {
            //消息监听方法同一个参数
            method.invoke(ui, args);
        } catch (Exception e) {
            ZLog.e(TAG, "LiveDataManger invokeMethod()  IViewLinstener:%s, Method:%s, params:%s, exception:%s", ui, method, args, e);
        }
    }

    private Object getNullObject(Class<?> parameter) {

        if (long.class.isAssignableFrom(parameter) || Long.class.isAssignableFrom(parameter)) {
            return 0L;
        } else if (boolean.class.isAssignableFrom(parameter) || Boolean.class.isAssignableFrom(parameter)) {
            return Boolean.FALSE;
        } else if (int.class.isAssignableFrom(parameter) || Integer.class.isAssignableFrom(parameter)) {
            return 0;
        } else if (float.class.isAssignableFrom(parameter) || Float.class.isAssignableFrom(parameter)) {
            return 0.0F;
        } else {
            return null;
        }
    }

    public boolean putFuture(Future future) {
        return mCompositeFuture.add(future);
    }

    public UILiveData getLiveData() {
        return mLiveData;
    }
}
