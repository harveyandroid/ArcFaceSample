package com.harvey.arcface.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.harvey.arcface.R
import com.harvey.arcface.model.FaceFindModel
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by hanhui on 2018/6/1 0001 11:16
 */
class SurfaceViewFace(content: Context, attrs: AttributeSet) : SurfaceView(content, attrs), SurfaceHolder.Callback {

    private var surfaceHolder: SurfaceHolder = holder
    private var surfaceRun = false
    private var surfaceStop = false
    // 旋转图片
    private var scan1: Bitmap? = null
    private var scan2: Bitmap? = null
    // SurfaceView尺寸
    private var surfaceWidth: Int = 0
    private var surfaceHeight: Int = 0
    // 人脸数据列表
    private var faceFindModels: MutableList<FaceFindModel> = CopyOnWriteArrayList()
    private lateinit var thread: Thread

    init {
        surfaceHolder.addCallback(this)
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)
        scan1 = BitmapFactory.decodeResource(resources, R.drawable.scan1)
        scan2 = BitmapFactory.decodeResource(resources, R.drawable.scan2)
    }

    // 更新人脸列表
    fun updateFace(faceFindModels: List<FaceFindModel>) {
        surfaceStop = false
        this.faceFindModels.clear()
        this.faceFindModels.addAll(faceFindModels)
    }


    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        surfaceRun = false
        surfaceStop = true
        scan1?.recycle()
        scan2?.recycle()
        while (thread.isAlive) {
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        surfaceRun = true
        surfaceStop = true
        surfaceWidth = width
        surfaceHeight = height
        thread = Thread(drawRunnable)
        thread.start()
    }

    private val drawRunnable = object : Runnable {
        // 旋转计数器
        internal var drawRotate = 0
        internal var drawRotateFind = 0
        internal var sleepCount = 100// 毫秒

        override fun run() {
            while (surfaceRun) {
                if (!surfaceStop) {
                    val canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                        for (faceFindModel in faceFindModels) {
                            drawNoFindFace(canvas, faceFindModel)
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

        private fun drawNoFindFace(canvas: Canvas, faceFindModel: FaceFindModel) {
            val matrix = Matrix()
            val matrix2 = Matrix()
            val scan1Width = scan1?.width ?: 0
            val scan1Height = scan1?.height ?: 0
            val scan2Width = scan2?.width ?: 0
            val scan2Height = scan2?.height ?: 0
            matrix.postTranslate((-scan1Width / 2).toFloat(), (-scan1Height / 2).toFloat())// 步骤1
            matrix2.postTranslate((-scan2Width / 2).toFloat(), (-scan2Height / 2).toFloat())// 步骤1
            matrix.postRotate(drawRotate.toFloat())// 步骤2
            matrix2.postRotate((360 - drawRotate * 2).toFloat())// 步骤2
            // 缩放
            val scaleWidth = faceFindModel.faceRect.width().toFloat() * surfaceWidth.toFloat() / faceFindModel.cameraWidth.toFloat() / scan1Width
            matrix.postScale(scaleWidth, scaleWidth)
            matrix2.postScale(scaleWidth, scaleWidth)
            // 中心点计算
            val mapRect = faceFindModel.getMappedFaceRect(surfaceHeight, surfaceWidth)

            val centerX = mapRect.centerX()
            val centerY = mapRect.centerY()

            matrix.postTranslate(centerX.toFloat(), centerY.toFloat())// 步骤3 屏幕的中心点
            matrix2.postTranslate(centerX.toFloat(), centerY.toFloat())// 步骤3 屏幕的中心点

            canvas.drawBitmap(scan1, matrix, null)
            canvas.drawBitmap(scan2, matrix2, null)
        }

    }
}