package com.harvey.arcface.view

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import java.io.IOException

/**
 * Created by hanhui on 2018/6/1 0001 11:10
 */
class SurfaceViewCamera(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
    private val surfaceHolder: SurfaceHolder? = holder
    private var mCamera: Camera? = null
    private var previewCallback: Camera.PreviewCallback? = null

    init {
        surfaceHolder!!.addCallback(this)
    }

    fun setCameraCallBack(previewCallback: Camera.PreviewCallback) {
        this.previewCallback = previewCallback
    }

    fun switchCamera() {
        if (Camera.getNumberOfCameras() == 1 || surfaceHolder == null)
            return
        currentCameraId = if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
            Camera.CameraInfo.CAMERA_FACING_BACK
        else
            Camera.CameraInfo.CAMERA_FACING_FRONT
        closeCamera()
        openCamera()
    }

    private fun openCamera() {
        try {
            mCamera = Camera.open(currentCameraId)
            mCamera!!.setPreviewDisplay(surfaceHolder)
            mCamera!!.setPreviewCallback(previewCallback)
            // 参数设定
            val cameraParams = mCamera!!.parameters
            for (size in cameraParams.supportedPreviewSizes) {
                Log.e("SurfaceViewCamera", "SIZE:" + size.width + "x" + size.height)
            }
            for (format in cameraParams.supportedPreviewFormats) {
                Log.e("SurfaceViewCamera", "FORMAT:" + format!!)
            }
            cameraParams.pictureFormat = ImageFormat.JPEG
            cameraParams.previewFormat = ImageFormat.NV21
            setCameraDisplayOrientation()
            mCamera!!.parameters = cameraParams
            mCamera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
            closeCamera()
        }

    }

    private fun closeCamera() {
        if (null != mCamera) {
            mCamera!!.setPreviewCallback(null)
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
        }
    }

    private fun setCameraDisplayOrientation() {
        try {
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(currentCameraId, cameraInfo)
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val rotation = wm.defaultDisplay.rotation
            var degrees = 0
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }

            var result: Int
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (cameraInfo.orientation + degrees) % 360
                result = (360 - result) % 360 // compensate the mirror
            } else { // back-facing
                result = (cameraInfo.orientation - degrees + 360) % 360
            }
            mCamera!!.setDisplayOrientation(result)
        } catch (ex: Exception) {

        }

    }

    override fun surfaceCreated(mSurfaceHolder: SurfaceHolder) {
        openCamera()
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {}

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        closeCamera()
    }
}
