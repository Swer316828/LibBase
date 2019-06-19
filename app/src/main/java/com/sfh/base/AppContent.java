package com.sfh.base;

import com.sfh.lib.AbstractApplication;

/**
 * 功能描述:全局唯一
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/6/19
 */
public class AppContent extends AbstractApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化操作
        super.init();
    }

    @Override
    public String getPreFile() {
        //SharedPreferences 文件
        return "test";
    }

    @Override
    public String getCachePath() {
        //缓存路径
        return "MVVMTest";
    }

    @Override
    public void onLoseToken(String exit) {
        // 退出
    }
}
