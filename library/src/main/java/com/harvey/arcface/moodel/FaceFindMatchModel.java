package com.harvey.arcface.moodel;

import java.io.Serializable;

/**
 * Created by harvey on 2018/1/12.
 */

public class FaceFindMatchModel implements Serializable {
    long student_id;
    boolean isMatching;
    String name;
    String gender;
    float score;
    String imagePath;

    public FaceFindMatchModel() {
        this.isMatching = false;
        this.score = 0f;
        this.imagePath = "";
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getStudent_id() {
        return student_id;
    }

    public void setStudent_id(long student_id) {
        this.student_id = student_id;
    }

    public String getImagePath() {
        return imagePath == null ? "" : imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isMatching() {
        return isMatching;

    }

    public void setMatching(boolean matching) {
        isMatching = matching;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
