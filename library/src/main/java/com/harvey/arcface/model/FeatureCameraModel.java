package com.harvey.arcface.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanhui on 2019/12/12 0012 16:50
 */
public class FeatureCameraModel extends CameraModel {
    List<FeatureModel> featureModels;

    public FeatureCameraModel(List<FeatureModel> data, byte[] nv21, int width, int height) {
        super(nv21, width, height);
        this.featureModels = data;
    }

    public FeatureCameraModel(List<FeatureModel> data, CameraModel cameraModel) {
        super(cameraModel);
        this.featureModels = data;
    }

    public FeatureCameraModel(FeatureCameraModel model) {
        super(model);
        this.featureModels = new ArrayList<>();
        for (FeatureModel findModel : model.featureModels) {
            featureModels.add(findModel.clone());
        }
    }

    public List<FeatureModel> getFeatureModels() {
        if (featureModels == null) {
            return new ArrayList<>();
        }
        return featureModels;
    }

    public void setFeatureModels(List<FeatureModel> featureModels) {
        this.featureModels = featureModels;
    }

    public FeatureCameraModel clone() {
        return new FeatureCameraModel(this);
    }
}
