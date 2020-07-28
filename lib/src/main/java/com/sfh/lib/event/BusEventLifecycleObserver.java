package com.sfh.lib.event;
/*=============================================================================================
 * 功能描述:
 *---------------------------------------------------------------------------------------------
 *  涉及业务 & 更新说明:
 *
 *
 *--------------------------------------------------------------------------------------------
 *  @Author     SunFeihu 孙飞虎  on  2020/7/28
 *=============================================================================================*/

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;

public class BusEventLifecycleObserver implements GenericLifecycleObserver {

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        if (source.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED){

        }
    }
}
