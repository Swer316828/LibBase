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
public @interface RxBusEvent {

    /****
     * 响应的类class
     */
    Class<? extends EventData> ofType();

    /***
     * 消息响应的运行线程
     * @return true 主线程 false：io线程，默认true
     */
    boolean mainThread() default true;

    /***
     * 数据来源说明
     * @return
     */
    String from();


}
