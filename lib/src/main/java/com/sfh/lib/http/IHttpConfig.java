package com.sfh.lib.http;

import okhttp3.Dns;
import okhttp3.Interceptor;

/**
 * 功能描述:通用网络配置
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public interface IHttpConfig {

    /**
     * 网络数据读取超时
     * @return
     */
    long getReadTimeout();

    /***
     * 网络连接超时
     * @return
     */
    long getConnectTimeout();

    /***
     * 网络输出超时
     * @return
     */
    long getWriteTimeout();

    /***
     * 请求和响应拦截器
     * @return
     */
    Interceptor getInterceptor();

    /***
     * 网络请求和响应拦截器
     * @return
     */
    Interceptor getNetworkInterceptor();

    /****
     * DNS
     * @return
     */
    Dns getHttpDns();
}
