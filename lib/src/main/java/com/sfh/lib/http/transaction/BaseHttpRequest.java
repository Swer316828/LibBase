package com.sfh.lib.http.transaction;

import android.text.TextUtils;
import android.util.Log;

import com.sfh.lib.http.HttpCodeException;
import com.sfh.lib.http.HttpMediaType;
import com.sfh.lib.http.IHttpConfig;
import com.sfh.lib.http.UtilRxHttp;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

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
abstract class BaseHttpRequest<T> implements Callable<T>, IHttpConfig {

    private static final String TAG = BaseHttpRequest.class.getName();

    public static final String GET = "GET";

    public static final String POST = "POST";


    /***
     * Okhttp 请求对象
     * @param httpConfig
     * @return
     */
    public abstract OkHttpClient getHttpService(IHttpConfig httpConfig);

    /***
     * 请求host
     * @return
     */
    public abstract String getUrl();

    /***
     * 数据源转为对象
     * @param reader  数据源转
     * @param cls 对象类型
     * @return
     */
    public abstract T parseResult(Reader reader, Type cls);

    /***
     * 对象转为Josn 字符串
     * @param object
     * @return
     */
    public abstract String toJson(Object object);

    /***
     * 处理请求参数
     * @return
     */
    public abstract Object buildParam();


    /*** 数据格式*/
    protected transient HttpMediaType mediaType = HttpMediaType.MEDIA_TYPE_JSON;

    /*** 请求路径*/
    protected transient String path;

    /*** 请求方式*/
    protected transient String method = POST;


    public BaseHttpRequest(String path) {

        this.path = path;
    }

    /***
     * 设置请求路径
     * @param path
     */
    public BaseHttpRequest<T> setPath(String path) {

        this.path = path;
        return this;
    }

    /***
     * 设置数据上传格式
     * @param mediaType {@link HttpMediaType#MEDIA_TYPE_JSON,HttpMediaType#MEDIA_TYPE_TEXT 等}
     */
    public BaseHttpRequest<T> setMediaType(HttpMediaType mediaType) {

        this.mediaType = mediaType;
        return this;
    }

    /***
     * 设置请求方式 {@link BaseHttpRequest#GET,BaseHttpRequest#POST }
     * @param method
     */
    public BaseHttpRequest<T> setMethod(String method) {

        this.method = method;
        return this;
    }

    /**
     * 发起请求
     */
    @Override
    public T call() throws Exception {

        if (TextUtils.isEmpty(this.getUrl())) {
            throw new NullPointerException("URL cannot be empty !");
        }

        final OkHttpClient okHttpClient = this.getHttpService(this);
        if (okHttpClient == null) {
            throw new NullPointerException("OkHttpClient Cannot be NULL !");
        }

        final Request.Builder builder = new Request.Builder();

        //设置请求头
        this.buildHeader(new IBuilderHeader() {
            @Override
            public void addHeader(String key, String value) {
                builder.addHeader(key, value);
            }
        });

        final Object params = this.buildParam();

        String url = TextUtils.isEmpty(this.path) ? this.getUrl() : this.getUrl() + this.path;

        if (TextUtils.equals(POST, this.method)) {

            builder.url(url);
            RequestBody body;
            if (HttpMediaType.MEDIA_TYPE_MULTIPART_FORM == this.mediaType) {
                //文件上传
                body = (MultipartBody) params;
            } else {
                //其他 请求参数
                body = RequestBody.create(MediaType.parse(this.mediaType.toString()), params.toString());
            }
            builder.post(body);
        } else {
            String paramsStr = params.toString();
            url = TextUtils.isEmpty(paramsStr) ? url : String.format("%s?%s", url, paramsStr);
            builder.url(url).get();
        }

        final Call call = okHttpClient.newCall(builder.build());

        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                T data = this.parseResult(response.body().charStream(), this.getClassType());
                this.cacheResponse(data);
                return data;
            } else {
                Log.d(TAG, String.format("Request name:%s,fail:%s", this.getClass().getName(), response.toString()));
                //Http请求错误-参考常见Http错误码如 401，403，404， 500 等
                throw new HttpCodeException(response.code(), response.toString());
            }
        } catch (IOException e) {
            Log.d(TAG, String.format("Request name:%s, url:%s,iOException:%s", this.getClass().getName(), url, e.getMessage()));
            boolean connect = UtilRxHttp.isNoteReachable(url, 3 * 1000);
            if (!connect) {
                throw new HttpCodeException(10012, "当前网络不可用，请检查!(10012)");
            }
            throw e;
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
     *
     * @param builder
     */
    public void buildHeader(IBuilderHeader builder) {
    }

    /***
     * 获取解析Class
     * @return
     */
    private Type getClassType() {

        Type type = getClass().getGenericSuperclass();
        return ((ParameterizedType) type).getActualTypeArguments()[0];
    }


}
