package com.sfh.base;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sfh.lib.MVCache;
import com.sfh.lib.mvvm.BusEvent;
import com.sfh.lib.exception.HandleException;
import com.sfh.lib.http.down.HttpDownHelper;
import com.sfh.lib.http.down.ProgressListener;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.rx.IResultSuccess;
import com.sfh.lib.rx.RetrofitManager;
import com.sfh.lib.rx.ui.UtilRxView;
import com.sfh.lib.ui.AbstractLifecycleActivity;
import com.sfh.lib.utils.ZLog;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;


public class TestMainActivity extends AbstractLifecycleActivity<TestMainModel> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_file);
        initView();


    }

    Button button =null;
    private void initView() {
        ZLog.setDEBUG(true);
       Disposable disposable =  UtilRxView.clicks(findViewById(R.id.bt_text1),1000,new IResultSuccess<Object>() {
           @Override
           public void onSuccess(Object o) throws Exception {
               showToast("调用了");
               button.setText("aasdasd");
           }
        });
       this.putDisposable(disposable);
//        findViewById(R.id.bt_text1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //对话框
//                DialogBuilder builder = new DialogBuilder();
//                builder.setMessage("对话框");
//                builder.setTitle("标题");
//                showDialog(builder);
//            }
//        });
        findViewById(R.id.bt_text2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //提示框
                new TextRequest().sendRequest(new IResult<String>() {
                    @Override
                    public void onFail(HandleException e) {
                        showDialogToast(e.toString());
                    }

                    @Override
                    public void onSuccess(String s) throws Exception {

                    }
                });
            }
        });
        findViewById(R.id.bt_text3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //HTTP请求'
                getViewModel().http();
            }
        });
        findViewById(R.id.bt_text4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //文件下载
            }
        });
        findViewById(R.id.bt_text5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Fragment使用
            }
        });
        findViewById(R.id.bt_text6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //消息监听
            }
        });
    }

    @BusEvent(from = "消息通知")
    public void eventMag(String msg){
        showDialogToast(msg);
    }

    private void loadFile(String url) {
        Disposable disposable = RetrofitManager.executeSigin(Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) throws Exception {
                File file = new HttpDownHelper.Builder(url).setTagFile(new File(MVCache.getFileCache(), "test.apk")).setProgressListener(new ProgressListener() {
                    @Override
                    public void onProgress(long total, long percent, long progress) {

                        System.out.println("文件  total" + total + " percent:" + percent + " progress:" + progress);
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
