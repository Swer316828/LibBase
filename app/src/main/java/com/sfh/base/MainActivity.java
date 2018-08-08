package com.sfh.base;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sfh.lib.mvvm.annotation.LiveDataMatch;
import com.sfh.lib.ui.AbstractLifecycleActivity;

import java.io.File;
import java.util.List;

public class MainActivity extends AbstractLifecycleActivity<FilePresenter> implements AdapterView.OnItemClickListener {

    ListView lv;
    ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = findViewById(R.id.lv);
        lv.setOnItemClickListener(this);
        mViewModel.getFileExtMht();

        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.getFileExtMht();
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Main2Activity.statUI(this, adapter.getItem(position).getAbsolutePath());
    }

    @LiveDataMatch(action = "getFileExtMht")
    public void onSuccess(List<File> data) {
        adapter = new ItemAdapter(this, data);
        lv.setAdapter(adapter);
    }


}
