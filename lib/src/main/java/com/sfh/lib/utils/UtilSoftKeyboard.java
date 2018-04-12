package com.sfh.lib.utils;


import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * 软键盘工具类
 *
 * @author zhoubo
 */
public class UtilSoftKeyboard {

     private UtilSoftKeyboard(){
         throw new IllegalStateException("you can't instantiate me!");
     }
    /**
     * 隐藏软键盘
     *
     * @param view
     */
    public static final void hide(View view) {

        InputMethodManager imm = (InputMethodManager) view.getContext ().getSystemService (Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow (view.getWindowToken (), 0);
    }

    /**
     * 显示软键盘
     *
     * @param view
     */
    public static final void show(View view) {

        view.setFocusable (true);
        view.requestFocus ();
        InputMethodManager imm = (InputMethodManager) view.getContext ().getSystemService (Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput (view, InputMethodManager.SHOW_FORCED);
        //显示软键盘
        imm.toggleSoftInput (0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
