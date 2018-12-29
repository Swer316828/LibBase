package com.sfh.lib.http.transaction;

import com.sfh.lib.exception.HandleException;
import com.sfh.lib.http.IRxHttpClient;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 功能描述:HTTP任务请求
 * 默认POST 方式，参数格式类型JSON
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/12/28
 */
public abstract class BaseRequest<T> extends ParseResult {

    /**
     * "application/x-www-form-urlencoded"，是默认的MIME内容编码类型，一般可以用于所有的情况，但是在传输比较大的二进制或者文本数据时效率低。
     * 这时候应该使用"multipart/form-data"。如上传文件或者二进制数据和非ASCII数据。
     */
    public static final String MEDIA_TYPE_NORAML_FORM = "application/x-www-form-urlencoded;charset=utf-8";

    /**
     * 既可以提交普通键值对，也可以提交(多个)文件键值对
     */
    public static final String MEDIA_TYPE_MULTIPART_FORM = "multipart/form-data;charset=utf-8";


    /**
     * 文本类型
     */
    public static final String MEDIA_TYPE_TEXT = "text/plain;charset=utf-8";

    /**
     * Json类型
     */
    public static final String MEDIA_TYPE_JSON = "application/json;charset=utf-8";

    public static final String GET = "GET";

    public static final String POST = "POST";

    protected transient String mediaType = MEDIA_TYPE_JSON;

    protected transient String path;

    protected transient String method = POST;


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

    public void setMethod(String method) {

        this.method = method;
    }

    public abstract String buildParam();

    public abstract IRxHttpClient getHttpService();

    /**
     * 发起请求
     */
    public T sendRequest() throws HandleException {


        String url = this.getHttpService().getHots () + this.path;
        Request.Builder builder = new Request.Builder ().url (url);

        String cotent = this.buildParam ();
        builder.method (this.method, RequestBody.create (MediaType.parse (mediaType), cotent));

        OkHttpClient httpClient = this.getHttpService().getHttpClientService ();

        try {

            Response response = httpClient.newCall (builder.build ()).execute ();
            if (response.isSuccessful ()) {

                return this.parseResult (response.body ().charStream (), this.getClassType ());

            } else {
                throw new HandleException (HandleException.CODE_HTTP_EXCEPTION, HandleException.HTTP_EXCEPTION, new Throwable (response.code () + ":" + response.message ()));
            }
        } catch (Exception e) {
            throw HandleException.handleException (e);
        }

    }


    private Type getClassType() {

        Type type = getClass ().getGenericSuperclass ();
        return ((ParameterizedType) type).getActualTypeArguments ()[0];

    }
}
