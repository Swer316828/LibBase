package com.sfh.lib.exception;

import android.net.ParseException;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLException;

import io.reactivex.annotations.Nullable;
import retrofit2.HttpException;

/**
 * 功能描述:异常统一处理信息
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public final class HandleException extends RuntimeException {

    @Nullable
    static volatile ICrashReport<? super Throwable> crashReportHandler;

    /**
     * 设置异常上报处理
     *
     * @param handler
     */
    public static void setErrorHandler(@Nullable ICrashReport<? super Throwable> handler) {
        crashReportHandler = handler;
    }


    /*** 未识别的异常*/
    public static final int CODE_UNKNOWN_EXCEPTION = 10000;
    public static final String UNKNOWN_EXCEPTION = "系统繁忙,请稍后再试(AD_%s)";

    /*** 数据解析的异常*/
    public static final int CODE_PARSE_EXCEPTION = 10001;
    public static final String PARSE_EXCEPTION = "系统繁忙,请稍后再试(AD_%s)";

    /*** 网络异常*/
    public static final int CODE_NET_EXCEPTION = 10002;
    public static final String NET_EXCEPTION = "系统繁忙,请稍后再试(AD_%s)";

    /*** 网络连接异常*/
    public static final int CODE_HTTP_EXCEPTION = 10003;
    public static final String HTTP_EXCEPTION = "系统繁忙,请稍后再试(AD_%s)";

    /*** 网络超时异常*/
    public static final int CODE_TIMEOUT_EXCEPTION = 10004;
    public static final String TIMEOUT_EXCEPTION = "系统繁忙,请稍后再试(AD_%s)";

    /*** SSL 安全异常*/
    public static final int CODE_SSL_EXCEPTION = 10005;
    public static final String SSL_EXCEPTION = "系统繁忙,请稍后再试(AD_%s)";

    /*** IO 异常*/
    public static final int CODE_IO_EXCEPTION = 10006;
    public static final String IO_EXCEPTION = "系统繁忙,请稍后再试(AD_%s)";

    /*** NullPointerException 指针 异常*/
    public static final int CODE_NULL_EXCEPTION = 10007;
    public static final String NULL_EXCEPTION = "系统繁忙,请稍后再试(AD_%s)";


    public static HandleException handleException(Throwable e) {

        if (crashReportHandler != null) {
            crashReportHandler.accept(e);
        }

        if (e == null || e.getCause() == null) {
            // bugly会将这个throwable上报
            return new HandleException(CODE_NULL_EXCEPTION, NULL_EXCEPTION);
        }

        Throwable throwable = e.getCause();

        // 服务器请求超时 or 服务器响应超时
        if (socketTimeoutException(e)) {
            return new HandleException(CODE_TIMEOUT_EXCEPTION, TIMEOUT_EXCEPTION);
        }

        // Http请求错误-参考常见Http错误码如 401，403，404， 500 等
        if (httpCodeException(e)) {
            return new HandleException(CODE_HTTP_EXCEPTION, HTTP_EXCEPTION);
        }

        // 表示无法连接，也就是说当前主机不存在 or 无法指定被请求的地址 or 无法解析该域名
        if (connectException(e)) {
            return new HandleException(CODE_NET_EXCEPTION, NET_EXCEPTION);
        }
        // 返回数据进行Json解析出现异常，如数据不符合Json数据格式
        if (parseException(e)) {
            return new HandleException(CODE_PARSE_EXCEPTION, PARSE_EXCEPTION);
        }

        // 读写数据的时出现
        if (e instanceof IOException) {
            return new HandleException(CODE_IO_EXCEPTION, IO_EXCEPTION);
        }

        //没有信任证书，导致请求失败
        if (throwable instanceof SSLException) {
            return new HandleException(CODE_SSL_EXCEPTION, SSL_EXCEPTION);
        }

        // 未捕获异常情况
        return new HandleException(CODE_UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION);
    }


    /***
     * 服务器请求超时 or 服务器响应超时
     * @param e
     * @return
     */
    private static boolean socketTimeoutException(Throwable e) {
        if (e instanceof ConnectTimeoutException
                || e instanceof java.net.SocketTimeoutException
                || e instanceof java.net.SocketException
                ) {
            return true;
        }
        Throwable throwable = e.getCause();

        if (throwable instanceof ConnectTimeoutException
                || throwable instanceof java.net.SocketTimeoutException
                || throwable instanceof java.net.SocketException
                ) {
            return true;
        }
        return false;
    }

    /***
     * Http请求错误-参考常见Http错误码如 401，403，404， 500 等
     * @param e
     * @return
     */
    private static boolean httpCodeException(Throwable e) {

        final List<Integer> httpCode = Arrays.asList(401, 403, 404, 408, 500, 502, 503, 504);
        HttpException httpException = null;
        if (e instanceof HttpException) {
            httpException = (HttpException) e;
        } else if (e.getCause() instanceof HttpException) {
            httpException = (HttpException) e.getCause();
        }
        // Http请求错误-参考常见Http错误码如 401，403，404， 500 等
        if (httpException != null && httpCode.contains(httpException.code())) {
            return true;
        }
        return false;
    }

    /***
     * 表示无法连接，也就是说当前主机不存在 or 无法指定被请求的地址 or 无法解析该域名
     * @param e
     * @return
     */
    private static boolean connectException(Throwable e) {
        if (e instanceof ConnectException
                || e instanceof java.net.NoRouteToHostException
                || e instanceof UnknownHostException
                ) {
            return true;
        }
        Throwable throwable = e.getCause();
        if (throwable instanceof ConnectException
                || throwable instanceof java.net.NoRouteToHostException
                || throwable instanceof UnknownHostException
                ) {
            return true;
        }
        return false;
    }

    /***
     * 返回数据进行Json解析出现异常，如数据不符合Json数据格式
     * @param e
     * @return
     */
    private static boolean parseException(Throwable e) {

        if (e instanceof JSONException
                || e instanceof ParseException
                || e instanceof com.google.gson.JsonSyntaxException
                || e instanceof com.google.gson.JsonParseException) {
            return true;
        }

        Throwable throwable = e.getCause();
        // 返回数据进行Json解析出现异常，如数据不符合Json数据格式
        if (throwable instanceof JSONException
                || throwable instanceof ParseException
                || throwable instanceof com.google.gson.JsonSyntaxException
                || throwable instanceof com.google.gson.JsonParseException) {
            return true;
        }
        return false;

    }


    /***
     * 错误码
     */
    private String code;

    /***
     * 错误信息
     */
    private String msg;

    private HandleException(int code, String msg) {
        super(msg);
        this.code = String.valueOf(code);
        this.msg = String.format(msg,this.code);
    }


    public String getCode() {

        return code;
    }

    public String getMsg() {

        return msg;
    }

}