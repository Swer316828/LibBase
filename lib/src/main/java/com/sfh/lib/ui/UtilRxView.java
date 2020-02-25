package com.sfh.lib.ui;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.Future;


/**
 * 功能描述:输入框监听，防止快速点击
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/4/8
 */
public class UtilRxView {

    /***
     *
     * 防止快速点击 ,一定时间内取第一次点击事件<p>
     * 注意：需要处理onNext() 方向出现异常情况<p>
     *
     * @param view
     * @param duration 单位毫秒
     * @return
     */
    public static Future clicks(@NonNull View view, long duration, View.OnClickListener clickListener) {

        return new ViewClickObservable(view, duration, clickListener);
    }

    /***
     *
     * 在约定时间内没有再次输入内容，则发送输入内容，如果再次触发了，则重新计算时间
     * 注意：需要处理onNext() 方向出现异常情况<p>
     *
     * @param view
     * @param timeout 单位毫秒
     * @return
     */
    public static Future textChanges(@NonNull TextView view, long timeout, TextChangedListener listener) {

        return new TextViewTextObservable(view,timeout, listener);
    }


}
