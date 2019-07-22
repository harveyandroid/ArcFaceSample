package com.harvey.db.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.util.Objects;

/**
 * Created by harvey on 2018/4/25 0025 15:33
 */
@Entity
public class FaceRegister {
    @Id
    private Long id;

    private long person_id;

    private String name;

    private int age;

    private String gender;

    private byte[] featureData;

    private String imagePath;

    private long faceTime;

    @Generated(hash = 1579544683)
    public FaceRegister(Long id, long person_id, String name, int age,
            String gender, byte[] featureData, String imagePath, long faceTime) {
        this.id = id;
        this.person_id = person_id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.featureData = featureData;
        this.imagePath = imagePath;
        this.faceTime = faceTime;
    }

    @Generated(hash = 1592309603)
    public FaceRegister() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getPerson_id() {
        return this.person_id;
    }

    public void setPerson_id(long person_id) {
        this.person_id = person_id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public byte[] getFeatureData() {
        return this.featureData;
    }

    public void setFeatureData(byte[] featureData) {
        this.featureData = featureData;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getFaceTime() {
        return this.faceTime;
    }

    public void setFaceTime(long faceTime) {
        this.faceTime = faceTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FaceRegister)) return false;
        FaceRegister that = (FaceRegister) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
