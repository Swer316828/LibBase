package com.sfh.lib.mvvm.data;

/**
 * 功能描述: TODO
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/8/20
 */
public class UIData<T> {
    String action;
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

    public void setAction(String action) {
        this.action = action;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
