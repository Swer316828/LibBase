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

import com.google.gson.Gson;
import com.sfh.lib.cache.CacheListener;

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
    /***
     * 内部静态对象
     * @author SunFeihu 孙飞虎
     */
    private static class AppCacheHolder {
        private static final AppCacheManager APP_CACHE = new AppCacheManager();
    }

    /***
     * 获取AbstractApplication
     * @return
     */
    public static Application  getApplication() {

        return AppCacheHolder.APP_CACHE.getApp();
    }

    /***
     * 返回缓存文件
     * @return
     */
    @Nullable
    public static File getFileCache() {

        String path = getCache(CACHE_FILE, String.class, "");
        return new File(path);
    }

    /***
     * 获取信息
     * @param key
     * @param <T>
     * @return
     */
    public static <T> T getCache(@NonNull String key, @NonNull Class<T> cls, Object... defaultObject) {

        T data = (defaultObject != null && defaultObject.length > 0) ? (T) defaultObject[0] : null;
        if (TextUtils.isEmpty(key)) {
            return data;
        }
        Object temp = AppCacheHolder.APP_CACHE.getValue(key, cls);
        return temp == null ? data : (T) temp;
    }


    /***
     * 清除信息
     * @param key
     */
    public static void removeCache(@NonNull String... key) {

        if (key == null || key.length == 0) {
            return;
        }
        AppCacheHolder.APP_CACHE.remove(key);
    }

    /***
     * 保存缓存信息
     *
     * @param persist true 持久化数据 false 不持久化数据
     * @param key
     * @param value
     * @return
     */
    public static <T> boolean putCache(@NonNull String key, @NonNull T value, boolean... persist) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        return AppCacheHolder.APP_CACHE.putCache(key, value);
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

        public Builder(@NonNull Application context) {

            this.application = context;
        }

        public Builder setCachePath(String cachePath) {
            this.cachePath = cachePath;
            return this;
        }

        /**
         * 初始化缓存
         *
         * @return
         */
        public synchronized AppCacheManager build() {

            AppCacheManager app = AppCacheHolder.APP_CACHE;
            app.init(this.application, this.cachePath);
            return app;
        }
    }

    /*--------------------------------------------------属性-----------------------------------------------------*/

    public static final String CACHE_FILE = "CACHE_FILE";

    private final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

    private Disposable mTaskdisposable;

    private Application mApplication;

    private CacheListener _CacheListener;

    private AppCacheManager() {
        //默认最大缓存15个对象数据
        mLruCache = new LruCache(10);
    }

    /***
     * 设置AbstractApplication
     * @param application
     * @return
     */
    private void init(Application application, final String path) {

        this.mApplication = application;
        this.mApplication.registerComponentCallbacks(this);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (TextUtils.equals("FinalizerWatchdogDaemon", t.getName()) && e instanceof TimeoutException) {
                System.out.println(" FinalizerWatchdogDaemon 出现 TimeoutException:" + e.getMessage());
                // FinalizerWatchdogDaemon 出现 TimeoutException 时主动忽略这个异常，阻断 UncaughtExceptionHandler 链式调用，使系统默认的 UncaughtExceptionHandler 不会被调用，防止APP停止运行
            } else {
                defaultUncaughtExceptionHandler.uncaughtException(t, e);
            }
        });

        // 防止Disposable 之后出现异常导致应用崩溃
        RxJavaPlugins.setErrorHandler(RxJavaDisposableThrowableHandler.newInstance());

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
        if (this.mLruCache != null) {
            this.mLruCache.evictAll();
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

            SharedPreferences.Editor editor = getPreferences().edit();
            File cache;
            if (TextUtils.isEmpty(path)) {
                //使用APP 私有目录 /storage/emulated/0/Android/data/应用包名/cache
                cache = new File(mApplication.getExternalCacheDir(), path);
                if (cache.exists() || cache.mkdirs()) {
                    editor.putString(CACHE_FILE, cache.getAbsolutePath()).commit();
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
                    editor.putString(CACHE_FILE, cache.getAbsolutePath()).commit();
                    return true;
                }

                // 创建目录 成功
                if (cache.mkdirs()) {
                    editor.putString(CACHE_FILE, cache.getAbsolutePath()).commit();
                    return true;
                } else {
                    //使用APP 私有目录 /storage/emulated/0/Android/data/应用包名/cache
                    cache = new File(mApplication.getExternalCacheDir(), path);
                    if (cache.exists() || cache.mkdirs()) {
                        editor.putString(CACHE_FILE, cache.getAbsolutePath()).commit();
                        return true;
                    }
                }
            }
            return false;
        }).observeOn(Schedulers.newThread()).subscribe(this);
    }

    @Override
    public void accept(Boolean result) throws Exception {

        if (result && mTaskdisposable != null) {
            mTaskdisposable.dispose();
        }
    }

    private SharedPreferences getPreferences() {
        String packName = this.mApplication.getPackageName().replace(".", "");
        return this.mApplication.getSharedPreferences(packName, Context.MODE_PRIVATE);
    }


    /***
     * 持久化数据
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    private <T> boolean putCache(@NonNull String key, @NonNull T value) {

        final SharedPreferences.Editor editor = getPreferences().edit();

        if (value instanceof Integer) {
            return editor.putInt(key, (Integer) value).commit();

        } else if (value instanceof Long) {
            return editor.putLong(key, (Long) value).commit();

        } else if (value instanceof Float || value instanceof Double) {
            return editor.putFloat(key, (Float) value).commit();

        } else if (value instanceof Boolean) {
            return editor.putBoolean(key, (Boolean) value).commit();

        } else if (value instanceof String) {
            return editor.putString(key, String.valueOf(value)).commit();
        } else {
            return editor.putString(key, new Gson().toJson(value)).commit();
        }
    }

    private <T> boolean putMemoryCache(@NonNull String key, @NonNull T value) {
        this.mLruCache.put(key, value);
        return true;
    }

    /***
     * 查询数据
     * @param key
     * @return
     */
    private Object getValue(@NonNull String key, @NonNull Class cls) {
        if (TextUtils.isEmpty(key) || cls == null) {
            return null;
        }
        //查询临时缓存集合
        Object dataObject = this.mLruCache.get(key);
        if (dataObject != null) {
            return dataObject;
        }

        //持久化数据查询
        SharedPreferences preferences = this.getPreferences();
        if (!preferences.contains(key)) {
            return null;
        }

        if (Integer.class.isAssignableFrom(cls)) {

            dataObject = preferences.getInt(key, 0);

        } else if (Long.class.isAssignableFrom(cls)) {

            dataObject = preferences.getLong(key, 0);

        } else if (Float.class.isAssignableFrom(cls) || Double.class.isAssignableFrom(cls)) {

            dataObject = preferences.getFloat(key, 0.0f);

        } else if (Boolean.class.isAssignableFrom(cls)) {

            dataObject = preferences.getBoolean(key, Boolean.FALSE);

        } else if (String.class.isAssignableFrom(cls)) {

            dataObject = preferences.getString(key, "");

        } else {
            String content = preferences.getString(key, "");
            if (TextUtils.isEmpty(content)) {
                return null;
            }
            dataObject = new Gson().fromJson(content, cls);
        }
        //存入临时缓存，下次加快再次获取速度
        this.mLruCache.put(key, dataObject);
        return dataObject;
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    private void remove(String... key) {
        SharedPreferences.Editor editor = getPreferences().edit();
        for (String k : key) {
            editor.remove(k);
            this.mLruCache.remove(k);
        }
        editor.commit();
    }

    private void removeMemoryCache(String key) {
        this.mLruCache.remove(key);
    }
}
