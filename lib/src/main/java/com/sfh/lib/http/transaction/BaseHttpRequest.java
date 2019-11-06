package com.sfh.lib.http.transaction;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sfh.lib.exception.HttpCodeException;
import com.sfh.lib.http.HttpMediaType;
import com.sfh.lib.http.IRxHttpClient;

import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 功能描述:HTTP任务请求
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/12/28
 */
abstract class BaseHttpRequest<T> {


    public static final String GET = "GET";

    public static final String POST = "POST";

    protected transient String mediaType = HttpMediaType.MEDIA_TYPE_JSON;

    protected transient String path;

    protected transient String method = POST;

    protected static transient volatile Gson mGson;

    /***处理请求参数*/
    public abstract Object buildParam();

    public abstract IRxHttpClient getHttpService();

    public abstract String getUrl();

    public BaseHttpRequest(String path) {

        this.path = path;
    }

    /***
     * 设置请求路径
     * @param path
     */
    public void setPath(String path) {

        this.path = path;
    }

    /**
     * 设置请求方式，请求路径
     *
     * @param method
     * @param path
     */
    public void setPath(String method, String path) {

        this.method = method;
        this.path = path;
    }

    /***
     * 设置数据上传格式
     * @param mediaType
     */
    public void setMediaType(String mediaType) {

        this.mediaType = mediaType;
    }

    /***
     * 设置请求方式 {@link BaseHttpRequest GET,POST }
     * @param method
     */
    public void setMethod(String method) {

        this.method = method;
    }

    /**
     * 发起请求
     */
    public T sendRequest() throws Exception {

        IRxHttpClient httpClient = this.getHttpService();
        if (httpClient == null) {
            throw new NullPointerException("IRxHttpClient Cannot be NULL !");
        }
        OkHttpClient okHttpClient = httpClient.getHttpClientService();
        if (okHttpClient == null) {
            throw new NullPointerException("okHttpClient Cannot be NULL !");
        }

        final Request.Builder builder = new Request.Builder();
        //请求头
        Map<String, String> header = this.buildHeader(httpClient.getHeader());
        if (header != null && header.size() > 0) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        //请求参数
        Object params = this.buildParam();

        String url = this.getUrl() + this.path;

        if (TextUtils.equals(POST, this.method.toUpperCase())) {
            builder.url(url);
            RequestBody body;
            if (TextUtils.equals(HttpMediaType.MEDIA_TYPE_MULTIPART_FORM, this.mediaType)) {
                //文件上传
                body = (MultipartBody) params;
            } else {
                //其他 请求参数
                body = RequestBody.create(MediaType.parse(this.mediaType), params.toString());
            }
            builder.post(body);
        } else {
            url = url + "?" + params.toString();
            builder.url(url).get();
        }

        Response response = okHttpClient.newCall(builder.build()).execute();

        if (response.isSuccessful()) {
            T data = this.parseResult(response.body().charStream(), this.getClassType());
            this.cacheResponse(data);
            return data;
        } else {
            //Http请求错误-参考常见Http错误码如 401，403，404， 500 等
            throw new HttpCodeException(response.code(), response.toString());
        }
    }

    /***
     * 处理返回数据
     * @param data
     */
    public void cacheResponse(T data) {
    }

    /**
     * 处理消息头
     */
    public Map<String, String> buildHeader(Map<String, String> header) {

        return header;
    }

    /***
     * 获取解析Class
     * @return
     */
    public Type getClassType() {

        Type type = getClass().getGenericSuperclass();
        return ((ParameterizedType) type).getActualTypeArguments()[0];

    }


    public <T> T parseResult(Reader reader, Type cls) {

        Gson gson = this.getGson();
        return gson.fromJson(reader, cls);

    }

    public String toJson(Object object) {

        Gson gson = this.getGson();
        return gson.toJson(object);
    }

    public Gson getGson() {
        if (mGson == null) {
            mGson = new GsonBuilder()
                    .setLenient()// json宽松
//                .enableComplexMapKeySerialization()//支持Map的key为复杂对象的形式
                    .serializeNulls() //智能null
                    .setPrettyPrinting()// 调整格式 ，使对齐
                    .create();
        }
        return mGson;
    }
}
