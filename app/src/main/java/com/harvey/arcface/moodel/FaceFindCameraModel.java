package com.harvey.arcface.moodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//一个Camera摄像头包含的所有人脸及图片信息

public class FaceFindCameraModel implements Serializable {
	List<FaceFindModel> faceFindModels;
	byte[] cameraData;

	public FaceFindCameraModel(FaceFindCameraModel model) {
        this.cameraData = model.cameraData.clone();
        this.faceFindModels=new ArrayList<>();
        for (FaceFindModel findModel:model.faceFindModels){
			faceFindModels.add(findModel.clone());
		}

    }
	public FaceFindCameraModel(List<FaceFindModel> faceFindModels, byte[] cameraData) {
		this.faceFindModels = faceFindModels;
		this.cameraData = cameraData;
	}

	public List<FaceFindModel> getFaceFindModels() {
		return faceFindModels;
	}

	public void setFaceFindModels(List<FaceFindModel> faceFindModels) {
		this.faceFindModels = faceFindModels;
	}

	public byte[] getCameraData() {
		return cameraData;
	}

	public void setCameraData(byte[] cameraData) {
		this.cameraData = cameraData;
	}

	public FaceFindCameraModel clone() {
		return new FaceFindCameraModel(this);
	}

}
