package com.sfh.lib.http;

import okhttp3.OkHttpClient;

/**
 * 功能描述:网络连接接口
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public interface IHttpClient {

    /***
     * 获取OkHttp 网络连接对象
     * @return
     */
    OkHttpClient getHttpService(IHttpConfig config);

}
