package com.sfh.base;

import android.os.Environment;

import com.sfh.lib.exception.HandleException;
import com.sfh.lib.rx.IResult;
import com.sfh.lib.mvvm.service.BaseViewModel;
import com.sfh.lib.mvvm.service.NetWorkState;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * 功能描述: TODO
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/28
 */
public class FilePresenter extends BaseViewModel {
    public void getFileExtMht() {
        Observable<List<File>> observable =  Observable.just("tencent").map(new Function<String, List<File>>() {
            @Override
            public List<File> apply(String s) throws Exception {
                //1 先查询微信目录
                List<File> list = new ArrayList<>(5);
                File storage = Environment.getExternalStorageDirectory();
                if (storage.canRead() && storage.isDirectory()) {
                    File[] files = storage.listFiles();
                    for (File f : files) {
                        if (f.isDirectory() && f.getName().contains(s)) {
                            list.add(f);
                        }
                    }
                }
                return list;
            }
        }).map(new Function<List<File>, List<File>>() {
            @Override
            public List<File> apply(List<File> files) throws Exception {
                if (files.isEmpty()) {
                    return files;
                }
                List<File> result = new ArrayList<>(5);
                for (File file : files) {
                    getFile( file,  ".mht", result);
                }
                return result;
            }
        });
        setValue(NetWorkState.SHOW_LOADING);
        this.execute(observable, new IResult<List<File>>() {
            @Override
            public void onSuccess(List<File> files) throws Exception {
                setValue(NetWorkState.HIDE_LOADING);
                if (files.isEmpty()){
                    setValue(NetWorkState.showToast("未找到相关文件"));
                    return;
                }

                setValue(files);
            }

            @Override
            public void onFail(HandleException e) {
                setValue(NetWorkState.HIDE_LOADING);

                setValue(NetWorkState.showToast("未找到相关文件"+e));

            }
        });
    }

    private List<File> getFile(File file, String name, List<File> result) {
        if (file.isFile()) {
            if (file.getName().contains(name)) {
                result.add(file);
            }
            return result;
        }

        File[] files = file.listFiles();
        for (File f : files) {
            getFile(f, name, result);
        }
        return result;
    }

    public void delete(String path){

    }

}
