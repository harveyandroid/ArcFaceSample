package com.harvey.arcface.model;

/**
 * Created by hanhui on 2019/12/12 0012 16:33
 */
public class CameraModel {
    byte[] nv21;
    int width;
    int height;

    public CameraModel(byte[] nv21, int width, int height) {
        this.nv21 = nv21;
        this.width = width;
        this.height = height;
    }

    public CameraModel(CameraModel model) {
        this.nv21 = model.nv21.clone();
        this.width = model.width;
        this.height = model.height;
    }

    public byte[] getNv21() {
        return nv21;
    }

    public void setNv21(byte[] nv21) {
        this.nv21 = nv21;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
