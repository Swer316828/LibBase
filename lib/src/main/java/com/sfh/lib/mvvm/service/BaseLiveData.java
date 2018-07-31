package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.MutableLiveData;

import com.sfh.lib.mvvm.data.IValue;

/**
 * 功能描述: TODO
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/28
 */
public class BaseLiveData<T> extends MutableLiveData<T> {

    private final static String TAG = BaseLiveData.class.getName();

    /**
     * postValue 可以在主线程中赋值，也可以在子线程中赋值
     * @param t
     */
    @Override
    public void postValue(T t) {
        if (t != null && (t instanceof IValue)) {
            ((IValue) t).setLiveData(this);
        }
        super.postValue(t);
    }

    /**
     * setValue 只能在主线程中赋值
     * @param t
     */
    @Override
    public void setValue(T t) {
        if (t != null && (t instanceof IValue)) {
            ((IValue) t).setLiveData(this);
        }
        super.setValue(t);
    }
}
