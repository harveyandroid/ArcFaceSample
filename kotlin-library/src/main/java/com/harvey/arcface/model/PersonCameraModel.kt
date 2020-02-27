package com.harvey.arcface.model

import android.graphics.Rect
import java.util.*

/**
 * Created by hanhui on 2019/12/12 0012 16:50
 */
class PersonCameraModel : CameraModel {
    private var personModels: MutableList<PersonModel>? = null

    constructor(data: MutableList<PersonModel>, nv21: ByteArray, width: Int, height: Int) : super(nv21, width, height) {
        this.personModels = data
    }

    constructor(data: MutableList<PersonModel>, model: CameraModel) : super(model) {
        this.personModels = data
    }

    constructor(model: PersonCameraModel) : super(model) {
        this.personModels = ArrayList()
        for (personModel in model.personModels!!) {
            personModels!!.add(personModel.copy(rect = Rect(personModel.rect)))
        }
    }

    fun getPersonModels(): List<PersonModel> {
        return personModels ?: ArrayList()
    }

    fun clone(): PersonCameraModel {
        return PersonCameraModel(this)
    }

}
