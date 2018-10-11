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

import java.lang.reflect.InvocationTargetException;

/**
 * 功能描述: TODO
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/24
 */
public class ViewModelProviders {

    private static long managerKey = 0;

    public static synchronized String createKey() {
        String str;
        synchronized (ViewModelProviders.class) {
            managerKey += System.currentTimeMillis();
            str = "ViewModelProviders" + String.valueOf(managerKey);
        }
        return str;
    }

    private static DefaultFactory sDefaultFactory;

    private static void initializeFactoryIfNeeded(Application application) {
        if (sDefaultFactory == null) {
            sDefaultFactory = new DefaultFactory(application);
        }
    }

    private static Application checkApplication(Activity activity) {
        Application application = activity.getApplication();
        if (application == null) {
            throw new IllegalStateException("Your activity/fragment is not yet attached to "
                    + "Application. You can't request ViewModel before onCreate call.");
        }
        return application;
    }

    private static Activity checkActivity(Fragment fragment) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            throw new IllegalStateException("Can't create ViewModelProvider for detached fragment");
        }
        return activity;
    }


    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment) {
        initializeFactoryIfNeeded(checkApplication(checkActivity(fragment)));
        return new ViewModelProvider(fragment.getViewModelStore(),sDefaultFactory);
    }


    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity) {
        initializeFactoryIfNeeded(checkApplication(activity));
        return new ViewModelProvider(activity.getViewModelStore(), sDefaultFactory);
    }


    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment, @NonNull ViewModelProvider.Factory factory) {
        checkApplication(checkActivity(fragment));
        return new ViewModelProvider(fragment.getViewModelStore(), factory);
    }


    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity,
                                       @NonNull ViewModelProvider.Factory factory) {
        checkApplication(activity);
        return new ViewModelProvider(activity.getViewModelStore(), factory);
    }

    @SuppressWarnings("WeakerAccess")
    public static class DefaultFactory extends ViewModelProvider.NewInstanceFactory {

        private Application mApplication;


        public DefaultFactory(@NonNull Application application) {
            mApplication = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (AndroidViewModel.class.isAssignableFrom(modelClass)) {
                //noinspection TryWithIdenticalCatches
                try {
                    return modelClass.getConstructor(Application.class).newInstance(mApplication);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            return super.create(modelClass);
        }
    }
}
