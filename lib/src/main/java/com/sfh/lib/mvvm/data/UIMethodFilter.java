package com.sfh.lib.mvvm.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/4/16
 */
public class UIMethodFilter {

    public static final int TYPE_LIVEMETHOD = 0x1;
    public static final int TYPE_EVENT = 0x2;

    public int type;
    /***方法*/
    public Method method;

    /**
     * 方法参数
     */
    public Class<?>[] parameterTypes;


    /***
     *
     * @param type {@link UIMethodFilter#TYPE_EVENT} 或 {@link UIMethodFilter#TYPE_LIVEMETHOD}
     * @param method
     */
    public UIMethodFilter(int type, Method method) {

        this.type = type;
        this.method = method;
        this.parameterTypes = method.getParameterTypes();
    }

    @Override
    public int hashCode() {

        if (type == TYPE_LIVEMETHOD) {
            return this.method.getName().hashCode();
        } else if (type == TYPE_EVENT) {
            this.parameterTypes[0].getSimpleName();
            return eventClass.getSimpleName().hashCode();
        }
        return super.hashCode();
    }

    public void showUIData(@NonNull Object view, @Nullable UIData data) throws Exception {
        //方法需要参数
        final int parameterLength = parameterTypes.length;

        //【响应方法】无参
        if (parameterLength == 0) {
            method.invoke(view);
            return;
        }

        final int dataLength = data.getDataLength();

        //【响应方法】有参
        if (parameterLength == dataLength) {
            // 正常匹配
            method.invoke(view, data.getData());
            return;
        }

        //补齐参数
        List<Object> list = new ArrayList<>(parameterLength);

        final Object[] temp = data.getData();
        for (int i = 0; i < parameterLength; i++) {
            if (i < dataLength) {
                list.add(temp[i]);
            } else {
                list.add(this.getNullObject(parameterTypes[i]));
            }
        }
        method.invoke(view, list.toArray());
    }

    private Object getNullObject(Class<?> parameter) {

        if (long.class.isAssignableFrom(parameter) || Long.class.isAssignableFrom(parameter)
                || int.class.isAssignableFrom(parameter) || Integer.class.isAssignableFrom(parameter)) {
            return 0;
        } else if (boolean.class.isAssignableFrom(parameter) || Boolean.class.isAssignableFrom(parameter)) {
            return false;
        } else if (float.class.isAssignableFrom(parameter) || Float.class.isAssignableFrom(parameter)
                || double.class.isAssignableFrom(parameter) || Double.class.isAssignableFrom(parameter)) {
            return 0.0f;
        } else {
            return null;
        }
    }
}
