package com.harvey.arcface.model

/**
 * Created by hanhui on 2018/6/1 0001 13:55
 */
data class FaceFindCameraModel(var faceFindModels: List<FaceFindModel>,
                               var cameraData: ByteArray) {
    fun clone(): FaceFindCameraModel = copy(faceFindModels = faceFindModels, cameraData = cameraData)

}