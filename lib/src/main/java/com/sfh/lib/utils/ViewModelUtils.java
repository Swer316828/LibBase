package com.sfh.lib.utils;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelStore;
import android.support.annotation.NonNull;

/**
 * 功能描述: ViewModel 对象生产器
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/24
 */
public class ViewModelUtils {

    public static ViewModelProvider of(ViewModelStore modelStore) {
        return new ViewModelProvider(modelStore, DefaultFactory.getInstance());
    }

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

        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

            return super.create(modelClass);
        }
    }
}
