package com.harvey.arcface.model

/**
 * Created by hanhui on 2019/12/12 0012 16:33
 */
open class CameraModel {
    var nv21: ByteArray
    var width: Int = 0
    var height: Int = 0

    constructor(nv21: ByteArray, width: Int, height: Int) {
        this.nv21 = nv21
        this.width = width
        this.height = height
    }

    constructor(model: CameraModel) {
        this.nv21 = model.nv21.clone()
        this.width = model.width
        this.height = model.height
    }
}
