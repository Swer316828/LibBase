package com.sfh.lib;

import com.sfh.lib.HandleException;

/**
 * 功能描述: 异常回调
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/8/8
 */
public interface ICrashReport {
    HandleException accept(Throwable t);
}
