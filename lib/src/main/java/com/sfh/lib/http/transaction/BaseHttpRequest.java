package com.sfh.lib.http.transaction;

import android.text.TextUtils;

import com.sfh.lib.exception.HandleException;
import com.sfh.lib.http.HttpMediaType;
import com.sfh.lib.http.IRxHttpClient;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.Policy;
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
public abstract class BaseHttpRequest<T> extends ParseResult {


    public static final String GET = "GET";

    public static final String POST = "POST";

    protected transient String mediaType = HttpMediaType.MEDIA_TYPE_JSON;

    protected transient String path;

    protected transient String method = POST;


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

    public abstract Object buildParam();

    public abstract IRxHttpClient getHttpService();

    /**
     * 发起请求
     */
    public T sendRequest() throws Exception {

        final IRxHttpClient httpClient = this.getHttpService ();
        if (httpClient == null) {
            throw new HandleException (HandleException.CODE_NULL_EXCEPTION, HandleException.NULL_EXCEPTION, new Throwable ("IRxHttpClient Cannot be NULL !"));
        }

        String url = httpClient.getHots () + this.path;
        Request.Builder builder = new Request.Builder ();
        //请求头
        this.buildHeader (httpClient, builder);

        Object params = this.buildParam ();

        if (TextUtils.equals (POST, this.method)) {

            builder.url (url);
            RequestBody body;
            if (TextUtils.equals (HttpMediaType.MEDIA_TYPE_MULTIPART_FORM, this.mediaType)) {
                //文件上传
                body = (MultipartBody) params;
            } else {
                //其他 请求参数
                body = RequestBody.create (MediaType.parse (this.mediaType), params.toString ());
            }
            builder.post (body);
        } else {
            url = url + "?" + params.toString ();
            builder.url (url).get ();
        }

        Call call = httpClient.getHttpClientService ().newCall (builder.build ());
        try {

            Response response = call.execute ();
            if (response.isSuccessful ()) {

                return this.parseResult (response.body ().charStream (), this.getClassType ());

            } else {
                throw new HandleException (HandleException.CODE_HTTP_EXCEPTION, HandleException.HTTP_EXCEPTION, new Throwable ("code:" + response.code () + ",msg:" + response.message ()));
            }
        } catch (Exception e) {
            throw HandleException.handleException (e);
        }

    }

    /**
     * 处理消息头
     *
     * @param builder
     */
    private void buildHeader(IRxHttpClient client, Request.Builder builder) {

        Map<String, String> header = client.getHeader ();
        if (header != null && header.size () > 0) {
            for (Map.Entry<String, String> entry : header.entrySet ()) {
                builder.addHeader (entry.getKey (), entry.getValue ());
            }
        }
    }

    /***
     * 获取解析Class
     * @return
     */
    private Type getClassType() {

        Type type = getClass ().getGenericSuperclass ();
        return ((ParameterizedType) type).getActualTypeArguments ()[0];

    }
}
