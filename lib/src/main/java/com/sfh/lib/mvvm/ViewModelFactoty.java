package com.sfh.lib.mvvm;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.sfh.lib.exception.HandleException;

import java.lang.reflect.Constructor;

/**
 * 功能描述: ViewModel 对象生产器
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/24
 */
public class ViewModelFactoty extends ViewModelProvider.NewInstanceFactory {

    IUIListener listener;

    public ViewModelFactoty(IUIListener listener) {
        this.listener = listener;
    }

    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (!BaseViewModel.class.isAssignableFrom(modelClass)){
            return super.create(modelClass);
        }
        try {
            Constructor<T> constructor = modelClass.getConstructor(new Class[]{IUIListener.class});
            constructor.setAccessible(true);
            return constructor.newInstance(listener);
        } catch (Exception e) {
            throw HandleException.handleException(e);
        }
    }
}
