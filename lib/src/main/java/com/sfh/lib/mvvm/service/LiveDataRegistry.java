package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.SparseArray;

import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.mvvm.IViewModel;
import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.annotation.LiveDataMatch;
import com.sfh.lib.rx.EmptyResult;
import com.sfh.lib.ui.AbstractLifecycleActivity;
import com.sfh.lib.ui.AbstractLifecycleFragment;
import com.sfh.lib.ui.AbstractLifecycleView;
import com.sfh.lib.utils.UtilLog;
import com.sfh.lib.utils.ViewModelProviders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

import static java.util.Arrays.asList;


/**
 * 功能描述:LiveDataRegistry 借助 ViewModel 生命周期管理
 * 2.处理UI LiveData 数据刷新,把LiveDate 数据持有者注入到真正业务ViewModel 中
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
public class LiveDataRegistry<V extends IView> extends ViewModel  {


    private final static String TAG = LiveDataRegistry.class.getName ();


    public static <T extends BaseViewModel> T getViewModel(AbstractLifecycleActivity activity, String managerKey) {

        ///对象的直接超类的 Type
        Type type = activity.getClass ().getGenericSuperclass ();
        if (type == null) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            //参数化类型
            Type[] types = ((ParameterizedType) type).getActualTypeArguments ();
            if (types != null && types.length > 0) {
                return ViewModelProviders.of (activity).get (managerKey, (Class<T>) types[0]);
            }
        }
        return null;
    }

    public static <T extends BaseViewModel> T getViewModel(AbstractLifecycleFragment fragment, String managerKey) {

        ///对象的直接超类的 Type
        Type type = fragment.getClass ().getGenericSuperclass ();
        if (type == null) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            //参数化类型
            Type[] types = ((ParameterizedType) type).getActualTypeArguments ();
            if (types != null && types.length > 0) {
                return ViewModelProviders.of (fragment).get (managerKey, (Class<T>) types[0]);
            }
        }
        return null;
    }

    public static <T extends BaseViewModel> T getViewModel(AbstractLifecycleView lifecycleView, String managerKey) {

        ///对象的直接超类的 Type
        Type type = lifecycleView.getClass ().getGenericSuperclass ();
        if (type == null) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            //参数化类型
            Type[] types = ((ParameterizedType) type).getActualTypeArguments ();
            if (types != null && types.length > 0) {
                return ViewModelProviders.of ((FragmentActivity) lifecycleView.getContext ()).get (managerKey, (Class<T>) types[0]);
            }
        }
        return null;
    }

    private volatile RetrofitManager mRetrofit;

    private SparseArray<String> mBindViewModelRecord;

    public LiveDataRegistry() {

        this.mRetrofit = new RetrofitManager ();
        this.mBindViewModelRecord = new SparseArray<> (3);
    }

    /***
     * 绑定UI 数据刷新
     * @param listener
     */
    final public void observe(@NonNull V listener) {

        UtilLog.d (TAG, "LiveDataRegistry observe =========== 注册监听");
        final IViewModel model = listener.getViewModel ();
        this.observe (listener, model);
    }

    /***
     * 绑定UI 数据刷新
     * @param listener
     */
    final public <T extends IViewModel> void observe(@NonNull V listener, @NonNull T t) {

        final IViewModel viewModel = t;
        if (viewModel == null){
            return;
        }
        final int key = viewModel.getClass ().getName ().hashCode ();
        if (TextUtils.isEmpty (mBindViewModelRecord.get (key))) {
            //LiveData 加入当前生命周期管理中
            listener.observer (viewModel.getLiveData ());
            // 解析响应方法
            this.mRetrofit.execute (Flowable.just (listener).map (new Function<V, Object> () {

                @Override
                public Boolean apply(V listener) throws Exception {

                    final Method[] methods = listener.getClass ().getDeclaredMethods ();
                    for (Method method : methods) {

                        LiveDataMatch liveEvent = method.getAnnotation (LiveDataMatch.class);
                        if (liveEvent == null) {
                            continue;
                        }
                        // 注册LiveData监听
                        viewModel.putLiveDataMethod (method);
                    }
                    //记录已绑定状态
                    mBindViewModelRecord.put (key, viewModel.getClass ().getName ());
                    return true;
                }
            }).onBackpressureLatest (), new EmptyResult ());
        }
    }


    @Override
    protected void onCleared() {

        super.onCleared ();
        UtilLog.d (TAG, "LiveDataRegistry onCleared =========== 资源销毁");
        if (this.mRetrofit != null) {
            this.mRetrofit.clearAll ();
            this.mRetrofit = null;
        }

        if (this.mBindViewModelRecord != null) {
            this.mBindViewModelRecord.clear ();
            this.mBindViewModelRecord = null;
        }
    }

    /***
     * 显示UI 数据
     * @param view
     * @param data
     */
    final public void showLiveData(@NonNull Object view, @Nullable UIData data) {
        // LiveData 数据监听
        if (view == null || data == null) {
            UtilLog.e (TAG, "LiveDataRegistry method: null");
            return;
        }
        Method method = data.getAction ();
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
    final public void putDisposable(Disposable disposable) {

        this.mRetrofit.put (disposable);
    }


}
