package com.harvey.arcfacedamo.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.arcsoft.face.FaceInfo;
import com.harvey.arcface.AIFace;
import com.harvey.arcface.model.FaceCameraModel;
import com.harvey.arcface.model.FeatureModel;
import com.harvey.arcface.model.OneFaceCameraModel;
import com.harvey.arcface.model.ProcessState;
import com.harvey.arcfacedamo.ui.FaceScanActivity;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by hanhui on 2020/12/4 14:05
 */
public class FaceMatchThread extends Thread {
    private AIFace mAiFace;
    private Handler mHandler;
    private volatile boolean setToStop = false;
    private FaceMatchHelper matchHelper;
    private BlockingQueue<OneFaceCameraModel> queue;

    public FaceMatchThread(Context context, AIFace aiFace, Handler handler) {
        mAiFace = aiFace;
        mHandler = handler;
        queue = new ArrayBlockingQueue(20);
        matchHelper = new FaceMatchHelper(context, aiFace);
    }

    /**
     * 加入队列
     * 某个faceId对应的人脸已经在等待或者在做FR的时候，
     * 在后面检测到的该faceId对应人脸将不再做FR，
     * 一直到FR检测结果出来以后，再做处理。
     *
     * @param model
     */
    public void offerFaceCameraModel(FaceCameraModel model) {
        if (model != null) {
            for (FaceInfo faceInfo : model.getFaceInfo()) {
                if (mAiFace.isFaceDetectState(faceInfo)) {
                    queue.offer(new OneFaceCameraModel(faceInfo, model));
                }
            }
        }
    }

    public void finish() {
        setToStop = true;
    }

    @Override
    public void run() {
        while (!setToStop) {
            try {
                OneFaceCameraModel model = queue.take();
                int state = mAiFace.getFaceProcessState(model.getFaceInfo().getFaceId());
                if (state == ProcessState.FD) {
                    FeatureModel featureModel = mAiFace.findSingleFaceFeature(model);
                    if (featureModel != null) {
                        FaceFindMatchModel faceFindMatchModel = matchHelper.matchFace(featureModel.getFaceFeature());
                        if (faceFindMatchModel != null) {
                            Message message = mHandler.obtainMessage(FaceScanActivity.ADD_MATCH_FACE_WHAT, faceFindMatchModel);
                            mHandler.sendMessage(message);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
