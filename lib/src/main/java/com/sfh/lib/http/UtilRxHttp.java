package com.sfh.lib.http;

import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * 判断服务器是否通畅
     * @param time
     * @return
     */
    public static boolean isNoteReachable(String url, int time) {
        try {

            if (url == null || url.trim().equals("")) {
                return false;
            }
            String host = "";
            Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
            Matcher matcher = p.matcher(url);
            if (matcher.find()) {
                host = matcher.group();
            }

            InetAddress address = InetAddress.getByName(host);
            if (address.isReachable(time)) {
                return true;
            } else {
                return 0 == Runtime.getRuntime().exec("ping -c 1 " + host).waitFor();
            }
        } catch (Exception e) {
            Log.e(UtilRxHttp.class.getName(), "httpHostContent Exception:" + e);
        }
        return false;
    }

    /***
     * 请求对象进行Map参数化处理
     * 要求在异步线程中调用
     * @return
     */
    @WorkerThread
    public static Map<String, String> buildParams(Object object) throws IllegalAccessException {

        Field[] fields = object.getClass().getDeclaredFields();
        Map<String, String> params = new HashMap<>(fields.length);
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
    public static boolean isLose(Object object, Field field) throws IllegalAccessException {
        //静态属性被忽略
        if (Modifier.isStatic(field.getModifiers())
                || Modifier.isFinal(field.getModifiers())
                || Modifier.isTransient(field.getModifiers())) {
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
    public static boolean isBaseType(Object object) {
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
