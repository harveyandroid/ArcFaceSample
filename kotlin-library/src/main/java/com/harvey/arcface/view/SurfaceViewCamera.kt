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
import java.util.*

/**
 * Created by harvey on 2018/5/30 0030 17:22
 */

class SurfaceViewCamera(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    var cameraWidth = 1280
    var cameraHeight = 720
    private var currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
    private val surfaceHolder: SurfaceHolder = holder
    private var mCamera: Camera? = null
    private var previewCallback: Camera.PreviewCallback? = null
    private var displayOrientation = 0

    val isFront: Boolean
        get() = currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT

    // compensate the mirror
    // back-facing
    val cameraDisplayOrientation: Int
        get() {
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
                result = (360 - result) % 360
            } else {
                result = (cameraInfo.orientation - degrees + 360) % 360
            }
            Log.i("SurfaceViewCamera", String.format("Camera Display Orientation :%d ", result))
            return result
        }

    init {
        surfaceHolder.addCallback(this)
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

    fun openCamera() {
        try {
            mCamera = Camera.open(currentCameraId)
            mCamera!!.setPreviewDisplay(surfaceHolder)
            mCamera!!.setPreviewCallback(previewCallback)
            // 参数设定
            val cameraParams = mCamera!!.parameters
            cameraParams.pictureFormat = ImageFormat.JPEG
            cameraParams.previewFormat = ImageFormat.NV21
            displayOrientation = cameraDisplayOrientation
            mCamera!!.setDisplayOrientation(displayOrientation)
            val bestPreviewSize = getBestPreviewSize(cameraParams, cameraWidth, cameraHeight)
            cameraWidth = bestPreviewSize.width
            cameraHeight = bestPreviewSize.height
            Log.i("SurfaceViewCamera", "设置摄像头分辨率：$cameraWidth*$cameraHeight")
            cameraParams.setPreviewSize(cameraWidth, cameraHeight)
            //对焦模式设置
            val supportedFocusModes = cameraParams.supportedFocusModes
            if (supportedFocusModes != null && supportedFocusModes.size > 0) {
                when {
                    supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) ->
                        cameraParams.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                    supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) ->
                        cameraParams.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                    supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO) ->
                        cameraParams.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                }
            }
            mCamera!!.parameters = cameraParams
            mCamera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
            closeCamera()
        }

    }

    /**
     * 通过传入的宽高算出最接近于宽高值的相机大小
     */
    private fun getBestPreviewSize(camPara: Camera.Parameters, width: Int, height: Int): Camera.Size {
        val allSupportedSize = camPara.supportedPreviewSizes
        val widthLargerSize = ArrayList<Camera.Size>()
        for (size in allSupportedSize) {
            Log.e("SurfaceViewCamera", "SIZE:" + size.width + "x" + size.height)
            if (size.width > size.height) {
                widthLargerSize.add(size)
            }
        }
        widthLargerSize.sortWith(Comparator { lhs, rhs ->
            val off_one = Math.abs(lhs.width * lhs.height - width * height)
            val off_two = Math.abs(rhs.width * rhs.height - width * height)
            off_one - off_two
        })
        for (format in camPara.supportedPreviewFormats) {
            Log.e("SurfaceViewCamera", "FORMAT:" + format!!)
        }
        return widthLargerSize[0]
    }


    fun closeCamera() {
        if (null != mCamera) {
            mCamera!!.setPreviewCallback(null)
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
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
