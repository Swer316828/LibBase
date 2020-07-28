package com.sfh.lib.mvvm;
/*=============================================================================================
 * 功能描述:
 *---------------------------------------------------------------------------------------------
 *  涉及业务 & 更新说明:
 *
 *
 *--------------------------------------------------------------------------------------------
 *  @Author     SunFeihu 孙飞虎  on  2020/7/28
 *=============================================================================================*/

import com.sfh.lib.event.BusEvent;
import com.sfh.lib.event.EventMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class LiveMethonFinder implements Callable<List<Method>> {
    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;
    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;

    final Class<?> targetCls;

    public LiveMethonFinder(Class<?> targetCls) {
        this.targetCls = targetCls;
    }

    @Override
    public List<Method> call() throws Exception {

        List<Method> findState = new LinkedList<>();

        Method[] methods = targetCls.getDeclaredMethods();

        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                LiveDataMatch liveDataMatch = method.getAnnotation(LiveDataMatch.class);
                if (liveDataMatch != null) {
                    findState.add(method);
                }
            } else if (method.isAnnotationPresent(LiveDataMatch.class)) {
                String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                throw new RuntimeException(methodName +
                        " is a illegal @LiveDataMatch method: must be public, non-static, and non-abstract");
            }
        }
        return findState;
    }
}
