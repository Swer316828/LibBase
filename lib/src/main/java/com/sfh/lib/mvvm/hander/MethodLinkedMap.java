package com.sfh.lib.mvvm.hander;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class MethodLinkedMap {
    private final LinkedHashMap<String, Method> uiMethods;
    private final List<Class<?>> eventClass;

    public MethodLinkedMap() {
        this(10);
    }

    public MethodLinkedMap(int initialCapacity) {
        uiMethods = new LinkedHashMap<>(initialCapacity);
        eventClass = new LinkedList<>();
    }

    public Method get(String key) {
        return uiMethods.get(key);
    }

    public Method put(String key, Method method) {
        return uiMethods.put(key, method);
    }

    public boolean putEventClass(Class<?> eventType) {
        if (eventClass.contains(eventType)){
            return false;
        }
        return eventClass.add(eventType);
    }

    public Method clear(String key) {
        return uiMethods.remove(key);
    }

    public boolean containsKey(String key) {
        return uiMethods.containsKey(key);
    }

    public boolean containsValue(String key) {
        return uiMethods.containsValue(key);
    }

    public void clear() {
        uiMethods.clear();
        eventClass.clear();
    }

    public void clearMeyhod() {
        uiMethods.clear();
    }

    public void clearEventClass() {
        eventClass.clear();
    }

    public LinkedHashMap<String, Method> getUiMethods() {
        return uiMethods;
    }

    public List<Class<?>> getEventClass() {
        return eventClass;
    }
}

