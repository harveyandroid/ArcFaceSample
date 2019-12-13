package com.harvey.arcface.model;

import com.harvey.arcface.utils.FaceConfig;

/**
 * Created by hanhui on 2019/12/12 0012 17:07
 */
public enum FaceAction {
    DETECT(FaceConfig.ASF_FACE_DETECT),

    DEEP_DETECT(FaceConfig.ASF_FACE_DETECT
            | FaceConfig.ASF_AGE
            | FaceConfig.ASF_GENDER
            | FaceConfig.ASF_FACE3DANGLE
            | FaceConfig.ASF_LIVENESS),

    FEATURE_EXTRACT(FaceConfig.ASF_FACE_DETECT
            | FaceConfig.ASF_FACE_RECOGNITION),

    MATCH(FaceConfig.ASF_FACE_DETECT
            | FaceConfig.ASF_FACE_RECOGNITION),

    ALL(FaceConfig.ASF_FACE_DETECT
            | FaceConfig.ASF_FACE_RECOGNITION
            | FaceConfig.ASF_AGE
            | FaceConfig.ASF_GENDER
            | FaceConfig.ASF_FACE3DANGLE
            | FaceConfig.ASF_LIVENESS);

    //需要启用的功能组合
    public int combinedMask;

    FaceAction(int mask) {
        this.combinedMask = mask;
    }
}