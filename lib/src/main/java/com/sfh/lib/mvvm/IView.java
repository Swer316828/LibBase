package com.sfh.lib.mvvm;


import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.Nullable;

/**
 * 功能描述:
 *
 * @author sunfeihu
 * @date 2016/11/14
 */

public interface IView  {

    @Nullable
    <T extends IViewModel> T getViewModel();

    <T> void observer(LiveData<T> liveData);
}
