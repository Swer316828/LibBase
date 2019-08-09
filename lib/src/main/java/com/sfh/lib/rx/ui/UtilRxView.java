package com.sfh.lib.rx.ui;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.sfh.lib.rx.IResultSuccess;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * 功能描述:输入框监听，防止快速点击
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/4/8
 */
public class UtilRxView {

    /***
     * 推荐使用{@link #clicks(View, long, IResultSuccess)}
     *
     * 防止快速点击 ,一定时间内取第一次点击事件<p>
     * 注意：需要处理onNext() 方向出现异常情况<p>
     *
     * @param view
     * @param duration 单位毫秒
     * @return
     */
    public static Observable<Object> clicks(@NonNull View view, long duration) {

        return new ViewClickObservable(view).throttleFirst(duration, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread());
    }

    public static Disposable clicks(@NonNull View view, long duration, IResultSuccess<Object> result) {

        return clicks(view,duration).subscribe(new RxViewConsumer<Object>(result));
    }

    /***
     * 推荐使用{@link #textChanges(TextView, long, IResultSuccess)}
     *
     * 在约定时间内没有再次输入内容，则发送输入内容，如果再次触发了，则重新计算时间
     * 注意：需要处理onNext() 方向出现异常情况<p>
     *
     * @param view
     * @param timeout 单位毫秒
     * @return
     */
    public static Observable<CharSequence> textChanges(@NonNull TextView view, long timeout) {

        return new TextViewTextObservable(view).debounce(timeout, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread());
    }

    public static Disposable textChanges(@NonNull TextView view, long timeout, IResultSuccess<CharSequence> result) {

        return textChanges(view,timeout).subscribe(new RxViewConsumer<CharSequence>(result));
    }

    /***
     * 推荐使用{@link #afterTextChangeEvents(TextView, long, IResultSuccess)}
     *
     * 在约定时间内没有再次输入内容，则发送输入内容，如果再次触发了，则重新计算时间
     * 注意：需要处理onNext() 方向出现异常情况<p>
     *
     * @param view
     * @param timeout 单位毫秒
     * @return
     */
    public static Observable<CharSequence> afterTextChangeEvents(
            @NonNull TextView view, long timeout) {

        return new TextViewAfterTextChangeEventObservable(view).debounce(timeout, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread());
    }
    public static Disposable  afterTextChangeEvents(
            @NonNull TextView view, long timeout, IResultSuccess<CharSequence> result) {

        return afterTextChangeEvents(view,timeout).subscribe(new RxViewConsumer<CharSequence>(result));
    }
    /***
     * 推荐使用{@link #textChanges(TextView, long, IResultSuccess)}
     *
     * 在约定时间内没有再次输入内容，则发送输入内容，如果再次触发了，则重新计算时间
     * 注意：需要处理onNext() 方向出现异常情况<p>
     *
     * @param view
     * @param timeout 单位毫秒
     * @return
     */
    public static Observable<TextViewTextChangeEvent> textChangeEvents(
            @NonNull TextView view, long timeout) {

        return new TextViewTextChangeEventObservable(view).debounce(timeout, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread());
    }
    public static Disposable textChangeEvents(
            @NonNull TextView view, long timeout,IResultSuccess<TextViewTextChangeEvent> result) {

        return textChangeEvents(view,timeout).subscribe(new RxViewConsumer<TextViewTextChangeEvent>(result));
    }

    /***
     * 推荐使用{@link #beforeTextChangeEvents(TextView, long, IResultSuccess)}
     *
     * 在约定时间内没有再次输入内容，则发送输入内容，如果再次触发了，则重新计算时间
     * 注意：需要处理onNext() 方向出现异常情况<p>
     *
     * @param view
     * @param timeout 单位毫秒
     * @return
     */
    public static Observable<TextViewBeforeTextChangeEvent> beforeTextChangeEvents(
            @NonNull TextView view, long timeout) {

        return new TextViewBeforeTextChangeEventObservable(view).debounce(timeout, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread());
    }

    public static Disposable beforeTextChangeEvents(
            @NonNull TextView view, long timeout,IResultSuccess<TextViewBeforeTextChangeEvent> result) {

        return beforeTextChangeEvents(view,timeout).subscribe(new RxViewConsumer<TextViewBeforeTextChangeEvent>(result));
    }

}
