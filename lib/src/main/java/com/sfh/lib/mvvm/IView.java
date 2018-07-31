package com.sfh.lib.mvvm;


import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 功能描述:
 *
 * @author sunfeihu
 * @date 2016/11/14
 */

public interface IView  {

    @NonNull
    LifecycleOwner getLifecycleOwner();


    @Nullable
    <T extends IModel> T getViewModel();


    /***
     * 获取LiveData 监听
     * @return
     */
    @NonNull
    Observer getObserver();

}
