package com.sfh.lib.mvp;

import com.sfh.lib.http.service.HandleException;

/**
 * 功能描述:处理结果
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/23
 */
public interface IResult<T> {
    void onSuccess(T t)  throws Exception ;
    void onFail(HandleException e);
}
