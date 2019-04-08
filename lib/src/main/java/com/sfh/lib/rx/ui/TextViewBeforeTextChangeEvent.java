package com.sfh.lib.rx.ui;

import android.widget.TextView;

/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/4/8
 */
public class TextViewBeforeTextChangeEvent {

    public TextView textView;
    public CharSequence s;
    public int start;
    public int count;
    public int after;

    TextViewBeforeTextChangeEvent( TextView textView,CharSequence s, int start,
                                    int count, int after){
        this.textView = textView;
        this.s = s;
        this.start = start;
        this.count = count;
        this.after = after;
    }
}
