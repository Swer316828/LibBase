package com.sfh.lib.ui;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.sfh.lib.mvp.ILifeCycle;
import com.sfh.lib.mvp.IView;
import com.sfh.lib.mvp.service.ViewProxy;


/**
 * 功能描述:MVP【不存在任何业务逻辑代码】
 *
 * @author SunFeihu 孙飞虎
 * @date 2017/7/5
 */
public abstract class AbstractFragment extends Fragment implements IView {


    /***
     * 生命周期管理
     */
    @Nullable
    private final ILifeCycle lifeCycle = new AndroidLifecycle();


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // 视图代理类
        ViewProxy viewProxy = new ViewProxy();

        //绑定生命周期管理
        viewProxy.bindToLifecycle(this.lifeCycle);

        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_CREATE);
    }

    @Override
    public void onStart() {

        super.onStart();
        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_START);
    }

    @Override
    public void onResume() {

        super.onResume();
        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_RESUME);
    }

    @Override
    public void onPause() {

        super.onPause();
        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_PAUSE);
    }


    @Override
    public void onStop() {

        super.onStop();
        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_STOP);
    }

    @Override
    public void onDestroyView() {

        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_FINISH);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_DESTROY);
        super.onDestroy();
    }

    public String getName() {
        return "";
    }

    @Override
    public void showLoading(boolean cancel) {
        AbstractActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.showLoading(cancel);

    }

    @Override
    public void hideLoading() {

        AbstractActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.hideLoading();
    }

    @Override
    public void showDialog(DialogView alert) {

        AbstractActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.showDialog(alert);
    }

    @Override
    public void hideDialog() {

        AbstractActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.hideDialog();
    }

    @Override
    public void showToast(CharSequence message) {
        AbstractActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.showToast(message);
    }

    @Override
    public void showToast(CharSequence message, int type) {
        AbstractActivity activity = this.lifeCycle();

        if (activity == null) {
            return;
        }
        activity.showToast(message, type);
    }

    private AbstractActivity lifeCycle() {
        FragmentActivity activity = getActivity();
        if (activity == null || activity.isFinishing() || !(activity instanceof AbstractActivity)) {
            return null;
        }
        return (AbstractActivity) activity;
    }


    public <T extends View> T findView(View view, @IdRes int resId) {
        return (T) view.findViewById(resId);
    }
}
