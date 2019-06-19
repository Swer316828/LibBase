package com.sfh.base;

import android.os.Bundle;
import android.view.View;

import com.sfh.lib.AppCacheManager;
import com.sfh.lib.exception.HandleException;
import com.sfh.lib.http.down.HttpDownHelper;
import com.sfh.lib.http.down.ProgressListener;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.ui.AbstractLifecycleActivity;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/***
 * 文件下载
 */
public class TestFileActivity extends AbstractLifecycleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_file);
        findViewById(R.id.bt_load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFile("");
            }
        });
    }

    private void loadFile(String url) {
        Disposable disposable = RetrofitManager.executeSigin(Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) throws Exception {
                File file = new HttpDownHelper.Builder(url).setTagFile(new File(AppCacheManager.getFileCache(), "test.apk")).setProgressListener(new ProgressListener() {
                    @Override
                    public void onProgress(long total, long percent, long progress) {

                        System.out.println("文件  total"+total+" percent:"+percent+" progress:"+progress);
                    }
                }).start();
                emitter.onNext(file);
                emitter.onComplete();
            }
        }), new IResult<File>() {
            @Override
            public void onFail(HandleException e) {
                showDialogToast(e.getMsg());
            }

            @Override
            public void onSuccess(File file) throws Exception {
                showDialogToast(file.getAbsolutePath());
            }
        });
        putDisposable(disposable);
    }

}
