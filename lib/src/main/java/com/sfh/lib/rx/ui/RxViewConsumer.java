package com.sfh.lib.rx.ui;

import com.sfh.lib.exception.HandleException;
import com.sfh.lib.rx.IResultSuccess;
import com.sfh.lib.utils.UtilLog;

import io.reactivex.functions.Consumer;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/8/9
 */
public class RxViewConsumer<T> implements Consumer<T> {

    IResultSuccess<T> mLinstener;

    public RxViewConsumer(IResultSuccess<T> linstener) {
        this.mLinstener = linstener;
    }

    @Override
    public void accept(T t) {
        if (this.mLinstener == null) {
           return;
        }
        try {
            this.mLinstener.onSuccess(t);
        } catch (Exception e) {
            UtilLog.d(RxViewConsumer.class,e.getMessage());

            if (HandleException.crashReportHandler != null) {
                HandleException.crashReportHandler.accept(e);
            }
        }
    }
}
