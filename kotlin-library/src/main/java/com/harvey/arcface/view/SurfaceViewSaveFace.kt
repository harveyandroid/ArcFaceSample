package com.harvey.arcface.view

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.harvey.arcface.model.FaceFindCameraModel
import com.harvey.arcface.model.FaceFindModel
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Created by hanhui on 2018/6/1 0001 13:54
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
    private var saveFaceHandler = SaveFaceHandler()
    // 人脸数据列表
    private var faceModel: FaceFindCameraModel? = null
    private var thread: Thread? = null
    private var faceOut: Rect? = null
    private var timeCount = 0
    private var drawRunnable: Runnable = object : Runnable {

        override fun run() {
            while (surfaceRun) {
                if (!surfaceStop) {
                    // 判断相框时间>时间退出
                    if (timeCount * SLEEP_COUNT > TimeSecondDown * 1000) {
                        surfaceStop = true
                        saveFaceHandler.onSuccess(faceModel!!.clone())
                        continue
                    }
                    val canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                        if (faceModel == null || faceModel!!.faceFindModels == null
                                || faceModel!!.faceFindModels.isEmpty()) {
                            drawFaceOutRect(canvas, false)
                            callErrorBack(ERROR_NOFACE)
                        } else if (faceModel!!.faceFindModels.size != 1) {
                            drawFaceOutRect(canvas, false)
                            callErrorBack(ERROR_MOREFACE)
                        } else {
                            val face = faceModel!!.faceFindModels.get(0).getMappedFaceRect(surfaceHeight,
                                    surfaceWidth)
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
                                // drawFaceImg(canvas,faceModel.getFaceFindModels().get(0),faceModel.getCameraData());
                                callTimeBack(timeCount * SLEEP_COUNT)
                                timeCount++
                            }// 距离摄像头过远
                        }
                        surfaceHolder.unlockCanvasAndPost(canvas)
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
        private fun drawFaceImg(canvas: Canvas, faceFindModel: FaceFindModel, data: ByteArray) {
            val yuv = YuvImage(data, ImageFormat.NV21, faceFindModel.cameraWidth,
                    faceFindModel.cameraHeight, null)
            val ops = ByteArrayOutputStream()
            yuv.compressToJpeg(faceFindModel.getFaceMoreRect(), 100, ops)
            val tmp = ops.toByteArray()
            val bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
            canvas.drawBitmap(bmp, (surfaceHeight + 100).toFloat(), 500f, null)
            try {
                ops.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    init {
        surfaceHolder.addCallback(this)
        // 透明背景
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)
    }

    fun uploadTimeSecondDown(time: Int) {
        this.TimeSecondDown = time
    }

    fun reset() {
        timeCount = 0
        surfaceStop = false
    }

    // 更新人脸列表
    fun uploadFace(faceFindModels: List<FaceFindModel>, frameBytes: ByteArray) {
        if (!surfaceRun)
            return
        if (surfaceStop)
            return
        if (this.faceModel == null) {
            faceModel = FaceFindCameraModel(faceFindModels, frameBytes)
        } else {
            faceModel!!.faceFindModels = faceFindModels
            faceModel!!.cameraData = frameBytes
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
        faceOut = Rect(100, 100, 100 + surfaceWidth - 200, 100 + surfaceHeight - 200)
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
        fun onSuccess(faceModel: FaceFindCameraModel?)
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

        fun onSuccess(faceModel: FaceFindCameraModel) {
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
                    onSuccess -> msaveFaceListener!!.onSuccess(msg.obj as FaceFindCameraModel)
                    onTimeSecondDown -> msaveFaceListener!!.onTimeSecondDown(msg.obj as Int)
                    onErrorMsg -> msaveFaceListener!!.onErrorMsg(msg.obj as Int)
                }
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
