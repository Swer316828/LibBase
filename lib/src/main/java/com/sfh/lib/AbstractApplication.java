package com.sfh.lib;

import android.app.Application;

/**
 * 功能描述:全局唯一
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/11
 */
public abstract class AbstractApplication extends Application {

    /***
     *  首选项-文件名
     * @return
     */
    public abstract String getPreFile();


    /***
     *  APP 缓存文件目录【APP 统一缓存文件夹】
     * @return
     */
    public abstract String getCachePath();


    /***
     * 退出应用重新进入
     */
    public abstract void onLoseToken(String exit);

    @Override
    public void onCreate() {

        super.onCreate();
        new AppCacheManager.
                Builder(this).build();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //清理内存缓存
        AppCacheManager.newInstance().onDertory();
    }


}
