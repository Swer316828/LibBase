package com.sfh.lib.mvp;




import android.arch.lifecycle.LifecycleObserver;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;


/**
 * 功能描述:基础中间控制层
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public interface IPresenter<V extends IView> extends LifecycleObserver{

    /**
     * 创建代理
     * @param proxy
     */
    void onBindProxy(V proxy);

    /***
     * 获取视图代理类对象
     * @return
     */
    V getView();

    /***
     * 绑定监听
     * @param taskId
     * @param disposable
     */
    void putDisposable(int taskId, Disposable disposable);

    /***
     * 操作处理
     * @param observable
     * @param observer
     * @return
     */
    <T> int execute( Observable<T> observable, IResult<T> observer);
}
