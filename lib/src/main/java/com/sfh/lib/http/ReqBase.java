package com.sfh.lib.http;

import java.util.Map;

/**
 * 功能描述:基础请求参数
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/5/28
 */
public interface ReqBase {

    /***
     * 基础参数转换
     * @return
     */
     Map<String,String> toMap();

     boolean checkParams();
}
