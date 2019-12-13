package com.harvey.arcface.model;

import android.graphics.Rect;

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;

//单个查询人脸信息特征
public class FeatureModel {
    // 摄像头的尺寸
    int cameraWidth;
    int cameraHeight;
    // 人脸矩形框
    Rect rect;
    // 人脸角度
    int degree;
    //人脸ID
    int faceId = -1;
    //人脸特征
    byte[] featureData;

    public FeatureModel(int cameraWidth, int cameraHeight, Rect rect, int degree, int faceId, byte[] featureData) {
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        this.rect = rect;
        this.degree = degree;
        this.faceId = faceId;
        this.featureData = featureData;
    }

    public FeatureModel(int cameraWidth, int cameraHeight, FaceInfo faceInfo, FaceFeature faceFeature) {
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        this.rect = faceInfo.getRect();
        this.degree = faceInfo.getOrient();
        this.faceId = faceInfo.getFaceId();
        this.featureData = faceFeature.getFeatureData();
    }

    public FeatureModel(FeatureModel findModel) {
        this.cameraWidth = findModel.cameraWidth;
        this.cameraHeight = findModel.cameraHeight;
        this.rect = new Rect(findModel.rect);
        this.degree = findModel.degree;
        this.faceId = findModel.getFaceId();
        this.featureData = findModel.featureData.clone();
    }

    public int getCameraWidth() {
        return cameraWidth;
    }

    public void setCameraWidth(int cameraWidth) {
        this.cameraWidth = cameraWidth;
    }

    public int getCameraHeight() {
        return cameraHeight;
    }

    public void setCameraHeight(int cameraHeight) {
        this.cameraHeight = cameraHeight;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

    public byte[] getFeatureData() {
        return featureData;
    }

    public void setFeatureData(byte[] featureData) {
        this.featureData = featureData;
    }

    public FaceFeature getFaceFeature() {
        return new FaceFeature(featureData);
    }

    public FaceInfo getFaceInfo() {
        return new FaceInfo(rect, degree);
    }

    public FeatureModel clone() {
        return new FeatureModel(this);
    }

    /**
     * 将图像中需要截取的Rect向外扩张一倍，若扩张一倍会溢出，则扩张到边界，若Rect已溢出，则收缩到边界
     *
     * @return
     */
    public Rect getFaceMoreRect() {
        //1.原rect边界已溢出宽高的情况
        int maxOverFlow = 0;
        int tempOverFlow = 0;
        if (rect.left < 0) {
            maxOverFlow = -rect.left;
        }
        if (rect.top < 0) {
            tempOverFlow = -rect.top;
            if (tempOverFlow > maxOverFlow) {
                maxOverFlow = tempOverFlow;
            }
        }
        if (rect.right > cameraWidth) {
            tempOverFlow = rect.right - cameraWidth;
            if (tempOverFlow > maxOverFlow) {
                maxOverFlow = tempOverFlow;
            }
        }
        if (rect.bottom > cameraHeight) {
            tempOverFlow = rect.bottom - cameraHeight;
            if (tempOverFlow > maxOverFlow) {
                maxOverFlow = tempOverFlow;
            }
        }
        if (maxOverFlow != 0) {
            return new Rect(rect.left + maxOverFlow,
                    rect.top + maxOverFlow,
                    rect.right - maxOverFlow,
                    rect.bottom - maxOverFlow);
        }
        //2.原rect边界未溢出宽高的情况
        int padding = rect.height() / 2;
        //若以此padding扩张rect会溢出，取最大padding为四个边距的最小值
        if (!(rect.left - padding > 0
                && rect.right + padding < cameraWidth && rect.top - padding > 0
                && rect.bottom + padding < cameraHeight)) {
            padding = Math.min(Math.min(Math.min(rect.left, cameraWidth - rect.right), cameraHeight - rect.bottom), rect.top);
        }
        return new Rect(rect.left - padding,
                rect.top - padding,
                rect.right + padding,
                rect.bottom + padding);
    }


    public int getOrientation() {
        switch (degree) {
            case 1:
                return 0;
            case 2:
                return 90;
            case 3:
                return 270;
            case 4:
                return 180;
            case 5:
                return 30;
            case 6:
                return 60;
            case 7:
                return 120;
            case 8:
                return 150;
            case 9:
                return 210;
            case 10:
                return 240;
            case 11:
                return 300;
            case 12:
                return 330;
            default:
                return 0;
        }
    }

    /**
     * 调整人脸框用来绘制(针对手机)
     *
     * @param displayOrientation 显示的角度
     * @param frontCamera        是否前置
     * @param canvasWidth
     * @param canvasHeight
     * @return 调整后的需要被绘制到View上的rect
     */
    public Rect adjustRect(int displayOrientation, boolean frontCamera, int canvasWidth, int canvasHeight) {
        Rect target = new Rect(rect);
        float horizontalRatio;
        float verticalRatio;
        if (displayOrientation % 180 == 0) {
            horizontalRatio = (float) canvasWidth / (float) cameraWidth;
            verticalRatio = (float) canvasHeight / (float) cameraHeight;
        } else {
            horizontalRatio = (float) canvasHeight / (float) cameraWidth;
            verticalRatio = (float) canvasWidth / (float) cameraHeight;
        }
        target.left *= horizontalRatio;
        target.right *= horizontalRatio;
        target.top *= verticalRatio;
        target.bottom *= verticalRatio;

        Rect newRect = new Rect();
        switch (displayOrientation) {
            case 0:
                if (frontCamera) {
                    newRect.left = canvasWidth - target.right;
                    newRect.right = canvasWidth - target.left;
                } else {
                    newRect.left = target.left;
                    newRect.right = target.right;
                }
                newRect.top = target.top;
                newRect.bottom = target.bottom;
                break;
            case 90:
                newRect.right = canvasWidth - target.top;
                newRect.left = canvasWidth - target.bottom;
                if (frontCamera) {
                    newRect.top = canvasHeight - target.right;
                    newRect.bottom = canvasHeight - target.left;
                } else {
                    newRect.top = target.left;
                    newRect.bottom = target.right;
                }
                break;
            case 180:
                newRect.top = canvasHeight - target.bottom;
                newRect.bottom = canvasHeight - target.top;
                if (frontCamera) {
                    newRect.left = target.left;
                    newRect.right = target.right;
                } else {
                    newRect.left = canvasWidth - target.right;
                    newRect.right = canvasWidth - target.left;
                }
                break;
            case 270:
                newRect.left = target.top;
                newRect.right = target.bottom;
                if (frontCamera) {
                    newRect.top = target.left;
                    newRect.bottom = target.right;
                } else {
                    newRect.top = canvasHeight - target.right;
                    newRect.bottom = canvasHeight - target.left;
                }
                break;
            default:
                break;
        }
        return newRect;
    }

    /**
     * 实际展示 宽高相反
     *
     * @param mappedWidth
     * @param mappedHeight
     * @return
     */
    public Rect getMappedFaceRect(int mappedWidth, int mappedHeight) {
        int left = rect.right * mappedWidth / cameraWidth;
        int top = rect.top * mappedHeight / cameraHeight;
        int right = rect.left * mappedWidth / cameraWidth;
        int bottom = rect.bottom * mappedHeight / cameraHeight;

        Rect rect = new Rect(left, top, right, bottom);
        return rect;
    }

}
