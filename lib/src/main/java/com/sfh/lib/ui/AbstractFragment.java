package com.sfh.lib.ui;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sfh.lib.mvp.ILifeCycle;
import com.sfh.lib.mvp.IView;
import com.sfh.lib.mvp.service.ViewProxy;
import com.sfh.lib.ui.dialog.DialogBuilder;


/**
 * 功能描述:MVP【不存在任何业务逻辑代码】
 *
 * @author SunFeihu 孙飞虎
 * @date 2017/7/5
 */
public abstract class AbstractFragment extends Fragment implements IView {
    protected ViewProxy<IView> viewProxy;
    protected View mRoot;
    /***
     * 生命周期管理
     */
    @Nullable
    private final ILifeCycle lifeCycle = new AndroidLifecycle();

    public abstract int getLayout();

    public void initData(View view) {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean initCreateView;
        //在Fragment onCreateView方法中缓存View
        //FragmentTab切换Fragment时避免重复加载UI
        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
            }
            initCreateView = false;
        } else {
            mRoot = inflater.inflate(this.getLayout(), container, false);
            initCreateView = true;
        }
        // 视图代理类
        if (viewProxy == null) {
            viewProxy = new ViewProxy();
        }

        //绑定生命周期管理
        viewProxy.bindToLifecycle(this.lifeCycle);

        this.lifeCycle.onEvent(this, ILifeCycle.EVENT_ON_CREATE);

        if (initCreateView) {
            this.initData(mRoot);
        }
        return mRoot;
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
        this.viewProxy = null;
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
    public void showDialog(DialogBuilder alert) {

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
