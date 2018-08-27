package com.sfh.lib.mvvm.data;

/**
 * 功能描述: UI 响应数据
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/8/20
 */
public class UIData<T> {
    /**响应此数据的方法名称*/
    String action;
    /**数据*/
    T data;

    public UIData(String action, T data){
        this.action = action;
        this.data = data;
    }
    public UIData(String action){
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public T getData() {
        return data;
    }

}
