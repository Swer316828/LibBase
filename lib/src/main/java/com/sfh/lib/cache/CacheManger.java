package com.sfh.lib.cache;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.google.gson.Gson;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/10/22
 */
public class CacheManger implements CacheListener {


    private final SharedPreferences mPreferences;
    private final Persist mPersist;
    private final LruCache<String, ItemCache> mLruCache;

    public CacheManger(Application application) {

        String packName = application.getPackageName().replace(".", "");
        this.mPreferences = application.getSharedPreferences(packName, Context.MODE_PRIVATE);
        //默认最大缓存15个对象数据
        this.mLruCache = new LruCache(17){
            @Override
            protected int sizeOf(Object key, Object value) {
                return 1;
            }
        };
        this.mPersist = new Persist(this.mPreferences.edit(), this.mLruCache);

    }

    @Override
    public String getString(String key, @Nullable String defValue) {
        //内存缓存查询
        ItemCache value = this.mLruCache.get(key);
        if (value == null) {
            //持久化查询
            String v = this.mPreferences.getString(key, defValue);
            if (!TextUtils.equals(v, defValue)) {
                this.mLruCache.put(key, new ItemCache(v));
                return v;
            }
            return defValue;

        }
        return value.itemString;
    }

    @Override
    public int getInt(String key, int defValue) {
        //内存缓存查询
        ItemCache value = this.mLruCache.get(key);
        if (value == null) {
            //持久化查询
            int v = this.mPreferences.getInt(key, defValue);
            if (defValue != v) {
                this.mLruCache.put(key, new ItemCache(v));
                return v;
            }
            return defValue;

        }
        return value.itemInt;
    }

    @Override
    public long getLong(String key, long defValue) {
        //内存缓存查询
        ItemCache value = this.mLruCache.get(key);
        if (value == null) {
            //持久化查询
            long v = this.mPreferences.getLong(key, defValue);
            if (defValue != v) {
                this.mLruCache.put(key, new ItemCache(v));
                return v;
            }
            return defValue;

        }
        return value.itemLong;
    }

    @Override
    public float getFloat(String key, float defValue) {
        //内存缓存查询
        ItemCache value = this.mLruCache.get(key);
        if (value == null) {
            //持久化查询
            float v = this.mPreferences.getFloat(key, defValue);
            if (v != defValue) {
                this.mLruCache.put(key, new ItemCache(v));
                return v;
            }
            return defValue;

        }
        return value.itemFloat;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        //内存缓存查询
        ItemCache value = this.mLruCache.get(key);
        if (value == null) {
            //持久化查询
            boolean v = this.mPreferences.getBoolean(key, defValue);
            if (v != defValue) {
                this.mLruCache.put(key, new ItemCache(v));
                return v;
            }
            return defValue;

        }
        return value.itemBoolean;
    }

    @Override
    public <T> T getObject(String key, Class<T> cls) {
        //内存缓存查询
        ItemCache value = this.mLruCache.get(key);
        if (value == null) {
            //持久化查询
            String v = this.mPreferences.getString(key, "");
            if (!TextUtils.isEmpty(v)) {
                T t = new Gson().fromJson(v, cls);
                this.mLruCache.put(key, new ItemCache(t));
                return t;
            }
            return null;

        }
        return (T) value.itemObject;
    }


    @Override
    public PersistListener getPersistListener() {
        return this.mPersist;
    }

    class Persist implements CacheListener.PersistListener {
        private final SharedPreferences.Editor editor;
        private final LruCache<String, ItemCache> mLruCache;


        public Persist(SharedPreferences.Editor editor, LruCache<String, ItemCache> lruCache) {
            this.editor = editor;
            this.mLruCache = lruCache;
        }


        @Override
        public CacheListener.PersistListener putString(String key, @Nullable String value, boolean persistent) {
            this.mLruCache.put(key, new ItemCache(value));
            if (persistent) {
                this.editor.putString(key, value);
                this.editor.commit();
            }
            return this;
        }

        @Override
        public CacheListener.PersistListener putObject(String key, @Nullable Object value, boolean persistent) {
            this.mLruCache.put(key, new ItemCache(value));
            if (persistent) {
                this.editor.putString(key, new Gson().toJson(value));
                this.editor.commit();
            }
            return this;
        }

        @Override
        public CacheListener.PersistListener putInt(String key, int value, boolean persistent) {
            this.mLruCache.put(key, new ItemCache(value));

            if (persistent) {
                this.editor.putInt(key, value);
                this.editor.commit();
            }
            return this;
        }

        @Override
        public CacheListener.PersistListener putLong(String key, long value, boolean persistent) {
            this.mLruCache.put(key, new ItemCache(value));

            if (persistent) {
                this.editor.putLong(key, value);
                this.editor.commit();
            }
            return this;
        }

        @Override
        public CacheListener.PersistListener putFloat(String key, float value, boolean persistent) {
            this.mLruCache.put(key, new ItemCache(value));

            if (persistent) {
                this.editor.putFloat(key, value);
                this.editor.commit();
            }
            return this;
        }

        @Override
        public CacheListener.PersistListener putBoolean(String key, boolean value, boolean persistent) {

            this.mLruCache.put(key, new ItemCache(value));

            if (persistent) {
                this.editor.putBoolean(key, value);
                this.editor.commit();
            }
            return this;
        }

        @Override
        public CacheListener.PersistListener remove(String key) {
            this.mLruCache.remove(key);
            this.editor.remove(key);
            this.editor.commit();
            return this;
        }

        @Override
        public CacheListener.PersistListener clear() {
            this.mLruCache.evictAll();
            this.editor.clear();
            this.editor.commit();
            return this;
        }

        @Override
        public PersistListener clearCache() {
            this.mLruCache.evictAll();
            return this;
        }

    }

    public class ItemCache {
        /***1 int 2 float 3long 4 boolean 5 String 6 Object*/
        public int type;
        public int itemInt;
        public float itemFloat;
        public long itemLong;
        public boolean itemBoolean;
        public String itemString;
        public Object itemObject;

        public ItemCache(int itemInt) {
            this.type = 1;
            this.itemInt = itemInt;
        }

        public ItemCache(float itemFloat) {
            this.type = 2;
            this.itemFloat = itemFloat;
        }

        public ItemCache(long itemLong) {
            this.type = 3;
            this.itemLong = itemLong;
        }

        public ItemCache(boolean itemBoolean) {
            this.type = 4;
            this.itemBoolean = itemBoolean;
        }

        public ItemCache(String itemString) {
            this.type = 5;
            this.itemString = itemString;
        }

        public ItemCache(Object itemObject) {
            this.type = 6;
            this.itemObject = itemObject;
        }
    }

}
