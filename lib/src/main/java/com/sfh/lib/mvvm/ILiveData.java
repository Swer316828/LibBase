package com.sfh.lib.mvvm;
/*=============================================================================================
 * 功能描述:
 *---------------------------------------------------------------------------------------------
 *  涉及业务 & 更新说明:
 *
 *
 *--------------------------------------------------------------------------------------------
 *=============================================================================================*/

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

public interface ILiveData<T> {
    void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer);
    void onCleared();
}
