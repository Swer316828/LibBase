package com.sfh.lib.mvp.service;


import com.sfh.lib.http.utils.UtilRxHttp;
import com.sfh.lib.mvp.IPresenter;
import com.sfh.lib.mvp.IResult;
import com.sfh.lib.mvp.IView;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * 功能描述:Mode 与 View 中间层
 * 1.通过Model层业务逻辑获取数据，在调用V层代理对象把数据传给真正V层对象
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public abstract class AbstractPresenter<V extends IView> implements IPresenter<V> {

    /**
     * 视图回调代理对象
     */
    private V proxy;

    /***
     * 管理操作
     */
    private RetrofitManager retrofit;

    @Override
    public V getView() {

        return this.proxy;
    }


    @Override
    public void onCreate(V proxy) {
        if (this.retrofit == null) {
            this.retrofit = new RetrofitManager();
        }
        this.proxy = proxy;
    }

    @Override
    public void onDestory() {
        //取消业务层监听
        if (retrofit != null) {
            this.retrofit.clearAll();
        }
    }

    @Override
    public void putDisposable(int taskId, Disposable disposable) {

        if (this.retrofit == null) {
            this.retrofit = new RetrofitManager();
        }
        this.retrofit.put(taskId, disposable);
    }

    @Override
    public <T> void execute(int taskId, Observable<T> observable, IResult<T> observer) {
        if (this.retrofit == null) {
            this.retrofit = new RetrofitManager();
        }
        Disposable disposable = this.retrofit.execute(observable, observer);
        this.putDisposable(taskId, disposable);
    }

    /***
     * 请求对象转换成Map<String, String>
     * @param params
     * @return
     */
    public Observable<Map<String, String>> buildParams(Object params) {

        return Observable.just(params).map(new Function<Object, Map<String, String>>() {
            @Override
            public Map<String, String> apply(Object o) throws Exception {
                return UtilRxHttp.buildParams(o);
            }
        });
    }
}
