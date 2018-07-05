package com.harvey.arcface.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by hanhui on 2018/7/5 0005 11:06
 */

public class ThreadManager {
    public static final int DEFAULT_THREAD_POOL_SIZE = SystemUtils.getDefaultThreadPoolSize(8);
    private final static ExecutorService io;
    private final static ExecutorService cache;
    private final static ExecutorService calculator;
    private final static ExecutorService file;
    private final static ScheduledExecutorService schedule;

    static {
        io = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
        cache = Executors.newCachedThreadPool();
        calculator = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
        file = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
        schedule = Executors.newScheduledThreadPool(DEFAULT_THREAD_POOL_SIZE);
    }

    public static ScheduledExecutorService getSchedule() {
        return schedule;
    }

    public static ExecutorService getIO() {
        return io;
    }

    public static ExecutorService getCache() {
        return cache;
    }

    public static ExecutorService getCalculator() {
        return calculator;
    }

    public static ExecutorService getFile() {
        return file;
    }
}
