package com.sfh.lib;

import android.net.ParseException;

import com.sfh.lib.http.HttpCodeException;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import io.reactivex.annotations.Nullable;

/**
 * 功能描述:异常统一处理信息
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public final class HandleException extends RuntimeException {
    public static final int CODE_UNKNOWN = 10000;
    public static final int CODE_PARSE = 10001;
    public static final int CODE_NET = 10002;
    public static final int CODE_HTTP = 10003;
    public static final int CODE_SSL = 10004;
    public static final int CODE_TIMEOUT = 10005;
    public static final int CODE_IO = 10006;
    public static final int CODE_NULL = 10007;

    /**** 错误码*/
    private int code;

    /**** 错误信息*/
    private String msg;

    public HandleException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public HandleException(int code, String msg, Throwable throwable) {
        super(throwable);
        this.code = code;
        this.msg = msg;
    }


    @Override
    public String getMessage() {

        return this.msg;
    }

    public int getCode() {

        return code;
    }

    public String getMsg() {

        return msg;
    }

    @Override
    public String toString() {

        return "HandleException{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", throwable='" + getCause() +
                '}';
    }

    @Nullable
    public static volatile ICrashReport crashReportHandler;

    /**
     * 设置异常上报处理
     *
     * @param handler
     */
    public static void setErrorHandler(@Nullable ICrashReport handler) {

        crashReportHandler = handler;
    }


    public static HandleException handleException(Throwable e) {

        if (e == null) {
            return new HandleException(CODE_NULL, AppCacheManager.getInitialization().getApplication().getString(R.string.exception_null));
        }

        if (e instanceof HandleException) {
            //已经处理
            return (HandleException) e;
        }

        HandleException exception;
        if (crashReportHandler != null && (exception = crashReportHandler.accept(e)) != null) {
            //上层处理异常
            return exception;
        }

        final Throwable throwable = e.getCause();
        //Http请求错误-参考常见Http错误码如 401，403，404， 500 等
        if (e instanceof HttpCodeException || throwable instanceof HttpCodeException){
            return new HandleException(CODE_HTTP, AppCacheManager.getInitialization().getApplication().getString(R.string.exception_http), e);
        }

        // 服务器请求超时 or 服务器响应超时
        if (e instanceof ConnectTimeoutException
                || e instanceof java.net.SocketTimeoutException
                || e instanceof java.net.SocketException

                || throwable instanceof ConnectTimeoutException
                || throwable instanceof java.net.SocketTimeoutException
                || throwable instanceof java.net.SocketException
        ) {
            return new HandleException(CODE_TIMEOUT, AppCacheManager.getInitialization().getApplication().getString(R.string.exception_timeout), e);
        }


        // 表示无法连接，也就是说当前主机不存在 or 无法指定被请求的地址 or 无法解析该域名
        if (e instanceof ConnectException
                || e instanceof java.net.NoRouteToHostException
                || e instanceof UnknownHostException

                || throwable instanceof ConnectException
                || throwable instanceof java.net.NoRouteToHostException
                || throwable instanceof UnknownHostException
        ) {
            return new HandleException(CODE_NET, AppCacheManager.getInitialization().getApplication().getString(R.string.exception_net), e);
        }

        // 返回数据进行Json解析出现异常，如数据不符合Json数据格式
        if (e instanceof JSONException
                || e instanceof ParseException
                || e instanceof com.google.gson.JsonParseException

                || throwable instanceof JSONException
                || throwable instanceof ParseException
                || throwable instanceof com.google.gson.JsonParseException) {

            return new HandleException(CODE_PARSE, AppCacheManager.getInitialization().getApplication().getString(R.string.exception_parse), e);
        }
        //没有信任证书，导致请求失败
        if (e instanceof SSLException || throwable instanceof SSLException ) {
            return new HandleException(CODE_SSL, AppCacheManager.getInitialization().getApplication().getString(R.string.exception_ssl), e);
        }
        // IO读写时出现
        if (e instanceof IOException || throwable instanceof IOException) {
            return new HandleException(CODE_IO, AppCacheManager.getInitialization().getApplication().getString(R.string.exception_io), e);
        }
        // 未捕获异常情况
        return new HandleException(CODE_UNKNOWN, AppCacheManager.getInitialization().getApplication().getString(R.string.exception_unknown), e);
    }


}
