package com.sfh.lib.cache;

import android.support.annotation.Nullable;

/**
 * 功能描述:获取缓存数据接口
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/10/22
 */
public interface CacheListener {
    /**
     * 持久化存储接口
     */
    interface PersistListener {

        PersistListener putString(String key, @Nullable String value, boolean persistent);

        PersistListener putObject(String key, @Nullable Object values, boolean persistent);

        PersistListener putInt(String key, int value, boolean persistent);

        PersistListener putLong(String key, long value, boolean persistent);

        PersistListener putFloat(String key, float value, boolean persistent);

        PersistListener putBoolean(String key, boolean value, boolean persistent);

        PersistListener remove(String key);

        PersistListener clear();

        PersistListener clearCache();
    }

    String getString(String key, @Nullable String defValue);

    int getInt(String key, int defValue);


    long getLong(String key, long defValue);


    float getFloat(String key, float defValue);


    boolean getBoolean(String key, boolean defValue);


    <T> T getObject(String key, Class<T> cls);

    PersistListener getPersistListener();

}
