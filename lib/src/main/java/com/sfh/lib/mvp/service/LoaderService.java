package com.sfh.lib.mvp.service;


import com.sfh.lib.mvp.annotation.Service;

/**
 * 功能描述:通过注入获取接口实现类型
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/10
 */
public class LoaderService {
    public static <T> T getService(Class<T> clz) {
        Service service = clz.getAnnotation(Service.class);
        if (service == null || service.achieve() == null) {
            return null;
        }
        try {
            return (T) service.achieve().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
