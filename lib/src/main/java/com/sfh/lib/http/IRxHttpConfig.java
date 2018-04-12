package com.sfh.lib.http;

/**
 * 功能描述:通用网络配置
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public interface IRxHttpConfig {

    /***
     * 获取基本地址
     * @return
     */
    String getHots();

    /***
     * 日志
     * @return
     */
    boolean log();


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

}
