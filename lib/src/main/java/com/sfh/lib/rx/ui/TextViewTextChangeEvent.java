package com.sfh.lib.rx.ui;

import android.widget.TextView;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/4/8
 */
public class TextViewTextChangeEvent {

    public TextView textView;

    public  CharSequence s;

    public   int start;

    public  int before;

    public  int count;

    public TextViewTextChangeEvent(TextView view, CharSequence s, int start, int before, int count) {

        this.textView = view;
        this.s = s;
        this.start = start;
        this.before = before;
        this.count = count;

    }
}
