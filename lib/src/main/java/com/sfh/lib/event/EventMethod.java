package com.sfh.lib.event;

import android.text.TextUtils;

import java.lang.reflect.Method;

/**
 * 功能描述:方法响应关联数据
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/3/21
 */
public class EventMethod {

    public static final int TYPE_UI = 1;

    public static final int TYPE_VM = 2;

    /***响应方法*/
    Method method;

    /***来源*/
    int from;

    public EventMethod(int from, Method method) {

        this.from = from;
        this.method = method;
    }


    @Override
    public boolean equals(Object obj) {

        EventMethod eventMethod = (EventMethod) obj;
        if (eventMethod.from == this.from
                && TextUtils.equals (this.method.getName (), eventMethod.method.getName ())) {
            return true;
        }
        return super.equals (obj);
    }

    @Override
    public int hashCode() {

        return this.from + this.method.getName ().hashCode ();
    }
}
