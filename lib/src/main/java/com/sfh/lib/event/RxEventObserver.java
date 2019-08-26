package com.sfh.lib.event;


import io.reactivex.functions.Consumer;

/**
 * 功能描述:结果处理[需要把异常处理成自定义异常]
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/4/3
 */
class RxEventObserver<T> implements Consumer<T> {

    public static final Consumer onError = (Consumer<Throwable>) e -> {
    };
    private IEventResult<T> result;

    public RxEventObserver(IEventResult<T> result) {
        this.result = result;
    }


    @Override
    public void accept(T t) throws Exception {
        if (result != null) {
            try {
                result.onEventSuccess(t);
            } catch (Exception e) {

            }
        }
    }
}
