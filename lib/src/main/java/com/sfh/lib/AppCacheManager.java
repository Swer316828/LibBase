package com.sfh.lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.LruCache;

import com.google.gson.Gson;

import java.io.File;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 功能描述: 全局唯一缓存类
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/3/29
 */
public class AppCacheManager implements Consumer<String> {
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
    private static class AppCacheHolder {

        public static final AppCacheManager APP_CACHE = new AppCacheManager();
    }


    private AbstractApplication application;

    /*** 缓存文件夹路径*/
    private String fileCachePath;

    private SharedPreferences preferences;

    /*** 内存缓存数据集合 10M以下Lru 缓存策略算法*/
    private final LruCache<String, Object> cacheObject = new LruCache<String, Object>((int) Runtime.getRuntime().maxMemory() / 1024 / 50) {
        @Override
        protected int sizeOf(String key, Object value) {
            return String.valueOf(value).getBytes().length / 1024;
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
     * 清除缓存数据
     */
    public void onDertory() {

        if (this.cacheObject != null) {
            this.cacheObject.evictAll();
        }
    }

    /***
     * 返回缓存文件 可能出现NULL
     * @return
     */
    @Nullable
    public File getFileCache() {

        if (TextUtils.isEmpty(this.fileCachePath)) {
            return null;
        }

        return new File(this.fileCachePath);
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

                app.inject(this.application);

                String cachePath = this.application.getCachePath();

                if (!TextUtils.isEmpty(cachePath)) {
                    // 创建缓存路径
                    app.createFile(cachePath);
                }

                String prefFile = this.application.getPreFile();
                if (!TextUtils.isEmpty(prefFile)) {
                    // 创建sharePrefer 文件
                    app.preferences = application.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
                }
            }

            return app;
        }
    }


    /***
     * 获取缓存信息
     * [先从内存查询，存在则返回，否则本地缓存文件查询存在则返回并放入内存 否则返回默认]
     * @param key
     * @param defaultObject 默认值
     * @param <T>
     * @return
     */
    public <T> T getCache(@NonNull  String key,@NonNull  T defaultObject) {

        if (TextUtils.isEmpty(key)) {
            return defaultObject;
        }

        Object temp = this.cacheObject.get(key);
        if (temp == null) {
            temp = this.getObject(key, defaultObject);
            if (temp != null) {
                this.putCache(true, key, temp);
            }
        }
        return temp == null ? defaultObject : (T) temp;
    }


    /**
     * 保存缓存信息[内存]
     *
     * @param key
     * @param value
     * @param <T>   class类型
     * @return
     */
    public <T> boolean putMemoryCache(@NonNull  String key,@NonNull  T value) {
        return this.putCache(false, key, value);
    }

    /***
     * 清除缓存信息【内存】
     * @param key
     * @return
     */
    public void removeMemoryCache(@NonNull  String key) {
        this.removeCache(false, key);
    }


    /***
     * 保存缓存信息
     *
     * @param persist true 持久化数据 false 不持久化数据
     * @param key
     * @param value
     * @return
     */
    public <T> boolean putCache(boolean persist,@NonNull  String key,@NonNull  T value) {
        if (this.cacheObject == null || TextUtils.isEmpty(key)) {
            return false;
        }

        this.cacheObject.put(key, value);
        if (persist) {
            this.putObject(key, value);
        }
        return true;
    }

    /***
     * 清除缓存信息
     * @param persist ture 清除持久化数据 false 不清除持久化数据
     * @param key
     * @return
     */
    public void removeCache(boolean persist,@NonNull  String key) {
        if (this.cacheObject == null) {
            return;
        }

        this.cacheObject.remove(key);
        if (persist) {
            this.remove(key);
        }
    }


    /***
     * 保存数据
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    private <T> boolean putObject(@NonNull String key,@NonNull  T value) {
        if (this.preferences == null || TextUtils.isEmpty(key)) {
            return false;
        }

        SharedPreferences.Editor editor = this.preferences.edit();
        if (value instanceof Integer
                || value instanceof String
                || value instanceof Float
                || value instanceof Long
                || value instanceof Boolean
                ) {
            editor.putString(key, String.valueOf(value));
        } else {
            // 非基本类型
            editor.putString(key, new Gson().toJson(value));
        }
        editor.apply();
        return true;
    }

    /***
     * 查询数据
     * @param key
     * @param defaultObject
     * @return
     */
    private Object getObject(String key, Object defaultObject) {
        if (this.preferences == null || TextUtils.isEmpty(key)) {
            return defaultObject;
        }
        String value = this.preferences.getString(key, "");
        if (TextUtils.isEmpty(value)) {
            return defaultObject;
        }
        if (defaultObject instanceof Integer) {
            return Integer.valueOf(value);
        } else if (defaultObject instanceof Long) {
            return Long.valueOf(value);
        } else if (defaultObject instanceof Float) {
            return Float.valueOf(value);
        } else if (defaultObject instanceof Boolean) {
            return Boolean.valueOf(value);
        } else if (defaultObject instanceof String) {
            return value;
        } else {
            return new Gson().fromJson(value, defaultObject.getClass());
        }

    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    private void remove(String key) {
        if (preferences.contains(key)) {
            SharedPreferences.Editor editor = this.preferences.edit();
            editor.remove(key);
            editor.apply();
        }
    }

    /***
     * 创建缓存文件目录
     * @param path 目录名称
     * @return
     */
    private Disposable createFile(String path) {
        // 背压模式
        return Flowable.just(path).map(new Function<String, String>() {
            @Override
            public String apply(String path) throws Exception {

                File cache;
                //目录
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    //SD
                    cache = new File(Environment.getExternalStorageDirectory() + File.separator + path);
                } else {
                    // 没有内存卡 cache缓存
                    cache = new File(Environment.getDownloadCacheDirectory() + File.separator + path);
                    if (!cache.exists()) {
                        //数据缓存目录
                        cache = new File(Environment.getDataDirectory() + File.separator + path);
                    }
                }
                // 创建目录
                if (!cache.exists()) {
                    cache.mkdirs();
                }
                return cache.getAbsolutePath();
            }
        }).onBackpressureLatest().subscribeOn(Schedulers.io()).subscribe(this);
    }

    @Override
    public void accept(String path) throws Exception {
        // 缓存目录成功
        this.fileCachePath = path;
    }
}
