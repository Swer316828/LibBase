package com.sfh.lib.mvp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述:View层消息事件监听
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RxBusEvent {

    /**监听数据类对象*/
    Class<?> eventClass();
}
