package com.harvey.arcface.model

import android.graphics.Rect
import java.util.*

/**
 * Created by hanhui on 2018/6/1 0001 13:55
 */
data class FaceFindCameraModel(var faceFindModels: List<FaceFindModel>,
                               var cameraData: ByteArray) {

    fun clone(): FaceFindCameraModel {
        for (model in faceFindModels) model.faceRect = Rect(model.faceRect)
        return copy(faceFindModels = faceFindModels, cameraData = cameraData.copyOf())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FaceFindCameraModel) return false

        if (faceFindModels != other.faceFindModels) return false
        if (!Arrays.equals(cameraData, other.cameraData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = faceFindModels.hashCode()
        result = 31 * result + Arrays.hashCode(cameraData)
        return result
    }

}