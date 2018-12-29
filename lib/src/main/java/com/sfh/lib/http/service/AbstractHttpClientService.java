package com.sfh.lib.http.service;

import android.text.TextUtils;

import com.sfh.lib.http.IRxHttpClient;

import okhttp3.OkHttpClient;

/**
 * 功能描述:网络服务[单列模式]
 *
 * 1.网络接口初始化
 * 2.不同服务IP地址可以继承当前类实现不同地址请求
 * 3.默认超时时间设置15s
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public  abstract class AbstractHttpClientService implements IRxHttpClient {

    private volatile OkHttpClientBuilder mClientBuilder;

    @Override
    public <T> T getRxHttpService(Class<T> service) {

        if (TextUtils.isEmpty(this.getHots())) {
            throw new RuntimeException("AbstractHttpClientService's host can't be empty");
        }

        if (this.mClientBuilder == null) {
            this.mClientBuilder = new OkHttpClientBuilder(this);
        }

        return this.mClientBuilder.builderRetrofit().create (service);
    }


    @Override
    public OkHttpClient getHttpClientService() {

        if (mClientBuilder == null) {
            mClientBuilder = new OkHttpClientBuilder (this);
        }
        return mClientBuilder.builderOKHttp ();
    }

    @Override
    public long getReadTimeout() {
        return 15 * 1000L;
    }

    @Override
    public long getConnectTimeout() {
        return 15 * 1000L;
    }

    @Override
    public long getWriteTimeout() {
        return 15 * 1000L;
    }

}
