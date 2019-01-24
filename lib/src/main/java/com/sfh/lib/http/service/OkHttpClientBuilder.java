package com.sfh.lib.http.service;

import android.text.TextUtils;

import com.sfh.lib.http.IRxHttpConfig;
import com.sfh.lib.http.service.gson.CustomGsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 功能描述:功能描述: OkHttp3.0 创建OkHttpClient
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
class OkHttpClientBuilder  {

    private IRxHttpConfig mConfig;

    private volatile OkHttpClient mOkHttpClient;

    private volatile Retrofit mRetrofit;

    public OkHttpClientBuilder(IRxHttpConfig config) {

        this.mConfig = config;
    }

    public <T> T builder(Class<T> service) {

        if (this.mRetrofit == null) {

            if (TextUtils.isEmpty (this.mConfig.getHots ())) {
                throw new RuntimeException ("AbstractHttpClientService's host can't be empty");
            }

            Retrofit.Builder builder = new Retrofit.Builder ();
            //适配RxJava2.0
            builder.addCallAdapterFactory (RxJava2CallAdapterFactory.create ());
            //请求的结果转为实体类
            builder.addConverterFactory (CustomGsonConverterFactory.create ());
            //地址base url
            builder.baseUrl (this.mConfig.getHots ());

            OkHttpClient okHttpClient = this.builder ();
            builder.client (okHttpClient);
            this.mRetrofit = builder.build ();
        }
        return this.mRetrofit.create (service);

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
