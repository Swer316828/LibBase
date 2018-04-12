package com.sfh.lib.http.service;

import android.text.TextUtils;


import com.sfh.lib.http.IRxHttpClient;
import com.sfh.lib.http.IRxHttpConfig;

import retrofit2.Retrofit;

/**
 * 功能描述:网络服务
 *
 * 1.网络接口初始化
 * 2.不同服务IP地址可以继承当前类实现不同地址请求
 * 3.默认超时时间设置15s
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public abstract class AbstractHttpClientService implements IRxHttpClient, IRxHttpConfig {

    private Retrofit retrofit;

    @Override
    public <T> T getRxHttpService(Class<T> service) {

        IRxHttpConfig config = this.getRxHttpConfig();
        if (config == null || TextUtils.isEmpty(config.getHots())) {
            throw new RuntimeException("AbstractHttpClientService's host can't be empty");
        }

        if (this.retrofit == null) {
            this.retrofit = new OkHttpClientBuilder(config.getHots())
                    .setTimeout(config.getReadTimeout(), config.getConnectTimeout(), config.getWriteTimeout())
                    .setLog(config.log()).build();

        }
        return this.retrofit.create(service);
    }

    @Override
    public long getReadTimeout() {
        return 15 * 1000L;
    }

    @Override
    public long getConnectTimeout() {
        return 45 * 1000L;
    }

    @Override
    public long getWriteTimeout() {
        return 45 * 1000L;
    }

}
