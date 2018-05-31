package com.harvey.arcface.utils;

import android.os.Handler;
import android.os.Looper;

public class MainHandler extends Handler {
	private static MainHandler instance = new MainHandler(Looper.getMainLooper());

	protected MainHandler(Looper looper) {
		super(looper);
	}

	public static MainHandler getInstance() {
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
