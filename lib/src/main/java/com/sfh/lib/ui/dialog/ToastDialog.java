package com.sfh.lib.ui.dialog;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.sfh.lib.R;


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

        if (this.data == null) {
            return;
        }
        this.setCancelable(data.isCancelable);
        this.tvContent.setGravity(data.gravity);
        if (TextUtils.isEmpty(data.title)) {
            this.tvTitle.setVisibility(View.GONE);
        } else {
            this.tvTitle.setVisibility(View.VISIBLE);
            this.tvTitle.setText(data.title);
        }

        setTextViewStyle(this.tvContent, data.message, data.messageTextColor, data.messageTextSize);
        setTextViewStyle(this.tvLeftClick, data.leftText, data.leftTextColor, data.leftTextSize);
        setTextViewStyle(this.tvRightClick, data.message, data.rightTextColor, data.rightTextSize);




    }

    private void setTextViewStyle(TextView tv, CharSequence msg, int color, int size) {
        if (TextUtils.isEmpty(msg)) {
            tv.setVisibility(View.GONE);
            return;
        }
        tv.setVisibility(View.VISIBLE);
        tv.setText(msg);
        if (color >= 0) {
            tv.setTextColor(ContextCompat.getColor(this.getContext(), data.messageTextColor));
        }
        if (size >= 0) {
            tv.setTextSize(getResources().getDimensionPixelSize(data.messageTextSize));
        }

    }

    public <T extends View> T findView(View view, @IdRes int resId) {
        return (T) view.findViewById(resId);
    }

    public void setData(DialogBuilder data) {
        this.data = data;
    }


    public void show(FragmentActivity activity) {
        if (activity == null) {
            return;
        }
        super.show(activity.getSupportFragmentManager(), ToastDialog.class.getName());
    }

    @Override
    public void onClick(View v) {
        if (this.data == null) {
            this.dismiss();
            return;
        }

        if (v == this.tvLeftClick) {

            if (this.data.leftListener == null) {
                this.dismiss();
                return;
            }
            this.data.leftListener.onClick(this, v.getId());
            return;
        }

        if (v == this.tvRightClick) {
            if (this.data.rightListener == null) {
                this.dismiss();
                return;
            }
            this.data.rightListener.onClick(this, v.getId());
        }
    }


}
