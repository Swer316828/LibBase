package com.sfh.lib.http.service;

import com.sfh.lib.http.IRxHttpConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 功能描述:功能描述: OkHttp3.0 创建OkHttpClient
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
class OkHttpClientBuilder {

    private IRxHttpConfig mConfig;

    private volatile OkHttpClient mOkHttpClient;

    public OkHttpClientBuilder(IRxHttpConfig config) {

        this.mConfig = config;
    }

    public OkHttpClient builder() {

        if (this.mOkHttpClient == null) {
            OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder ();
            if (this.mConfig.getInterceptor () != null) {
                httpBuilder.addInterceptor (this.mConfig.getInterceptor ());
            }
            //失败重连
            httpBuilder.retryOnConnectionFailure (true);
            httpBuilder.readTimeout (this.mConfig.getReadTimeout (), TimeUnit.MILLISECONDS);
            httpBuilder.connectTimeout (this.mConfig.getConnectTimeout (), TimeUnit.MILLISECONDS);
            httpBuilder.writeTimeout (this.mConfig.getWriteTimeout (), TimeUnit.MILLISECONDS);

            if (this.mConfig.log ()) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor ();
                loggingInterceptor.setLevel (HttpLoggingInterceptor.Level.BODY);
                httpBuilder.addNetworkInterceptor (loggingInterceptor);
            }

            if (this.mConfig.getNetworkInterceptor () != null) {
                httpBuilder.addNetworkInterceptor (this.mConfig.getNetworkInterceptor ());
            }
            this.mOkHttpClient = httpBuilder.build ();
        }
        return this.mOkHttpClient;
    }
}
