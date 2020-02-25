package com.sfh.lib.http.service;


import com.sfh.lib.http.IHttpClient;
import com.sfh.lib.http.IHttpConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * 功能描述:网络服务[需求单列模式]
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public class HttpClientService implements IHttpClient {

    private static class Builder {

        private static final IHttpClient INSTANCE = new HttpClientService();
    }

    public static IHttpClient newInstance() {

        return Builder.INSTANCE;
    }


    private volatile OkHttpClient mOkHttpClient;

    @Override
    public OkHttpClient getHttpService(IHttpConfig config) {
        if (this.mOkHttpClient == null) {

            OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
            //失败重连
            httpBuilder.retryOnConnectionFailure(true);
            if (config != null) {
                httpBuilder.readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS);
                httpBuilder.connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS);
                httpBuilder.writeTimeout(config.getWriteTimeout(), TimeUnit.MILLISECONDS);

                Interceptor interceptor = config.getInterceptor();
                if (interceptor != null) {
                    httpBuilder.addInterceptor(interceptor);
                }
                Interceptor networkInterceptor = config.getNetworkInterceptor();
                if (networkInterceptor != null) {
                    httpBuilder.addNetworkInterceptor(networkInterceptor);
                }
                Dns dns = config.getHttpDns();
                if (dns != null) {
                    httpBuilder.dns(dns);
                }
            } else {
                httpBuilder.readTimeout(10 * 1000L, TimeUnit.MILLISECONDS);
                httpBuilder.connectTimeout(10 * 1000L, TimeUnit.MILLISECONDS);
                httpBuilder.writeTimeout(10 * 1000L, TimeUnit.MILLISECONDS);
            }

            this.mOkHttpClient = httpBuilder.build();
        }
        return this.mOkHttpClient;
    }

}
