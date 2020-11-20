package com.sfh.lib.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * 任务
 */
public class ThreadTaskUtils {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_SECONDS = 30;

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(128);

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "MVVM AsyncTask #" + mCount.getAndIncrement());
        }
    };

    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                sPoolWorkQueue, sThreadFactory);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        THREAD_POOL_EXECUTOR = threadPoolExecutor;


        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(
                CORE_POOL_SIZE,sThreadFactory);
        SCHEDULED_EXECUTOR = scheduledThreadPoolExecutor;
    }

    public static ScheduledThreadPoolExecutor SCHEDULED_EXECUTOR;
    public static final Executor THREAD_POOL_EXECUTOR;

    /***
     * 立即执行
     * @param runnable
     */
    public static void execute(Runnable runnable) {
        THREAD_POOL_EXECUTOR.execute(runnable);
    }

    /***
     * 立即执行
     * @param runnable
     */
    public static <T> FutureTask<T> execute(Callable<T> runnable) {
        FutureTask<T> futureTask = new FutureTask(runnable);
        THREAD_POOL_EXECUTOR.execute(futureTask);
        return futureTask;
    }

    /***
     * 延时任务
     * @param command
     * @param delay
     * @param unit
     * @return
     */
    public static Future<?> schedule(Runnable command, long delay, TimeUnit unit) {

        return SCHEDULED_EXECUTOR.schedule(command, delay, unit);
    }


    /***
     * 固定的间隔执行任务
     * @param command
     * @param initialDelay
     * @param delay
     * @param unit
     * @return
     */
    public static Future<?> scheduleWithFixedDelay(Runnable command,
                                                   long initialDelay,
                                                   long delay,
                                                   TimeUnit unit) {

        return SCHEDULED_EXECUTOR.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }


    public static void close(Future... tasks){
        for (Future future : tasks) {
            if (!future.isDone() && !future.isCancelled()) {
                future.cancel(true);
            }
        }
    }
}
