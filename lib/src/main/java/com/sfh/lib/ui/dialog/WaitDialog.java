package com.sfh.lib.ui.dialog;

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
public class WaitDialog extends DialogFragment {


    public static WaitDialog newToastDialog() {
        return new WaitDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.base_wait_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.getDialog().getWindow().setWindowAnimations(R.style.dialogAnim);
    }

    @Override
    public int getTheme() {

        return R.style.dialogTransparent;
    }

    public boolean isShowing() {
        return this.getDialog() != null
                && this.getDialog().isShowing();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.showAtom.set(false);
    }

   private  volatile  AtomicBoolean showAtom = new AtomicBoolean(false);

    public synchronized void show(FragmentActivity activity) {
        if (activity == null) {
            return;
        }

        if (this.isAdded()) {
            return;
        }
        if (showAtom.get()){
            return;
        }
        showAtom.set(true);
        super.show(activity.getSupportFragmentManager(), WaitDialog.class.getName());

    }

}
