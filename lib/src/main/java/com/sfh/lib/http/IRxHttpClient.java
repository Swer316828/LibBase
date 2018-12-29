package com.sfh.lib.http;

import okhttp3.OkHttpClient;

/**
 * 功能描述:网络连接接口
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public interface IRxHttpClient extends IRxHttpConfig{


    /****
     * 获取Retrofit 形式网络调用接口
     * @param service
     * @return
     */
    <T> T getRxHttpService(Class<T> service);

    /***
     * 获取OkHttp 网络连接对象
     * @return
     */
    OkHttpClient getHttpClientService();

}
