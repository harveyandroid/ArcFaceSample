package com.harvey.arcface.model;

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;

//单个查询人脸信息特征
public class FeatureModel {
    FaceInfo faceInfo;
    FaceFeature faceFeature;

    public FeatureModel(FaceInfo faceInfo, FaceFeature faceFeature) {
        this.faceInfo = faceInfo;
        this.faceFeature = faceFeature;
    }

    public FeatureModel(FeatureModel findModel) {
        this.faceInfo = findModel.faceInfo.clone();
        this.faceFeature = findModel.faceFeature.clone();
    }

    public FaceInfo getFaceInfo() {
        return faceInfo;
    }

    public void setFaceInfo(FaceInfo faceInfo) {
        this.faceInfo = faceInfo;
    }

    public FaceFeature getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(FaceFeature faceFeature) {
        this.faceFeature = faceFeature;
    }

    public FeatureModel clone() {
        return new FeatureModel(this);
    }

}
