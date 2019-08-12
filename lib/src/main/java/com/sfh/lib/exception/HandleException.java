package com.sfh.lib.exception;

import android.net.ParseException;

import com.sfh.lib.utils.UtilLog;

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

    public HandleException(ExceptionType type, Throwable throwable) {

        super(throwable);
        this.code = type.code;
        this.msg = type.format;
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
    public static volatile ICrashReport<? super Throwable> crashReportHandler;

    /**
     * 设置异常上报处理
     *
     * @param handler
     */
    public static void setErrorHandler(@Nullable ICrashReport<? super Throwable> handler) {

        crashReportHandler = handler;
    }


    public static HandleException handleException(Throwable e) {

        if (crashReportHandler != null) {
            crashReportHandler.accept(e);
        }

        if (e == null) {
            return new HandleException(ExceptionType.NULL.code, ExceptionType.NULL.format);
        }

        if (e instanceof HandleException) {
            return (HandleException) e;
        }

        final Throwable throwable = e.getCause();

        // 服务器请求超时 or 服务器响应超时
        if (e instanceof ConnectTimeoutException
                || e instanceof java.net.SocketTimeoutException
                || e instanceof java.net.SocketException

                || throwable instanceof ConnectTimeoutException
                || throwable instanceof java.net.SocketTimeoutException
                || throwable instanceof java.net.SocketException
        ) {
            return new HandleException(ExceptionType.TIMEOUT, e);
        }


        // 表示无法连接，也就是说当前主机不存在 or 无法指定被请求的地址 or 无法解析该域名
        if (e instanceof ConnectException
                || e instanceof java.net.NoRouteToHostException
                || e instanceof UnknownHostException

                || throwable instanceof ConnectException
                || throwable instanceof java.net.NoRouteToHostException
                || throwable instanceof UnknownHostException
        ) {
            return new HandleException(ExceptionType.NET, e);
        }

        // 返回数据进行Json解析出现异常，如数据不符合Json数据格式
        if (e instanceof JSONException
                || e instanceof ParseException
                || e instanceof com.google.gson.JsonParseException

                || throwable instanceof JSONException
                || throwable instanceof ParseException
                || throwable instanceof com.google.gson.JsonParseException) {

            return new HandleException(ExceptionType.PARSE, e);
        }

        // IO读写时出现
        if (e instanceof IOException || throwable instanceof IOException) {
            return new HandleException(ExceptionType.IO, e);
        }

        //没有信任证书，导致请求失败
        if (e instanceof SSLException || throwable instanceof SSLException) {
            return new HandleException(ExceptionType.SSL, e);
        }

        // 未捕获异常情况
        return new HandleException(ExceptionType.UNKNOWN, e);
    }


}
