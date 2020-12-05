package com.harvey.arcfacedamo.ui;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.harvey.arcface.AIFace;
import com.harvey.arcface.FaceMatchThread;
import com.harvey.arcface.model.FaceAction;
import com.harvey.arcface.model.FaceCameraModel;
import com.harvey.arcface.model.FeatureModel;
import com.harvey.arcface.model.PersonCameraModel;
import com.harvey.arcface.view.SurfaceViewCamera;
import com.harvey.arcface.view.SurfaceViewFace;
import com.harvey.arcfacedamo.BuildConfig;
import com.harvey.arcfacedamo.R;
import com.harvey.arcfacedamo.adapter.MatchFaceAdapter;
import com.harvey.arcfacedamo.utils.FaceFindMatchModel;
import com.harvey.arcfacedamo.utils.FaceMatchHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by harvey on 2018/1/12.
 */

public class FaceScanActivity extends AppCompatActivity
        implements
        Camera.PreviewCallback {
    public static final int ADD_MATCH_FACE_WHAT = 2;
    public static final int FINISH_SHOW_WHAT = 1;
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
                case ADD_MATCH_FACE_WHAT:
                    FaceFindMatchModel matchModel = (FaceFindMatchModel) msg.obj;
                    if (matchModel != null) {
                        boolean isExist = false;
                        int updatePosition = -1;
                        List<FaceFindMatchModel> models = matchFaceAdapter.getData();
                        for (int i = 0; i < models.size(); i++) {
                            FaceFindMatchModel item = models.get(i);
                            if (item.getPersonId() == matchModel.getPersonId()) {
                                if (!item.equals(matchModel)) {
                                    updatePosition = i;
                                }
                                isExist = true;
                                break;
                            }
                        }
                        if (!isExist) {
                            matchFaceAdapter.addData(matchModel);
                        }
                        if (updatePosition != -1) {
                            matchFaceAdapter.setData(updatePosition, matchModel);
                        }
                        mHandler.removeMessages(FINISH_SHOW_WHAT);
                        mHandler.sendEmptyMessageDelayed(FINISH_SHOW_WHAT, TimeUnit.SECONDS.toMillis(5));
                    }
                    break;
            }
            return false;
        }
    });
    AIFace mAiFace;
    FaceMatchThread faceMatchThread;
    FaceMatchHelper matchHelper;

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
        AIFace.showLog(BuildConfig.DEBUG);
        mAiFace = new AIFace
                .Builder(this)
                .combinedMask(FaceAction.ALL_WITHOUT_IR)
                .orientPriority(surfaceViewCamera.getCameraDisplayOrientation())
                .build();
        matchHelper = new FaceMatchHelper(this, mAiFace);
        faceMatchThread = new FaceMatchThread(mAiFace) {

            @Override
            protected void handleMatch(FeatureModel featureModel) {
                FaceFindMatchModel faceFindMatchModel = matchHelper.matchFace(featureModel.getFaceFeature());
                if (faceFindMatchModel != null) {
                    Message message = mHandler.obtainMessage(FaceScanActivity.ADD_MATCH_FACE_WHAT, faceFindMatchModel);
                    mHandler.sendMessage(message);
                }
            }
        };
        faceMatchThread.start();
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
        surfaceViewFace.setFrontCamera(surfaceViewCamera.isFront());
    }

    public void switchCamera(View view) {
        surfaceViewCamera.switchCamera();
        surfaceViewFace.setFrontCamera(surfaceViewCamera.isFront());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAiFace.destroy();
        faceMatchThread.finish();
        mHandler.removeMessages(FINISH_SHOW_WHAT);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        FaceCameraModel faceCameraModel = mAiFace.detectFaceWithCamera(data, size.width, size.height);
        PersonCameraModel personCameraModel = mAiFace.detectPersonWithCamera(faceCameraModel);
        surfaceViewFace.updateFace(personCameraModel);
        faceMatchThread.offerFaceCameraModel(faceCameraModel);
    }
}
