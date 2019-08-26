package com.sfh.lib.mvvm.data;

import android.util.SparseArray;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/8/21
 */
public class SparseArrayMethod {

    private final ReentrantLock mLock = new ReentrantLock();
    private final SparseArray<UIMethodFilter> mUIMethod = new SparseArray<> (5);

    public void put(UIMethodFilter method){
        mLock.lock();
        mUIMethod.put(method.hashCode(),method);
    }

    public UIMethodFilter get(){
        return null;
    }
}
