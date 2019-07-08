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

    public static boolean put(Object object, @NonNull Disposable disposable) {
        return Hond.mHandler.add(object, disposable);
    }

    public static void onClearAll(Object object) {
        Hond.mHandler.clearAll(object);
    }

    public static boolean onRemove(Object object, @NonNull Disposable disposable) {
        return Hond.mHandler.remove(object, disposable);
    }


    private final CompositeDisposable mDisposableList;

    private RxJavaDisposableThrowableHandler() {
        this.mDisposableList = new CompositeDisposable();
    }

    private boolean add(Object object, @NonNull Disposable disposable) {

        UtilLog.w(RxJavaDisposableThrowableHandler.class, "RxJava Disposable [Add] Thread Id: " + Thread.currentThread().getId() + " Task:" + object + " disposable:" + disposable);
        return mDisposableList.add(disposable);
    }

    /***
     * 移除并且取消监听任务
     * @param disposable
     * @return
     */
    private boolean remove(@NonNull Object object, @NonNull Disposable disposable) {
        UtilLog.w(RxJavaDisposableThrowableHandler.class, "RxJava Disposable [remove] Thread Id: " + Thread.currentThread().getId() + " Task:" + object + " disposable:" + disposable);
        return mDisposableList.remove(disposable);
    }

    /***
     *  移除所有任务并且取消监听任务
     * @return
     */
    private void clearAll(@NonNull Object object) {
        UtilLog.w(RxJavaDisposableThrowableHandler.class, "RxJava Disposable [clearAll]");
        this.mDisposableList.clear();
    }


    @Override
    public void accept(Throwable throwable) throws Exception {
        UtilLog.w(RxJavaDisposableThrowableHandler.class, "RxJava Disposable [Throwable] Thread Id:" + Thread.currentThread().getId() + " [连续2次异常，请注意]Throwable:" + throwable.getMessage());
        this.mDisposableList.clear();
    }

}
