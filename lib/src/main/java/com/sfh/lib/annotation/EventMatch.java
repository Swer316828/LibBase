package com.sfh.lib.annotation;

import com.sfh.lib.utils.ThreadModel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述:消息监听注入
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventMatch {
    /***
     * 数据来源说明
     * @return
     */
    String from() ;

    /***
     * 运行线程
     * @return
     */
    @ThreadModel.Thread
    int threadMode() default ThreadModel.MAIN;
}
