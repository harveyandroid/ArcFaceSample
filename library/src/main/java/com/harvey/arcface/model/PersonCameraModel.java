package com.harvey.arcface.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanhui on 2019/12/12 0012 16:50
 */
public class PersonCameraModel extends CameraModel {
    List<PersonModel> personModels;

    public PersonCameraModel(List<PersonModel> data, byte[] nv21, int width, int height) {
        super(nv21, width, height);
        this.personModels = data;
    }

    public PersonCameraModel(List<PersonModel> data, CameraModel model) {
        super(model);
        this.personModels = data;
    }

    public PersonCameraModel(PersonCameraModel model) {
        super(model);
        this.personModels = new ArrayList<>();
        for (PersonModel personModel : model.personModels) {
            personModels.add(personModel.clone());
        }
    }

    public List<PersonModel> getPersonModels() {
        if (personModels == null) {
            return new ArrayList<>();
        }
        return personModels;
    }

    public PersonCameraModel clone() {
        return new PersonCameraModel(this);
    }

}
