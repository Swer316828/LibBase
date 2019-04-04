package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;

import com.sfh.lib.event.RxBusEvent;
import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.IViewModel;
import com.sfh.lib.mvvm.annotation.LiveDataMatch;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.rx.EmptyResult;
import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.ui.AbstractLifecycleActivity;
import com.sfh.lib.ui.AbstractLifecycleFragment;
import com.sfh.lib.ui.AbstractLifecycleView;
import com.sfh.lib.utils.UtilLog;
import com.sfh.lib.utils.ViewModelProviders;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;


/**
 * 功能描述:LiveDataRegistry 借助 ViewModel 生命周期管理
 * 2.处理UI LiveData 数据刷新,把LiveDate 数据持有者注入到真正业务ViewModel 中
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
public class LiveDataRegistry<V extends IView> extends ViewModel implements Function<V, Boolean> {

    private final static String TAG = LiveDataRegistry.class.getName ();

    public static <T extends BaseViewModel> T getViewModel(AbstractLifecycleActivity activity) {

        ///对象的直接超类的 Type
        Type type = activity.getClass ().getGenericSuperclass ();
        if (type == null) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            //参数化类型
            Type[] types = ((ParameterizedType) type).getActualTypeArguments ();
            if (types != null && types.length > 0) {
                return ViewModelProviders.of (activity).get ((Class<T>) types[0]);
            }
        }
        return null;
    }

    public static <T extends BaseViewModel> T getViewModel(AbstractLifecycleFragment fragment) {

        ///对象的直接超类的 Type
        Type type = fragment.getClass ().getGenericSuperclass ();
        if (type == null) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            //参数化类型
            Type[] types = ((ParameterizedType) type).getActualTypeArguments ();
            if (types != null && types.length > 0) {
                return ViewModelProviders.of (fragment).get ((Class<T>) types[0]);
            }
        }
        return null;
    }

    public static <T extends BaseViewModel> T getViewModel(AbstractLifecycleView lifecycleView) {

        ///对象的直接超类的 Type
        Type type = lifecycleView.getClass ().getGenericSuperclass ();
        if (type == null) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            //参数化类型
            Type[] types = ((ParameterizedType) type).getActualTypeArguments ();
            if (types != null && types.length > 0) {
                return ViewModelProviders.of ((FragmentActivity) lifecycleView.getContext ()).get ((Class<T>) types[0]);
            }
        }
        return null;
    }

//    private volatile RetrofitManager mRetrofit;

    /*** 响应方法集合*/
    private volatile SparseArray<Method> mLiveDataMethod = new SparseArray<> (5);

    private volatile CompositeDisposable mDisposableList = new CompositeDisposable ();


    /***
     * 解析业务响应方法,消息监听方法
     * @param listener
     */
    public final void handerMethod(@NonNull V listener) {

        UtilLog.d (TAG, "LiveDataRegistry observe =========== 注册监听");
        // 解析业务响应方法,消息监听方法
//        this.mRetrofit.execute (Flowable.just (listener).map (this).onBackpressureLatest (), new EmptyResult ());
        EmptyResult result = new EmptyResult ();
        Disposable disposable = RetrofitManager.executeSigin (Flowable.just (listener).map (this).onBackpressureLatest (), result);
        result.addDisposable (disposable);
    }


    @Override
    public Boolean apply(V iView) throws Exception {

        final IViewModel viewModel = iView.getViewModel ();
        if (viewModel == null) {
            return false;
        }
        final Method[] methods = iView.getClass ().getDeclaredMethods ();

        for (Method method : methods) {

            LiveDataMatch liveEvent = method.getAnnotation (LiveDataMatch.class);
            RxBusEvent rxBusEvent = method.getAnnotation (RxBusEvent.class);
            if (liveEvent != null) {
                // 注册LiveData监听
                this.mLiveDataMethod.put (method.getName ().hashCode (), method);
            }
            if (rxBusEvent != null && viewModel != null) {
                // 注册RxEvent消息监听
                viewModel.putEventMethod (method);
            }
        }
        return true;
    }

    @Override
    protected void onCleared() {

        super.onCleared ();
        UtilLog.d (TAG, "LiveDataRegistry onCleared =========== 资源销毁");
        this.mDisposableList.clear ();
        this.mDisposableList = null;

        this.mLiveDataMethod.clear ();
        this.mLiveDataMethod = null;
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

        Method method = this.mLiveDataMethod.get (data.getAction ().hashCode ());
        if (method == null) {
            UtilLog.e (TAG, "LiveDataRegistry not find method: " + data.getAction ());
            return;
        }

        //方法需要参数
        final Class<?>[] parameter = method.getParameterTypes ();

        try {

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
            UtilLog.e (TAG, "LiveDataRegistry method:" + method.getName () + " e:" + e);
        }
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
