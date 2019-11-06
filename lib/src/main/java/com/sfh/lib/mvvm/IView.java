package com.sfh.lib.mvvm;


import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sfh.lib.event.IEventResult;
import com.sfh.lib.mvvm.service.NetWorkState;
import com.sfh.lib.ui.dialog.DialogBuilder;
import com.sfh.lib.ui.dialog.IDialog;

/**
 * 功能描述:
 *
 * @author sunfeihu
 * @date 2016/11/14
 */

public abstract class IView implements LifecycleOwner, Observer {

    public IView(IDialog dialog) {
        this.dialog = dialog;
    }

    IDialog dialog;

    public IDialog getDialog() {
        return dialog;
    }
}
