package com.sfh.lib.rx;

/**
 * 功能描述:仅处理结果,异常不提示
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/23
 */
public interface IResultSuccessNoFail<T> {
    void onSuccess(T t)  throws Exception ;
}
