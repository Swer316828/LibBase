package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;

import com.sfh.lib.event.IEventResult;
import com.sfh.lib.event.RxBusEvent;
import com.sfh.lib.event.RxBusEventManager;
import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.annotation.LiveDataMatch;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.rx.EmptyResult;
import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.utils.UtilLog;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;


/**
 * 功能描述:LiveDataRegistry 借助 ViewModel 生命周期管理
 * 1.处理UI LiveData 数据刷新,把LiveDate 数据持有者注入到真正业务ViewModel 中
 * 2.处理消息监听
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
public class LiveDataRegistry implements Function<IView, Boolean>, IEventResult {

    private final static String TAG = LiveDataRegistry.class.getName ();

    private volatile SparseArray<UIMethod> mUIMethod = new SparseArray<> (5);

    private CompositeDisposable mDisposableList = new CompositeDisposable ();

    private final ObjectMutableLiveData mLiveData = new ObjectMutableLiveData ();

    private ViewModelProvider mViewModelProvider;

    /***
     * 解析业务响应方法,消息监听方法
     * @param listener
     */
    public final void register(@NonNull IView listener) {

        this.mLiveData.observe (listener, listener);
        this.mDisposableList.add (RetrofitManager.executeSigin (Flowable.just (listener).map (this).onBackpressureLatest (), new EmptyResult ()));
    }

    public void onDestroy() {

        UtilLog.d (TAG, "LiveDataRegistry onCleared =========== 资源销毁");
        this.mLiveData.onCleared ();
        this.mDisposableList.clear ();
        this.mUIMethod.clear ();
    }

    /***
     * 绑定数据监听与
     * @param viewModel
     */
    public void bindLiveDataAndCompositeDisposable(BaseViewModel viewModel){
        viewModel.putLiveData(this.mLiveData);
    }


    public ObjectMutableLiveData getLiveData() {

        return this.mLiveData;
    }

    @Override
    public Boolean apply(IView iView) throws Exception {

        final Method[] methods = iView.getClass ().getDeclaredMethods ();

        for (Method method : methods) {

            final int modifiers = method.getModifiers ();
            if (!Modifier.isPublic (modifiers)
                    || Modifier.isFinal (modifiers)
                    || Modifier.isAbstract (modifiers)
                    || Modifier.isStatic (modifiers)) {
                continue;
            }

            LiveDataMatch liveEvent = method.getAnnotation (LiveDataMatch.class);
            if (liveEvent != null) {
                // 注册LiveData监听
                UIMethod uiMethod = new UIMethod (method);
                this.mUIMethod.put (uiMethod.hashCode (), uiMethod);
            }

            RxBusEvent rxBusEvent = method.getAnnotation (RxBusEvent.class);
            if (rxBusEvent != null) {
                Class<?>[] parameterTypes = method.getParameterTypes ();
                Class<?> dataClass;
                if (parameterTypes != null && (dataClass = parameterTypes[0]) != null) {

                    RxBusEventManager.register (dataClass, this);
                    UIMethod uiMethod = new UIMethod (method, dataClass);
                    this.mUIMethod.put (uiMethod.hashCode (), uiMethod);
                }
            }
        }
        return true;
    }

    @Override
    public void onEventSuccess(Object data) throws Exception {
        // RxBus 消息监听
        UIMethod eventMethod = this.mUIMethod.get (data.getClass ().getSimpleName ().hashCode ());
        if (eventMethod != null) {
            //响应方法：当前ViewModel 监听方法
            this.mLiveData.setValue (new UIData (eventMethod.method.getName (), data));
        }
    }

    @Override
    public void onSubscribe(Disposable d) {

        this.mDisposableList.add (d);
    }


    /***
     * 显示UI 数据
     * @param view
     * @param data
     */
    public final void showLiveData(@NonNull Object view, @Nullable UIData data) {
        // LiveData 数据监听
        if (view == null || data == null) {
            UtilLog.e (TAG, "LiveDataRegistry method: null");
            return;
        }

        try {
            Method method = this.getPolishingMethod (view, data);
            if (method == null) {
                return;
            }

            //方法需要参数
            final Class<?>[] parameter = method.getParameterTypes ();
            final int originalLength = parameter.length;

            //【响应方法】无参
            if (originalLength == 0) {
                method.invoke (view);
                return;
            }

            final int dataLength = data.getDataLength ();

            //【响应方法】有参
            if (originalLength == dataLength) {
                // 正常匹配
                method.invoke (view, data.getData ());
                return;
            }

            //需要一个参数
            if (originalLength == 1 && dataLength == 0) {
                method.invoke (view, new Object[]{this.getNullObject (parameter[0])});
                return;
            }

            //补齐参数
            List<Object> list = new ArrayList<> (originalLength);

            final Object[] temp = data.getData ();
            for (int i = 0; i < originalLength; i++) {
                if (i < dataLength) {
                    list.add (temp[i]);
                } else {
                    list.add (this.getNullObject (parameter[i]));
                }
            }
            method.invoke (view, list.toArray ());

        } catch (Exception e) {
            UtilLog.e (TAG, "LiveDataRegistry method:" + data.getAction () + " e:" + e);
        }
    }

    /***
     * 补齐方法
     * @param view
     * @param data
     * @return
     */
    private Method getPolishingMethod(Object view, UIData data) {

        //LiveData
        UIMethod uiMethod = this.mUIMethod.get (data.getAction ().hashCode ());
        if (uiMethod != null) {
            return uiMethod.method;
        }

        //RxBusEvent
        Class<?> liveClass = data.getDataClass ();
        if (liveClass != null) {
            uiMethod = this.mUIMethod.get (liveClass.getSimpleName ().hashCode ());
        }
        if (uiMethod != null) {
            return uiMethod.method;
        }

        // 补齐方法
        final Method[] methods = view.getClass ().getDeclaredMethods ();

        for (Method method : methods) {
            final int modifiers = method.getModifiers ();
            if (!Modifier.isPublic (modifiers)
                    || Modifier.isFinal (modifiers)
                    || Modifier.isAbstract (modifiers)
                    || Modifier.isStatic (modifiers)) {
                continue;
            }

            if (TextUtils.equals (method.getName (), data.getAction ())) {
                // 注册LiveData监听
                uiMethod = new UIMethod (method);
                this.mUIMethod.put (uiMethod.hashCode (), uiMethod);
                return method;
            }
        }
        return null;
    }

    private Object getNullObject(Class<?> parameter) {

        if (long.class.isAssignableFrom (parameter) || Long.class.isAssignableFrom (parameter)) {
            return 0L;
        } else if (boolean.class.isAssignableFrom (parameter) || Boolean.class.isAssignableFrom (parameter)) {
            return false;
        } else if (int.class.isAssignableFrom (parameter) || Integer.class.isAssignableFrom (parameter)) {
            return 0;
        } else if (float.class.isAssignableFrom (parameter) || Float.class.isAssignableFrom (parameter)) {
            return 0.0f;
        } else {
            return null;
        }
    }

    /***
     * 添加RxJava监听
     * @param disposable
     */
    public final void putDisposable(Disposable disposable) {

        this.mDisposableList.add (disposable);
    }


}
