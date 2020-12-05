package com.harvey.arcfacedamo.ui;

import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arcsoft.face.FaceInfo;
import com.guo.android_extend.widget.ExtImageView;
import com.harvey.arcface.AIFace;
import com.harvey.arcface.model.CameraModel;
import com.harvey.arcface.model.FaceAction;
import com.harvey.arcface.model.FaceCameraModel;
import com.harvey.arcface.utils.FaceUtils;
import com.harvey.arcface.view.SurfaceViewCamera;
import com.harvey.arcface.view.SurfaceViewSaveFace;
import com.harvey.arcfacedamo.R;
import com.harvey.arcfacedamo.utils.FaceMatchHelper;
import com.harvey.arcfacedamo.utils.ToastUtil;

/**
 * Created by harvey on 2018/1/12.
 */

public class FaceRegisterActivity extends AppCompatActivity
        implements
        Camera.PreviewCallback {
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
    AIFace mAiFace;
    FaceMatchHelper faceRegisterHelper;

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
        dialogSex.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_man) {
                faceSex = "男";
            }
            if (checkedId == R.id.rb_women) {
                faceSex = "女";
            }
        });
    }

    protected void initData() {
        mAiFace = new AIFace
                .Builder(this)
                .combinedMask(FaceAction.DETECT_FACE_FEATURE)
                .orientPriority(surfaceViewCamera.getCameraDisplayOrientation())
                .build();
        faceRegisterHelper = new FaceMatchHelper(this, mAiFace);
        AIFace.showLog(true);
        surfaceViewCamera.setCameraCallBack(this);
        surfaceViewSaveFace.uploadTimeSecondDown(1);
        surfaceViewSaveFace.setSaveFaceListener(new SurfaceViewSaveFace.SaveFaceListener() {
            @Override
            public void onSuccess(FaceCameraModel faceModel) {
                if (faceModel != null && faceModel.getFaceInfo().size() > 0) {
                    showSaveFaceDialog(faceModel.getFaceInfo().get(0), faceModel);
                } else {
                    surfaceViewSaveFace.reset();
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
        surfaceViewSaveFace.setDisplayOrientation(surfaceViewCamera.getCameraDisplayOrientation());
        surfaceViewSaveFace.setFrontCamera(surfaceViewCamera.isFront());

    }

    public void showSaveFaceDialog(final FaceInfo faceInfo, final CameraModel cameraModel) {
        Log.e("harvey", String.format("showSaveFaceDialog-->FaceInfo:%d,Nv21:%s", faceInfo.hashCode(), cameraModel.getNv21()));
        if (registerDialog != null && registerDialog.isShowing()) {
            registerDialog.dismiss();
        }
        ViewParent parent = dialogLayout.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeAllViews();
        }
        final Bitmap faceBitmap = FaceUtils.getFaceBitmap(faceInfo, cameraModel);
        dialogExtImageView.setImageBitmap(faceBitmap);
        registerDialog = new AlertDialog.Builder(this).
                setTitle("是否注册该图片?")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(dialogLayout).setPositiveButton("确定", null)
                .setNegativeButton("取消", (dialog, which) -> {
                    if (!faceBitmap.isRecycled())
                        faceBitmap.recycle();
                    surfaceViewSaveFace.reset();
                    dialog.dismiss();
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
                    boolean result = faceRegisterHelper.registerNv21(faceInfo, cameraModel, faceName,
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
        surfaceViewSaveFace.setFrontCamera(surfaceViewCamera.isFront());
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        FaceCameraModel faceCameraModel = mAiFace.detectFaceWithCamera(data, size.width, size.height);
        surfaceViewSaveFace.uploadFace(faceCameraModel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAiFace.destroy();
    }
}
