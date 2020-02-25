package com.sfh.lib.http.transaction;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sfh.lib.http.HttpMediaType;
import com.sfh.lib.http.IHttpConfig;
import com.sfh.lib.http.UtilRxHttp;
import com.sfh.lib.http.service.HttpClientService;
import com.sfh.lib.http.service.gson.NullStringToEmptyAdapterFactory;

import java.io.File;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * 功能描述:请求对象,默认POST方式，参数格式类型JSON,Gson解析返回数据
 *
 * <p>1.设置请求方式{@link OutreachRequest#setMethod(String)}</p>
 * <p>2.设置提交参数格式类型{@link OutreachRequest#setMediaType(HttpMediaType)}</p>
 * <p>3.对请求头进行处理操作，重载{@link OutreachRequest#buildHeader(IBuilderHeader)}</p>
 * <p>4.对请求参数进行处理操作，重载{@link OutreachRequest#buildParam()}</p>
 * <p>处理返回结果 可以重写方法 {@link OutreachRequest#cacheResponse(Object)}</p>
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/12/17
 */
public abstract class OutreachRequest<T> extends BaseHttpRequest<T> {


    public OutreachRequest(String path) {

        super(path);
    }

    @Override
    public OkHttpClient getHttpService(IHttpConfig config) {
        return HttpClientService.newInstance().getHttpService(config);
    }

    @Override
    public T parseResult(Reader reader, Type cls) {

        Gson gson = this.getGson();
        return gson.fromJson(reader, cls);
    }

    @Override
    public String toJson(Object object) {

        Gson gson = this.getGson();
        return gson.toJson(object);
    }

    private static transient Gson GSON;

    private Gson getGson() {
        if (GSON == null) {
            GSON = new GsonBuilder()
                    .setLenient()// json宽松
                    .registerTypeAdapterFactory(new NullStringToEmptyAdapterFactory()).create();
        }
        return GSON;
    }

    @Override
    public Object buildParam() {

        if (HttpMediaType.MEDIA_TYPE_MULTIPART_FORM == this.mediaType) {
            // 文件类型
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            this.buildParamMultipart(this, builder);
            return builder.build();
        }

        if (HttpMediaType.MEDIA_TYPE_JSON == this.mediaType) {
            return this.toJson(this);
        }

        return this.buildParamKeyValue(this);
    }

    /**
     * 文件上传
     *
     * @param object
     * @param builder
     */
    private void buildParamMultipart(Object object, MultipartBody.Builder builder) {

        Field[] fields = object.getClass().getDeclaredFields();
        if (fields == null || fields.length <= 0) {
            return;
        }

        //准备实体
        for (Field field : fields) {
            field.setAccessible(true);
            if (this.isLoseParameter(field)) {
                continue;
            }
            Object value = null;
            try {
                value = field.get(object);
            } catch (IllegalAccessException e) {
            }
            if (value == null || TextUtils.isEmpty(value.toString())) {
                continue;
            }
            String key = field.getName();
            if (UtilRxHttp.isBaseType(value)) {
                // 基础类型
                builder.addPart(MultipartBody.Part.createFormData(key, String.valueOf(value)));

            } else if (value instanceof File) {
                File f = (File) value;
                builder.addPart(MultipartBody.Part.createFormData(field.getName(), f.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), f)));
            } else {
                builder.addPart(MultipartBody.Part.createFormData(key, this.toJson(value)));
            }
        }
    }

    /***
     * 处理 key=value&key=value
     * @param object
     * @return
     */
    private String buildParamKeyValue(Object object) {

        Field[] fields = object.getClass().getDeclaredFields();
        if (fields == null || fields.length <= 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer(100);

        for (Field field : fields) {
            field.setAccessible(true);

            if (this.isLoseParameter(field)) {
                continue;
            }
            Object value = null;
            try {
                value = field.get(object);
            } catch (IllegalAccessException e) {
            }
            if (value == null || TextUtils.isEmpty(value.toString())) {
                continue;
            }
            if (UtilRxHttp.isBaseType(value)) {
                // 基础类型
                buffer.append(field.getName()).append("=").append(value.toString()).append("&");
            } else if (value instanceof File) {

            } else {
                buffer.append(field.getName()).append("=").append(this.toJson(value)).append("&");
            }
        }
        return buffer.toString();
    }


    /***
     * 忽略参数
     * @param field
     * @return
     */
    private boolean isLoseParameter(Field field) {
        //静态属性被忽略
        if (Modifier.isStatic(field.getModifiers())
                || Modifier.isFinal(field.getModifiers())
                || Modifier.isTransient(field.getModifiers())) {
            return true;
        }
        return false;
    }

    /**
     * 网络数据读取超时
     *
     * @return
     */
    @Override
    public long getReadTimeout() {
        return 10 * 1000L;
    }

    /***
     * 网络连接超时
     * @return
     */
    @Override
    public long getConnectTimeout() {
        return 10 * 1000L;
    }

    /***
     * 网络输出超时
     * @return
     */
    @Override
    public long getWriteTimeout() {
        return 10 * 1000L;
    }

    /***
     * 请求和响应拦截器
     * @return
     */
    @Override
    public Interceptor getInterceptor() {
        return null;
    }

    /***
     * 网络请求和响应拦截器
     * @return
     */
    @Override
    public Interceptor getNetworkInterceptor() {
        return null;
    }

    /****
     * DNS
     * @return
     */
    @Override
    public Dns getHttpDns() {
        return null;
    }
}
