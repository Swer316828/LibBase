package com.sfh.base;

import android.app.Application;

import com.sfh.lib.MVCache;

/**
 * 功能描述:全局唯一
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/6/19
 */
public class AppContent extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化操作
        new MVCache.Builder(this)
                .setCachePath("MVVMTest")//设置缓存文件目录
                .build();
    }
}
