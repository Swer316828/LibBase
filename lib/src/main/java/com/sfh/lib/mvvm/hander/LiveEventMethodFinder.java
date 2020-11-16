package com.sfh.lib.mvvm.hander;
/*=============================================================================================
 * 功能描述:UI 响应方法查询
 *---------------------------------------------------------------------------------------------
 *  涉及业务 & 更新说明:
 *
 *
 *--------------------------------------------------------------------------------------------
 *  @Author     SunFeihu 孙飞虎  on  2020/7/28
 *=============================================================================================*/


import com.sfh.lib.event.Event;
import com.sfh.lib.event.EventManager;
import com.sfh.lib.event.IEventListener;
import com.sfh.lib.mvvm.LiveDataMatch;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class LiveEventMethodFinder implements Callable<MethodLinkedMap> {
    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;
    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;

    final Class<?> targetCls;

    public LiveEventMethodFinder(Class<?> targetCls) {
        this.targetCls = targetCls;
    }

    @Override
    public MethodLinkedMap call() throws Exception {

        MethodLinkedMap findState = null;
        Method[] methods = targetCls.getDeclaredMethods();

        for (Method method : methods) {

            int modifiers = method.getModifiers();


            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {

                LiveDataMatch live = method.getAnnotation(LiveDataMatch.class);
                if (live != null) {
                    findState.put(method.getName(), method);
                }

                Event event = method.getAnnotation(Event.class);
                if (event != null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1){
                        Class<?> eventType = parameterTypes[0];

                        findState.put(eventType.getName(), method);
                        findState.putEventClass(eventType);

                    }

                }


            } else if (method.isAnnotationPresent(LiveDataMatch.class) || method.isAnnotationPresent(Event.class)) {

                String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                throw new RuntimeException(methodName +
                        " is a illegal @LiveDataMatch or @Event method: must be public, non-static, and non-abstract");
            }
        }
        return findState;
    }
}
