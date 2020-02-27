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
    private val surfaceHolder: SurfaceHolder = holder
    private var surfaceRun = false
    private var surfaceStop = true
    // SurfaceView尺寸
    private var surfaceWidth: Int = 0
    private var surfaceHeight: Int = 0
    // 人脸数据列表
    private var faceModel: FeatureCameraModel? = null
    private val saveFaceHandler: SaveFaceHandler
    private val mLock = ReentrantLock(true)
    private val condition = mLock.newCondition()
    private var frontCamera = true//默认前置
    private var displayOrientation = 0
    private var thread: Thread? = null
    private var faceOut: Rect? = null
    private var timeCount = 0
    private var drawRunnable = DrawAction()

    init {
        surfaceHolder.addCallback(this)
        // 透明背景
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)
        saveFaceHandler = SaveFaceHandler()
    }

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
    fun uploadFace(faceFindModels: MutableList<FeatureModel>, frameBytes: ByteArray, cameraWidth: Int, cameraHeight: Int) {
        if (!surfaceRun)
            return
        if (surfaceStop)
            return
        mLock.lock()
        try {
            if (faceModel == null) {
                faceModel = FeatureCameraModel(faceFindModels, frameBytes, cameraWidth, cameraHeight)
            } else {
                faceModel!!.setFeatureModels(faceFindModels)
                faceModel!!.nv21 = frameBytes
                faceModel!!.width = cameraWidth
                faceModel!!.height = cameraHeight
            }
        } finally {
            mLock.unlock()
        }
    }

    fun setSaveFaceListener(saveFaceListener: SaveFaceListener?) {
        saveFaceHandler.setListener(saveFaceListener)
    }

    override fun surfaceCreated(mSurfaceHolder: SurfaceHolder) {
        surfaceRun = true
        surfaceStop = false
        surfaceWidth = width
        surfaceHeight = height
        thread = Thread(drawRunnable)
        thread!!.start()
        // 设置矩形框
        faceOut = Rect(100, 100, surfaceWidth - 100, surfaceHeight - 100)
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {

    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        setSaveFaceListener(null)
        surfaceRun = false
        surfaceStop = true
        while (thread!!.isAlive) {
        }
    }

    interface SaveFaceListener {
        fun onSuccess(faceModel: FeatureCameraModel)

        fun onTimeSecondDown(TimeSecond: Int)

        fun onErrorMsg(errorCode: Int)
    }

    private inner class SaveFaceHandler : Handler() {
        private val onSuccess = 1
        private val onTimeSecondDown = 2
        private val onErrorMsg = 3
        private var msaveFaceListener: SaveFaceListener? = null

        fun setListener(saveFaceListener: SaveFaceListener?) {
            this.msaveFaceListener = saveFaceListener
        }

        fun onSuccess(faceModel: FeatureCameraModel) {
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
            if (msaveFaceListener != null) {
                when (msg.what) {
                    onSuccess -> msaveFaceListener!!.onSuccess(msg.obj as FeatureCameraModel)
                    onTimeSecondDown -> msaveFaceListener!!.onTimeSecondDown(msg.obj as Int)
                    onErrorMsg -> msaveFaceListener!!.onErrorMsg(msg.obj as Int)
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
                            saveFaceHandler.onSuccess(faceModel!!.clone())
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
                            if (faceModel == null || faceModel!!.getFeatureModels() == null
                                    || faceModel!!.getFeatureModels().size == 0) {
                                drawFaceOutRect(canvas, false)
                                callErrorBack(ERROR_NOFACE)
                            } else if (faceModel!!.getFeatureModels().size != 1) {
                                drawFaceOutRect(canvas, false)
                                callErrorBack(ERROR_MOREFACE)
                            } else {
                                val face = faceModel!!.getFeatureModels()[0].adjustRect(displayOrientation, frontCamera, surfaceWidth, surfaceHeight)
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
                                    //                                    drawFaceImg(canvas, faceModel.getFeatureModels().get(0), faceModel.getCameraData());
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
