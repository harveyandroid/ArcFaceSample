package com.harvey.arcface.model;

import com.arcsoft.face.FaceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 摄像头一帧包含的所有人脸信息
 * Created by hanhui on 2019/12/12 0012 16:43
 */
public class FaceCameraModel extends CameraModel {
    List<FaceInfo> faceInfos;

    public FaceCameraModel(List<FaceInfo> data, byte[] nv21, int width, int height) {
        super(nv21, width, height);
        this.faceInfos = data;
    }

    public FaceCameraModel(FaceCameraModel model) {
        super(model);
        this.faceInfos = new ArrayList<>();
        for (FaceInfo faceInfo : model.faceInfos) {
            faceInfos.add(faceInfo.clone());
        }
    }

    public List<FaceInfo> getFaceInfo() {
        if (faceInfos == null) {
            return new ArrayList<>();
        }
        return faceInfos;
    }

    public FaceCameraModel clone() {
        return new FaceCameraModel(this);
    }
}
