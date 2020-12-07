package com.harvey.arcface;

import com.arcsoft.face.FaceInfo;
import com.harvey.arcface.model.FaceCameraModel;
import com.harvey.arcface.model.FeatureModel;
import com.harvey.arcface.model.OneFaceCameraModel;
import com.harvey.arcface.model.OneFeatureCameraModel;
import com.harvey.arcface.model.ProcessState;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 人脸匹配线程(匹配的是有效的人脸数据)
 * Created by hanhui on 2020/12/4 14:05
 */
public abstract class FaceMatchThread extends Thread {
    private AIFace mAiFace;
    private volatile boolean setToStop = false;
    private BlockingQueue<OneFaceCameraModel> queue;

    public FaceMatchThread(AIFace aiFace) {
        mAiFace = aiFace;
        queue = new ArrayBlockingQueue(20);
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
                    boolean result = queue.offer(new OneFaceCameraModel(faceInfo, model));
                    if (result) {
                        mAiFace.replaceFaceProcessState(faceInfo.getFaceId(), ProcessState.FR_WAITING);
                    }
                }
            }
        }
    }

    protected void handleMatch(OneFeatureCameraModel featureModel) {
        handleMatch(featureModel.getFeatureModel());
    }

    protected void handleMatch(FeatureModel featureModel) {

    }

    public void finish() {
        setToStop = true;
    }

    @Override
    public void run() {
        while (!setToStop) {
            try {
                OneFaceCameraModel model = queue.take();
                if (mAiFace.isFRWaitingState(model.getFaceInfo())) {
                    OneFeatureCameraModel featureModel = mAiFace.findSingleFaceFeatureCamera(model);
                    if (featureModel != null) {
                        handleMatch(featureModel);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
