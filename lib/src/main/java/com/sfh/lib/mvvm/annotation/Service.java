package com.sfh.lib.mvvm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述:标注接口实现类
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/10
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    /**
     * 接口实现类
     * @return
     */
    Class<?> achieve();
}
