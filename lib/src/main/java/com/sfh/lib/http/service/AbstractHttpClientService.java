package com.sfh.lib.http.service;

import com.sfh.lib.http.IRxHttpClient;
import com.sfh.lib.http.IRxHttpConfig;
import com.sfh.lib.utils.UtilLog;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 功能描述:网络服务[需求单列模式]
 * 参考一下方式
 * private static class Builder {
 * private static final CommHttpClientService CLIENTSERVICE = new CommHttpClientService();
 * }
 * <p>
 * public static CommHttpClientService newInstance() {
 * return Builder.CLIENTSERVICE;
 * }
 * <p>
 * 1.网络接口初始化
 * 2.默认超时时间设置15s
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public abstract class AbstractHttpClientService implements IRxHttpClient {

    private volatile OkHttpClient mOkHttpClient;
    private AtomicBoolean mBuilder = new AtomicBoolean(false);

    protected AbstractHttpClientService() {
        //构建网络连接
        UtilLog.setDEBUG(this.log());
    }

    @Override
    public OkHttpClient getHttpClientService() {

        if (this.mOkHttpClient == null) {

            OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
            //失败重连
            httpBuilder.retryOnConnectionFailure(true);
            httpBuilder.readTimeout(this.getReadTimeout(), TimeUnit.MILLISECONDS);
            httpBuilder.connectTimeout(this.getConnectTimeout(), TimeUnit.MILLISECONDS);
            httpBuilder.writeTimeout(this.getWriteTimeout(), TimeUnit.MILLISECONDS);
            if (this.getInterceptor() != null) {
                httpBuilder.addInterceptor(this.getInterceptor());
            }
            if (this.getHttpDns() != null) {
                httpBuilder.dns(this.getHttpDns());
            }
            if (this.log()) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpBuilder.addNetworkInterceptor(loggingInterceptor);
            }

            if (this.getNetworkInterceptor() != null) {
                httpBuilder.addNetworkInterceptor(this.getNetworkInterceptor());
            }

            if (this.mOkHttpClient == null && this.mBuilder.compareAndSet(false, true)) {
                this.mOkHttpClient = httpBuilder.build();
            }
        }
        return this.mOkHttpClient;
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

    @Override
    public Dns getHttpDns() {
        return null;
    }

}
