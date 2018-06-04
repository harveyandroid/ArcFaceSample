package com.harvey.arcface.model

import android.graphics.Rect

/**
 * Created by hanhui on 2018/6/1 0001 09:52
 */
data class FaceFindModel(var cameraWidth: Int = 0, var cameraHeight: Int = 0, var faceRect: Rect = Rect(), var degree: Int = 0) {


    // 扩大人脸矩形框范围
    fun getFaceMoreRect(): Rect {
        val more_width = faceRect.width() / 3
        val more_height = faceRect.height() / 2
        val top = if (faceRect.top - more_height >= 0) faceRect.top - more_height else 0
        val left = if (faceRect.left - more_width >= 0) faceRect.left - more_width else 0
        val bottom = if (faceRect.bottom + more_height <= cameraHeight) faceRect.bottom + more_height else cameraHeight
        val right = if (faceRect.right + more_width <= cameraWidth) faceRect.right + more_width else cameraWidth
        return Rect(left, top, right, bottom)
    }

    fun getOrientation(): Int {
        return when (degree) {
            1 -> 0
            2 -> 90
            3 -> 270
            4 -> 180
            5 -> 30
            6 -> 60
            7 -> 120
            8 -> 150
            9 -> 210
            10 -> 240
            11 -> 300
            12 -> 330
            else -> 0
        }
    }

    // 映射
    // 实际展示 宽高相反
    fun getMappedFaceRect(mappedWidth: Int, mappedHeight: Int): Rect {
        // 根据摄像头进行转化(垂直视角)
        val top = faceRect.right * mappedWidth / cameraWidth
        val right = mappedHeight - faceRect.top * mappedHeight / cameraHeight
        val bottom = faceRect.left * mappedWidth / cameraWidth
        val left = mappedHeight - faceRect.bottom * mappedHeight / cameraHeight
        return Rect(left, top, right, bottom)
    }
}