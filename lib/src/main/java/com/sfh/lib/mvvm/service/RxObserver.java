package com.sfh.lib.mvvm.service;


import com.sfh.lib.exception.HandleException;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.utils.UtilLog;

import io.reactivex.functions.Consumer;

/**
 * 功能描述:结果处理[需要把异常处理成自定义异常]
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
public final class RxObserver<T> implements Consumer<T> {

    private IResult<T> result;

    public RxObserver(IResult<T> result) {
        this.result = result;
    }

    @Override
    public void accept(T o) throws Exception {
        if (this.result == null) {
            return;
        }
        this.result.onSuccess(o);
    }

    public Consumer onError =  new Consumer<HandleException>() {
        @Override
        public void accept(HandleException e) throws Exception {
            UtilLog.e(RxObserver.class, e.toString());
            if (result == null) {
                return;
            }
            result.onFail(e);
        }
    };


}
