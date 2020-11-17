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

import com.sfh.lib.utils.SafeIterableMap;

import java.util.List;
import java.util.Map;

public class EventLifecycleObserver implements GenericLifecycleObserver {

    SafeIterableMap<LifecycleOwner, List<IEventListener>> mObsever = new SafeIterableMap<>();

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        if (source.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {

            List<IEventListener> values = mObsever.remove(source);
        }
    }
}
