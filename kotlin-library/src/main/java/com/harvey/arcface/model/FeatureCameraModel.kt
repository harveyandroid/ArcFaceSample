package com.harvey.arcface.model

/**
 * Created by hanhui on 2019/12/12 0012 16:50
 */
class FeatureCameraModel(val featureModels: List<FeatureModel>, nv21: ByteArray, width: Int, height: Int) :
        CameraModel(nv21, width, height)