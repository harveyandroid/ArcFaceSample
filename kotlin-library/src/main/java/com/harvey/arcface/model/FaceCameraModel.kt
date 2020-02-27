package com.harvey.arcface.model

import com.arcsoft.face.FaceInfo
import java.util.*

/**
 * 一个Camera摄像头包含的所有人脸信息
 * Created by hanhui on 2019/12/12 0012 16:43
 */
class FaceCameraModel : CameraModel {
    private var faceInfos: MutableList<FaceInfo>? = null

    val faceInfo: MutableList<FaceInfo>
        get() = faceInfos ?: ArrayList()

    constructor(data: MutableList<FaceInfo>, nv21: ByteArray, width: Int, height: Int) : super(nv21, width, height) {
        this.faceInfos = data
    }

    constructor(model: FaceCameraModel) : super(model) {
        this.faceInfos = ArrayList()
        for (faceInfo in model.faceInfos!!) {
            faceInfos!!.add(faceInfo.clone())
        }
    }

    fun clone(): FaceCameraModel {
        return FaceCameraModel(this)
    }
}
