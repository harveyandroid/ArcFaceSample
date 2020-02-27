package com.harvey.arcface.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.harvey.arcface.R
import com.harvey.arcface.model.FeatureModel
import java.util.concurrent.CopyOnWriteArrayList

class SurfaceViewFace(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    private var thread: Thread? = null
    private val surfaceHolder: SurfaceHolder = holder
    private var surfaceRun = false
    @Volatile
    private var surfaceStop = false
    // 旋转图片
    private var scan1: Bitmap? = null
    private var scan2: Bitmap? = null
    // SurfaceView尺寸
    private var surfaceWidth: Int = 0
    private var surfaceHeight: Int = 0
    // 人脸数据列表
    private val faceFindModels: MutableList<FeatureModel>
    private var frontCamera = true//默认前置
    private var displayOrientation = 0
    private val rectPaint: Paint
    private val drawRunnable = DrawAction()

    init {
        surfaceHolder.addCallback(this)
        // 透明背景
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)
        faceFindModels = CopyOnWriteArrayList()
        rectPaint = Paint()
        rectPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        rectPaint.color = Color.RED
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 5f
    }

    // 更新人脸列表
    fun updateFace(faceFindModels: List<FeatureModel>?) {
        surfaceStop = false
        if (faceFindModels != null && faceFindModels.isNotEmpty()) {
            this.faceFindModels.clear()
            this.faceFindModels.addAll(faceFindModels)
        } else {
            this.faceFindModels.clear()
        }
    }

    fun setFrontCamera(frontCamera: Boolean) {
        this.frontCamera = frontCamera
    }

    fun setDisplayOrientation(displayOrientation: Int) {
        this.displayOrientation = displayOrientation
    }

    override fun surfaceCreated(mSurfaceHolder: SurfaceHolder) {
        surfaceRun = true
        surfaceStop = true
        scan1 = BitmapFactory.decodeResource(resources, R.drawable.scan1)
        scan2 = BitmapFactory.decodeResource(resources, R.drawable.scan2)
        surfaceWidth = width
        surfaceHeight = height
        thread = Thread(drawRunnable)
        thread!!.start()
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {

    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        surfaceRun = false
        surfaceStop = true
        scan1!!.recycle()
        scan2!!.recycle()
    }

    private inner class DrawAction : Runnable {
        // 旋转计数器
        private var drawRotate = 0
        private var drawRotateFind = 0
        private var sleepCount = 100// 毫秒

        override fun run() {
            while (surfaceRun) {
                if (!surfaceStop) {
                    val canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                        for (faceFindModel in faceFindModels) {
                            drawFaceRect(canvas, faceFindModel)
                            drawFindFace(canvas, faceFindModel)
                        }
                        drawRotate += 15
                        drawRotateFind += 5
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
                try {
                    Thread.sleep(sleepCount.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }

        private fun drawFaceRect(canvas: Canvas, model: FeatureModel) {
            canvas.drawRect(model.adjustRect(displayOrientation, frontCamera, surfaceWidth, surfaceHeight), rectPaint)
        }

        private fun drawFindFace(canvas: Canvas, model: FeatureModel) {
            val matrix = Matrix()
            val matrix2 = Matrix()

            matrix.postTranslate((-scan1!!.width / 2).toFloat(), (-scan1!!.height / 2).toFloat())// 步骤1
            matrix2.postTranslate((-scan2!!.width / 2).toFloat(), (-scan2!!.height / 2).toFloat())// 步骤1

            matrix.postRotate(drawRotate.toFloat())// 步骤2
            matrix2.postRotate((360 - drawRotate * 2).toFloat())// 步骤2

            val scaleWidth = model.rect.width().toFloat() * surfaceWidth.toFloat() / model.cameraWidth.toFloat() / scan1!!.width

            matrix.postScale(scaleWidth, scaleWidth)
            matrix2.postScale(scaleWidth, scaleWidth)
            // 中心点计算
            val mapRect = model.adjustRect(displayOrientation, frontCamera, surfaceWidth, surfaceHeight)
            val centerX = mapRect.centerX()
            val centerY = mapRect.centerY()
            matrix.postTranslate(centerX.toFloat(), centerY.toFloat())// 步骤3 屏幕的中心点
            matrix2.postTranslate(centerX.toFloat(), centerY.toFloat())// 步骤3 屏幕的中心点

            canvas.drawBitmap(scan1!!, matrix, Paint())
            canvas.drawBitmap(scan2!!, matrix2, Paint())
        }

    }
}
