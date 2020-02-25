package com.sfh.lib.mvvm;

import android.app.Activity;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;


import com.sfh.lib.IViewLinstener;
import com.sfh.lib.ui.AppDialog;
import com.sfh.lib.ui.DialogBuilder;
import com.sfh.lib.ui.IDialog;
import com.sfh.lib.utils.ThreadTaskUtils;
import com.sfh.lib.utils.ThreadUIUtils;
import com.sfh.lib.utils.ViewModelUtils;
import com.sfh.lib.utils.ZLog;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.FutureTask;


/**
 * 功能描述:LiveDataManger 借助 ViewModel 生命周期管理
 * 1.处理UI LiveData 数据刷新,把LiveDate 数据持有者注入到真正业务ViewModel 中
 * 2.处理消息监听
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
public class LiveDataManger extends AbstractVM implements IShowDataListener, GenericLifecycleObserver {

    private final static String TAG = LiveDataManger.class.getName();

    private SoftReference<IViewLinstener> softReference;

    private ViewModelProvider mViewModelProvider;

    /***
     * 对话框句柄【基础操作】
     */
    private IDialog mDialog;

    public LiveDataManger(IViewLinstener linstener) {

        linstener.getLifecycle().addObserver(this);
        this.softReference = new SoftReference<>(linstener);

        FutureTask futureTask = new FutureTask(this);
        ThreadTaskUtils.execute(futureTask);
        this.putFuture(futureTask);

        ViewModelStore viewModelStore = linstener.getViewModelStore();
        if (viewModelStore != null) {
            this.mViewModelProvider = ViewModelUtils.of(viewModelStore);
            try {
                //put(String key, ViewModel viewModel)
                Method put = ViewModelStore.class.getDeclaredMethod("put", new Class[]{String.class, ViewModel.class});
                put.setAccessible(true);
                put.invoke(viewModelStore, "android.arch.lifecycle.ViewModelProvider.DefaultKey:" + LiveDataManger.class.getCanonicalName(), this);
            } catch (Exception e) {
                ZLog.d(TAG, "LiveDataManger() Exception: %s", e);
            }
        }
    }

    @Override
    public Object call() throws Exception {
        final IViewLinstener linstener = this.softReference.get();
        if (null == linstener) {
            ZLog.d(TAG, "LiveDataManger  run() IViewLinstener is null !");
            return false;
        }

        this.loadMethods(linstener);
        return true;
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        ZLog.d(TAG, "LiveDataManger onStateChanged: " + event.name());
    }

    /***
     * 使用ViewModel
     * @param cls
     * @param <T>
     * @return
     */
    @Nullable
    public final <T extends BaseViewModel> T getViewModel(@NonNull Class<T> cls) {

        T t = this.mViewModelProvider.get(cls);
        if (t != null) {
            t.setShowDataListener(this);
        }
        return t;
    }

    @Override
    protected void onCleared() {
        ZLog.d(TAG, "LiveDataManger onCleared() =========== ");
        super.onCleared();
        this.softReference.clear();
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
            ZLog.d(TAG, String.format("LiveDataManger call() mActive:%s, method:%s", this.mActive,methodName));
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
