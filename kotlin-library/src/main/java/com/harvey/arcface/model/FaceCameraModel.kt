package com.harvey.arcface.model

import com.arcsoft.face.FaceInfo

/**
 * 一个Camera摄像头包含的所有人脸信息
 * Created by hanhui on 2019/12/12 0012 16:43
 */
class FaceCameraModel(val faceInfo: List<FaceInfo>, nv21: ByteArray, width: Int, height: Int) :
        CameraModel(nv21, width, height)