package com.sfh.lib;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModelStoreOwner;

import com.sfh.lib.mvvm.IDialog;

public interface ViewLinstener extends LifecycleOwner, ViewModelStoreOwner {

    Activity getActivity();

    IDialog getDialog();
}
