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
    public interface PersistListener {

        PersistListener putString(String key, @Nullable String value);

        PersistListener putObject(String key, @Nullable Object values);

        PersistListener putInt(String key, int value);

        PersistListener putLong(String key, long value);

        PersistListener putFloat(String key, float value);

        PersistListener putBoolean(String key, boolean value);

        PersistListener remove(String key);

        PersistListener clear();

        boolean commit();
    }

    String getString(String key, @Nullable String defValue);

    int getInt(String key, int defValue);


    long getLong(String key, long defValue);


    float getFloat(String key, float defValue);


    boolean getBoolean(String key, boolean defValue);


    boolean contains(String key);


    PersistListener getPersistListener();

}
