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
import com.harvey.arcface.model.PersonCameraModel;
import com.harvey.arcface.model.PersonModel;
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
    private List<PersonModel> faceList;
    private boolean frontCamera = true;//默认前置
    private int displayOrientation = 0;
    private Paint faceInfoPaint;
    private Paint textPaint;
    private float textHeight;

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
                        for (PersonModel model : faceList) {
                            drawFaceInfo(canvas, model);
                            drawFindFace(canvas, model);
                        }
                        drawRotate += 15;
                        drawRotateFind += 5;
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        private void drawFaceInfo(Canvas canvas, PersonModel model) {
            FaceInfo faceInfo = model.getFaceInfo();
            Rect mapRect = FaceUtils.adjustRect(faceInfo.getRect(), cameraWidth, cameraHeight,
                    displayOrientation, frontCamera, surfaceWidth, surfaceHeight);
            faceInfoPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(mapRect, faceInfoPaint);
            int textRectPadding = 10;
            int textRectWidth = (int) textPaint.measureText("性别:未知") + textRectPadding * 2;
            int leftTextRect;
            int topTextRect = (int) (mapRect.centerY() - textHeight * 2f - textRectPadding);
            int rightTextRect;
            int bottomTextRect = mapRect.centerY();
            if (mapRect.centerX() > surfaceWidth / 2) {
                leftTextRect = (mapRect.left - textRectWidth) / 2;
                rightTextRect = leftTextRect + textRectWidth;
                canvas.drawLine(mapRect.left, mapRect.top, rightTextRect, topTextRect, faceInfoPaint);
            } else {
                leftTextRect = mapRect.right + (surfaceWidth - mapRect.right - textRectWidth) / 2;
                rightTextRect = leftTextRect + textRectWidth;
                canvas.drawLine(mapRect.right, mapRect.top, leftTextRect, topTextRect, faceInfoPaint);
            }
            rightTextRect = leftTextRect + textRectWidth;
            faceInfoPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(leftTextRect, topTextRect, rightTextRect, bottomTextRect, faceInfoPaint);
            canvas.drawText("性别：" + model.getGender(),
                    leftTextRect + textRectPadding,
                    topTextRect + textHeight / 2 + textRectPadding,
                    textPaint);
            canvas.drawText("年龄：" + model.getAge(),
                    leftTextRect + textRectPadding,
                    topTextRect + textHeight / 2 + textRectPadding + textHeight,
                    textPaint);
        }


        private void drawFindFace(Canvas canvas, PersonModel model) {
            FaceInfo faceInfo = model.getFaceInfo();

            Matrix matrix = new Matrix();
            Matrix matrix2 = new Matrix();

            matrix.postTranslate(-scan1.getWidth() / 2, -scan1.getHeight() / 2);// 步骤1
            matrix2.postTranslate(-scan2.getWidth() / 2, -scan2.getHeight() / 2);// 步骤1

            matrix.postRotate(drawRotate);// 步骤2
            matrix2.postRotate(360 - drawRotate * 2);// 步骤2

            float scaleWidth = ((float) faceInfo.getRect().width() * (float) surfaceWidth
                    / (float) cameraWidth) / scan1.getWidth();

            matrix.postScale(scaleWidth, scaleWidth);
            matrix2.postScale(scaleWidth, scaleWidth);
            // 中心点计算
            Rect mapRect = FaceUtils.adjustRect(faceInfo.getRect(), cameraWidth, cameraHeight,
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
        faceList = new CopyOnWriteArrayList<>();

        faceInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        faceInfoPaint.setAntiAlias(true);
        faceInfoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        faceInfoPaint.setColor(Color.parseColor("#4ed0ff"));
        faceInfoPaint.setStyle(Paint.Style.STROKE);
        faceInfoPaint.setStrokeWidth(4);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(16);
        textPaint.setTextAlign(Paint.Align.LEFT);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textHeight = fontMetrics.bottom - fontMetrics.top;
    }

    // 更新人脸列表
    public void updateFace(PersonCameraModel model) {
        surfaceStop = false;
        if (model != null) {
            cameraHeight = model.getHeight();
            cameraWidth = model.getWidth();
            this.faceList.clear();
            this.faceList.addAll(model.getPersonModels());
        } else {
            this.faceList.clear();
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
