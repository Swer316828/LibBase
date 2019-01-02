package com.sfh.lib.http.transaction;

import android.text.TextUtils;

import com.sfh.lib.exception.HandleException;
import com.sfh.lib.http.UtilRxHttp;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.rx.RetrofitManager;

import java.lang.reflect.Field;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * 功能描述:请求对象
 * 1.请求base地址
 * 2.请求路径
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/12/17
 */
public abstract class OutreachRequest<T> extends BaseHttpRequest<T> {

    /***
     * 异步任务
     * @param result
     * @return
     */
    public Disposable sendRequest(IResult<T> result) {

        return RetrofitManager.executeSigin (Observable.just (1).map (new Function<Integer, T> () {

            @Override
            public T apply(Integer integer) throws Exception {

                return OutreachRequest.this.sendRequest ();
            }
        }), result);
    }

    /***
     * 同步任务
     * @return
     * @throws HandleException
     */
    @Override
    public T sendRequest() throws HandleException {
        return super.sendRequest ();
    }


    @Override
    public String buildParam() {

        if (TextUtils.equals (MEDIA_TYPE_JSON, this.mediaType)) {
            return this.toJson (this);
        }
        return buildParam (this);
    }

    private String buildParam(Object object) {

        Field[] fields = object.getClass ().getDeclaredFields ();
        if (fields == null || fields.length <= 0) {
            return "";
        }

        StringBuffer buffer = new StringBuffer ();
        for (Field field : fields) {
            field.setAccessible (true);
            Object value = null;
            try {
                value = field.get (object);
            } catch (IllegalAccessException e) {
                e.printStackTrace ();
            }
            if (UtilRxHttp.isBaseType (value)) {
                // 基础类型
                buffer.append (field.getName ()).append ("=").append (value.toString ()).append ("&");
            } else {
                buffer.append (field.getName ()).append ("=").append (this.toJson (value)).append ("&");
            }
        }

        return buffer.toString ();
    }


}
