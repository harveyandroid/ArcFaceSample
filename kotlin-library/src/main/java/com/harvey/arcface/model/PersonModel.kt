package com.harvey.arcface.model

import android.graphics.Rect

data class PersonModel(
        var rect: Rect? = null,
        var orient: Int = 0,
        var faceId: Int = -1,
        var age: Int = 0,
        var gender: Int = 0,
        var yaw: Float = 0f,
        var roll: Float = 0f,
        var pitch: Float = 0f,
        var status: Int = 0,
        var liveness: Int = 0
)