package com.sfh.lib.rx;


import com.sfh.lib.exception.HandleException;
import com.sfh.lib.utils.UtilLog;

import io.reactivex.functions.Consumer;

/**
 * 功能描述:结果处理[需要把异常处理成自定义异常]
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
class RxObserver<T> implements Consumer<T> {

    private IResult<T> result;
    private IHanderLoading loading;

    public RxObserver(IResult<T> result) {
        this.result = result;
    }

    public RxObserver(IResult<T> result, IHanderLoading loading) {
        this.result = result;
        this.loading = loading;
    }

    @Override
    public void accept(T o) throws Exception {
        if (this.loading != null) {
            this.loading.hideLoading();
        }
        if (this.result != null) {
            this.result.onSuccess(o);
        }

    }

    public Consumer onError = new Consumer<HandleException>() {
        @Override
        public void accept(HandleException e) throws Exception {
            if (loading != null) {
                loading.hideLoading();
            }
            if (result != null) {
                result.onFail(e);
            }

        }
    };


}
