package com.sfh.lib.http;

import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sfh.lib.http.HttpBase;
import com.sfh.lib.http.annotation.Lose;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 功能描述:RxHttp 辅助工具
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public final class UtilRxHttp {

    private UtilRxHttp() {
    }


    /***
     * 请求对象进行Map参数化处理
     * 要求在异步线程中调用
     * @return
     */
    @WorkerThread
    public static Map<String, String> buildParams(Object object) throws IllegalAccessException {

        Field[] fields = object.getClass().getDeclaredFields();
        Map<String, String> params;
        if (object instanceof HttpBase) {
            // 公共参数
            params = ((HttpBase) object).toMap();
        } else {
            params = new HashMap<>(fields.length);
        }

        for (Field field : fields) {
            field.setAccessible(true);
            if (isLose(object, field)) {
                continue;
            }
            Object value = field.get(object);
            if (isBaseType(value)) {
                // 基础类型
                params.put(field.getName(), value.toString());
            } else {
                params.put(field.getName(), new Gson().toJson(value));
            }
        }
        return params;
    }

    /***
     * 忽略参数
     * @param field
     * @return
     */
    private static boolean isLose(Object object, Field field) throws IllegalAccessException {
        //静态属性被忽略
        if (Modifier.isStatic(field.getModifiers())
                || Modifier.isFinal(field.getModifiers())
                || Modifier.isTransient(field.getModifiers())) {
            return true;
        }
        Lose lose = field.getAnnotation(Lose.class);
        if (lose != null) {
            // 忽略参数
            return true;
        }

        Object value = field.get(object);
        if (value == null || TextUtils.isEmpty(value.toString())) {
            return true;
        }
        return false;
    }

    /**
     * 基础类型判断
     *
     * @param object
     * @return
     */
    private static boolean isBaseType(Object object) {
        if (object instanceof Integer) {
            return true;
        }
        if (object instanceof Long) {
            return true;
        }
        if (object instanceof Float) {
            return true;
        }
        if (object instanceof Boolean) {
            return true;
        }
        if (object instanceof String) {
            return true;
        }
        return false;
    }



    /**
     * 文件加其他类型数据参数处理
     *
     * @param params
     * @return
     */
    public static MultipartBody.Builder buildMultiParts(Map<String, Object> params) {
        //准备实体
        MultipartBody.Builder builder = new MultipartBody.Builder();
        Iterator iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry) iterator.next();
            Object value = entry.getValue();

            if (value == null) {
                continue;
            }

            String key = entry.getKey();
            if (value instanceof File) {
                File f = (File) value;
                RequestBody fileRequestBody = RequestBody.create(MediaType.parse("application/octet-stream"), f);
                builder.addPart(MultipartBody.Part.createFormData(key, f.getName(), fileRequestBody));
            } else {
                builder.addPart(MultipartBody.Part.createFormData(key, String.valueOf(value)));
            }
        }
        builder.setType(MultipartBody.FORM);
        return builder;
    }


    /***
     * 文件直接上传参数处理
     * @param key
     * @param f
     * @return
     */
    public static MultipartBody.Builder buildMultiParts2(String key, File f) {
        //准备实体
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        RequestBody fileRequestBody = RequestBody.create(MediaType.parse("application/octet-stream"), f);
        builder.addPart(MultipartBody.Part.createFormData(key, f.getName(), fileRequestBody));
        return builder;
    }
}