package com.sfh.lib.mvp;



import com.sfh.lib.mvp.service.AbstractObserver;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;


/**
 * 功能描述:基础中间控制层
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public interface IPresenter<V extends IView>{

    /**
     * 创建代理
     * @param proxy
     */
    void onCreate(V proxy);

    /***
     * 销毁
     */
    void onDestory();

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
     * @param taskId
     * @param observable
     * @param observer
     * @return
     */
    <T> void execute(int taskId, Observable<T> observable, AbstractObserver<T> observer);

}
