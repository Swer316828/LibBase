package com.sfh.lib.rx;


import com.sfh.lib.exception.HandleException;
import com.sfh.lib.utils.UtilLog;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

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
        public void accept(HandleException throwable) {
            if (result != null) {
                result.onFail(throwable);
            }
        }
    };


    @Override
    public void run()  {

    }

}
