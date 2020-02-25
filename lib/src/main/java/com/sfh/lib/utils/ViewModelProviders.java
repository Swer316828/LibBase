package com.sfh.lib.utils;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.sfh.lib.AppCacheManager;

import java.lang.reflect.InvocationTargetException;

/**
 * 功能描述: ViewModel 对象生产器
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/24
 */
public class ViewModelProviders {


    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment) {
        return new ViewModelProvider(fragment.getViewModelStore(), DefaultFactory.getInstance());
    }

    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity) {
        return new ViewModelProvider(activity.getViewModelStore(), DefaultFactory.getInstance());
    }

    @SuppressWarnings("WeakerAccess")
    public static class DefaultFactory extends ViewModelProvider.NewInstanceFactory {
        private static DefaultFactory sInstance;

        public static DefaultFactory getInstance() {
            if (sInstance == null) {
                sInstance = new DefaultFactory();
            }
            return sInstance;
        }

        private DefaultFactory() {

        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

            return super.create(modelClass);
        }
    }
}
