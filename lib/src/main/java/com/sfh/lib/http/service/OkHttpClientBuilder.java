package com.sfh.lib.http.service;

import android.os.Build;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 功能描述:功能描述: OkHttp 与 Retrofit2，RxJava2 组合
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
class OkHttpClientBuilder implements Interceptor {

    private String host;

    private long readTimeout = 10 * 1000L;

    private long connectTimeout = 10 * 1000L;

    private long writeTimeout = 10 * 1000L;

    private boolean log = false;

    /***
     * 构造
     * @param host IP 地址
     */
    public OkHttpClientBuilder(String host) {

        this.host = host;
    }

    /***
     *
     * @param log true 打开日志 false 关闭日志
     * @return
     */
    public OkHttpClientBuilder setLog(boolean log) {
        this.log = log;
        return this;
    }

    /***
     * 设置超时时间
     * @param readTimeout
     * @param connectTimeout
     * @param writeTimeout
     * @return
     */
    public OkHttpClientBuilder setTimeout(long readTimeout, long connectTimeout, long writeTimeout) {
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
        this.writeTimeout = writeTimeout;
        return this;
    }

    /***
     * 创建Retrofit
     * @return
     */
    public Retrofit build() {

        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();

        httpBuilder.addInterceptor(this);
        //失败重连
        httpBuilder.retryOnConnectionFailure(true);
        httpBuilder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
        httpBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        httpBuilder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
        if (log) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpBuilder.addNetworkInterceptor(loggingInterceptor);
        }
        OkHttpClient okHttpClient = httpBuilder.build();

        Retrofit.Builder builder = new Retrofit.Builder();
        //适配RxJava2.0
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        //请求的结果转为实体类
        builder.addConverterFactory(GsonConverterFactory.create());
        builder.baseUrl(this.host);
        builder.client(okHttpClient);
        return builder.build();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request;
        //增加关闭连接，不让它保持连接,防止出现Caused by: java.io.EOFException: \n not found: size=0 content=
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
            request = chain.request()
                    .newBuilder()
                    .addHeader("Connection", "close")
                    .build();
        } else {
            request = chain.request();
        }
        return chain.proceed(request);
    }
}
