package com.sfh.lib.mvvm;

import android.support.annotation.IntDef;

public class VMData {

    public static final int TYPE_UI = 0x1;
    public static final int TYPE_DATA = 0x2;

    @IntDef({TYPE_UI,TYPE_DATA})
    public @interface TypeNode {
    }


    public String methodName;
    public Object[] args;

    @TypeNode
    public int type = TYPE_DATA;
}
