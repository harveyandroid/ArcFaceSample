package com.harvey.arcface.model

import android.graphics.Rect

import com.arcsoft.face.FaceFeature
import com.arcsoft.face.FaceInfo

//单个查询人脸信息特征
class FeatureModel {
    // 摄像头的尺寸
    var cameraWidth: Int = 0
    var cameraHeight: Int = 0
    // 人脸矩形框
    var rect: Rect
    // 人脸角度
    var degree: Int = 0
    //人脸ID
    var faceId = -1
    //人脸特征
    var featureData: ByteArray

    val faceFeature: FaceFeature
        get() = FaceFeature(featureData)

    val faceInfo: FaceInfo
        get() = FaceInfo(rect, degree)

    /**
     * 将图像中需要截取的Rect向外扩张一倍，若扩张一倍会溢出，则扩张到边界，若Rect已溢出，则收缩到边界
     *
     * @return
     */
    //1.原rect边界已溢出宽高的情况
    //2.原rect边界未溢出宽高的情况
    //若以此padding扩张rect会溢出，取最大padding为四个边距的最小值
    val faceMoreRect: Rect
        get() {
            var maxOverFlow = 0
            var tempOverFlow = 0
            if (rect.left < 0) {
                maxOverFlow = -rect.left
            }
            if (rect.top < 0) {
                tempOverFlow = -rect.top
                if (tempOverFlow > maxOverFlow) {
                    maxOverFlow = tempOverFlow
                }
            }
            if (rect.right > cameraWidth) {
                tempOverFlow = rect.right - cameraWidth
                if (tempOverFlow > maxOverFlow) {
                    maxOverFlow = tempOverFlow
                }
            }
            if (rect.bottom > cameraHeight) {
                tempOverFlow = rect.bottom - cameraHeight
                if (tempOverFlow > maxOverFlow) {
                    maxOverFlow = tempOverFlow
                }
            }
            if (maxOverFlow != 0) {
                return Rect(rect.left + maxOverFlow,
                        rect.top + maxOverFlow,
                        rect.right - maxOverFlow,
                        rect.bottom - maxOverFlow)
            }
            var padding = rect.height() / 2
            if (!(rect.left - padding > 0
                            && rect.right + padding < cameraWidth && rect.top - padding > 0
                            && rect.bottom + padding < cameraHeight)) {
                padding = Math.min(Math.min(Math.min(rect.left, cameraWidth - rect.right), cameraHeight - rect.bottom), rect.top)
            }
            return Rect(rect.left - padding,
                    rect.top - padding,
                    rect.right + padding,
                    rect.bottom + padding)
        }


    val orientation: Int
        get() {
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

    constructor(cameraWidth: Int, cameraHeight: Int, rect: Rect, degree: Int, faceId: Int, featureData: ByteArray) {
        this.cameraWidth = cameraWidth
        this.cameraHeight = cameraHeight
        this.rect = rect
        this.degree = degree
        this.faceId = faceId
        this.featureData = featureData
    }

    constructor(cameraWidth: Int, cameraHeight: Int, faceInfo: FaceInfo, faceFeature: FaceFeature) {
        this.cameraWidth = cameraWidth
        this.cameraHeight = cameraHeight
        this.rect = faceInfo.rect
        this.degree = faceInfo.orient
        this.faceId = faceInfo.faceId
        this.featureData = faceFeature.featureData
    }

    constructor(findModel: FeatureModel) {
        this.cameraWidth = findModel.cameraWidth
        this.cameraHeight = findModel.cameraHeight
        this.rect = Rect(findModel.rect)
        this.degree = findModel.degree
        this.faceId = findModel.faceId
        this.featureData = findModel.featureData.clone()
    }

    fun clone(): FeatureModel {
        return FeatureModel(this)
    }

    /**
     * 调整人脸框用来绘制(针对手机)
     *
     * @param displayOrientation 显示的角度
     * @param frontCamera        是否前置
     * @param canvasWidth
     * @param canvasHeight
     * @return 调整后的需要被绘制到View上的rect
     */
    fun adjustRect(displayOrientation: Int, frontCamera: Boolean, canvasWidth: Int, canvasHeight: Int): Rect {
        val target = Rect(rect)
        val horizontalRatio: Float
        val verticalRatio: Float
        if (displayOrientation % 180 == 0) {
            horizontalRatio = canvasWidth.toFloat() / cameraWidth.toFloat()
            verticalRatio = canvasHeight.toFloat() / cameraHeight.toFloat()
        } else {
            horizontalRatio = canvasHeight.toFloat() / cameraWidth.toFloat()
            verticalRatio = canvasWidth.toFloat() / cameraHeight.toFloat()
        }
        target.left *= horizontalRatio.toInt()
        target.right *= horizontalRatio.toInt()
        target.top *= verticalRatio.toInt()
        target.bottom *= verticalRatio.toInt()

        val newRect = Rect()
        when (displayOrientation) {
            0 -> {
                if (frontCamera) {
                    newRect.left = canvasWidth - target.right
                    newRect.right = canvasWidth - target.left
                } else {
                    newRect.left = target.left
                    newRect.right = target.right
                }
                newRect.top = target.top
                newRect.bottom = target.bottom
            }
            90 -> {
                newRect.right = canvasWidth - target.top
                newRect.left = canvasWidth - target.bottom
                if (frontCamera) {
                    newRect.top = canvasHeight - target.right
                    newRect.bottom = canvasHeight - target.left
                } else {
                    newRect.top = target.left
                    newRect.bottom = target.right
                }
            }
            180 -> {
                newRect.top = canvasHeight - target.bottom
                newRect.bottom = canvasHeight - target.top
                if (frontCamera) {
                    newRect.left = target.left
                    newRect.right = target.right
                } else {
                    newRect.left = canvasWidth - target.right
                    newRect.right = canvasWidth - target.left
                }
            }
            270 -> {
                newRect.left = target.top
                newRect.right = target.bottom
                if (frontCamera) {
                    newRect.top = target.left
                    newRect.bottom = target.right
                } else {
                    newRect.top = canvasHeight - target.right
                    newRect.bottom = canvasHeight - target.left
                }
            }
            else -> {
            }
        }
        return newRect
    }

    /**
     * 实际展示 宽高相反
     *
     * @param mappedWidth
     * @param mappedHeight
     * @return
     */
    fun getMappedFaceRect(mappedWidth: Int, mappedHeight: Int): Rect {
        val left = rect.right * mappedWidth / cameraWidth
        val top = rect.top * mappedHeight / cameraHeight
        val right = rect.left * mappedWidth / cameraWidth
        val bottom = rect.bottom * mappedHeight / cameraHeight

        return Rect(left, top, right, bottom)
    }

}
