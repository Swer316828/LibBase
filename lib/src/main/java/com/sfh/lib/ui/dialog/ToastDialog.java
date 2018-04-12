package com.sfh.lib.ui.dialog;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.sfh.lib.R;
import com.sfh.lib.ui.DialogView;


/**
 * 功能描述:
 *
 * @author SunFeihu 孙飞虎
 * 2018/3/28
 */
public class ToastDialog extends DialogFragment implements View.OnClickListener, DialogView.DialogInterface {


    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvLeftClick;
    private TextView tvRightClick;
    private View vLine;
    private DialogView data;

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

        if (data == null) {
            return;
        }
        this.setCancelable(data.cancele());
        this.tvContent.setGravity(data.gravity());
        this.tvTitle.setText(TextUtils.isEmpty(data.getTitle()) ? "提示" : data.getTitle());
        this.tvContent.setText(TextUtils.isEmpty(data.getMessage()) ? "" : data.getMessage());
        this.tvLeftClick.setText(TextUtils.isEmpty(data.getLeftText()) ? "取消" : data.getLeftText());
        this.tvRightClick.setText(TextUtils.isEmpty(data.getRightText()) ? "确定" : data.getRightText());
        this.getDialog().getWindow().setWindowAnimations(R.style.dialogAnim);

    }


    public <T extends View> T findView(View view, @IdRes int resId) {
        return (T) view.findViewById(resId);
    }

    public void setData(DialogView data) {
        this.data = data;
    }


    public void show(FragmentActivity activity){
        if (activity == null){
            return;
        }
        super.show(activity.getSupportFragmentManager(),ToastDialog.class.getName());
    }

    @Override
    public void onClick(View v) {
        if (this.data == null) {
            this.dismiss();
            return;
        }

        if (v == this.tvLeftClick) {

            if (this.data.getOnLeftClick() == null) {
                this.dismiss();
                return;
            }
            this.data.getOnLeftClick().onClick(this, v.getId());
            return;
        }

        if (v == this.tvRightClick) {
            if (this.data.getOnRightClick() == null) {
                this.dismiss();
                return;
            }
            this.data.getOnRightClick().onClick(this, v.getId());
        }
    }


}
