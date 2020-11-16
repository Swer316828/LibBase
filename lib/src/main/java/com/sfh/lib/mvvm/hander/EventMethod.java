package com.sfh.lib.mvvm.hander;
/*=============================================================================================
 * 功能描述:
 *---------------------------------------------------------------------------------------------
 *  涉及业务 & 更新说明:
 *
 *
 *--------------------------------------------------------------------------------------------
 *  @Author     SunFeihu 孙飞虎  on  2020/7/28
 *=============================================================================================*/

import java.lang.reflect.Method;

public class EventMethod {
    Class<?> dataClass;
    Method method;

    public EventMethod(Class<?> dataClass, Method method) {
        this.dataClass = dataClass;
        this.method = method;
    }

    public Class<?> getDataClass() {
        return dataClass;
    }

    public Method getMethod() {
        return method;
    }
}
