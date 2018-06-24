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

/**
 * Created by harvey on 2018/5/30 0030 17:22
 */

public class SurfaceViewCamera extends SurfaceView implements SurfaceHolder.Callback {

	int currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
	private SurfaceHolder surfaceHolder;
	private Camera mCamera;
	private Camera.PreviewCallback previewCallback;

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

	public void openCamera() {
		try {
			mCamera = Camera.open(currentCameraId);
			mCamera.setPreviewDisplay(surfaceHolder);
			mCamera.setPreviewCallback(previewCallback);
			// 参数设定
			Camera.Parameters cameraParams = mCamera.getParameters();
			for (Camera.Size size : cameraParams.getSupportedPreviewSizes()) {
				Log.e("SurfaceViewCamera", "SIZE:" + size.width + "x" + size.height);
			}
			for (Integer format : cameraParams.getSupportedPreviewFormats()) {
				Log.e("SurfaceViewCamera", "FORMAT:" + format);
			}
			cameraParams.setPictureFormat(ImageFormat.JPEG);
			cameraParams.setPreviewFormat(ImageFormat.NV21);
			setCameraDisplayOrientation();
			mCamera.setParameters(cameraParams);
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
			closeCamera();
		}
	}
	public void closeCamera() {
		if (null != mCamera) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	public void setCameraDisplayOrientation() {
		try {
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			Camera.getCameraInfo(currentCameraId, cameraInfo);
			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			int rotation = wm.getDefaultDisplay().getRotation();
			int degrees = 0;
			switch (rotation) {
				case Surface.ROTATION_0 :
					degrees = 0;
					break;
				case Surface.ROTATION_90 :
					degrees = 90;
					break;
				case Surface.ROTATION_180 :
					degrees = 180;
					break;
				case Surface.ROTATION_270 :
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
			mCamera.setDisplayOrientation(result);
		} catch (Exception ex) {

		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder mSurfaceHolder) {
		openCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		closeCamera();
	}
}
