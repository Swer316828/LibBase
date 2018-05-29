package com.sfh.base;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sfh.lib.mvp.IPresenter;
import com.sfh.lib.ui.AbstractActivity;

public class MainActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public IPresenter getPresenter() {
        return null;
    }
}
