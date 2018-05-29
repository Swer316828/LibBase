package com.sfh.lib.mvp.service;


import android.support.annotation.Nullable;

import com.sfh.lib.mvp.annotation.Service;
import com.sfh.lib.utils.UtilLog;

/**
 * 功能描述:通过注入获取接口实现类型
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/10
 */
public class LoaderService {
    @Nullable
    public static <T> T getService(Class<T> clz) {
        Service service = clz.getAnnotation(Service.class);
        if (service == null || service.achieve() == null) {
            return null;
        }
        try {
            return (T) service.achieve().newInstance();
        } catch (InstantiationException e) {
            UtilLog.e(LoaderService.class, e.toString());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            UtilLog.e(LoaderService.class, e.toString());
        }
        return null;
    }
}
