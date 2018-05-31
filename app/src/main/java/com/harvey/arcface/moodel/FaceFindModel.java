package com.harvey.arcface.moodel;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

//单个查询人脸信息
public class FaceFindModel implements Parcelable {
	public static final Creator<FaceFindModel> CREATOR = new Creator<FaceFindModel>() {
		@Override
		public FaceFindModel createFromParcel(Parcel source) {
			return new FaceFindModel(source);
		}

		@Override
		public FaceFindModel[] newArray(int size) {
			return new FaceFindModel[size];
		}
	};
	// 摄像头的尺寸
	int cameraWidth;
	int cameraHeight;
	// 人脸矩形框
	Rect faceRect;
	// 人脸角度
	int degree;

	public FaceFindModel() {
		this.cameraWidth = 0;
		this.cameraHeight = 0;
		this.faceRect = new Rect();
		this.degree = 0;
	}

	public FaceFindModel(FaceFindModel self) {
		this.cameraWidth = self.cameraWidth;
		this.cameraHeight = self.cameraHeight;
		this.faceRect = new Rect(self.faceRect);
		this.degree = self.degree;
	}

	protected FaceFindModel(Parcel in) {
		this.cameraWidth = in.readInt();
		this.cameraHeight = in.readInt();
		this.faceRect = in.readParcelable(Rect.class.getClassLoader());
		this.degree = in.readInt();
	}

	public Rect getFaceRect() {
		return faceRect;
	}

	public void setFaceRect(Rect faceRect) {
		this.faceRect = faceRect;
	}

	// 扩大人脸矩形框范围
	public Rect getFaceMoreRect() {
		int more_width = faceRect.width() / 3;
		int more_height = faceRect.height() / 2;
		int top = (faceRect.top - more_height) >= 0 ? faceRect.top - more_height : 0;
		int left = (faceRect.left - more_width) >= 0 ? faceRect.left - more_width : 0;
		int bottom = (faceRect.bottom + more_height) <= cameraHeight ? faceRect.bottom + more_height : cameraHeight;
		int right = (faceRect.right + more_width) <= cameraWidth ? faceRect.right + more_width : cameraWidth;
		return new Rect(left, top, right, bottom);
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

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}

	public int getOrientation() {
		switch (degree) {
			case 1 :
				return 0;
			case 2 :
				return 90;
			case 3 :
				return 270;
			case 4 :
				return 180;
			case 5 :
				return 30;
			case 6 :
				return 60;
			case 7 :
				return 120;
			case 8 :
				return 150;
			case 9 :
				return 210;
			case 10 :
				return 240;
			case 11 :
				return 300;
			case 12 :
				return 330;
			default :
				return 0;
		}
	}

	// 映射
	// 实际展示 宽高相反
	public Rect getMappedFaceRect(int mappedWidth, int mappedHeight) {
		// int left = faceRect.right * mappedWidth / cameraWidth;
		// int top = faceRect.top * mappedHeight / cameraHeight;
		// int right = faceRect.left * mappedWidth / cameraWidth;
		// int bottom = faceRect.bottom * mappedHeight / cameraHeight;

		// 根据摄像头进行转化(垂直视角)
		int top = faceRect.right * mappedWidth / cameraWidth;

		int right = mappedHeight - faceRect.top * mappedHeight / cameraHeight;

		int bottom = faceRect.left * mappedWidth / cameraWidth;
		int left = mappedHeight - faceRect.bottom * mappedHeight / cameraHeight;

		Rect rect = new Rect(left, top, right, bottom);
		return rect;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.cameraWidth);
		dest.writeInt(this.cameraHeight);
		dest.writeParcelable(this.faceRect, flags);
		dest.writeInt(this.degree);
	}

	public FaceFindModel clone() {
		return new FaceFindModel(this);
	}

}
