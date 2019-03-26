package com.sfh.lib.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.sfh.lib.R;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 功能描述:等待对话框
 *
 * @author SunFeihu 孙飞虎
 * 2018/3/28
 */
public class WaitDialog extends AlertDialog {

    public static WaitDialog newToastDialog(Context context) {

        return new WaitDialog (context);
    }


    protected WaitDialog(Context context) {

        super (context, R.style.dialogTransparent);
        this.getWindow ().setWindowAnimations (R.style.dialogAnim);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate (savedInstanceState);
        this.setContentView (R.layout.base_wait_dialog);
    }

    @Override
    public void show() {

        Context context = this.getContext ();
        if (context == null) {
            return;
        }
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing ()) {
                return;
            }
        }
        super.show ();
    }
}
