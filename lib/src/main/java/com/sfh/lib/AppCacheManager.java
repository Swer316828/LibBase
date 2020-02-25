package com.sfh.lib;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.sfh.lib.cache.CacheListener;
import com.sfh.lib.cache.CacheManger;
import com.sfh.lib.exception.HandleException;
import com.sfh.lib.exception.ICrashReport;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

/**
 * 功能描述: 全局唯一缓存类
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/3/29
 */
public class AppCacheManager implements Consumer<Boolean>, ComponentCallbacks {
    /*--------------------------------------------------属性-----------------------------------------------------*/

    public static final String CACHE_FILE = "CACHE_FILE";

    private final Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

    private Disposable mTaskdisposable;

    private Application mApplication;

    private CacheListener mCacheListener;

    /***
     * 内部静态对象
     * @author SunFeihu 孙飞虎
     */
    private static class AppCacheHolder {
        private static final AppCacheManager APP_CACHE = new AppCacheManager();
    }

    public static AppCacheManager getInitialization(){
        return AppCacheHolder.APP_CACHE;
    }

    /***
     * 获取AbstractApplication
     * @return
     */
    public Application getApplication() {

        return AppCacheHolder.APP_CACHE.getApp();
    }

    /***
     * 返回缓存文件
     * @return
     */
    public File getFileCache() {

        String path = this.mCacheListener.getString(CACHE_FILE, "");
        return new File(path);
    }

    /***
     * 获取信息
     * @return
     */
    public  CacheListener getCacheListener() {

        return this.mCacheListener;
    }


    /*--------------------------------------------------全局缓存构建Builder模式-----------------------------------------------------*/

    /**
     * 功能描述:全局缓存构建Builder模式
     *
     * @date 2018/3/29
     */
    public static class Builder {

        Application application;
        String cachePath;
        CacheListener cacheListener;
        ICrashReport crashReport;

        public Builder(@NonNull Application context) {

            this.application = context;
        }

        public Builder setCachePath(String cachePath) {
            this.cachePath = cachePath;
            return this;
        }

        public Builder setCacheListener(CacheListener cacheListener) {
            this.cacheListener = cacheListener;
            return this;
        }

        public Builder setCrashReport(ICrashReport crashReport) {
            this.crashReport = crashReport;
            return this;
        }

        /**
         * 初始化缓存
         *
         * @return
         */
        public synchronized AppCacheManager build() {

            AppCacheManager app = AppCacheHolder.APP_CACHE;
            app.init(this.application, this.cachePath, this.cacheListener);
            HandleException.setErrorHandler(crashReport);
            return app;
        }
    }


    private AppCacheManager() {
        //默认最大缓存15个对象数据
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (TextUtils.equals("FinalizerWatchdogDaemon", t.getName()) && e instanceof TimeoutException) {
                System.out.println(AppCacheManager.class.getName() + " FinalizerWatchdogDaemon TimeoutException:" + e.getMessage());
                // FinalizerWatchdogDaemon 出现 TimeoutException 时主动忽略这个异常，阻断 UncaughtExceptionHandler 链式调用，使系统默认的 UncaughtExceptionHandler 不会被调用，防止APP停止运行

            } else {
                mDefaultUncaughtExceptionHandler.uncaughtException(t, e);
            }
        });
        // 防止Disposable 之后出现异常导致应用崩溃
        RxJavaPlugins.setErrorHandler(throwable -> {
            System.out.println(AppCacheManager.class.getName() + " RxJavaPlugins:" + throwable);
        });

    }

    /***
     * 设置AbstractApplication
     * @param application
     * @return
     */
    private void init(Application application, final String path, CacheListener cacheListener) {

        this.mApplication = application;
        this.mApplication.registerComponentCallbacks(this);
        if (cacheListener != null) {
            this.mCacheListener = cacheListener;
        } else {
            this.mCacheListener = new CacheManger(this.mApplication);
        }

        String file = path;
        if (TextUtils.isEmpty(file)) {
            //文件缓存路径已包名作为文件名
            file = application.getPackageName().replace(".", "");
        }
        this.createFile(file);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onLowMemory() {
        //内存紧张
        if (this.mCacheListener != null) {
            this.mCacheListener.getPersistListener().clearCache();
        }
    }

    private Application getApp() {
        return this.mApplication;
    }


    /***
     * 创建缓存文件目录
     * @param path 目录名称
     * @return
     */
    private void createFile(final String path) {

        // 背压模式
        if (this.mTaskdisposable != null) {
            this.mTaskdisposable.dispose();
        }
        //创建缓存失败，线创建定时30秒检查一次，10次结束
        this.mTaskdisposable = Observable.interval(1, 30, TimeUnit.SECONDS).take(10).map(aLong -> {

            CacheListener.PersistListener editor = this.mCacheListener.getPersistListener();
            File cache;
            if (TextUtils.isEmpty(path)) {
                //使用APP 私有目录 /storage/emulated/0/Android/data/应用包名/cache
                cache = new File(mApplication.getExternalCacheDir(), path);
                if (cache.exists() || cache.mkdirs()) {
                    editor.putString(CACHE_FILE, cache.getAbsolutePath(), true);
                    return true;
                }
            } else {
                //SD目录
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    //内部存储 /storage/emulated/0
                    cache = new File(Environment.getExternalStorageDirectory(), path);
                } else {
                    // 公共目录 /storage/emulated/0/Downloads
                    cache = new File(Environment.getDownloadCacheDirectory(), path);
                    if (!cache.exists()) {
                        cache = new File(Environment.getDataDirectory(), path);
                    }
                }

                if (cache.exists()) {
                    //创建目录存在
                    editor.putString(CACHE_FILE, cache.getAbsolutePath(), true);
                    return true;
                }

                // 创建目录 成功
                if (cache.mkdirs()) {
                    editor.putString(CACHE_FILE, cache.getAbsolutePath(), true);
                    return true;
                } else {
                    //使用APP 私有目录 /storage/emulated/0/Android/data/应用包名/cache
                    cache = new File(mApplication.getExternalCacheDir(), path);
                    if (cache.exists() || cache.mkdirs()) {
                        editor.putString(CACHE_FILE, cache.getAbsolutePath(), true);
                        return true;
                    }
                }
            }
            return false;
        }).observeOn(Schedulers.newThread()).subscribe(this);
    }

    @Override
    public void accept(Boolean result) throws Exception {

        if (result && this.mTaskdisposable != null) {
            this.mTaskdisposable.dispose();
        }
    }


}
