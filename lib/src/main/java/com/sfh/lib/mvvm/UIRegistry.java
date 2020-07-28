package com.sfh.lib.mvvm;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.Toast;


import com.sfh.lib.IViewLinstener;
import com.sfh.lib.event.BusEventManager;
import com.sfh.lib.event.EventMethod;
import com.sfh.lib.event.EventMethodFinder;
import com.sfh.lib.ui.AppDialog;
import com.sfh.lib.ui.DialogBuilder;
import com.sfh.lib.ui.IDialog;
import com.sfh.lib.utils.ThreadTaskUtils;
import com.sfh.lib.utils.ThreadUIUtils;
import com.sfh.lib.utils.ZLog;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;


/**
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
public class UIRegistry implements GenericLifecycleObserver, Callable<Boolean> {

    private final static String TAG = UIRegistry.class.getName();

    private WeakReference<IViewLinstener> softReference;
    private SparseArray<Method> mMethodArrays = new SparseArray<>();
    private Class<?> targetCalss;
    /***
     * 对话框句柄【基础操作】
     */
    private IDialog mDialog;

    private volatile boolean mAction = true;

    public UIRegistry(IViewLinstener linstener) {

        this.softReference = new WeakReference<>(linstener);

    }

    public void register(Object subscriber) {

        Future futureTask = ThreadTaskUtils.execute(this);
        this.putFuture(futureTask);
    }

    class MerhodFinder implements Callable{
       final Class<?> targetCalss;

        MerhodFinder(Class<?> targetCalss) {
            this.targetCalss = targetCalss;
        }

        @Override
        public Object call() throws Exception {

            Future<List<Method>> live = ThreadTaskUtils.execute(new LiveMethonFinder(targetCalss));
            Future<List<EventMethod>> eventFuture= ThreadTaskUtils.execute(new EventMethodFinder(targetCalss));
            List<EventMethod> eventMethodList =  eventFuture.get();

            if (mAction){
                for (EventMethod eventMethod: eventMethodList){
                    BusEventManager.register(eventMethod.getDataClass(),)
                }
            }
            return Boolean.TRUE;
        }
    }

    @Override
    public Boolean call() throws Exception {
        final Class<?> targetCls = subscriber.getClass();

        Future<List<Method>> live = ThreadTaskUtils.execute(new LiveMethonFinder(targetCls));
        Future<List<EventMethod>> eventMethods = ThreadTaskUtils.execute(new EventMethodFinder(targetCls));
        List<Method> methods = live.get();
        if (mAction){

        }
        event.get();
        return Boolean.TRUE;
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        ZLog.d(TAG, "LiveDataManger onStateChanged: " + event.name());
        if (source.getLifecycle().getCurrentState() == DESTROYED) {
            source.getLifecycle().removeObserver(this);
        }
    }



    @Override
    public void setEventSuccess(Method method, Object data) {
        //接收到消息通知
        ZLog.d(TAG, "LiveDataManger onEventSuccess() start");
        final IViewLinstener linstener = this.softReference.get();
        if (null == linstener) {
            ZLog.d(TAG, "LiveDataManger onEventSuccess() but IViewLinstener is null, ClassName:%s", data.getClass().getName());
            return;
        }
        //消息监听方法同一个参数
        this.showUI(linstener, method, data);

        ZLog.d(TAG, "LiveDataManger onEventSuccess() end");
    }


    @Override
    public void call(String methodName, Object... args) {

        ZLog.d(TAG, "LiveDataManger showUIValue() start");
        if (TextUtils.isEmpty(methodName)) {
            ZLog.d(TAG, "LiveDataManger showUIValue()  methodName is null");
            return;
        }
        if (!this.mActive) {
            ZLog.d(TAG, String.format("LiveDataManger call() mActive:%s, method:%s", this.mActive, methodName));
            return;
        }
        final IViewLinstener linstener = this.softReference.get();
        if (null == linstener) {
            ZLog.d(TAG, "LiveDataManger onChanged() but IViewLinstener is null, methodName:%s", methodName);
            return;
        }

        //LiveData
        Method targetMethod = this.mMethods.get(methodName);
        if (targetMethod == null) {
            final Method[] methods = linstener.getClass().getDeclaredMethods();

            for (Method mtd : methods) {
                final int modifiers = mtd.getModifiers();
                if (!Modifier.isPublic(modifiers)
                        || Modifier.isFinal(modifiers)
                        || Modifier.isAbstract(modifiers)
                        || Modifier.isStatic(modifiers)) {
                    continue;
                }

                if (TextUtils.equals(mtd.getName(), methodName)) {
                    // 注册LiveData监听
                    this.mMethods.put(mtd.getName(), mtd);
                    targetMethod = mtd;
                    break;
                }
            }
        }

        if (targetMethod == null) {
            ZLog.d(TAG, "LiveDataManger showUILiveData() Method is null, MethodName:%s", methodName);
            return;
        }

        this.showUI(linstener, targetMethod, args);

        ZLog.d(TAG, "LiveDataManger onChanged() end");
    }


    private void showUI(final IViewLinstener linstener, final Method method, Object... args) {

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
            this.invokeMethod(linstener, method, dyArgs);
        } else {
            this.runUIThread(new Runnable() {
                @Override
                public void run() {
                    invokeMethod(linstener, method, dyArgs);
                }
            });
        }
    }

    private void invokeMethod(IViewLinstener linstener, Method method, Object... args) {
        try {
            //消息监听方法同一个参数
            method.invoke(linstener, args);
        } catch (Exception e) {
            ZLog.d(TAG, "LiveDataManger invokeMethod()  IViewLinstener:%s, Method:%s, params:%s, exception:%s", linstener, method, args, e);
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

    @Override
    public void showLoading(final boolean cancel) {
        if (!this.enableShowDialog()) {
            return;
        }

        if (ThreadUIUtils.isInUiThread()) {
            mDialog.showLoading(cancel);
        } else {
            this.runUIThread(() -> mDialog.showLoading(cancel));
        }
    }

    @Override
    public void hideLoading() {

        if (!this.enableShowDialog()) {
            return;
        }

        if (ThreadUIUtils.isInUiThread()) {
            mDialog.hideLoading();
        } else {
            this.runUIThread(() -> mDialog.hideLoading());
        }
    }

    @Override
    public void showDialog(final DialogBuilder dialog) {

        if (!this.enableShowDialog()) {
            return;
        }

        if (ThreadUIUtils.isInUiThread()) {
            this.mDialog.showDialog(dialog);
        } else {
            this.runUIThread(() -> mDialog.showDialog(dialog));
        }
    }

    @Override
    public void showToast(final CharSequence msg) {
        if (!this.enableShowDialog()) {
            return;
        }
        if (ThreadUIUtils.isInUiThread()) {
            mDialog.showToast(msg, Toast.LENGTH_SHORT);
        } else {
            this.runUIThread(() -> mDialog.showToast(msg, Toast.LENGTH_SHORT));
        }
    }

    @Override
    public void showDialogToast(CharSequence msg) {
        DialogBuilder dialog = new DialogBuilder();
        dialog.setTitle("提示");
        dialog.setHideCancel(true);
        dialog.setMessage(msg);
        this.showDialog(dialog);
    }

    private void runUIThread(Runnable runnable) {

        ThreadUIUtils.runOnUiThread(runnable);
    }

    public boolean enableShowDialog() {
        if (!this.mActive) {
            return false;
        }

        IViewLinstener linstener = this.softReference.get();
        if (null == linstener) {
            ZLog.d(TAG, "LiveDataManger createDialog() but IViewLinstener is null");
            return false;
        }

        //先外部获取
        this.mDialog = linstener.getDialog();

        if (this.mDialog == null) {
            this.mDialog = new AppDialog(linstener.getActivity());
        }
        return true;
    }

}
