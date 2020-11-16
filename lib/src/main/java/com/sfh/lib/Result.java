package com.sfh.lib;


import com.sfh.lib.exception.HandleException;

/**
 * 功能描述:处理结果
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/23
 */
public interface Result<T> extends ResultSuccess<T> {
    void onFail(HandleException e);
}
