package com.sfh.lib;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModelStoreOwner;

public interface IViewLinstener extends LifecycleOwner, ViewModelStoreOwner {

    boolean isFinishing();

    Activity getActivity();
}
