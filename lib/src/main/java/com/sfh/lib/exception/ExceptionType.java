package com.sfh.lib.exception;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/8/9
 */
public enum ExceptionType {
    //未识别的异常
    UNKNOWN(10000, "服务请求异常，请重试!(10000)"),
    //数据解析异常
    PARSE(10001, "服务数据解析异常，请重试!(10001)"),
    //表示无法连接，也就是说当前主机不存在 or 无法指定被请求的地址 or 无法解析该域名
    NET(10002, "服务请求异常，请重试！(10002)"),
    //Http请求错误Code
    HTTP(10003, "服务请求超时，请重试!(10003)"),
    // SSL 安全异常
    SSL(10004, "请检查手机设置后重试，谢谢!(10004)"),
    //服务器请求超时 or 服务器响应超时
    TIMEOUT(10005, "服务请求超时，请稍后再试!(10005)"),
    //读写数据的时出现错误
    IO(10006, "请核实输入的数据再提交，谢谢!(10006)"),
    // NullPointerException 指针 异常
    NULL(10007, "系统繁忙,请稍后再试!(10007)");

    public int code;
    public String format;

    ExceptionType(int code, String format) {
        this.code = code;
        this.format = format;
    }

    @Override
    public String toString() {
        return "ExceptionType{" +
                "code=" + code +
                ", format='" + format + '\'' +
                '}';
    }}
