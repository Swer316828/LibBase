package com.sfh.lib;

import android.app.Application;
import android.text.TextUtils;

import com.sfh.lib.exception.HandleException;

import java.util.concurrent.TimeoutException;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * 功能描述:全局唯一
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/11
 */
public abstract class AbstractApplication extends Application {

    /***
     *  首选项-文件名
     * @return
     */
    public abstract String getPreFile();


    /***
     *  APP 缓存文件目录【APP 统一缓存文件夹】
     * @return
     */
    public abstract String getCachePath();


    /***
     * 退出应用重新进入
     */
    public abstract void onLoseToken(String exit);


    public void init() {
        new AppCacheManager.
                Builder(this).build();
    }

    final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (TextUtils.equals("FinalizerWatchdogDaemon", t.getName()) && e instanceof TimeoutException) {
                    // FinalizerWatchdogDaemon 出现 TimeoutException 时主动忽略这个异常，阻断 UncaughtExceptionHandler 链式调用，使系统默认的 UncaughtExceptionHandler 不会被调用，防止APP停止运行
                } else {
                    defaultUncaughtExceptionHandler.uncaughtException(t, e);
                }
            }
        });

        // 防止Disposable 之后出现异常导致应用崩溃
        RxJavaPlugins.setErrorHandler (new Consumer<Throwable>() {

            @Override
            public void accept(Throwable throwable) throws Exception {

                HandleException.handleException (throwable);
            }
        });
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //清理内存缓存
        AppCacheManager.onLowMemory();
    }


}
