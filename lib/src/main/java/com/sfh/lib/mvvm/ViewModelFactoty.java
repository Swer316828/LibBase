package com.sfh.lib.mvvm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.sfh.lib.HandleException;

import java.lang.reflect.Constructor;

/**
 * 功能描述: ViewModel 对象生产器
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/24
 */
public class ViewModelFactoty extends ViewModelProvider.NewInstanceFactory {

    ILiveData baseLiveData;

    public ViewModelFactoty(ILiveData liveData) {
        this.baseLiveData = liveData;
    }

    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (!BaseViewModel.class.isAssignableFrom(modelClass)){
            return super.create(modelClass);
        }
        try {
            Constructor<T> constructor = modelClass.getConstructor(new Class[]{ILiveData.class});
            constructor.setAccessible(true);
            return constructor.newInstance(baseLiveData);
        } catch (Exception e) {
            throw HandleException.handleException(e);
        }
    }
}
