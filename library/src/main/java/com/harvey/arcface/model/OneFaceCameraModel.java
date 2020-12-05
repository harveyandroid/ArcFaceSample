package com.harvey.arcface.model;

import com.arcsoft.face.FaceInfo;

/**
 * 一个人脸信息包含摄像头数据
 * Created by hanhui on 2019/12/12 0012 16:43
 */
public class OneFaceCameraModel extends CameraModel {
    FaceInfo faceInfo;

    public OneFaceCameraModel(FaceInfo data, byte[] nv21, int width, int height) {
        super(nv21, width, height);
        this.faceInfo = data;
    }

    public OneFaceCameraModel(FaceInfo data, CameraModel cameraModel) {
        super(cameraModel);
        this.faceInfo = data;
    }

    public OneFaceCameraModel(OneFaceCameraModel model) {
        super(model);
        this.faceInfo = model.faceInfo.clone();
    }

    public FaceInfo getFaceInfo() {
        return faceInfo;
    }

    public OneFaceCameraModel clone() {
        return new OneFaceCameraModel(this);
    }
}
