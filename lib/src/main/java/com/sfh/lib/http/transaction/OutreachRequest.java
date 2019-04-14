package com.sfh.lib.http.transaction;

import android.text.TextUtils;

import com.sfh.lib.exception.HandleException;
import com.sfh.lib.http.HttpMediaType;
import com.sfh.lib.http.UtilRxHttp;
import com.sfh.lib.http.annotation.LoseParameter;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.rx.RetrofitManager;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 功能描述:请求对象,默认POST方式，参数格式类型JSON,Gson解析返回数据
 *
 * <p>参数格式类型{@link HttpMediaType}</p>
 * <p>如自定义或处理返回结果 可以重写{@link ParseResult parseResult方法}</p>
 * <p>需参数进行处理操作，重载{@link OutreachRequest buildParam方法}</p>
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/12/17
 */
public abstract class OutreachRequest<T> extends BaseHttpRequest<T> {


    public OutreachRequest(String path) {

        super (path);
    }

    /***
     * 异步任务
     * @param result 返回数据回调接口
     * @return
     */
    public Disposable sendRequest(IResult<T> result) {

        return RetrofitManager.executeSigin (this.getTask (), result);
    }

    /***
     * 任务对象
     * @return
     */
    public Observable<T> getTask() {

        return Observable.create (new ObservableOnSubscribe<T> () {

            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {

                T t = OutreachRequest.this.sendRequest ();
                if (t == null) {
                    //onNext called with null. Null values are generally not allowed in 2.x operators and sources.
                    emitter.onError (new HandleException ("H1000", "请求失败，结果为NULL,Url：" + getUrl (code) + path));
                } else {
                    emitter.onNext (t);
                }

                emitter.onComplete ();

            }
        });
    }

    /***
     * 同步执行任务
     * @return 返回数据对象
     * @throws HandleException
     */
    @Override
    public T sendRequest() throws Exception {

        return super.sendRequest ();
    }

    @Override
    public String getUrl(String code) {

        return this.getHttpService ().getHots ();
    }

    @Override
    public Object buildParam() {

        if (TextUtils.equals (HttpMediaType.MEDIA_TYPE_MULTIPART_FORM, this.mediaType)) {
            // 文件类型
            MultipartBody.Builder builder = new MultipartBody.Builder ().setType (MultipartBody.FORM);
            this.buildParamMultipart (this, builder);
            return builder.build ();
        }
        if (TextUtils.equals (HttpMediaType.MEDIA_TYPE_JSON, this.mediaType)) {
            return this.toJson (this);
        }

        return this.buildParamKeyValue (this);
    }

    /**
     * 文件上传
     *
     * @param object
     * @param builder
     */
    private void buildParamMultipart(Object object, MultipartBody.Builder builder) {

        Field[] fields = object.getClass ().getDeclaredFields ();
        if (fields == null || fields.length <= 0) {
            return;
        }

        //准备实体
        for (Field field : fields) {
            field.setAccessible (true);
            if (this.isLoseParameter (field)) {
                continue;
            }
            Object value = null;
            try {
                value = field.get (object);
            } catch (IllegalAccessException e) {
            }
            if (value == null || TextUtils.isEmpty (value.toString ())) {
                continue;
            }
            String key = field.getName ();
            if (UtilRxHttp.isBaseType (value)) {
                // 基础类型
                builder.addPart (MultipartBody.Part.createFormData (key, String.valueOf (value)));

            } else if (value instanceof File) {
                File f = (File) value;
                builder.addPart (MultipartBody.Part.createFormData (field.getName (), f.getName (), RequestBody.create (MediaType.parse ("application/octet-stream"), f)));
            } else {
                builder.addPart (MultipartBody.Part.createFormData (key, this.toJson (value)));
            }
        }
    }

    /***
     * 处理 key=value&key=value
     * @param object
     * @return
     */
    private String buildParamKeyValue(Object object) {

        Field[] fields = object.getClass ().getDeclaredFields ();
        if (fields == null || fields.length <= 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer (100);

        for (Field field : fields) {
            field.setAccessible (true);

            if (this.isLoseParameter (field)) {
                continue;
            }
            Object value = null;
            try {
                value = field.get (object);
            } catch (IllegalAccessException e) {
            }
            if (value == null || TextUtils.isEmpty (value.toString ())) {
                continue;
            }
            if (UtilRxHttp.isBaseType (value)) {
                // 基础类型
                buffer.append (field.getName ()).append ("=").append (value.toString ()).append ("&");
            } else if (value instanceof File) {

            } else {
                buffer.append (field.getName ()).append ("=").append (this.toJson (value)).append ("&");
            }
        }
        return buffer.toString ();
    }


    /***
     * 忽略参数
     * @param field
     * @return
     */
    private boolean isLoseParameter(Field field) {
        //静态属性被忽略
        if (Modifier.isStatic (field.getModifiers ())
                || Modifier.isFinal (field.getModifiers ())
                || Modifier.isTransient (field.getModifiers ())) {
            return true;
        }
        LoseParameter lose = field.getAnnotation (LoseParameter.class);
        if (lose != null) {
            // 忽略参数
            return true;
        }
        return false;
    }
}
