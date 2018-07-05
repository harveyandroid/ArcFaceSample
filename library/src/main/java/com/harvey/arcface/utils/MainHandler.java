package com.harvey.arcface.utils;

import android.os.Handler;
import android.os.Looper;

public final class MainHandler extends Handler {
    
    private static MainHandler instance = new MainHandler();

    private MainHandler() {
        super(Looper.getMainLooper());
    }

    public static MainHandler getHandler() {
        return instance;
    }

    public static void run(Runnable runnable) {
        if (Looper.getMainLooper().equals(Looper.myLooper())) {
            runnable.run();
        } else {
            instance.post(runnable);
        }

    }
}
