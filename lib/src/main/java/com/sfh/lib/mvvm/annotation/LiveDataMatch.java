package com.sfh.lib.mvvm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述:LiveData事件监听
 * 匹配规则：优先匹配方法名，在匹配tag
 * @author SunFeihu 孙飞虎
 * @date 2018/4/8
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LiveDataMatch {

    /***
     * 标识TAG
     * @return
     */
    String tag() default "";


}
