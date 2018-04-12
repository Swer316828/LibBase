package com.sfh.lib.mvp;

import android.support.annotation.Nullable;

/**
 * 功能描述:activity 与 Fragemnt 生命周期 回调
 *
 * @author sunfeihu
 * @date 2017/7/6
 */
public interface ILifeCycle<T extends IView> {

    int EVENT_ON_CREATE = 0x0001;

    int EVENT_ON_START = 0x0002;

    int EVENT_ON_RESUME = 0x0003;

    int EVENT_ON_PAUSE = 0x0004;

    int EVENT_ON_STOP = 0x0005;

    int EVENT_ON_FINISH = 0x0006;

    int EVENT_ON_DESTROY = 0x0007;


    /***
     * 生命周期响应
     * @param listener
     * @param event
     */
    void  onEvent(@Nullable T listener, int event);

    /***
     * 绑定生命周期管理对象
     * @param lifeCycle
     */
    void bindToLifecycle(@Nullable ILifeCycle<T> lifeCycle);

}
