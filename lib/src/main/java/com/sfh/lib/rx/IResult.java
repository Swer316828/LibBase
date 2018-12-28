package com.sfh.lib.rx;

import com.sfh.lib.exception.HandleException;

/**
 * 功能描述:处理结果
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/23
 */
public interface IResult<T> extends IResultSuccess<T> {
    void onFail(HandleException e);
}
