package com.harvey.arcface.model;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;

public class PersonModel {
    FaceInfo faceInfo;
    AgeInfo ageInfo;
    Face3DAngle face3DAngle;
    GenderInfo genderInfo;
    LivenessInfo livenessInfo;

    public PersonModel(FaceInfo faceInfo, AgeInfo ageInfo, Face3DAngle face3DAngle, GenderInfo genderInfo, LivenessInfo livenessInfo) {
        this.faceInfo = faceInfo;
        this.ageInfo = ageInfo;
        this.face3DAngle = face3DAngle;
        this.genderInfo = genderInfo;
        this.livenessInfo = livenessInfo;
    }

    public PersonModel() {
    }

    public PersonModel(PersonModel model) {
        faceInfo = model.faceInfo.clone();
        ageInfo = model.ageInfo;
        face3DAngle = model.face3DAngle;
        genderInfo = model.genderInfo;
        livenessInfo = model.livenessInfo;
    }

    public FaceInfo getFaceInfo() {
        return faceInfo;
    }

    public void setFaceInfo(FaceInfo faceInfo) {
        this.faceInfo = faceInfo;
    }

    public AgeInfo getAgeInfo() {
        return ageInfo;
    }

    public void setAgeInfo(AgeInfo ageInfo) {
        this.ageInfo = ageInfo;
    }

    public String getGender() {
        switch (genderInfo.getGender()) {
            case GenderInfo.MALE:
                return "男";
            case GenderInfo.FEMALE:
                return "女";
            default:
                return "未知";
        }

    }

    public Face3DAngle getFace3DAngle() {
        return face3DAngle;
    }

    public void setFace3DAngle(Face3DAngle face3DAngle) {
        this.face3DAngle = face3DAngle;
    }

    public GenderInfo getGenderInfo() {
        return genderInfo;
    }

    public void setGenderInfo(GenderInfo genderInfo) {
        this.genderInfo = genderInfo;
    }

    public LivenessInfo getLivenessInfo() {
        return livenessInfo;
    }

    public void setLivenessInfo(LivenessInfo livenessInfo) {
        this.livenessInfo = livenessInfo;
    }

    public PersonModel clone() {
        return new PersonModel(this);
    }
}
