package com.sfh.lib.mvvm.data;

import java.lang.reflect.Method;

/**
 * 功能描述: UI 响应数据
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/8/20
 */
public class UIData<T> {
    /**
     * 响应此数据的方法名称
     */
    private Method action;
    /**
     * 数据
     */
    private T data;

    public UIData(Method action, T data) {
        this.action = action;
        this.data = data;
    }

    public UIData(Method action) {
        this.action = action;
    }

    public Method getAction() {
        return action;
    }

    public T getData() {
        return data;
    }

}
