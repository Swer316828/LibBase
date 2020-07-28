package com.sfh.lib.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述:RxBus消息监听注入
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BusEvent {
    /***
     * 数据来源说明
     * @return
     */
    String from();

    /***
     * 运行线程
     * @return
     */
    int ThreadMode() default ThreadModel.MAIN;
}
