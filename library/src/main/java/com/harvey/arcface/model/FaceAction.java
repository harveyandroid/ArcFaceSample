package com.harvey.arcface.model;


import com.arcsoft.face.FaceEngine;

/**
 * Created by hanhui on 2019/12/12 0012 17:07
 */
public enum FaceAction {
    DETECT(FaceEngine.ASF_FACE_DETECT),

    FACE_PROPERTY(FaceEngine.ASF_AGE
            | FaceEngine.ASF_GENDER
            | FaceEngine.ASF_FACE3DANGLE
            | FaceEngine.ASF_LIVENESS),

    FACE_PROPERTY_WITH_IR(FaceEngine.ASF_AGE
            | FaceEngine.ASF_GENDER
            | FaceEngine.ASF_FACE3DANGLE
            | FaceEngine.ASF_LIVENESS
            | FaceEngine.ASF_IR_LIVENESS),

    DETECT_FACE_PROPERTY(FaceEngine.ASF_FACE_DETECT
            | FaceEngine.ASF_AGE
            | FaceEngine.ASF_GENDER
            | FaceEngine.ASF_FACE3DANGLE
            | FaceEngine.ASF_LIVENESS),

    DETECT_FACE_PROPERTY_WITH_IR(FaceEngine.ASF_FACE_DETECT
            | FaceEngine.ASF_AGE
            | FaceEngine.ASF_GENDER
            | FaceEngine.ASF_FACE3DANGLE
            | FaceEngine.ASF_LIVENESS
            | FaceEngine.ASF_IR_LIVENESS),

    DETECT_FACE_FEATURE(FaceEngine.ASF_FACE_DETECT
            | FaceEngine.ASF_FACE_RECOGNITION),


    ALL_WITHOUT_IR(FaceEngine.ASF_FACE_DETECT
            | FaceEngine.ASF_FACE_RECOGNITION
            | FaceEngine.ASF_AGE
            | FaceEngine.ASF_GENDER
            | FaceEngine.ASF_FACE3DANGLE
            | FaceEngine.ASF_LIVENESS);

    //需要启用的功能组合
    public int combinedMask;

    FaceAction(int mask) {
        this.combinedMask = mask;
    }
}