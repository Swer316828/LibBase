package com.sfh.lib.mvvm;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.Nullable;
import android.text.TextUtils;


import com.sfh.lib.ViewLinstener;
import com.sfh.lib.event.EventManager;
import com.sfh.lib.event.IEventListener;
import com.sfh.lib.mvvm.hander.LiveEventMethodFinder;
import com.sfh.lib.mvvm.hander.MethodLinkedMap;
import com.sfh.lib.utils.ThreadTaskUtils;
import com.sfh.lib.utils.ThreadUIUtils;
import com.sfh.lib.utils.ZLog;
import com.sfh.lib.utils.thread.CompositeFuture;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;


/**
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
public class UIRegistry implements GenericLifecycleObserver, Callable<Boolean>{

    private final static String TAG = UIRegistry.class.getName();

    private MethodLinkedMap mMethods;

    private volatile boolean mActive = true;

    private final CompositeFuture compositeFuture = new CompositeFuture();

    private Class<?> mTagClass;

    private IEventListener eventListener;

    private LiveData<List<Class>> mEvents = new MutableLiveData<>();

    public LiveData<List<Class>> getEvents() {
        return mEvents;
    }

    public UIRegistry(Object tag) {
        mTagClass = tag.getClass();
        Future future = ThreadTaskUtils.execute(this);
        compositeFuture.add(future);
    }

    @Override
    public Boolean call() throws Exception {

        mMethods = new LiveEventMethodFinder(mTagClass).call();
        if (!mActive) {
            mMethods.clear();
            return Boolean.FALSE;
        }

        List<Class<?>> eventTypes = mMethods.getEventClass();
        if (!eventTypes.isEmpty()) {
            for (Class<?> cls : eventTypes) {
                Future future = EventManager.register(cls, eventListener);
                compositeFuture.add(future);
            }
            mMethods.clearEventClass();
        }
        return Boolean.TRUE;
    }

    private IEventListener eventListener = new IEventListener() {
        @Override
        public void onEventSuccess(Object data) {
            //接收到消息通知
            ZLog.d(TAG, "LiveDataManger onEventSuccess() start");
            final IUIListener linstener = linstenerWeakReference.get();
            if (null == linstener || mMethods == null) {
                ZLog.d(TAG, "LiveDataManger onEventSuccess() but IViewLinstener is null, ClassName:%s", data.getClass().getName());
                return;
            }

            Method method = mMethods.get(data.getClass().getName());
            if (method != null) {
                //消息监听方法同一个参数
                showUI(linstener, method, data);
            }

            ZLog.d(TAG, "LiveDataManger onEventSuccess() end");
        }
    };

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {

        ZLog.d(TAG, "LiveDataManger onStateChanged: " + event.name());
        if (source.getLifecycle().getCurrentState() == DESTROYED) {
            mActive = false;
            compositeFuture.clear();
            source.getLifecycle().removeObserver(this);
        } else {
            mActive = true;
        }
    }
    @Override
    public void onChanged(@Nullable VMData data) {
        final ViewLinstener linstener = linstenerWeakReference.get();
        if (null == linstener || mMethods == null) {
            ZLog.d(TAG, "LiveDataManger onEventSuccess() but IViewLinstener is null, ClassName:%s", data.getClass().getName());
            return;
        }

        ZLog.d(TAG, "LiveDataManger showUIValue() start");
        if (TextUtils.isEmpty(data.methodName)) {
            ZLog.d(TAG, "LiveDataManger showUIValue()  methodName is null");
            return;
        }
        if (!this.mActive) {
            ZLog.d(TAG, String.format("LiveDataManger call() mActive:%s, method:%s", this.mActive, data.methodName));
            return;
        }

        //LiveData
        Method targetMethod = this.mMethods.get(data.methodName.trim());

        if (targetMethod == null) {
            ZLog.d(TAG, "LiveDataManger showUILiveData() Method is null, MethodName:%s", data.methodName);
            return;
        }

        this.showUI(linstener, targetMethod, data.args);

        ZLog.d(TAG, "LiveDataManger onChanged() end");
    }


    public void call(Object ui,String method, Object... args){

    }

    public void showUI(Object ui, final Method method, Object... args) {

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
      return   compositeFuture.add(future);
    }

    public ILiveDataUI getLiveData() {
        return liveData;
    }
    public IUIListener getIUIListener(){
        return this;
    }
}
