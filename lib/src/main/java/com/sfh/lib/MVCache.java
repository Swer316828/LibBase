package com.sfh.lib;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.sfh.lib.cache.CacheListener;
import com.sfh.lib.cache.CacheManger;
import com.sfh.lib.exception.CrashReport;
import com.sfh.lib.utils.ThreadTaskUtils;
import com.sfh.lib.utils.ZLog;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * 功能描述: 全局唯一缓存类
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/3/29
 */
public class MVCache implements Callable<Boolean>, ComponentCallbacks {

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
        CrashReport crashReport;
        boolean debug;

        public Builder setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

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

        public Builder setCrashReport(CrashReport crashReport) {
            this.crashReport = crashReport;
            return this;
        }

        /**
         * 初始化缓存
         *
         * @return
         */
        public MVCache build() {

            MVCache app = Holder.CACHE;
            app.init(this);
            return app;
        }
    }

    public static MVCache getInstance() {
        if (!Holder.CACHE.initStatus) {
            throw new IllegalStateException("MVCache not init!!");
        }
        return Holder.CACHE;
    }


    public static Application getApplication() {

        return Holder.CACHE.getApp();
    }


    /*--------------------------------------------------属性-----------------------------------------------------*/
    private static class Holder {
        private static final MVCache CACHE = new MVCache();
    }

    private static final String CACHE_FILE = "CACHE_FILE";

    private static Thread.UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();

    private Application mApplication;

    private CacheListener mCacheListener;

    private String mPath;

    private volatile boolean initStatus;

    private CrashReport mCrashReport;


    private boolean debug;

    private MVCache() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {

            System.out.println(MVCache.class.getName() + " FinalizerWatchdogDaemon TimeoutException:" + e.getMessage());

            if (mCrashReport != null) {
                mCrashReport.accept(e);
            }

            if (debug) {
                DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(t, e);
            }
            //主动忽略异常，阻断 UncaughtExceptionHandler 链式调用，使系统默认的 UncaughtExceptionHandler 不会被调用，防止APP停止运行

        });

    }


    private void init(Builder builder) {

        synchronized (MVCache.class) {

            if (initStatus) {
                ZLog.d("");
                return;
            }
            this.initStatus = true;

            this.debug = builder.debug;
            if (debug) {
                ZLog.setLevel(Log.VERBOSE);
            }

            this.mApplication = builder.application;
            this.mApplication.registerComponentCallbacks(this);

            //数据缓存
            if (builder.cacheListener != null) {
                this.mCacheListener = builder.cacheListener;
            } else {
                this.mCacheListener = new CacheManger(this.mApplication);
            }

            //文件缓存路径
            this.mPath = builder.cachePath;
            if (TextUtils.isEmpty(this.mPath)) {
                //文件缓存路径已包名作为文件名
                this.mPath = mApplication.getPackageName().replace(".", "");
            }
            ThreadTaskUtils.execute(this);
        }

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
        if (!initStatus) {
            throw new IllegalStateException("MVCache not init!!");
        }
        return this.mApplication;
    }

    @Override
    public Boolean call() throws Exception {
        CacheListener.PersistListener editor = this.mCacheListener.getPersistListener();
        File cache;
        if (TextUtils.isEmpty(mPath)) {
            //使用APP 私有目录 /storage/emulated/0/Android/data/应用包名/cache
            cache = new File(mApplication.getExternalCacheDir(), mPath);
            if (cache.exists() || cache.mkdirs()) {
                editor.putString(CACHE_FILE, cache.getAbsolutePath(), true);
                return true;
            }
        } else {
            //SD目录
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                //内部存储 /storage/emulated/0
                cache = new File(Environment.getExternalStorageDirectory(), mPath);
            } else {
                // 公共目录 /storage/emulated/0/Downloads
                cache = new File(Environment.getDownloadCacheDirectory(), mPath);
                if (!cache.exists()) {
                    cache = new File(Environment.getDataDirectory(), mPath);
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
                cache = new File(mApplication.getExternalCacheDir(), mPath);
                if (cache.exists() || cache.mkdirs()) {
                    editor.putString(CACHE_FILE, cache.getAbsolutePath(), true);
                    return true;
                }
            }
        }
        return true;
    }

    public CrashReport getCrashReport() {
        if (!initStatus) {
            throw new IllegalStateException("MVCache not init!!");
        }
        return mCrashReport;
    }

    public CacheListener getCacheListener() {
        if (!initStatus) {
            throw new IllegalStateException("MVCache not init!!");
        }
        return mCacheListener;
    }

    public  String getCacheFile() {
        if (!initStatus) {
            throw new IllegalStateException("MVCache not init!!");
        }
        return this.mPath;
    }
}
