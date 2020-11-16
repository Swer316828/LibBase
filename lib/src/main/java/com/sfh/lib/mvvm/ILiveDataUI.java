package com.sfh.lib.mvvm;
/*=============================================================================================
 * 功能描述:
 *---------------------------------------------------------------------------------------------
 *  涉及业务 & 更新说明:
 *
 *
 *--------------------------------------------------------------------------------------------
 *=============================================================================================*/

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

public interface ILiveDataUI<T> {

    void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer);

    void onCleared();

    Activity getActivity();

    IDialog getDialog();

    void call(String method, Object... args);
}
