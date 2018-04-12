package com.sfh.lib.http;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * 功能描述:通用网络请求
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public interface IRxHttpService {


    @GET("{url}")
    Observable<ResponseBody> executeGet(
            @Path(value = "url", encoded = true) String url,
            @QueryMap Map<String, String> params);

    @FormUrlEncoded
    @POST("{url}")
    Observable<ResponseBody> executePost(
            @Path(value = "url", encoded = true) String url,
            @FieldMap Map<String, String> params);
    /**
     * 文件
     */
    @POST("{url}")
    Observable<ResponseBody> executeFile(@Path(value = "url", encoded = true) String url, @Body MultipartBody file);
}
