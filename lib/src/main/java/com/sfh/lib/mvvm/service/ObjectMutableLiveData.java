package com.sfh.lib.mvvm.service;

import android.arch.lifecycle.MutableLiveData;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 功能描述:数据监听
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/3/12
 */
public class ObjectMutableLiveData<T> extends MutableLiveData<T> {

    private boolean mOnActive = true;

    private volatile Set<T> mNetWorkState = new HashSet<> (7, 0.75f);

    @Override
    public void setValue(T value) {

        if (this.mOnActive) {
            super.setValue (value);
        } else if (this.hasObservers ()) {
            //应用在后台,暂存数据，防止丢失。
            this.mNetWorkState.add (value);
        }
    }

    public void onCleared() {
        this.mNetWorkState.clear ();
    }

    @Override
    protected void onActive() {

        this.mOnActive = true;
        super.onActive ();
        if (!this.mNetWorkState.isEmpty ()) {

            for (Iterator<T> it = this.mNetWorkState.iterator (); it.hasNext (); ) {
                super.setValue (it.next ());
            }
            this.mNetWorkState.clear ();
        }
    }

    @Override
    protected void onInactive() {

        this.mOnActive = false;
        super.onInactive ();
    }


}
