package com.sfh.lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sfh.lib.exception.HandleException;
import com.sfh.lib.utils.UtilsToast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
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
public class AppCacheManager implements Consumer<Boolean> {

    /***
     * 获取静态对象
     * @return AppCacheManager
     */
    public static AppCacheManager newInstance() {

        return AppCacheHolder.APP_CACHE;
    }

    /***
     * 获取AbstractApplication
     * @return
     */
    public static AbstractApplication getApplication() {

        return AppCacheHolder.APP_CACHE.application;
    }

    /***
     * 内部静态对象
     * @author SunFeihu 孙飞虎
     */
    protected static class AppCacheHolder {

        public static final AppCacheManager APP_CACHE = new AppCacheManager ();
    }

    /***
     * 清除缓存数据
     */
    public static void onLowMemory() {

        AppCacheHolder.APP_CACHE.cacheObject.evictAll ();
    }

    /**
     * 功能描述:全局缓存构建Builder模式
     *
     * @author SunFeihu 孙飞虎
     * @company 中储南京智慧物流科技有限公司
     * @copyright （版权）中储南京智慧物流科技有限公司所有
     * @date 2018/3/29
     */
    public static class Builder {

        AbstractApplication application;

        public Builder(@NonNull AbstractApplication context) {

            this.application = context;
        }

        /**
         * 创建缓存
         *
         * @return
         */
        public AppCacheManager build() {

            AppCacheManager app = AppCacheHolder.APP_CACHE;

            synchronized (app) {

                app.inject (this.application);

                String cachePath = this.application.getCachePath ();

                if (!TextUtils.isEmpty (cachePath)) {
                    // 创建缓存路径
                    app.createFile (cachePath);
                }

                String prefFile = this.application.getPreFile ();
                if (!TextUtils.isEmpty (prefFile)) {
                    // 创建sharePrefer 文件
                    app.preferences = application.getSharedPreferences (prefFile, Context.MODE_MULTI_PROCESS);
                }
                // 防止Disposable 之后出现异常导致应用崩溃
                RxJavaPlugins.setErrorHandler (new Consumer<Throwable> () {

                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        HandleException.handleException (throwable);
                    }
                });
            }

