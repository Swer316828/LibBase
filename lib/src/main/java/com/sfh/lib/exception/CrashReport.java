package com.sfh.lib.exception;

import com.sfh.lib.exception.HandleException;

/**
 * 功能描述: 异常回调
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/8/8
 */
public interface CrashReport {
    HandleException accept(Throwable t);
}
