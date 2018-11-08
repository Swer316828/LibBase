package com.sfh.lib.ui.dialog;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sfh.lib.R;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 功能描述:提示类对话框
 *
 * @author SunFeihu 孙飞虎
 * 2018/3/28
 */
public class ToastDialog extends DialogFragment implements View.OnClickListener, DialogBuilder.DialogInterface {


    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvLeftClick;
    private TextView tvRightClick;
    private View vLine;
    private DialogBuilder data;
    private FrameLayout fyContent;

    public static ToastDialog newToastDialog() {
        return new ToastDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.base_toast_dialog, container, false);
        tvTitle = findView(view, R.id.tvTitle);
        tvContent = findView(view, R.id.tvContent);
        tvLeftClick = findView(view, R.id.tvLeftClick);
        tvRightClick = findView(view, R.id.tvRightClick);
        fyContent = findView(view, R.id.fyContent);
        vLine = findView(view, R.id.vLine);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.tvLeftClick.setOnClickListener(this);
        this.tvRightClick.setOnClickListener(this);
        this.tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        this.getDialog().getWindow().setWindowAnimations(R.style.dialogAnim);
        this.getDialog().getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this.getContext(),R.drawable.base_file_toast_dialog));
        if (this.data == null) {
            return;
        }
        this.setCancelable(data.isCancelable());

        if (TextUtils.isEmpty(data.getTitle())) {
            this.tvTitle.setVisibility(View.GONE);
        } else {
            this.tvTitle.setVisibility(View.VISIBLE);
            this.tvTitle.setText(data.getTitle());
        }
        fyContent.removeAllViews();
        View v = data.getView();
        if (v != null){
            tvContent.setVisibility(View.GONE);
            fyContent.setVisibility(View.VISIBLE);
            fyContent.addView(v);

        }else {
            tvContent.setVisibility(View.VISIBLE);
            fyContent.setVisibility(View.GONE);
            this.tvContent.setGravity(data.getGravity());
            this.setTextViewStyle(this.tvContent, data.getMessage(), data.getMessageTextColor(), data.getMessageTextSize());
        }

        if (data.isHideCancel()){
            this.tvLeftClick.setVisibility(View.GONE);
            this.vLine.setVisibility(View.GONE);
        }else{
            this.tvLeftClick.setVisibility(View.VISIBLE);
            this.vLine.setVisibility(View.VISIBLE);
            this.setTextViewStyle(this.tvLeftClick, data.getCancelText(), data.getCancelTextColor(), data.getCancelTextSize());
        }

        this.setTextViewStyle(this.tvRightClick, data.getOkText(), data.getOkTextColor(), data.getOkTextSize());
    }

    private void setTextViewStyle(TextView tv, CharSequence msg, int color, int size) {
        if (TextUtils.isEmpty(msg)) {
            tv.setVisibility(View.GONE);
            return;
        }
        tv.setVisibility(View.VISIBLE);
        tv.setText(msg);
        if (color > 0) {
            tv.setTextColor(ContextCompat.getColor(this.getContext(), color));
        }
        if (size > 0) {
            tv.setTextSize(getResources().getDimensionPixelSize(size));
        }
    }

    public <T extends View> T findView(View view, @IdRes int resId) {
        return (T) view.findViewById(resId);
    }

    public void setData(DialogBuilder data) {
        this.data = data;
    }

    @Override
    public void onPause() {
        super.onPause();
        showAtom.set(false);
    }

    private AtomicBoolean showAtom = new AtomicBoolean(false);
    public synchronized  void show(FragmentActivity activity) {
        if (activity == null) {
            return;
        }
        if (this.isAdded()){
            return;
        }
        if (showAtom.get()){
            return;
        }
        showAtom.set(true);
        super.show(activity.getSupportFragmentManager(), ToastDialog.class.getName());
    }

    @Override
    public void dismiss() {
        super.dismiss();
        this.data = null;
    }

    @Override
    public void onClick(View v) {

        if (this.data == null) {
            this.dismiss();
            return;
        }

        if (v == this.tvLeftClick) {

            if (this.data.getCancelListener() == null) {
                this.dismiss();
                return;
            }
            this.data.getCancelListener().onClick(this, v.getId());
            return;
        }

        if (v == this.tvRightClick) {
            if (this.data.getOkListener() == null) {
                this.dismiss();
                return;
            }
            this.data.getOkListener().onClick(this, v.getId());
        }
    }


}
