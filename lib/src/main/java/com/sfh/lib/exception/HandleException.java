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

    /*** 未识别的异常*/
    public static final int CODE_UNKNOWN_EXCEPTION = 10000;

    public static final String UNKNOWN_EXCEPTION = "服务请求异常，请重试！(%s)";

    /*** 数据解析的异常*/
    public static final int CODE_PARSE_EXCEPTION = 10001;

    public static final String PARSE_EXCEPTION = "服务请求异常，请重试！(%s)";

    /*** 表示无法连接，也就是说当前主机不存在 or 无法指定被请求的地址 or 无法解析该域名*/
    public static final int CODE_NET_EXCEPTION = 10002;

    public static final String NET_EXCEPTION = "服务请求异常，请重试！(%s)";

    /***  Http请求错误-参考常见Http错误码如 400,401, 403, 404,405,406,407, 408,409,410,411,412,413,414,415,416,417, 500, 502,502, 503, 504,505*/
    public static final int CODE_HTTP_EXCEPTION = 10003;

    public static final String HTTP_EXCEPTION = "服务请求超时，请重试！(%s)";

    /*** SSL 安全异常*/
    public static final int CODE_SSL_EXCEPTION = 10004;

    public static final String SSL_EXCEPTION = "请检查手机设置后重试，谢谢！(%s)";

    /*** 服务器请求超时 or 服务器响应超时*/
    public static final int CODE_TIMEOUT_EXCEPTION = 10005;

    public static final String TIMEOUT_EXCEPTION = "服务请求超时，请稍后再试！(%s)";

    /*** 读写数据的时出现错误*/
    public static final int CODE_IO_EXCEPTION = 10006;

    public static final String IO_EXCEPTION = "请核实输入的数据再提交，谢谢！(%s)";

    /*** NullPointerException 指针 异常*/
    public static final int CODE_NULL_EXCEPTION = 10007;

    public static final String NULL_EXCEPTION = "系统繁忙,请稍后再试(%s)";


    public static HandleException handleException(Throwable e) {

        UtilLog.e(HandleException.class, "HandleException Throwable:" + e);

        if (crashReportHandler != null) {
            crashReportHandler.accept(e);
        }

        if (e == null) {
            // bugly会将这个throwable上报
            return new HandleException(CODE_NULL_EXCEPTION, NULL_EXCEPTION, e);
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
            return new HandleException(CODE_TIMEOUT_EXCEPTION, TIMEOUT_EXCEPTION, e);
        }


        // 表示无法连接，也就是说当前主机不存在 or 无法指定被请求的地址 or 无法解析该域名
        if (e instanceof ConnectException
                || e instanceof java.net.NoRouteToHostException
                || e instanceof UnknownHostException
                || throwable instanceof ConnectException
                || throwable instanceof java.net.NoRouteToHostException
                || throwable instanceof UnknownHostException
        ) {
            return new HandleException(CODE_NET_EXCEPTION, NET_EXCEPTION, e);
        }

        // 返回数据进行Json解析出现异常，如数据不符合Json数据格式
        if (e instanceof JSONException
                || e instanceof ParseException
                || e instanceof com.google.gson.JsonSyntaxException
                || e instanceof com.google.gson.JsonParseException
                || e instanceof IllegalArgumentException
                || throwable instanceof JSONException
                || throwable instanceof ParseException
                || throwable instanceof com.google.gson.JsonSyntaxException
                || throwable instanceof com.google.gson.JsonParseException
                || throwable instanceof IllegalArgumentException) {
            return new HandleException(CODE_PARSE_EXCEPTION, PARSE_EXCEPTION, e);
        }

        // 读写数据的时出现
        if (e instanceof IOException || throwable instanceof IOException) {
            return new HandleException(CODE_IO_EXCEPTION, IO_EXCEPTION, e);
        }

        //没有信任证书，导致请求失败
        if (e instanceof SSLException || throwable instanceof SSLException) {
            return new HandleException(CODE_SSL_EXCEPTION, SSL_EXCEPTION, e);
        }

        // 未捕获异常情况
        return new HandleException(CODE_UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION, e);
    }


    /***
     * 错误码
     */
    private String code;

    /***
     * 错误信息
     */
    private String msg;

    public HandleException(int code, String msg, Throwable throwable) {

        super(msg, throwable);
        this.code = String.valueOf(code);
        this.msg = String.format(msg, this.code);
    }

    public HandleException(String code, String msg) {

        super(msg);
        this.code = code;
        this.msg = String.format(msg, this.code);
    }

    public HandleException(int code, String msg) {

        super(msg);
        this.code = String.valueOf(code);
        this.msg = String.format(msg, this.code);
    }

    @Override
    public String getMessage() {

        return this.msg;
    }

    public String getCode() {

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
}
