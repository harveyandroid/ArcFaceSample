package com.harvey.arcface.view

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.arcsoft.face.util.ImageUtils
import com.harvey.arcface.model.FeatureCameraModel
import com.harvey.arcface.model.FeatureModel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by harvey on 2018/1/12.
 */
class SurfaceViewSaveFace(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    private var TimeSecondDown = 3
    private var ERRORCODE = 0
    private val surfaceHolder = holder.also {
        it.addCallback(this)
        it.setFormat(PixelFormat.TRANSPARENT)
    }
    private var surfaceRun = false
    private var surfaceStop = true
    // SurfaceView尺寸
    private var surfaceWidth = width
    private var surfaceHeight = height
    // 人脸数据列表
    private var featureCameraModel: FeatureCameraModel? = null
    private val saveFaceHandler by lazy {
        SaveFaceHandler()
    }
    private val mLock = ReentrantLock(true)
    private var frontCamera = true
    private var displayOrientation = 0
    private val faceOut by lazy {
        Rect(100, 100, surfaceWidth - 100, surfaceHeight - 100)
    }
    private var timeCount = 0
    private var drawRunnable = DrawAction()

    fun uploadTimeSecondDown(time: Int) {
        this.TimeSecondDown = time
    }

    fun setFrontCamera(frontCamera: Boolean) {
        this.frontCamera = frontCamera
    }

    fun setDisplayOrientation(displayOrientation: Int) {
        this.displayOrientation = displayOrientation
    }

    fun reset() {
        timeCount = 0
        surfaceStop = false
    }

    // 更新人脸列表
    fun uploadFace(model: FeatureCameraModel) {
        if (!surfaceRun)
            return
        if (surfaceStop)
            return
        mLock.lock()
        try {
            featureCameraModel = model
        } finally {
            mLock.unlock()
        }
    }

    fun setSaveFaceListener(saveFaceListener: SaveFaceListener?) {
        saveFaceHandler.setListener(saveFaceListener)
    }

    override fun surfaceCreated(mSurfaceHolder: SurfaceHolder) {
        val thread = Thread(drawRunnable)
        thread.start()
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {

    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        setSaveFaceListener(null)
        surfaceRun = false
        surfaceStop = true
    }

    interface SaveFaceListener {
        fun onSuccess(faceModel: FeatureCameraModel?)

        fun onTimeSecondDown(TimeSecond: Int)

        fun onErrorMsg(errorCode: Int)
    }

    private inner class SaveFaceHandler : Handler() {
        private val onSuccess = 1
        private val onTimeSecondDown = 2
        private val onErrorMsg = 3
        private var mSaveFaceListener: SaveFaceListener? = null

        fun setListener(saveFaceListener: SaveFaceListener?) {
            this.mSaveFaceListener = saveFaceListener
        }

        fun onSuccess(faceModel: FeatureCameraModel?) {
            val msg = Message()
            msg.what = onSuccess
            msg.obj = faceModel
            sendMessage(msg)
        }

        fun onTimeSecondDown(TimeSecond: Int) {
            val msg = Message()
            msg.what = onTimeSecondDown
            msg.obj = TimeSecond
            sendMessage(msg)

        }

        fun onErrorMsg(errorCode: Int) {
            val msg = Message()
            msg.what = onErrorMsg
            msg.obj = errorCode
            sendMessage(msg)

        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (mSaveFaceListener != null) {
                when (msg.what) {
                    onSuccess -> mSaveFaceListener!!.onSuccess(if (msg.obj == null) null else (msg.obj as FeatureCameraModel))
                    onTimeSecondDown -> mSaveFaceListener!!.onTimeSecondDown(msg.obj as Int)
                    onErrorMsg -> mSaveFaceListener!!.onErrorMsg(msg.obj as Int)
                }
            }
        }
    }

    private inner class DrawAction : Runnable {

        override fun run() {
            while (surfaceRun) {
                if (!surfaceStop) {
                    // 判断相框时间>时间退出
                    if (timeCount * SLEEP_COUNT > TimeSecondDown * 1000) {
                        surfaceStop = true
                        mLock.lock()
                        try {
                            saveFaceHandler.onSuccess(featureCameraModel)
                        } finally {
                            mLock.unlock()
                        }
                        continue
                    }
                    val canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        mLock.lock()
                        try {
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                            if (featureCameraModel == null || featureCameraModel!!.featureModels.isNullOrEmpty()) {
                                drawFaceOutRect(canvas, false)
                                callErrorBack(ERROR_NOFACE)
                            } else if (featureCameraModel!!.featureModels.size != 1) {
                                drawFaceOutRect(canvas, false)
                                callErrorBack(ERROR_MOREFACE)
                            } else {
                                val face = featureCameraModel!!.featureModels[0].adjustRect(displayOrientation, frontCamera, surfaceWidth, surfaceHeight)
                                // 在矩形框外侧
                                if (!faceOut!!.contains(face)) {
                                    drawFaceOutRect(canvas, false)
                                    drawFaceRect(canvas, face, false, "在矩形框外侧")
                                    callErrorBack(ERROR_OUTFACE)
                                } else if (face.width() < 300) {
                                    drawFaceOutRect(canvas, false)
                                    drawFaceRect(canvas, face, false, "距离摄像头过远")
                                    callErrorBack(ERROR_FARFACE)
                                } else {
                                    drawFaceOutRect(canvas, true)
                                    drawFaceRect(canvas, face, true)
                                    //                                    drawFaceImg(canvas, featureCameraModel.getFeatureModels().get(0), featureCameraModel.getCameraData());
                                    callTimeBack(timeCount * SLEEP_COUNT)
                                    timeCount++
                                }// 距离摄像头过远
                            }
                            surfaceHolder.unlockCanvasAndPost(canvas)
                        } finally {
                            mLock.unlock()
                        }
                    }
                }
                try {
                    Thread.sleep(SLEEP_COUNT.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }

        private fun callErrorBack(errorCode: Int) {
            timeCount = 0
            if (ERRORCODE != errorCode) {
                saveFaceHandler.onErrorMsg(errorCode)
                ERRORCODE = errorCode
            }
        }

        // 毫秒数
        private fun callTimeBack(time: Int) {
            ERRORCODE = ERROR_NO
            if (time == 0 || time % 1000 == 0) {
                saveFaceHandler.onTimeSecondDown(time / 1000)
            }
        }

        // 绘制外矩形框
        private fun drawFaceOutRect(canvas: Canvas?, success: Boolean) {
            val paint = Paint()
            if (success)
                paint.color = Color.parseColor("#3498db")
            else
                paint.color = Color.parseColor("#e74c3c")

            paint.strokeWidth = 20f
            paint.style = Paint.Style.STROKE
            canvas!!.drawRect(faceOut!!, paint)
        }

        // 绘制人脸框
        private fun drawFaceRect(canvas: Canvas?, face: Rect, success: Boolean) {
            val paint = Paint()
            if (success)
                paint.color = Color.parseColor("#3498db")
            else
                paint.color = Color.parseColor("#e74c3c")

            paint.strokeWidth = 10f
            paint.style = Paint.Style.STROKE
            canvas!!.drawRect(face, paint)
        }

        // 绘制人脸框
        private fun drawFaceRect(canvas: Canvas?, face: Rect, success: Boolean, remindText: String) {
            val paint = Paint()
            if (success)
                paint.color = Color.parseColor("#3498db")
            else
                paint.color = Color.parseColor("#e74c3c")
            paint.strokeWidth = 10f
            paint.style = Paint.Style.STROKE
            canvas!!.drawRect(face, paint)
            // 绘制坐标
            paint.reset()
            paint.textSize = 16f
            paint.color = Color.BLUE
            canvas.drawText(face.toString(), 100f, 100f, paint)
            canvas.drawText(remindText, 200f, 100f, paint)

        }

        // 绘制人脸
        private fun drawFaceImg(canvas: Canvas, model: FeatureModel, data: ByteArray) {
            var ops: ByteArrayOutputStream? = null
            var bmp: Bitmap? = null
            try {
                val yuv = YuvImage(data, ImageFormat.NV21, model.cameraWidth, model.cameraHeight, null)
                ops = ByteArrayOutputStream()
                yuv.compressToJpeg(model.faceMoreRect, 100, ops)
                val tmp = ops.toByteArray()
                bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
                bmp = ImageUtils.rotateBitmap(bmp, model.orientation.toFloat())
                canvas.drawBitmap(bmp!!, 100f, 100f, null)
            } finally {
                if (ops != null) {
                    try {
                        ops.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                bmp?.recycle()
            }
        }

    }

    companion object {
        val ERROR_NO = 0
        val ERROR_MOREFACE = 1
        val ERROR_NOFACE = 2
        val ERROR_OUTFACE = 3
        val ERROR_FARFACE = 4
        private val SLEEP_COUNT = 100// 毫秒
    }
}
