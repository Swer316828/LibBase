package com.sfh.lib.mvvm.data;

/**
 * 功能描述: UI 响应数据
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/8/20
 */
public class UIData {
    /**
     * 响应此数据的方法名称
     */
    private String action;

    /***
     * 数据
     */
    private Object[] data;


    /***
     * 参数
     * @param action
     * @param args
     */
    public UIData(String action, Object... args) {
        this.action = action;
        this.data = args;
    }

    /***
     * 无参
     * @param action
     */
    public UIData(String action) {
        this.action = action;
    }

    /***
     * UI响应方法
     * @return
     */
    public String getAction() {
        return this.action;
    }

    /***
     * 数据
     * @return
     */
    public Object[] getData() {
        return this.data;
    }

    /***
     * 数据个数
     * @return
     */
    public int getDataLength(){
        return this.data == null? 0: this.data.length;
    }
}
