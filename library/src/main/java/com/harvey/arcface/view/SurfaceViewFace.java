package com.harvey.arcface.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.arcsoft.face.FaceInfo;
import com.harvey.arcface.R;
import com.harvey.arcface.model.FaceCameraModel;
import com.harvey.arcface.utils.FaceUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SurfaceViewFace extends SurfaceView implements SurfaceHolder.Callback {
    public int cameraWidth = 1280;
    public int cameraHeight = 720;
    private Thread thread;
    private SurfaceHolder surfaceHolder;
    private boolean surfaceRun = false;
    private volatile boolean surfaceStop = false;
    // 旋转图片
    private Bitmap scan1;
    private Bitmap scan2;
    // SurfaceView尺寸
    private int surfaceWidth;
    private int surfaceHeight;
    // 人脸数据列表
    private List<FaceInfo> faceInfoList;
    private boolean frontCamera = true;//默认前置
    private int displayOrientation = 0;
    private Paint rectPaint;
    private Runnable drawRunnable = new Runnable() {
        // 旋转计数器
        int drawRotate = 0;
        int drawRotateFind = 0;
        int sleepCount = 100;// 毫秒

        @Override
        public void run() {
            while (surfaceRun) {
                if (!surfaceStop) {
                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        for (FaceInfo faceInfo : faceInfoList) {
                            drawFaceRect(canvas, faceInfo);
                            drawFindFace(canvas, faceInfo);
                        }
                        drawRotate += 15;
                        drawRotateFind += 5;
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        private void drawFaceRect(Canvas canvas, FaceInfo faceInfo) {
            Rect mapRect = FaceUtils.adjustRect(faceInfo.getRect(), cameraWidth, cameraHeight,
                    displayOrientation, frontCamera, surfaceWidth, surfaceHeight);
            canvas.drawRect(mapRect, rectPaint);
        }

        private void drawFindFace(Canvas canvas, FaceInfo model) {
            Matrix matrix = new Matrix();
            Matrix matrix2 = new Matrix();

            matrix.postTranslate(-scan1.getWidth() / 2, -scan1.getHeight() / 2);// 步骤1
            matrix2.postTranslate(-scan2.getWidth() / 2, -scan2.getHeight() / 2);// 步骤1

            matrix.postRotate(drawRotate);// 步骤2
            matrix2.postRotate(360 - drawRotate * 2);// 步骤2

            float scaleWidth = ((float) model.getRect().width() * (float) surfaceWidth
                    / (float) cameraWidth) / scan1.getWidth();

            matrix.postScale(scaleWidth, scaleWidth);
            matrix2.postScale(scaleWidth, scaleWidth);
            // 中心点计算
            Rect mapRect = FaceUtils.adjustRect(model.getRect(), cameraWidth, cameraHeight,
                    displayOrientation, frontCamera, surfaceWidth, surfaceHeight);
            int centerX = mapRect.centerX();
            int centerY = mapRect.centerY();
            matrix.postTranslate(centerX, centerY);// 步骤3 屏幕的中心点
            matrix2.postTranslate(centerX, centerY);// 步骤3 屏幕的中心点

            canvas.drawBitmap(scan1, matrix, new Paint());
            canvas.drawBitmap(scan2, matrix2, new Paint());
        }

    };

    public SurfaceViewFace(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        // 透明背景
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        faceInfoList = new CopyOnWriteArrayList<>();

        rectPaint = new Paint();
        rectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(5);
    }

    // 更新人脸列表
    public void updateFace(FaceCameraModel model) {
        surfaceStop = false;
        if (model != null) {
            cameraHeight = model.getHeight();
            cameraWidth = model.getWidth();
            this.faceInfoList.clear();
            this.faceInfoList.addAll(model.getFaceInfo());
        } else {
            this.faceInfoList.clear();
        }
    }

    public void setFrontCamera(boolean frontCamera) {
        this.frontCamera = frontCamera;
    }

    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    @Override
    public void surfaceCreated(SurfaceHolder mSurfaceHolder) {
        surfaceRun = true;
        surfaceStop = true;
        scan1 = BitmapFactory.decodeResource(getResources(), R.drawable.scan1);
        scan2 = BitmapFactory.decodeResource(getResources(), R.drawable.scan2);
        surfaceWidth = getWidth();
        surfaceHeight = getHeight();
        thread = new Thread(drawRunnable);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        surfaceRun = false;
        surfaceStop = true;
        scan1.recycle();
        scan2.recycle();
    }
}
