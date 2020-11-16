package com.sfh.lib.mvvm;


import android.arch.lifecycle.LifecycleOwner;

public interface IUIListener {

    void call(String method, Object... args);

    LifecycleOwner getLifecycleOwner();

    IDialog getDialog();

}
