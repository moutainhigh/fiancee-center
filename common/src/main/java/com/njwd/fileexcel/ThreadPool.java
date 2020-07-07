package com.njwd.fileexcel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/6 9:58
 */
public class ThreadPool {

    /**
     * 线程数
     */
    private static int threadCount;
    /**
     * 线程池
     */
    private static ExecutorService executorService;

    static {
        ThreadPool.threadCount = Runtime.getRuntime().availableProcessors();
        ThreadPool.executorService = Executors.newFixedThreadPool(threadCount);
    }

    public static int getThreadCount(){
        return ThreadPool.threadCount;
    }

    public static Future<?> submit(Runnable runnable){
        return executorService.submit(runnable);
    }


}
