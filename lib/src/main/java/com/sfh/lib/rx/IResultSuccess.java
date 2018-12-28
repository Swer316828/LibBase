package com.sfh.lib.rx;

/**
 * 功能描述:仅处理结果,异常对话框自动提示
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/23
 */
public interface IResultSuccess<T> {
    void onSuccess(T t)  throws Exception ;
}
