package com.harvey.arcfacedamo.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.guo.android_extend.widget.ExtImageView;
import com.harvey.arcface.ArcFaceEngine;
import com.harvey.arcface.DetectFaceAction;
import com.harvey.arcfacedamo.R;
import com.harvey.arcface.moodel.FaceFindCameraModel;
import com.harvey.arcface.moodel.FaceFindModel;
import com.harvey.arcface.utils.FaceUtils;
import com.harvey.arcfacedamo.utils.ToastUtil;
import com.harvey.arcface.view.SurfaceViewCamera;
import com.harvey.arcface.view.SurfaceViewSaveFace;

import java.util.List;

/**
 * Created by harvey on 2018/1/12.
 */

public class FaceRegisterActivity extends AppCompatActivity
		implements
			Camera.PreviewCallback,
			DetectFaceAction.OnFaceDetectListener {
	final DetectFaceAction mDetectFaceAction = new DetectFaceAction();
	SurfaceViewSaveFace surfaceViewSaveFace;
	SurfaceViewCamera surfaceViewCamera;
	View dialogLayout;
	ExtImageView dialogExtImageView;
	EditText dialogName;
	EditText dialogAge;
	RadioGroup dialogSex;
	String faceName;
	String faceAge;
	String faceSex;
	AlertDialog registerDialog;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewId());
		initHolder();
		initListener();
		initData();
	}

	protected int getContentViewId() {
		return R.layout.activity_register_face;
	}

	protected void initHolder() {
		surfaceViewCamera = findViewById(R.id.surfaceView_Camera);
		surfaceViewSaveFace = findViewById(R.id.surfaceViewSaveFace);
		dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_save_face, null);
		dialogExtImageView = dialogLayout.findViewById(R.id.extimageview);
		dialogName = dialogLayout.findViewById(R.id.et_name);
		dialogAge = dialogLayout.findViewById(R.id.et_age);
		dialogSex = dialogLayout.findViewById(R.id.rg_sex);

	}

	public void initListener() {
		dialogSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.rb_man) {
					faceSex = "男";

				}
				if (checkedId == R.id.rb_women) {
					faceSex = "女";
				}
			}
		});
	}

	protected void initData() {
		mDetectFaceAction.init();
		mDetectFaceAction.setOnFaceDetectListener(this);
		surfaceViewCamera.setCameraCallBack(this);
		surfaceViewSaveFace.uploadTimeSecondDown(1);
		surfaceViewSaveFace.setSaveFaceListener(new SurfaceViewSaveFace.SaveFaceListener() {
			@Override
			public void onSuccess(FaceFindCameraModel faceModel) {
				if (faceModel != null && faceModel.getFaceFindModels().size() > 0) {
					showSaveFaceDialog(faceModel.getFaceFindModels().get(0), faceModel.getCameraData());
				}
			}

			@Override
			public void onTimeSecondDown(int TimeSecond) {
				Log.e("harvey", "onTimeSecondDown---->" + TimeSecond);
			}

			@Override
			public void onErrorMsg(int errorCode) {
				Log.e("harvey", "onErrorMsg---->" + errorCode);
			}
		});
	}

	public void showSaveFaceDialog(final FaceFindModel faceModel, final byte[] data) {
		if (registerDialog != null && registerDialog.isShowing()) {
			registerDialog.dismiss();
		}
		ViewParent parent = dialogLayout.getParent();
		if (parent != null) {
			((ViewGroup) parent).removeAllViews();
		}
		final Bitmap faceBitmap = FaceUtils.getFaceBitmap(faceModel, data);
		dialogExtImageView.setImageBitmap(faceBitmap);
		registerDialog = new AlertDialog.Builder(this).setTitle("是否注册该图片?").setIcon(android.R.drawable.ic_dialog_info)
				.setView(dialogLayout).setPositiveButton("确定", null)
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!faceBitmap.isRecycled())
							faceBitmap.recycle();
						surfaceViewSaveFace.reset();
						dialog.dismiss();
					}
				}).create();
		registerDialog.show();
		registerDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				faceName = dialogName.getText().toString().trim();
				faceAge = dialogAge.getText().toString().trim();
				if (TextUtils.isEmpty(faceName)) {
					ToastUtil.showToast(FaceRegisterActivity.this, "请输入姓名！");
				} else if (TextUtils.isEmpty(faceAge)) {
					ToastUtil.showToast(FaceRegisterActivity.this, "请输入年龄！");
				} else if (TextUtils.isEmpty(faceSex)) {
					ToastUtil.showToast(FaceRegisterActivity.this, "请选择性别！");
				} else {
					boolean result = ArcFaceEngine.getInstance().saveFace(data, faceModel, faceName,
							Integer.valueOf(faceAge), faceSex, getApplication().getExternalCacheDir().getPath());
					if (result)
						ToastUtil.showToast(FaceRegisterActivity.this, "注册人脸成功！");
					else
						ToastUtil.showToast(FaceRegisterActivity.this, "注册人脸失败！");
					if (!faceBitmap.isRecycled())
						faceBitmap.recycle();
					surfaceViewSaveFace.reset();
					registerDialog.dismiss();
				}
			}
		});
		registerDialog.setCanceledOnTouchOutside(false);
	}

	public void switchCamera(View view) {
		surfaceViewCamera.switchCamera();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Camera.Size size = camera.getParameters().getPreviewSize();
		mDetectFaceAction.detectFace(data, size.width, size.height);
	}

	@Override
	public void onFaceDetect(List<FaceFindModel> faceFindModels, byte[] frameBytes) {
		surfaceViewSaveFace.uploadFace(faceFindModels, frameBytes);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDetectFaceAction.destroy();
	}
}
