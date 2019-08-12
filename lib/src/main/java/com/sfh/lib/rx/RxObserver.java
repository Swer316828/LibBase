package com.sfh.lib.rx;


import com.sfh.lib.exception.HandleException;
import com.sfh.lib.utils.UtilLog;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * 功能描述:结果处理[需要把异常处理成自定义异常]
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
class RxObserver<T> implements Consumer<T>, Action {

    private IResult<T> result;

    public RxObserver(IResult<T> result) {

        this.result = result;
    }

    @Override
    public void accept(T o) throws Exception {
        if (this.result != null) {
            this.result.onSuccess(o);
        }
    }

    public Consumer getOnError() {
        return this.onError;
    }

    private Consumer onError = new Consumer<HandleException>() {

        @Override
        public void accept(HandleException e) throws Exception {
            if (result != null) {
                result.onFail(e);
            }
        }
    };


    @Override
    public void run() throws Exception {

    }
}
