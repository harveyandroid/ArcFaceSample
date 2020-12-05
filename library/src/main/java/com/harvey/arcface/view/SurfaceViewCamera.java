package com.harvey.arcface.view;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by harvey on 2018/5/30 0030 17:22
 */

public class SurfaceViewCamera extends SurfaceView implements SurfaceHolder.Callback {
    public int cameraWidth = 1280;
    public int cameraHeight = 720;
    int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    private Camera.PreviewCallback previewCallback;
    private int displayOrientation = 0;

    public SurfaceViewCamera(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public void setCameraCallBack(Camera.PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
    }

    public void switchCamera() {
        if (Camera.getNumberOfCameras() == 1 || surfaceHolder == null)
            return;
        currentCameraId = (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                ? Camera.CameraInfo.CAMERA_FACING_BACK
                : Camera.CameraInfo.CAMERA_FACING_FRONT;
        closeCamera();
        openCamera();
    }

    public boolean isFront() {
        return currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    public void openCamera() {
        try {
            mCamera = Camera.open(currentCameraId);
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.setPreviewCallback(previewCallback);
            // 参数设定
            Camera.Parameters cameraParams = mCamera.getParameters();
            cameraParams.setPictureFormat(ImageFormat.JPEG);
            cameraParams.setPreviewFormat(ImageFormat.NV21);
            displayOrientation = getCameraDisplayOrientation();
            mCamera.setDisplayOrientation(displayOrientation);
            Camera.Size bestPreviewSize = getBestPreviewSize(cameraParams, cameraWidth, cameraHeight);
            cameraWidth = bestPreviewSize.width;
            cameraHeight = bestPreviewSize.height;
            Log.i("SurfaceViewCamera", "设置摄像头分辨率：" + cameraWidth + "*" + cameraHeight);
            cameraParams.setPreviewSize(cameraWidth, cameraHeight);
            //对焦模式设置
            List<String> supportedFocusModes = cameraParams.getSupportedFocusModes();
            if (supportedFocusModes != null && supportedFocusModes.size() > 0) {
                if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            }
            mCamera.setParameters(cameraParams);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            closeCamera();
        }
    }

    /**
     * 通过传入的宽高算出最接近于宽高值的相机大小
     */
    private Camera.Size getBestPreviewSize(Camera.Parameters camPara, final int width, final int height) {
        List<Camera.Size> allSupportedSize = camPara.getSupportedPreviewSizes();
        List<Camera.Size> widthLargerSize = new ArrayList<>();
        for (Camera.Size size : allSupportedSize) {
            Log.e("SurfaceViewCamera", "SIZE:" + size.width + "x" + size.height);
            if (size.width > size.height) {
                widthLargerSize.add(size);
            }
        }
        Collections.sort(widthLargerSize, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                int off_one = Math.abs(lhs.width * lhs.height - width * height);
                int off_two = Math.abs(rhs.width * rhs.height - width * height);
                return off_one - off_two;
            }
        });
        for (Integer format : camPara.getSupportedPreviewFormats()) {
            Log.e("SurfaceViewCamera", "FORMAT:" + format);
        }
        return widthLargerSize.get(0);
    }


    public void closeCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public int getCameraDisplayOrientation() {
        if (true) {
            return 270;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraId, cameraInfo);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        Log.i("SurfaceViewCamera", String.format("Camera Display Orientation :%d ", result));
        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder mSurfaceHolder) {
        openCamera();
        Log.i("SurfaceViewCamera", "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.i("SurfaceViewCamera", String.format("surfaceChanged :%d,%d,%d ", i, i1, i2));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        closeCamera();
        Log.i("SurfaceViewCamera", "surfaceDestroyed");
    }
}
