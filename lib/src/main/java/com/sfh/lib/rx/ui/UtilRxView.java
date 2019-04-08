package com.sfh.lib.rx.ui;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * 功能描述:输入框监听，防止快速点击
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/4/8
 */
public class UtilRxView {

    /***
     * 防止快速点击 ,一定时间内取第一次点击事件。
     * @param view
     * @param windowDuration 单位毫秒
     * @return
     */
    public static Observable<Object> clicks(@NonNull View view, long windowDuration) {

        return new ViewClickObservable (view).throttleFirst (windowDuration, TimeUnit.MILLISECONDS).observeOn (AndroidSchedulers.mainThread ());
    }

    /***
     * 在约定时间内没有再次输入内容，则发送输入内容，如果再次触发了，则重新计算时间
     * @param view
     * @param timeout 单位毫秒
     * @return
     */
    public static Observable<CharSequence> textChanges(@NonNull TextView view, long timeout) {

        return new TextViewTextObservable (view).debounce (timeout, TimeUnit.MILLISECONDS).observeOn (AndroidSchedulers.mainThread ());
    }

    /***
     * 在约定时间内没有再次输入内容，则发送输入内容，如果再次触发了，则重新计算时间
     * @param view
     * @param timeout 单位毫秒
     * @return
     */
    public static Observable<CharSequence> afterTextChangeEvents(
            @NonNull TextView view, long timeout) {

        return new TextViewAfterTextChangeEventObservable (view).debounce (timeout, TimeUnit.MILLISECONDS).observeOn (AndroidSchedulers.mainThread ());
    }

    /***
     * 在约定时间内没有再次输入内容，则发送输入内容，如果再次触发了，则重新计算时间
     * @param view
     * @param timeout 单位毫秒
     * @return
     */
    public static Observable<TextViewTextChangeEvent> textChangeEvents(
            @NonNull TextView view, long timeout) {

        return new TextViewTextChangeEventObservable (view).debounce (timeout, TimeUnit.MILLISECONDS).observeOn (AndroidSchedulers.mainThread ());
    }

    /***
     * 在约定时间内没有再次输入内容，则发送输入内容，如果再次触发了，则重新计算时间
     * @param view
     * @param timeout 单位毫秒
     * @return
     */
    public static Observable<TextViewBeforeTextChangeEvent> beforeTextChangeEvents(
            @NonNull TextView view, long timeout) {

        return new TextViewBeforeTextChangeEventObservable (view).debounce (timeout, TimeUnit.MILLISECONDS).observeOn (AndroidSchedulers.mainThread ());
    }


}
