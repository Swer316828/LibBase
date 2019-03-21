package com.sfh.lib.event;

import android.util.SparseArray;

import com.sfh.lib.mvvm.IViewModel;
import com.sfh.lib.mvvm.data.UIData;
import com.sfh.lib.mvvm.service.BaseViewModel;
import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.rx.EmptyResult;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * 功能描述: 注入RxEvent 监听
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/30
 */
public class RxBusRegistry implements Function<String, Boolean>, IEventResult {

    private final static String TAG = RxBusRegistry.class.getName ();

    private IViewModel mViewModel;

    private SparseArray<EventMethod> mMethod;

    public RxBusRegistry(BaseViewModel viewModel) {

        this.mViewModel = viewModel;
        this.mMethod = new SparseArray<> (3);
    }

    public void init() {

        this.mViewModel.execute (Observable.just (TAG).map (this));
    }

    public synchronized void putEventMethod(int from, Method method) {

        Class<?>[] parameterTypes = method.getParameterTypes ();
        Class<?> dataClass;
        if (parameterTypes != null && (dataClass = parameterTypes[0]) != null) {
            this.mMethod.put (dataClass.hashCode (), new EventMethod (from,method));
            RxBusEventManager.register (dataClass, this);
        }
    }

    @Override
    public Boolean apply(String data) throws Exception {

        if (this.mViewModel != null) {
            final Method[] methods = this.mViewModel.getClass ().getDeclaredMethods ();
            for (Method method : methods) {
                // 注册RxBus监听
                RxBusEvent event = method.getAnnotation (RxBusEvent.class);
                if (event != null) {
                    this.putEventMethod (EventMethod.TYPE_VM, method);
                }
            }
        }
        return true;
    }

    public void onCleared() {

        if (this.mMethod != null) {
            this.mMethod.clear ();
            this.mMethod = null;
        }
        this.mViewModel = null;
    }

    @Override
    public void onEventSuccess(Object data) throws Exception {
        // RxBus 消息监听
        if (data != null) {
            EventMethod eventMethod = this.mMethod.get (data.getClass ().hashCode ());
            if (eventMethod.from == EventMethod.TYPE_VM) {
                eventMethod.method.invoke (this.mViewModel, data);
            } else {
                this.mViewModel.getLiveData ().setValue (new UIData (eventMethod.method.getName (), data));
            }
        }
    }

    @Override
    public void onSubscribe(Disposable d) {

        this.mViewModel.putDisposable (d);
    }
}

