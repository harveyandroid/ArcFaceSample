package com.harvey.arcface

import android.os.Looper

/**
 * Created by hanhui on 2018/6/1 0001 09:51
 */
class MainHandler private constructor(looper: Looper) : android.os.Handler(looper) {

    companion object {
        
        private val instance = MainHandler(Looper.getMainLooper())

        fun run(r: Runnable) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                r.run()
            } else {
                instance.post(r)
            }
        }

        fun runDelayed(r: Runnable, delayMillis: Long) {
            instance.postDelayed(r, delayMillis)
        }
    }
}
