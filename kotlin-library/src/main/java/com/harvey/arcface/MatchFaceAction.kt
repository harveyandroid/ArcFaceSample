package com.harvey.arcface

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.support.annotation.MainThread
import android.text.TextUtils
import android.util.Log
import com.harvey.arcface.model.FaceFindMatchModel
import com.harvey.arcface.model.FaceFindModel
import com.harvey.db.OwnerDBHelper
import com.harvey.db.bean.RegisteredFace
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by hanhui on 2018/6/1 0001 10:59
 */
class MatchFaceAction : Runnable {
    private val TAG = "MatchFace"
    private val faceFindModels: MutableList<FaceFindModel> = CopyOnWriteArrayList<FaceFindModel>()
    private var frameBytes: ByteArray? = null
    private var matchListener: OnFaceMatchListener? = null
    @Volatile private var isMatchingFace = false
    private var mMatchFaceHandler: Handler
    private var mMatchFaceLooper: Looper
    @Volatile private var isInit = false
    private var registeredFaces = OwnerDBHelper.registeredFaces
    init {
        val matchFaceThread = HandlerThread("MatchFace")
        matchFaceThread.start()
        mMatchFaceLooper = matchFaceThread.looper
        mMatchFaceHandler = Handler(mMatchFaceLooper)
        isInit = true
    }


    fun destroy() {
        if (isInit) {
            mMatchFaceLooper.quit()
        }
        faceFindModels.clear()
        matchListener = null
        frameBytes = null
        isMatchingFace = false
        isInit = false
    }

    fun setFrameBytes(frameBytes: ByteArray) {
        this.frameBytes = frameBytes
    }

    fun matchFace(data: List<FaceFindModel>) {
        if (!isInit)
            throw NullPointerException("没有初始化人脸匹配!")
        this.faceFindModels.clear()
        this.faceFindModels.addAll(data)
        mMatchFaceHandler.post(this)
    }

    fun setOnFaceMatchListener(l: OnFaceMatchListener) {
        this.matchListener = l
    }

    override fun run() {
        if (isMatchingFace) {
            return
        }
        if (frameBytes == null) {
            return
        }
        if (faceFindModels.isEmpty()) {
            return
        }
        if (registeredFaces.isEmpty()) {
            return
        }
        isMatchingFace = true
        faceFindModels.forEach { findModel ->
            val matchModel = ArcFaceEngine.matchFace(frameBytes!!, findModel,
                    registeredFaces)
            callOnFaceMatch(matchModel)
        }
        faceFindModels.clear()
        frameBytes = null
        isMatchingFace = false
    }

    private fun callOnFaceMatch(face: FaceFindMatchModel) {
        Log.e(TAG, "人脸匹配结果-->" + face.toString())
        if (!TextUtils.isEmpty(face.name)) {
            MainHandler.run(Runnable { matchListener?.onFaceMatch(face) })
        }
    }

    interface OnFaceMatchListener {

        @MainThread
        fun onFaceMatch(face: FaceFindMatchModel)

    }

    inner class FaceObserver : Observer {

        override fun update(o: Observable?, arg: Any?) {
            registeredFaces = OwnerDBHelper.registeredFaces
        }

    }
}
