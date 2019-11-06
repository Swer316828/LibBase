package com.sfh.lib.cache;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/10/22
 */
class CacheManger implements CacheListener {
    private final LruCache<String, Object> mLruCache;
    private SharedPreferences preferences;
    private  Persist persist;

    private CacheManger(Application application) {
        String packName = application.getPackageName().replace(".", "");
        this.preferences = application.getSharedPreferences(packName, Context.MODE_PRIVATE);
        this.persist = new Persist( preferences.edit());

        //默认最大缓存15个对象数据
        mLruCache = new LruCache(10);
    }

    @Override
    public String getString(String key, @Nullable String defValue) {
        //内存缓存查询
        String value = mLruCache.get(key).toString();
        if (TextUtils.isEmpty(value)){
            //持久化查询
        }
        return TextUtils.isEmpty(value)?defValue:value;
    }

    @Override
    public int getInt(String key, int defValue) {
        return 0;
    }

    @Override
    public long getLong(String key, long defValue) {
        return 0;
    }

    @Override
    public float getFloat(String key, float defValue) {
        return 0;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return false;
    }

    @Override
    public boolean contains(String key) {
        return false;
    }

    @Override
    public PersistListener getPersistListener() {
        return persist;
    }

    class Persist implements CacheListener.PersistListener {
        SharedPreferences.Editor editor;

        public Persist(SharedPreferences.Editor editor) {
            this.editor = editor;
        }


        @Override
        public CacheListener.PersistListener putString(String key, @Nullable String value) {
            this.editor.putString(key, value);
            return this;
        }

        @Override
        public CacheListener.PersistListener putObject(String key, @Nullable Object values) {
            return null;
        }

        @Override
        public CacheListener.PersistListener putInt(String key, int value) {
            this.editor.putInt(key, value);
            return this;
        }

        @Override
        public CacheListener.PersistListener putLong(String key, long value) {
            this.editor.putLong(key, value);
            return this;
        }

        @Override
        public CacheListener.PersistListener putFloat(String key, float value) {
            this.editor.putFloat(key, value);
            return this;
        }

        @Override
        public CacheListener.PersistListener putBoolean(String key, boolean value) {
            this.editor.putBoolean(key, value);
            return this;
        }

        @Override
        public CacheListener.PersistListener remove(String key) {
            this.editor.remove(key);
            return this;
        }

        @Override
        public CacheListener.PersistListener clear() {
            this.editor.clear();
            return this;
        }

        @Override
        public boolean commit(boolean persist) {
            return editor.commit();
        }
    }

}
