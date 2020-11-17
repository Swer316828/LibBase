package com.sfh.lib.utils;
/*=============================================================================================
 * 功能描述:
 *---------------------------------------------------------------------------------------------
 *  涉及业务 & 更新说明:
 *
 *
 *--------------------------------------------------------------------------------------------
 *  @Author     SunFeihu 孙飞虎  on  2020/7/28
 *=============================================================================================*/

import android.support.annotation.IntDef;


public interface ThreadModel {
    int MAIN = 0x100;
    int ASYNC = 0x101;

    @IntDef({ThreadModel.ASYNC,ThreadModel.MAIN})
    @interface Thread{}

}