package com.sfh.lib.http.service;

import com.sfh.lib.http.IRxHttpClient;
import com.sfh.lib.utils.UtilLog;

import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * 功能描述:网络服务[需求单列模式]
 * 参考一下方式
 * private static class Builder {
 * private static final HttpClientService CLIENTSERVICE = new HttpClientService();
 * }
 * <p>
 * public static HttpClientService newInstance() {
 * return Builder.CLIENTSERVICE;
 * }
 * <p>
 * 1.网络接口初始化
 * 2.不同服务IP地址可以继承当前类实现不同地址请求
 * 3.默认超时时间设置15s
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public abstract class AbstractHttpClientService implements IRxHttpClient {

    private OkHttpClientBuilder mClientBuilder;

    protected AbstractHttpClientService() {

        //构建网络连接
        this.mClientBuilder = new OkHttpClientBuilder (this);
        UtilLog.setDEBUG (this.log ());
    }


    @Override
    public <T> T getRxHttpService(Class<T> service) {

        return this.mClientBuilder.builder (service);
    }


    @Override
    public OkHttpClient getHttpClientService() {

        return this.mClientBuilder.builder ();
    }

    @Override
    public boolean log() {

        return true;
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

    @Override
    public Map<String, String> getHeader() {

        return null;
    }

    @Override
    public Interceptor getInterceptor() {

        return null;
    }

    @Override
    public Interceptor getNetworkInterceptor() {

        return null;
    }

}
