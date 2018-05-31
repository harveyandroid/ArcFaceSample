package com.harvey.arcfacedamo.ui;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.harvey.arcfacedamo.R;
import com.harvey.arcface.DetectFaceAction;
import com.harvey.arcface.MatchFaceAction;
import com.harvey.arcfacedamo.adapter.MatchFaceAdapter;
import com.harvey.arcface.moodel.FaceFindMatchModel;
import com.harvey.arcface.moodel.FaceFindModel;
import com.harvey.arcface.view.SurfaceViewCamera;
import com.harvey.arcface.view.SurfaceViewFace;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kenny on 2018/1/12.
 */

public class FaceScanActivity extends AppCompatActivity
		implements
			Camera.PreviewCallback,
			DetectFaceAction.OnFaceDetectListener,
			MatchFaceAction.OnFaceMatchListener {
	final int FINISH_SHOW_WHAT = 1;
	final DetectFaceAction mDetectFaceAction = new DetectFaceAction();
	final MatchFaceAction matchFaceAction = new MatchFaceAction();
	SurfaceViewFace surfaceViewFace;
	SurfaceViewCamera surfaceViewCamera;
	RecyclerView faceList;
	MatchFaceAdapter matchFaceAdapter;
	final Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case FINISH_SHOW_WHAT :
					matchFaceAdapter.setNewData(null);
					break;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewId());
		initHolder();
		initData();
	}

	protected int getContentViewId() {
		return R.layout.activity_scan_face;
	}

	protected void initHolder() {
		surfaceViewCamera = findViewById(R.id.surfaceView_Camera);
		surfaceViewFace = findViewById(R.id.surfaceViewFace);
		faceList = findViewById(R.id.face_list);
	}

	protected void initData() {
		mDetectFaceAction.init();
		mDetectFaceAction.setOnFaceDetectListener(this);
		matchFaceAction.init();
		matchFaceAction.setOnFaceMatchListener(this);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setSmoothScrollbarEnabled(true);
		layoutManager.setAutoMeasureEnabled(true);
		faceList.setLayoutManager(layoutManager);
		matchFaceAdapter = new MatchFaceAdapter();
		faceList.setAdapter(matchFaceAdapter);
		faceList.setHasFixedSize(true);
		faceList.setNestedScrollingEnabled(false);
		surfaceViewCamera.setCameraCallBack(this);
	}
	public void switchCamera(View view) {
		surfaceViewCamera.switchCamera();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		matchFaceAction.destroy();
		mDetectFaceAction.destroy();
		mHandler.removeMessages(FINISH_SHOW_WHAT);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Camera.Size size = camera.getParameters().getPreviewSize();
		mDetectFaceAction.detectFace(data, size.width, size.height);
		matchFaceAction.setFrameBytes(data);
	}

	@Override
	public void onFaceDetect(List<FaceFindModel> faceFindModels, byte[] frameBytes) {
		surfaceViewFace.updateFace(faceFindModels);
		matchFaceAction.matchFace(faceFindModels);
	}

	@Override
	public void onFaceMatch(FaceFindMatchModel face) {
		boolean isExist = false;
		int existPosition = 0;
		List<FaceFindMatchModel> models = matchFaceAdapter.getData();
		for (int i = 0; i < models.size(); i++) {
			if (models.get(i).getName().equals(face.getName())) {
				existPosition = i;
				isExist = true;
				break;
			}
		}
		if (!isExist) {
			matchFaceAdapter.addData(face);
		} else {
			matchFaceAdapter.setData(existPosition, face);
		}
		mHandler.removeMessages(FINISH_SHOW_WHAT);
		mHandler.sendEmptyMessageDelayed(FINISH_SHOW_WHAT, TimeUnit.SECONDS.toMillis(5));
	}
}
