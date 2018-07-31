package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sfh.lib.mvvm.IModel;
import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.annotation.LiveDataMatch;
import com.sfh.lib.mvvm.service.empty.EmptyResult;
import com.sfh.lib.utils.UtilLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;


/**
 * 功能描述:LiveDataUIRegistry 借助 ViewModel 生命周期管理
 * 2.处理UI LiveData 数据刷新,把LiveDate 数据持有者注入到真正业务ViewModel 中
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
public class LiveDataUIRegistry<V extends IView> extends ViewModel implements  Function<V, Boolean> {


    private final static String TAG = LiveDataUIRegistry.class.getName();

    private volatile Map<Class<?>, Method> mLiveDataMethod;

    private RetrofitManager mRetrofit;

    public LiveDataUIRegistry() {

        this.mLiveDataMethod = new HashMap<>(2);
        this.mRetrofit = new RetrofitManager();
    }


    /***
     * 绑定UI 数据刷新
     * @param listener
     */
    public void observe(@NonNull V listener) {
        UtilLog.d(TAG, "LiveDataUIRegistry =========== 注册监听");
        IModel model = listener.getViewModel();
        if (model != null) {
            //LiveData 加入生命周期管理中
            model.getLiveData().observe(listener.getLifecycleOwner(),listener.getObserver());
            // 注册网络数据监听
            model.onLiveDataClass(NetWorkState.class);
        }
        // 注册LiveData监听
        this.mRetrofit.execute(Flowable.just(listener).map(this).onBackpressureLatest(), new EmptyResult());
    }

    @Override
    public Boolean apply(V listener) throws Exception {

        final IModel viewModel = listener.getViewModel();
        if (viewModel == null) {
            return false;
        }

        final Method[] methods = listener.getClass().getDeclaredMethods();
        for (Method method : methods) {

            LiveDataMatch liveEvent = method.getAnnotation(LiveDataMatch.class);
            if (liveEvent == null) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> dataClass;
            if (parameterTypes != null && (dataClass = parameterTypes[0]) != null) {
                this.mLiveDataMethod.put(dataClass, method);
                // 注册LiveData监听
                viewModel.onLiveDataClass(dataClass);
            }
        }
        return true;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        UtilLog.d(TAG, "LiveDataUIRegistry =========== 资源销毁");
        if (this.mLiveDataMethod != null) {
            this.mLiveDataMethod.clear();
        }
        if (this.mRetrofit != null){
            this.mRetrofit.clearAll();
        }
    }

    /***
     * 显示UI 数据
     * @param view
     * @param data
     */
    public void showLiveData(@NonNull Object view, @Nullable Object data) {
        // LiveData 数据监听
        if (data == null) {
            return;
        }

        Method method = this.getMethod(this.mLiveDataMethod, data);
        this.invokeMethod(view, method, data);
    }

    private Method getMethod(Map<Class<?>, Method> map, Object data) {

        for (Map.Entry<Class<?>, Method> entry : map.entrySet()) {
            Class<?> type = entry.getKey();
            if (type.isInstance(data)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /***
     * 调用对象的方法
     * @param view
     * @param method
     * @param args
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void invokeMethod(@NonNull Object view, @Nullable Method method, Object... args) {

        if (view == null || method == null) {
            return;
        }

        try {
            UtilLog.d(TAG, "LiveDataUIRegistry =========== 显示UI 数据");
            method.invoke(view, args);
        } catch (Exception e) {
            UtilLog.e(TAG, "LiveDataUIRegistry method:" + method + " e:" + e);
        }

    }
}
