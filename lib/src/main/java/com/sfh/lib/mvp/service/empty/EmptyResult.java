package com.sfh.lib.mvp.service.empty;

import com.sfh.lib.http.service.HandleException;
import com.sfh.lib.mvp.IResult;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/6/11
 */
public class EmptyResult<T> implements IResult<T> {
    @Override
    public void onSuccess(T t) throws Exception {

    }

    @Override
    public void onFail(HandleException e) {

    }
}
