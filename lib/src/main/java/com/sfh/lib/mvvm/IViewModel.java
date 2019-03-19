package com.sfh.lib.mvvm;


import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sfh.lib.rx.IResult;

import java.lang.reflect.Method;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;


/**
 * 功能描述:基础中间控制层
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public interface IViewModel {

    /***
     * 数据响应
     * @return
     */
    <T> MutableLiveData<T> getLiveData();

    /***
     * 存储响应方法
     * @param method
     */
    void putLiveDataMethod(Method method);
}
