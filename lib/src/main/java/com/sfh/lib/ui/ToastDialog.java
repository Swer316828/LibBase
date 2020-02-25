package com.sfh.lib.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sfh.lib.R;


/**
 * 功能描述:提示类对话框
 *
 * @author SunFeihu 孙飞虎
 * 2018/3/28
 */
public class ToastDialog extends AlertDialog implements View.OnClickListener, DialogBuilder.DialogInterface {


    public static ToastDialog newToastDialog(Context context) {

        return new ToastDialog(context);
    }

    public TextView tvTitle;

    public TextView tvContent;

    public TextView tvLeftClick;

    public TextView tvRightClick;

    public View vLine;

    private DialogBuilder data;

    public FrameLayout fyContent;


    protected ToastDialog(Context context) {

        super(context, R.style.dialogToast);
        this.getWindow().setWindowAnimations(R.style.dialogAnim);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.base_toast_dialog);
        this.tvTitle = findViewById(R.id.tvTitle);
        this.tvContent = findViewById(R.id.tvContent);
        this.tvLeftClick = findViewById(R.id.tvLeftClick);
        this.tvRightClick = findViewById(R.id.tvRightClick);
        this.fyContent = findViewById(R.id.fyContent);
        this.vLine = findViewById(R.id.vLine);

        this.tvLeftClick.setOnClickListener(this);
        this.tvRightClick.setOnClickListener(this);
        this.tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());

        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }


    public void show(DialogBuilder data) {

        Context context = this.getContext();
        if (context == null) {
            return;
        }
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return;
            }
        }
        super.show();
        this.showUIData(data);
    }

    private void showUIData(DialogBuilder data) {
        this.data = data;
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
        if (v != null) {
            tvContent.setVisibility(View.GONE);
            fyContent.setVisibility(View.VISIBLE);
            fyContent.addView(v);

        } else {
            tvContent.setVisibility(View.VISIBLE);
            fyContent.setVisibility(View.GONE);
            this.tvContent.setGravity(data.getGravity());
            this.setTextViewStyle(this.tvContent, data.getMessage(), data.getMessageTextColor(), data.getMessageTextSize());
        }

        if (data.isHideCancel()) {
            this.tvLeftClick.setVisibility(View.GONE);
            this.vLine.setVisibility(View.GONE);
        } else {
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
            tv.setTextSize(this.getContext().getResources().getDimensionPixelSize(size));
        }
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
