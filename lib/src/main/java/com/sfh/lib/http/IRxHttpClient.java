package com.sfh.lib.http;

/**
 * 功能描述:网络连接接口
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public interface IRxHttpClient {


    /****
     * 获取网络调用接口
     * @param service
     * @return
     */
    <T> T getRxHttpService(Class<T> service);

}