            return app;
        }
    }


    /***
     * 获取缓存信息
     * [先从内存查询，存在则返回，否则本地缓存文件查询存在则返回并放入内存 否则返回默认]
     * @param key
     * @param <T>
     * @return
     */
    public static <T> T getCache(@NonNull String key, @NonNull Class<T> cls, Object... defaultObject) {

        //内存查询
        Object temp = AppCacheHolder.APP_CACHE.cacheObject.get (key);
        if (temp == null) {
            //文件查询
            temp = AppCacheHolder.APP_CACHE.getObject (key, cls, defaultObject);
            if (temp != null) {
                //存在临时缓存
                putCache (key, temp, true);
            }
        }
        return (T) temp;
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

        if (TextUtils.isEmpty (key)) {
            return false;
        }

        AppCacheHolder.APP_CACHE.cacheObject.put (key, value);
        if (persist != null && persist.length > 0 && persist[0]) {
            AppCacheHolder.APP_CACHE.saveObject (key, value);
        }
        return true;
    }

    /***
     * 清除缓存信息
     * @param key
     * @return
     */
    public static void removeCache(@NonNull String... key) {

        if (key == null) {
            return;
        }
        AppCacheManager app = AppCacheHolder.APP_CACHE;
        SharedPreferences.Editor editor = app.preferences.edit ();
        for (String k : key) {
            app.cacheObject.remove (k);
            editor.remove (k);
        }
        editor.apply ();
    }

    /***
     * 清除所有信息
     * @return
     */
    public static void onDestroy() {

        AppCacheHolder.APP_CACHE.cacheObject.evictAll ();
        AppCacheHolder.APP_CACHE.preferences.edit ().clear ().commit ();
    }

    /*--------------------------------------------------属性-----------------------------------------------------*/

    private AbstractApplication application;

    private SharedPreferences preferences;

    /*** 内存缓存数据集合 10M以下Lru 缓存策略算法*/
    private final LruCache<String, Object> cacheObject = new LruCache<String, Object> ((int) Runtime.getRuntime ().maxMemory () / 1024 / 50) {

        @Override
        protected int sizeOf(String key, Object value) {

            return String.valueOf (value).getBytes ().length / 1024;
        }
    };

    private AppCacheManager() {

    }


    /***
     * 设置AbstractApplication
     * @param application
     * @return
     */
    private AppCacheManager inject(AbstractApplication application) {

        this.application = application;
        return this;
    }

    /***
     * 保存数据
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    private <T> boolean saveObject(@NonNull String key, @NonNull T value) {

        if (this.preferences == null || TextUtils.isEmpty (key)) {
            return false;
        }

        SharedPreferences.Editor editor = this.preferences.edit ();
        if (value instanceof Integer
                || value instanceof String
                || value instanceof Float
                || value instanceof Long
                || value instanceof Boolean
        ) {
            editor.putString (key, String.valueOf (value));
        } else {
            // 非基本类型
            editor.putString (key, new Gson ().toJson (value));
        }
        editor.apply ();
        return true;
    }

    /***
     * 查询数据
     * @param key
     * @param defaultObject
     * @return
     */
    private Object getObject(@NonNull String key, @NonNull Class cls, Object... defaultObject) {

        if (this.preferences == null || TextUtils.isEmpty (key) || cls == null) {
            return (defaultObject != null && defaultObject.length > 0) ? defaultObject[0] : null;
        }

        String value = this.preferences.getString (key, "");
        if (TextUtils.isEmpty (value)) {
            return (defaultObject != null && defaultObject.length > 0) ? defaultObject[0] : null;
        }

        if (Integer.class.isAssignableFrom (cls)) {
            return Integer.valueOf (value);
        } else if (Long.class.isAssignableFrom (cls)) {
            return Long.valueOf (value);
        } else if (Float.class.isAssignableFrom (cls)) {
            return Float.valueOf (value);
        } else if (Boolean.class.isAssignableFrom (cls)) {
            return Boolean.valueOf (value);
        } else if (String.class.isAssignableFrom (cls)) {
            return value;
        } else {
            return new Gson ().fromJson (value, cls);
        }

    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    private void remove(String key) {

        if (preferences.contains (key)) {
            SharedPreferences.Editor editor = this.preferences.edit ();
            editor.remove (key);
            editor.apply ();
        }
    }

    /***
     * 返回缓存文件 可能出现NULL
     * @return
     */
    @Nullable
    public static File getFileCache() {

        String path = getApplication ().getCachePath ();
        if (TextUtils.isEmpty (path)) {
            return null;
        }

        File cache = AppCacheHolder.APP_CACHE.application.getExternalFilesDir (path);
        if (cache.exists ()) {
            return cache;
        }

        if (cache.mkdirs ()) {
            return cache;
        }
        return null;
    }

    Disposable mTaskdisposable;

    /***
     * 创建缓存文件目录
     * @param path 目录名称
     * @return
     */
    private void createFile(final String path) {
        // 背压模式
        if (this.mTaskdisposable != null) {
            this.mTaskdisposable.dispose ();
        }
        //创建缓存失败，线创建定时30秒检查一次，10次结束
        this.mTaskdisposable = Flowable.interval (3, 30, TimeUnit.SECONDS).take (10).map (new Function<Long, Boolean> () {

            @Override
            public Boolean apply(Long aLong) throws Exception {

                File cache;
                //SD目录
                if (android.os.Environment.MEDIA_MOUNTED.equals (android.os.Environment.getExternalStorageState ())) {
                    //内部存储 /storage/emulated/0
                    cache = new File (Environment.getExternalStorageDirectory (), path);
                } else {
                    // 公共目录 /storage/emulated/0/Downloads
                    cache = new File (android.os.Environment.getDownloadCacheDirectory (), path);
                    if (!cache.exists ()) {
                        cache = new File (android.os.Environment.getDataDirectory () , path);
                    }
                }

                if (cache.exists ()) {
                    //创建目录存在
                    return true;
                }

                // 创建目录 成功
                if (cache.mkdirs ()) {
                    return true;
                } else {
                    //使用APP 私有目录 /storage/emulated/0/Android/data/应用包名/cache
                    cache = new File (application.getExternalCacheDir (), path);
                    if (cache.exists ()) {
                        return true;
                    } else {
                        return cache.mkdirs ();
                    }
                }
            }
        }).onBackpressureLatest ().observeOn (Schedulers.newThread ()).subscribe (this);
    }


    @Override
    public void accept(Boolean result) throws Exception {

        if (result && mTaskdisposable != null) {
            mTaskdisposable.dispose ();
        } else {
            Toast toast = Toast.makeText (application, "请检查应用存储权限是否打开", Toast.LENGTH_LONG);
            UtilsToast.hook (toast);
            toast.show ();
        }
    }
}
