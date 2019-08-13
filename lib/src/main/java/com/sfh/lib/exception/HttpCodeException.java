package com.sfh.lib.exception;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/8/13
 */
public class HttpCodeException extends RuntimeException {

    private int code;

    public HttpCodeException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "httpCode:" + this.code + " " + super.toString();
    }
}
