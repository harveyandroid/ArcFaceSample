package com.harvey.arcface.model

/**
 * Created by hanhui on 2018/6/1 0001 09:58
 */
data class FaceFindMatchModel(var student_id: Long = 0, var isMatching: Boolean = false, var name: String? = "", var gender: String? = "", var score: Float = 0f, var imagePath: String? = "") {
}