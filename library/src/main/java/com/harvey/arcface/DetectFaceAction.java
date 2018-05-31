package com.harvey.arcface;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.util.Log;

import com.harvey.arcface.moodel.FaceFindModel;
import com.harvey.arcface.utils.MainHandler;

import java.util.List;

/**
 * Created by hanhui on 2018/2/7 0007 09:45
 */

public class DetectFaceAction implements Runnable {
	final String TAG = "DetectFace";
	Handler mDetectFaceHandler;
	Looper mDetectFaceLooper;
	byte[] frameBytes;
	volatile boolean isDetectingFace = false;// 正在检测人脸
	volatile boolean isInit = false;
	int cameraWidth;
	int cameraHeight;
	OnFaceDetectListener faceDetectListener;

	public void init() {
		HandlerThread detectFaceThread = new HandlerThread("DetectFace");
		detectFaceThread.start();
		mDetectFaceLooper = detectFaceThread.getLooper();
		mDetectFaceHandler = new Handler(mDetectFaceLooper);
		isInit = true;
	}

	public void destroy() {
		if (isInit) {
			mDetectFaceLooper.quit();
		}
		frameBytes = null;
		isDetectingFace = false;
		faceDetectListener = null;
		isInit = false;
	}

	public void detectFace(byte[] bytes, int width, int height) {
		if (!isInit)
			throw new NullPointerException("没有初始化人脸检测!");
		if (isDetectingFace)
			return;
		this.frameBytes = bytes;
		this.cameraWidth = width;
		this.cameraHeight = height;
		mDetectFaceHandler.post(this);
	}

	public void setOnFaceDetectListener(OnFaceDetectListener l) {
		this.faceDetectListener = l;
	}

	@Override
	public void run() {
		if (isDetectingFace)
			return;
		if (frameBytes == null)
			return;
		isDetectingFace = true;
		final List<FaceFindModel> faceFindModels = ArcFaceEngine.getInstance().detectFace(frameBytes, cameraWidth,
				cameraHeight);
		callOnFaceDetect(faceFindModels);
		isDetectingFace = false;
	}

	void callOnFaceDetect(final List<FaceFindModel> data) {
		if (data.size() > 0)
			Log.d(TAG, "检测人脸数量-->" + data.size());
		if (faceDetectListener != null) {
			MainHandler.run(new Runnable() {
				@Override
				public void run() {
					faceDetectListener.onFaceDetect(data, frameBytes);
				}
			});
		}
	}

	public interface OnFaceDetectListener {
		@MainThread
		void onFaceDetect(List<FaceFindModel> faceFindModels, byte[] frameBytes);
	}
}
