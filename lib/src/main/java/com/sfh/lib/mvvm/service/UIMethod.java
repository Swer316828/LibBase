package com.sfh.lib.mvvm.service;

import java.lang.reflect.Method;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/4/16
 */
public class UIMethod {

    /***
     * LiveDataMatch
     * @param method
     */
    public UIMethod(Method method) {

        this.type = 0;
        this.method = method;
    }

    /**
     * RxBusEvent
     *
     * @param method
     * @param eventClass
     */
    public UIMethod(Method method, Class eventClass) {

        this.type = 1;
        this.method = method;
        this.eventClass = eventClass;
    }

    public int type;

    public Method method;

    public Class eventClass;

    @Override
    public int hashCode() {

        if (type == 0) {
            return this.method.getName ().hashCode ();
        } else if (type == 1){
            return eventClass.getSimpleName ().hashCode ();
        }
        return super.hashCode ();
    }
}
