package com.sfh.lib.mvvm;


import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 功能描述:
 *
 * @author sunfeihu
 * @date 2016/11/14
 */

public interface IView extends LifecycleOwner, Observer {

    @Nullable
    <T extends IViewModel> T getViewModel();

}
