package com.harvey.arcface.model

import com.harvey.arcface.utils.FaceConfig

/**
 * Created by hanhui on 2019/12/12 0012 17:07
 */
enum class FaceAction(//需要启用的功能组合
        var combinedMask: Int) {
    DETECT(FaceConfig.ASF_FACE_DETECT),

    DEEP_DETECT(FaceConfig.ASF_FACE_DETECT
            or FaceConfig.ASF_AGE
            or FaceConfig.ASF_GENDER
            or FaceConfig.ASF_FACE3DANGLE
            or FaceConfig.ASF_LIVENESS),

    FEATURE_EXTRACT(FaceConfig.ASF_FACE_DETECT or FaceConfig.ASF_FACE_RECOGNITION),

    MATCH(FaceConfig.ASF_FACE_DETECT or FaceConfig.ASF_FACE_RECOGNITION),

    ALL(FaceConfig.ASF_FACE_DETECT
            or FaceConfig.ASF_FACE_RECOGNITION
            or FaceConfig.ASF_AGE
            or FaceConfig.ASF_GENDER
            or FaceConfig.ASF_FACE3DANGLE
            or FaceConfig.ASF_LIVENESS)
}