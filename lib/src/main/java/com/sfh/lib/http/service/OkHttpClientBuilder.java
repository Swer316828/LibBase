package com.sfh.lib.http.service;

import com.sfh.lib.http.IRxHttpConfig;
import com.sfh.lib.http.service.gson.CustomGsonConverterFactory;
import com.sfh.lib.utils.UtilLog;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 功能描述:功能描述: OkHttp 与 Retrofit2，RxJava2 组合
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public class OkHttpClientBuilder implements Interceptor {

    IRxHttpConfig mConfig;


    /***
     * 构造
     * @param config IP 地址
     */
    public OkHttpClientBuilder(IRxHttpConfig config) {

        this.mConfig = config;
        UtilLog.setDEBUG(config.log());
    }


    /***
     * 创建Retrofit
     * @return
     */
    public Retrofit build() {

        OkHttpClient okHttpClient = this.httpBuilder();

        Retrofit.Builder builder = new Retrofit.Builder();
        //适配RxJava2.0
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        //请求的结果转为实体类
        builder.addConverterFactory(CustomGsonConverterFactory.create());
        builder.baseUrl(this.mConfig.getHots());
        builder.client(okHttpClient);
        return builder.build();
    }

    public OkHttpClient httpBuilder() {

        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();

        httpBuilder.addInterceptor(this);
        //失败重连
        httpBuilder.retryOnConnectionFailure(true);
        httpBuilder.readTimeout(this.mConfig.getReadTimeout(), TimeUnit.MILLISECONDS);
        httpBuilder.connectTimeout(this.mConfig.getConnectTimeout(), TimeUnit.MILLISECONDS);
        httpBuilder.writeTimeout(this.mConfig.getWriteTimeout(), TimeUnit.MILLISECONDS);
        if (this.mConfig.log()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpBuilder.addNetworkInterceptor(loggingInterceptor);
        }
        return httpBuilder.build();
    }
    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request;

        Map<String, String> head = this.mConfig.getHeader();
        if (head != null && head.size() > 0) {
            Request.Builder builder = chain.request()
                    .newBuilder();
            for (Map.Entry<String, String> entry : head.entrySet()) {
                builder.addHeader(entry.getKey(),entry.getValue());
            }
            request = builder.build();
        }else{
            request = chain.request();
        }

//        //增加关闭连接，不让它保持连接,防止出现Caused by: java.io.EOFException: \n not found: size=0 content=
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
//            request = chain.request()
//                    .newBuilder()
//                    .addHeader("Connection", "close")
//                    .build();
//        } else {
//            request = chain.request();
//        }
        try {
            return chain.proceed(request);
        } catch (SocketTimeoutException e) {
            throw new IOException(e);
        }
    }

    public IRxHttpConfig getConfig() {

        return mConfig;
    }
}
