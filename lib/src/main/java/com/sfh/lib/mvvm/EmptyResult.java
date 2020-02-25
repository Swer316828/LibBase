package com.sfh.lib.mvvm;


import com.sfh.lib.IResult;
import com.sfh.lib.HandleException;
import com.sfh.lib.utils.ZLog;

/**
 * 功能描述:空任务
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/6/11
 */
public class EmptyResult<T> implements IResult<T> {


    @Override
    public void onSuccess(T t) throws Exception {
        ZLog.w(EmptyResult.class.getName(), " EmptyResult.class onSuccess:" + t);
    }

    @Override
    public void onFail(HandleException e) {
        ZLog.w(EmptyResult.class.getName(), "EmptyResult.class onFail:" + e);
    }
}
