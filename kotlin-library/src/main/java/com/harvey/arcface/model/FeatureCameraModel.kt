package com.harvey.arcface.model

/**
 * Created by hanhui on 2019/12/12 0012 16:50
 */
class FeatureCameraModel : CameraModel {
    private var featureModels: MutableList<FeatureModel>? = null

    constructor(data: MutableList<FeatureModel>, nv21: ByteArray, width: Int, height: Int) : super(nv21, width, height) {
        this.featureModels = data
    }

    constructor(data: MutableList<FeatureModel>, cameraModel: CameraModel) : super(cameraModel) {
        this.featureModels = data
    }

    constructor(model: FeatureCameraModel) : super(model) {
        this.featureModels = mutableListOf()
        for (findModel in model.featureModels!!) {
            featureModels!!.add(findModel.clone())
        }
    }

    fun getFeatureModels(): List<FeatureModel> {
        return featureModels ?: mutableListOf()
    }

    fun setFeatureModels(featureModels: MutableList<FeatureModel>) {
        this.featureModels = featureModels
    }

    fun clone(): FeatureCameraModel {
        return FeatureCameraModel(this)
    }
}
