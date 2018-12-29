package com.sfh.lib.rx;

import com.sfh.lib.exception.HandleException;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.utils.UtilLog;

/**
 * 功能描述:空任务
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/6/11
 */
public class EmptyResult<T> implements IResult<T> {

    @Override
    public void onSuccess(T t) throws Exception {

        UtilLog.d (EmptyResult.class.getName (), "onSuccess:" + t);
    }

    @Override
    public void onFail(HandleException e) {

        UtilLog.d (EmptyResult.class.getName (), "onFail:" + e);
    }
}
