package com.harvey.arcface;

import androidx.annotation.MainThread;
import android.util.Log;

import com.harvey.arcface.moodel.FaceFindModel;
import com.harvey.arcface.utils.MainHandler;

import java.util.List;

/**
 * Created by harvey on 2018/2/7 0007 09:45
 */

public class DetectFaceAction implements Runnable {
    final String TAG = "DetectFace";
    byte[] frameBytes;
    volatile boolean setToStop = false;
    volatile boolean isDetectingFace = false;
    int cameraWidth;
    int cameraHeight;
    OnFaceDetectListener faceDetectListener;

    public DetectFaceAction() {
    }

    public void destroy() {
        setToStop = true;
        frameBytes = null;
        faceDetectListener = null;
        isDetectingFace = false;
    }

    public void setData(byte[] bytes, int width, int height) {
        this.frameBytes = bytes;
        this.cameraWidth = width;
        this.cameraHeight = height;
    }

    public void setOnFaceDetectListener(OnFaceDetectListener l) {
        this.faceDetectListener = l;
    }

    @Override
    public void run() {
        while (!setToStop) {
            if (frameBytes != null && !isDetectingFace) {
                isDetectingFace = true;
                final List<FaceFindModel> faceFindModels = FaceManager.getInstance().extractAllFaceFeature(frameBytes, cameraWidth,
                        cameraHeight);
                callOnFaceDetect(faceFindModels);
                isDetectingFace = false;
            }
        }
    }

    void callOnFaceDetect(final List<FaceFindModel> data) {
        if (data.size() > 0)
            Log.d(TAG, "检测人脸数量-->" + data.size());
        MainHandler.run(new Runnable() {
            @Override
            public void run() {
                if (faceDetectListener != null) {
                    faceDetectListener.onFaceDetect(data, frameBytes);
                }
            }
        });

    }

    public interface OnFaceDetectListener {
        @MainThread
        void onFaceDetect(List<FaceFindModel> faceFindModels, byte[] frameBytes);
    }
}
