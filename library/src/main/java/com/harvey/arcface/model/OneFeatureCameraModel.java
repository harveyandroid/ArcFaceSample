package com.harvey.arcface.model;

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;

/**
 * 一个人脸特征信息 包含一帧数据
 * Created by hanhui on 2019/12/12 0012 16:50
 */
public class OneFeatureCameraModel extends CameraModel {
    FeatureModel featureModel;

    public OneFeatureCameraModel(FeatureModel data, byte[] nv21, int width, int height) {
        super(nv21, width, height);
        this.featureModel = data;
    }

    public OneFeatureCameraModel(FeatureModel data, CameraModel cameraModel) {
        super(cameraModel);
        this.featureModel = data;
    }

    public OneFeatureCameraModel(FaceInfo faceInfo, FaceFeature faceFeature, CameraModel cameraModel) {
        super(cameraModel);
        this.featureModel = new FeatureModel(faceInfo, faceFeature);
    }

    public OneFeatureCameraModel(OneFeatureCameraModel model) {
        super(model);
        this.featureModel = model.featureModel.clone();
    }

    public FeatureModel getFeatureModel() {
        return featureModel;
    }

    public OneFeatureCameraModel clone() {
        return new OneFeatureCameraModel(this);
    }
}
