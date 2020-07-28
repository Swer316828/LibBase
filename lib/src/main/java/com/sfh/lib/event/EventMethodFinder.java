package com.sfh.lib.event;
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
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

 public class EventMethodFinder implements Callable<List<EventMethod>> {

    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;
    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;

    final Class<?> targetClass;

    public EventMethodFinder(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public List<EventMethod> call() throws Exception {

        List<EventMethod> findState = new LinkedList<>();

        Method[] methods = targetClass.getDeclaredMethods();

        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    BusEvent busEvent = method.getAnnotation(BusEvent.class);
                    if (busEvent != null) {
                        Class<?> eventType = parameterTypes[0];
                        findState.add(new EventMethod(eventType, method));
                    }
                }
            } else if (method.isAnnotationPresent(BusEvent.class)) {
                String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                throw new RuntimeException(methodName +
                        " is a illegal @BusEvent method: must be public, non-static, and non-abstract");
            }
        }
        return findState;
    }
}
