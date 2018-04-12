package com.sfh.lib.ui;

import android.support.annotation.NonNull;


import com.sfh.lib.mvp.ILifeCycle;
import com.sfh.lib.mvp.IView;

import java.util.ArrayList;
import java.util.List;


/**
 * 功能描述:生命周期管理的接口
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/11
 */
 final class AndroidLifecycle<T extends IView> implements ILifeCycle<T> {

    @NonNull
    public static <T extends IView> ILifeCycle<T> createLifecycleProvider() {
        return new AndroidLifecycle();
    }

    /**
     * 需要声明周期管理的接口
     */
    private final List<ILifeCycle<T>> lifeCycles = new ArrayList<>(2);

    @Override
    public void bindToLifecycle(ILifeCycle<T> lifeCycle) {
        this.lifeCycles.add(lifeCycle);
    }


    @Override
    public void onEvent(T listener, int event) {

        if (this.lifeCycles.isEmpty()) {
            return;
        }

        for (ILifeCycle<T> lifeCycle : lifeCycles) {
            lifeCycle.onEvent(listener, event);
        }
        // 清空管理
        if (event == EVENT_ON_FINISH || event == EVENT_ON_DESTROY){
            this.lifeCycles.clear();
        }
    }


}
