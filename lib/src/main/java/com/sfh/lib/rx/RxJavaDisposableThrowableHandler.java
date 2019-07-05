package com.sfh.lib.rx;

import com.sfh.lib.utils.UtilLog;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 功能描述:防止Disposable 之后出现异常导致应用崩溃,异常处理
 *
 * @author SunFeihu 孙飞虎
 * @date 2019/7/5
 */
public class RxJavaDisposableThrowableHandler implements Consumer<Throwable> {

    private static final class Hond {
        private static final RxJavaDisposableThrowableHandler mHandler = new RxJavaDisposableThrowableHandler();
    }

    public static RxJavaDisposableThrowableHandler newInstance() {
        return Hond.mHandler;
    }

    public static boolean put(@NonNull Object object,@NonNull Disposable disposable) {
        return Hond.mHandler.add(disposable);
    }

    public static void clear(@NonNull Object object) {
        Hond.mHandler.clear();
    }

    public static boolean remove(@NonNull Object object, @NonNull Disposable disposable) {
        return Hond.mHandler.remove(disposable);
    }

    private final CompositeDisposable mDisposableList;

    private RxJavaDisposableThrowableHandler() {
        this.mDisposableList = new CompositeDisposable();
    }

    private boolean add(@NonNull Disposable disposable) {

        UtilLog.w(RxJavaDisposableThrowableHandler.class,"RxJavaDisposableThrowableHandler [Add] Thread Id: "+ Thread.currentThread().getId());
        return mDisposableList.add(disposable);
    }

    /***
     *  移除所有任务并且取消监听任务
     * @return
     */
    private void clear() {
        UtilLog.w(RxJavaDisposableThrowableHandler.class,"RxJavaDisposableThrowableHandler [clear] Thread Id: "+Thread.currentThread().getId());
        this.mDisposableList.clear();
    }

    /***
     * 移除并且取消监听任务
     * @param disposable
     * @return
     */
    private boolean remove(@NonNull Disposable disposable) {
        UtilLog.w(RxJavaDisposableThrowableHandler.class,"RxJavaDisposableThrowableHandler [remove] Thread Id: "+ Thread.currentThread().getId());
        return mDisposableList.remove(disposable);
    }


    @Override
    public void accept(Throwable throwable) throws Exception {
        UtilLog.w(RxJavaDisposableThrowableHandler.class, "RxJavaDisposableThrowableHandler [Throwable] Thread Id:"+Thread.currentThread().getId()+" [连续2次异常，请注意]Throwable:"+throwable.getMessage());
        this.mDisposableList.clear();
    }

}
