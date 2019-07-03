package com.sfh.lib;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.Toast;

import com.getkeepsafe.relinker.ReLinker;
import com.google.gson.Gson;
import com.sfh.lib.exception.HandleException;
import com.sfh.lib.utils.UtilLog;
import com.sfh.lib.utils.UtilsToast;
import com.tencent.mmkv.MMKV;
import com.tencent.mmkv.MMKVLogLevel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
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
    public static <T extends Application> T getApplication() {

        return (T) defaultManager().getApp();
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
        Object temp = defaultManager().getValue(key, cls);
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
        defaultManager().remove(key);
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
        return defaultManager().putCache(key, value);
    }

    private static AppCacheManager defaultManager() {
        return AppCacheHolder.APP_CACHE;
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
        public AppCacheManager build() {

            AppCacheManager app = AppCacheHolder.APP_CACHE;

            synchronized (app) {

                app.init(this.application);
                // 创建缓存路径
                app.createFile(this.cachePath);
            }
            return app;
        }
    }

    /*--------------------------------------------------属性-----------------------------------------------------*/

    public static final String CACHE_FILE = "CACHE_FILE";
    public static final String MMAPID = "PROCESS_MMAPID";

    private final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

    private Disposable mTaskdisposable;

    private Application mApplication;

    private MMKV mMmkv;

    private Gson mGson;

    private AppCacheManager() {
    }

    public Gson getGson() {
        if (this.mGson == null) {
            this.mGson = new Gson();
        }
        return this.mGson;
    }

    /***
     * 设置AbstractApplication
     * @param application
     * @return
     */
    private  <T extends Application> void init(T application) {

        this.mApplication = application;
        String root = application.getFilesDir().getAbsolutePath() + "/mmkv";
        MMKV.initialize(root, new MMKV.LibLoader() {
            @Override
            public void loadLibrary(String libName) {
                ReLinker.loadLibrary(mApplication, libName);
            }
        });
        MMKV.setLogLevel(MMKVLogLevel.LevelNone);
        this.mApplication.registerComponentCallbacks(this);
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
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable throwable) throws Exception {

                HandleException.handleException(throwable);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onLowMemory() {
        this.getMmkv().clearMemoryCache();
    }

    private Application getApp() {
        return this.mApplication;
    }

    /***
     * 持久化数据
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    private <T> boolean putCache(@NonNull String key, @NonNull T value) {

        final MMKV mmkv = this.getMmkv();

        if (value instanceof Integer) {
            return mmkv.encode(key, (Integer) value);

        } else if (value instanceof Long) {
            return mmkv.encode(key, (Long) value);

        } else if (value instanceof Float) {
            return mmkv.encode(key, (Float) value);

        } else if (value instanceof Boolean) {
            return mmkv.encode(key, (Boolean)value);

        } else if (value instanceof String) {
            return mmkv.encode(key, String.valueOf(value));

        } else if (value instanceof Double) {
            return mmkv.encode(key, (Double) value);

        } else if (value instanceof Parcelable) {
            return mmkv.encode(key, (Parcelable) value);

        } else {
            return mmkv.encode(key, this.getGson().toJson(value));
        }
    }

    /***
     * 查询数据[持久化数据]
     * @param key
     * @return
     */
    private Object getValue(@NonNull String key, @NonNull Class cls) {
        if (TextUtils.isEmpty(key) || cls == null) {
            return null;
        }
        //持久化数据查询
        final MMKV mmkv = this.getMmkv();
        if (!mmkv.contains(key)) {
            return null;
        }
        if (Integer.class.isAssignableFrom(cls)) {
            return mmkv.decodeInt(key, 0);

        } else if (Long.class.isAssignableFrom(cls)) {
            return mmkv.decodeLong(key, 0);

        } else if (Float.class.isAssignableFrom(cls)) {
            return mmkv.decodeFloat(key, 0.0f);

        } else if (Boolean.class.isAssignableFrom(cls)) {
            return mmkv.decodeBool(key, false);

        } else if (String.class.isAssignableFrom(cls)) {
            return mmkv.decodeString(key, "");

        } else if (Double.class.isAssignableFrom(cls)) {
            return mmkv.decodeDouble(key, 0.0);

        } else if (Parcelable.class.isAssignableFrom(cls)) {
            return mmkv.decodeParcelable(key, cls);

        } else {
            return this.getGson().fromJson(mmkv.decodeString(key), cls);
        }
    }

    private MMKV getMmkv() {
        if (this.mMmkv == null) {
            try {
                this.mMmkv = MMKV.mmkvWithID(MMAPID, MMKV.MULTI_PROCESS_MODE);
            } catch (IllegalStateException e) {
                UtilLog.e(AppCacheManager.class, e.getMessage());

                MMKV.initialize(this.mApplication);
                this.mMmkv = MMKV.mmkvWithID(MMAPID, MMKV.MULTI_PROCESS_MODE);
            }
        }
        return this.mMmkv;
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    private void remove(String... key) {
        MMKV mmkv = this.getMmkv();
        mmkv.removeValuesForKeys(key);
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
        this.mTaskdisposable = Observable.interval(1, 30, TimeUnit.SECONDS).take(10).map(new Function<Long, Boolean>() {

            @Override
            public Boolean apply(Long aLong) throws Exception {
                final MMKV mmkv = getMmkv();
                File cache;
                if (TextUtils.isEmpty(path)) {
                    //使用APP 私有目录 /storage/emulated/0/Android/data/应用包名/cache
                    cache = new File(mApplication.getExternalCacheDir(), path);
                    if (cache.exists() || cache.mkdirs()) {
                        mmkv.encode(CACHE_FILE, cache.getAbsolutePath());
                        return true;
                    }
                } else {
                    //SD目录
                    if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
                        //内部存储 /storage/emulated/0
                        cache = new File(Environment.getExternalStorageDirectory(), path);
                    } else {
                        // 公共目录 /storage/emulated/0/Downloads
                        cache = new File(android.os.Environment.getDownloadCacheDirectory(), path);
                        if (!cache.exists()) {
                            cache = new File(android.os.Environment.getDataDirectory(), path);
                        }
                    }

                    if (cache.exists()) {
                        //创建目录存在
                        mmkv.encode(CACHE_FILE, cache.getAbsolutePath());
                        return true;
                    }

                    // 创建目录 成功
                    if (cache.mkdirs()) {
                        mmkv.encode(CACHE_FILE, cache.getAbsolutePath());
                        return true;
                    } else {
                        //使用APP 私有目录 /storage/emulated/0/Android/data/应用包名/cache
                        cache = new File(mApplication.getExternalCacheDir(), path);
                        if (cache.exists() || cache.mkdirs()) {
                            mmkv.encode(CACHE_FILE, cache.getAbsolutePath());
                            return true;
                        }
                    }
                }
                return false;
            }
        }).observeOn(Schedulers.newThread()).subscribe(this);
    }

    @Override
    public void accept(Boolean result) throws Exception {

        if (result && mTaskdisposable != null) {
            mTaskdisposable.dispose();
        }
    }
}
