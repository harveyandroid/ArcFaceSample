package com.harvey.arcface.model;

import android.graphics.Rect;

public class PersonModel {
    //人脸信息
    Rect rect;
    int orient;
    int faceId = -1;
    int age;
    int gender;
    float yaw;
    float roll;
    float pitch;
    int status;
    int liveness;

    public PersonModel() {
    }

    public PersonModel(PersonModel model) {
        this.rect = new Rect(model.rect);
        this.orient = model.orient;
        this.faceId = model.faceId;
        this.age = model.age;
        this.gender = model.gender;
        this.yaw = model.yaw;
        this.roll = model.roll;
        this.pitch = model.pitch;
        this.status = model.status;
        this.liveness = model.liveness;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public int getOrient() {
        return orient;
    }

    public void setOrient(int orient) {
        this.orient = orient;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getLiveness() {
        return liveness;
    }

    public void setLiveness(int liveness) {
        this.liveness = liveness;
    }

    public PersonModel clone() {
        return new PersonModel(this);
    }

    @Override
    public String toString() {
        return "FaceFindPersonModel{" +
                "rect=" + rect +
                ", orient=" + orient +
                ", faceId=" + faceId +
                ", age=" + age +
                ", gender=" + gender +
                ", yaw=" + yaw +
                ", roll=" + roll +
                ", pitch=" + pitch +
                ", status=" + status +
                ", liveness=" + liveness +
                '}';
    }
}
