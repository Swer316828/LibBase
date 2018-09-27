package com.sfh.lib.http;

import android.arch.lifecycle.MutableLiveData;

import java.util.Map;

/**
 * 功能描述:基础请求参数
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/5/28
 */
public interface HttpBase {

    /***
     * 基础参数转换
     * @return
     */
     Map<String,String> toMap();

    /***
     * 参数检查
     * @param liveData
     * @return
     */
    boolean checkParams(MutableLiveData liveData);
}
