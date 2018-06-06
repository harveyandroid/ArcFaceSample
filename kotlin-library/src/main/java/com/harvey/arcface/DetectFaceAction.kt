package com.harvey.arcface

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.support.annotation.MainThread
import android.util.Log
import com.harvey.arcface.model.FaceFindModel

/**
 * Created by hanhui on 2018/6/1 0001 10:49
 */
class DetectFaceAction : Runnable {

    private val TAG = "DetectFace"
    private var mDetectFaceHandler: Handler
    private var mDetectFaceLooper: Looper
    private var frameBytes: ByteArray? = null
    @Volatile
    private var isDetectingFace = false// 正在检测人脸
    @Volatile
    private var isInit = false
    private var cameraWidth: Int = 0
    private var cameraHeight: Int = 0
    private var faceDetectListener: OnFaceDetectListener? = null

    init {
        val detectFaceThread = HandlerThread("DetectFace")
        detectFaceThread.start()
        mDetectFaceLooper = detectFaceThread.looper
        mDetectFaceHandler = Handler(mDetectFaceLooper)
        isInit = true
    }

    fun destroy() {
        if (isInit) {
            mDetectFaceLooper.quit()
        }
        frameBytes = null
        isDetectingFace = false
        faceDetectListener = null
        isInit = false
    }

    fun detectFace(bytes: ByteArray, width: Int, height: Int) {
        if (!isInit)
            throw NullPointerException("没有初始化人脸检测!")
        if (isDetectingFace)
            return
        this.frameBytes = bytes
        this.cameraWidth = width
        this.cameraHeight = height
        mDetectFaceHandler.post(this)
    }

    fun setOnFaceDetectListener(l: OnFaceDetectListener) {
        this.faceDetectListener = l
    }

    override fun run() {
        if (isDetectingFace)
            return
        if (frameBytes == null)
            return
        isDetectingFace = true
        val faceFindModels = ArcFaceEngine.detectFace(frameBytes!!, cameraWidth,
                cameraHeight)
        callOnFaceDetect(faceFindModels)
        isDetectingFace = false
    }

    private fun callOnFaceDetect(data: List<FaceFindModel>) {
        if (data.isNotEmpty())
            Log.d(TAG, "检测人脸数量-->" + data.size)
        MainHandler.run(Runnable { faceDetectListener?.onFaceDetect(data, frameBytes!!) })
    }

    interface OnFaceDetectListener {
        @MainThread
        fun onFaceDetect(faceFindModels: List<FaceFindModel>, frameBytes: ByteArray)
    }
}