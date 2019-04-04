package com.sfh.lib.rx;

import com.sfh.lib.exception.HandleException;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.utils.UtilLog;

import io.reactivex.disposables.Disposable;

/**
 * 功能描述:空任务
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/6/11
 */
public class EmptyResult<T> implements IResult<T> {

    Disposable disposable;

    public void addDisposable(Disposable disposable) {

        this.disposable = disposable;
    }

    @Override
    public void onSuccess(T t) throws Exception {

        UtilLog.d (EmptyResult.class.getName (), "onSuccess:" + t);
        this.dispose ();
    }

    @Override
    public void onFail(HandleException e) {

        UtilLog.d (EmptyResult.class.getName (), "onFail:" + e);
        this.dispose ();
    }

    private void dispose() {

        if (this.disposable != null) {
            UtilLog.d (EmptyResult.class.getName (), "in EmptyResult.class Disposable dispose 对象释放");
            //任务结束
            this.disposable.dispose ();
            this.disposable = null;
        }
    }
}
