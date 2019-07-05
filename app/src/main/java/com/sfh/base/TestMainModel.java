package com.sfh.base;

import com.sfh.lib.mvvm.service.BaseViewModel;
import com.sfh.lib.rx.IResultSuccess;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/7/4
 */
public class TestMainModel extends BaseViewModel {

    public void http(){
        this.execute(Observable.just(1).map(new Function<Integer, Object>() {
            @Override
            public Object apply(Integer integer) throws Exception {
                String str = null;
                return  str.contains("1");
            }
        }), new IResultSuccess<Object>() {
            @Override
            public void onSuccess(Object o) throws Exception {
                showDialogToast(o.toString());
            }
        });
    }
}
