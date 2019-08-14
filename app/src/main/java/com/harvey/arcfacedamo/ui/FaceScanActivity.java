package com.harvey.arcfacedamo.ui;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;

import com.harvey.arcface.DetectFaceAction;
import com.harvey.arcface.MatchFaceAction;
import com.harvey.arcface.moodel.FaceFindMatchModel;
import com.harvey.arcface.moodel.FaceFindModel;
import com.harvey.arcface.utils.ThreadManager;
import com.harvey.arcface.view.SurfaceViewCamera;
import com.harvey.arcface.view.SurfaceViewFace;
import com.harvey.arcfacedamo.R;
import com.harvey.arcfacedamo.adapter.MatchFaceAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by harvey on 2018/1/12.
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
                case FINISH_SHOW_WHAT:
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
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
        mDetectFaceAction.setOnFaceDetectListener(this);
        matchFaceAction.setOnFaceMatchListener(this);
        ThreadManager.getCalculator().submit(mDetectFaceAction);
        ThreadManager.getCalculator().submit(matchFaceAction);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setAutoMeasureEnabled(true);
        faceList.setLayoutManager(layoutManager);
        matchFaceAdapter = new MatchFaceAdapter();
        faceList.setAdapter(matchFaceAdapter);
        faceList.setHasFixedSize(true);
        faceList.setNestedScrollingEnabled(false);
        surfaceViewCamera.setCameraCallBack(this);
        surfaceViewFace.setDisplayOrientation(surfaceViewCamera.getCameraDisplayOrientation());
    }

    public void switchCamera(View view) {
        surfaceViewCamera.switchCamera();
        surfaceViewFace.setFrontCamera(surfaceViewCamera.isFront());
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
        mDetectFaceAction.setData(data, size.width, size.height);

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
