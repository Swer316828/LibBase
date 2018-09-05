package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.mvvm.IViewModel;
import com.sfh.lib.mvvm.IView;
import com.sfh.lib.mvvm.annotation.LiveDataMatch;
import com.sfh.lib.rx.EmptyResult;
import com.sfh.lib.ui.AbstractLifecycleActivity;
import com.sfh.lib.ui.AbstractLifecycleFragment;
import com.sfh.lib.utils.UtilLog;
import com.sfh.lib.utils.ViewModelProviders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;


/**
 * 功能描述:LiveDataRegistry 借助 ViewModel 生命周期管理
 * 2.处理UI LiveData 数据刷新,把LiveDate 数据持有者注入到真正业务ViewModel 中
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
public class LiveDataRegistry<V extends IView> extends ViewModel implements Function<V, Boolean> {


    private final static String TAG = LiveDataRegistry.class.getName();

    private RetrofitManager mRetrofit;

    public LiveDataRegistry() {
        this.mRetrofit = new RetrofitManager();
    }

    public <T extends BaseViewModel> T getViewModel(AbstractLifecycleActivity activity) {

        ///对象的直接超类的 Type
        Type type = activity.getClass().getGenericSuperclass();
        if (type == null) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            //参数化类型
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if (types != null && types.length > 0) {
                return ViewModelProviders.of(activity).get((Class<T>) types[0]);
            }
        }
        return null;
    }

    public <T extends BaseViewModel> T getViewModel(AbstractLifecycleFragment fragment) {

        ///对象的直接超类的 Type
        Type type = fragment.getClass().getGenericSuperclass();
        if (type == null) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            //参数化类型
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if (types != null && types.length > 0) {
                return ViewModelProviders.of(fragment).get((Class<T>) types[0]);
            }
        }
        return null;
    }

    /***
     * 绑定UI 数据刷新
     * @param listener
     */
    public void observe(@NonNull V listener) {
        UtilLog.d(TAG, "LiveDataRegistry =========== 注册监听");
        IViewModel model = listener.getViewModel();
        if (model != null) {
            //LiveData 加入生命周期管理中
            model.getLiveData().observe(listener.getLifecycleOwner(), listener.getObserver());
            // 注册LiveData监听
            this.mRetrofit.execute(Flowable.just(listener).map(this).onBackpressureLatest(), new EmptyResult());
        }

    }

    @Override
    public Boolean apply(V listener) throws Exception {

        final IViewModel viewModel = listener.getViewModel();
        if (viewModel == null) {
            return false;
        }

        final Method[] methods = listener.getClass().getDeclaredMethods();
        for (Method method : methods) {

            LiveDataMatch liveEvent = method.getAnnotation(LiveDataMatch.class);
            if (liveEvent == null) {
                continue;
            }
            // 注册LiveData监听
            viewModel.putLiveDataMethod(method);
        }
        return true;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        UtilLog.d(TAG, "LiveDataRegistry =========== 资源销毁");
        if (this.mRetrofit != null) {
            this.mRetrofit.clearAll();
            this.mRetrofit = null;
        }

    }

    /***
     * 显示UI 数据
     * @param view
     * @param data
     */
    public void showLiveData(@NonNull Object view, @Nullable UIData data) {
        // LiveData 数据监听
        if (view == null || data == null) {
            return;
        }

        Method method = data.getAction();
        try {
            //方法需要参数
            Class<?>[] parameter = method.getParameterTypes();
            if (parameter == null || parameter.length == 0) {
                method.invoke(view);
            } else {
                method.invoke(view, data.getData());
            }
        } catch (Exception e) {
            UtilLog.e(TAG, "LiveDataRegistry method:" + method.getName() + " e:" + e);
        }
    }
}
