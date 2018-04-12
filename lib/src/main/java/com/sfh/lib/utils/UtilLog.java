package com.sfh.lib.utils;

import android.content.Context;
import android.util.Log;

import com.sfh.lib.BuildConfig;


/**
 * 功能描述:日志
 *
 * @date 2017/6/21
 */
public class UtilLog {

    private UtilLog() {
        throw new IllegalStateException("you can't instantiate me!");
    }

    public static boolean DEBUG = BuildConfig.DEBUG;

    public static void setDEBUG(boolean bug) {

        UtilLog.DEBUG = bug;
    }


    public static void e(String tag, String msg) {

        if (DEBUG) {
            Log.e(tag, msg);
        }

    }

    public static void e(Class tag, String msg) {

        if (DEBUG) {
            Log.e(tag.getName(), msg);
        }

    }

    public static void e(Context tag, String msg) {

        if (DEBUG) {
            Log.e(tag.getClass().getName(), msg);
        }

    }

    public static void d(String tag, String msg) {

        if (DEBUG) {
            Log.d(tag, msg);
        }

    }

    public static void d(Class tag, String msg) {

        if (DEBUG) {
            Log.d(tag.getName(), msg);
        }

    }

    public static void d(Context tag, String msg) {

        if (DEBUG) {
            Log.d(tag.getClass().getName(), msg);
        }

    }

    public static void i(String tag, String msg) {

        if (DEBUG) {

            Log.i(tag, msg);
        }

    }

    public static void i(Class tag, String msg) {

        if (DEBUG) {
            Log.i(tag.getName(), msg);
        }

    }

    public static void i(Context tag, String msg) {

        if (DEBUG) {
            Log.i(tag.getClass().getName(), msg);
        }

    }

    public static void v(Class tag, String msg) {

        if (DEBUG) {
            Log.v(tag.getName(), msg);
        }

    }

    public static void v(String tag, String msg) {

        if (DEBUG) {
            Log.v(tag, msg);
        }

    }

    public static void v(Context tag, String msg) {

        if (DEBUG) {
            Log.v(tag.getClass().getName(), msg);
        }

    }

    public static void w(Class tag, String msg) {

        if (DEBUG) {
            Log.w(tag.getName(), msg);
        }

    }

    public static void w(String tag, String msg) {

        if (DEBUG) {
            Log.w(tag, msg);
        }

    }


    public static void w(Context tag, String msg) {

        if (DEBUG) {
            Log.w(tag.getClass().getName(), msg);
        }

    }

}
