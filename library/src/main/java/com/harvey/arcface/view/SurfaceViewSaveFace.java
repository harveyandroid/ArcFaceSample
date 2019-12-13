package com.harvey.arcface.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.arcsoft.face.util.ImageUtils;
import com.harvey.arcface.model.FeatureCameraModel;
import com.harvey.arcface.model.FeatureModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by harvey on 2018/1/12.
 */
public class SurfaceViewSaveFace extends SurfaceView implements SurfaceHolder.Callback {
    public static final int ERROR_NO = 0;
    public static final int ERROR_MOREFACE = 1;
    public static final int ERROR_NOFACE = 2;
    public static final int ERROR_OUTFACE = 3;
    public static final int ERROR_FARFACE = 4;
    private static final int SLEEP_COUNT = 100;// 毫秒
    private int TimeSecondDown = 3;
    private int ERRORCODE = 0;
    private SurfaceHolder surfaceHolder;
    private boolean surfaceRun = false;
    private boolean surfaceStop = true;
    // SurfaceView尺寸
    private int surfaceWidth;
    private int surfaceHeight;
    // 人脸数据列表
    private FeatureCameraModel faceModel;
    private SaveFaceHandler saveFaceHandler;
    private Lock mLock = new ReentrantLock(true);
    private Condition condition = mLock.newCondition();
    private boolean frontCamera = true;//默认前置
    private int displayOrientation = 0;
    private Thread thread;
    private Rect faceOut;
    private int timeCount = 0;
    public Runnable drawRunnable = new Runnable() {

        @Override
        public void run() {
            while (surfaceRun) {
                if (!surfaceStop) {
                    // 判断相框时间>时间退出
                    if (timeCount * SLEEP_COUNT > TimeSecondDown * 1000) {
                        surfaceStop = true;
                        mLock.lock();
                        try {
                            saveFaceHandler.onSuccess(faceModel.clone());
                        } finally {
                            mLock.unlock();
                        }
                        continue;
                    }
                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        mLock.lock();
                        try {
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            if (faceModel == null || faceModel.getFeatureModels() == null
                                    || faceModel.getFeatureModels().size() == 0) {
                                drawFaceOutRect(canvas, false);
                                callErrorBack(ERROR_NOFACE);
                            } else if (faceModel.getFeatureModels().size() != 1) {
                                drawFaceOutRect(canvas, false);
                                callErrorBack(ERROR_MOREFACE);
                            } else {
                                Rect face = faceModel.getFeatureModels().get(0).adjustRect(displayOrientation, frontCamera, surfaceWidth, surfaceHeight);
                                // 在矩形框外侧
                                if (!faceOut.contains(face)) {
                                    drawFaceOutRect(canvas, false);
                                    drawFaceRect(canvas, face, false, "在矩形框外侧");
                                    callErrorBack(ERROR_OUTFACE);
                                }
                                // 距离摄像头过远
                                else if (face.width() < 300) {
                                    drawFaceOutRect(canvas, false);
                                    drawFaceRect(canvas, face, false, "距离摄像头过远");
                                    callErrorBack(ERROR_FARFACE);
                                } else {
                                    drawFaceOutRect(canvas, true);
                                    drawFaceRect(canvas, face, true);
//                                    drawFaceImg(canvas, faceModel.getFeatureModels().get(0), faceModel.getCameraData());
                                    callTimeBack(timeCount * SLEEP_COUNT);
                                    timeCount++;
                                }
                            }
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        } finally {
                            mLock.unlock();
                        }
                    }
                }
                try {
                    Thread.sleep(SLEEP_COUNT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void callErrorBack(int errorCode) {
            timeCount = 0;
            if (ERRORCODE != errorCode) {
                saveFaceHandler.onErrorMsg(errorCode);
                ERRORCODE = errorCode;
            }
        }

        // 毫秒数
        private void callTimeBack(int time) {
            ERRORCODE = ERROR_NO;
            if (time == 0 || time % 1000 == 0) {
                saveFaceHandler.onTimeSecondDown(time / 1000);
            }
        }

        // 绘制外矩形框
        private void drawFaceOutRect(Canvas canvas, boolean success) {
            Paint paint = new Paint();
            if (success)
                paint.setColor(Color.parseColor("#3498db"));
            else
                paint.setColor(Color.parseColor("#e74c3c"));

            paint.setStrokeWidth(20f);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(faceOut, paint);
        }

        // 绘制人脸框
        private void drawFaceRect(Canvas canvas, Rect face, boolean success) {
            Paint paint = new Paint();
            if (success)
                paint.setColor(Color.parseColor("#3498db"));
            else
                paint.setColor(Color.parseColor("#e74c3c"));

            paint.setStrokeWidth(10f);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(face, paint);
        }

        // 绘制人脸框
        private void drawFaceRect(Canvas canvas, Rect face, boolean success, String remindText) {
            Paint paint = new Paint();
            if (success)
                paint.setColor(Color.parseColor("#3498db"));
            else
                paint.setColor(Color.parseColor("#e74c3c"));
            paint.setStrokeWidth(10f);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(face, paint);
            // 绘制坐标
            paint.reset();
            paint.setTextSize(16);
            paint.setColor(Color.BLUE);
            canvas.drawText(face.toString(), 100, 100, paint);
            canvas.drawText(remindText, 200, 100, paint);

        }

        // 绘制人脸
        private void drawFaceImg(Canvas canvas, FeatureModel model, byte[] data) {
            ByteArrayOutputStream ops = null;
            Bitmap bmp = null;
            try {
                YuvImage yuv = new YuvImage(data, ImageFormat.NV21, model.getCameraWidth(), model.getCameraHeight(), null);
                ops = new ByteArrayOutputStream();
                yuv.compressToJpeg(model.getFaceMoreRect(), 100, ops);
                byte[] tmp = ops.toByteArray();
                bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
                bmp = ImageUtils.rotateBitmap(bmp, model.getOrientation());
                canvas.drawBitmap(bmp, 100, 100, null);
            } finally {
                if (ops != null) {
                    try {
                        ops.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bmp != null) {
                    bmp.recycle();
                }
            }
        }

    };

    public SurfaceViewSaveFace(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        // 透明背景
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        saveFaceHandler = new SaveFaceHandler();
    }

    public void uploadTimeSecondDown(int time) {
        this.TimeSecondDown = time;
    }

    public void setFrontCamera(boolean frontCamera) {
        this.frontCamera = frontCamera;
    }

    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    public void reset() {
        timeCount = 0;
        surfaceStop = false;
    }

    // 更新人脸列表
    public void uploadFace(List<FeatureModel> faceFindModels, byte[] frameBytes, int cameraWidth, int cameraHeight) {
        if (!surfaceRun)
            return;
        if (surfaceStop)
            return;
        mLock.lock();
        try {
            if (faceModel == null) {
                faceModel = new FeatureCameraModel(faceFindModels, frameBytes, cameraWidth, cameraHeight);
            } else {
                faceModel.setFeatureModels(faceFindModels);
                faceModel.setNv21(frameBytes);
                faceModel.setWidth(cameraWidth);
                faceModel.setHeight(cameraHeight);
            }
        } finally {
            mLock.unlock();
        }
    }

    public void setSaveFaceListener(SaveFaceListener saveFaceListener) {
        saveFaceHandler.setListener(saveFaceListener);
    }

    @Override
    public void surfaceCreated(SurfaceHolder mSurfaceHolder) {
        surfaceRun = true;
        surfaceStop = false;
        surfaceWidth = getWidth();
        surfaceHeight = getHeight();
        thread = new Thread(drawRunnable);
        thread.start();
        // 设置矩形框
        faceOut = new Rect(100, 100, surfaceWidth - 100, surfaceHeight - 100);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        setSaveFaceListener(null);
        surfaceRun = false;
        surfaceStop = true;
        while (thread.isAlive()) {
        }
    }

    public interface SaveFaceListener {
        void onSuccess(FeatureCameraModel faceModel);

        void onTimeSecondDown(int TimeSecond);

        void onErrorMsg(int errorCode);
    }

    private class SaveFaceHandler extends Handler {
        private final int onSuccess = 1;
        private final int onTimeSecondDown = 2;
        private final int onErrorMsg = 3;
        private SaveFaceListener msaveFaceListener;

        public void setListener(SaveFaceListener saveFaceListener) {
            this.msaveFaceListener = saveFaceListener;
        }

        public void onSuccess(FeatureCameraModel faceModel) {
            Message msg = new Message();
            msg.what = onSuccess;
            msg.obj = faceModel;
            sendMessage(msg);
        }

        public void onTimeSecondDown(int TimeSecond) {
            Message msg = new Message();
            msg.what = onTimeSecondDown;
            msg.obj = TimeSecond;
            sendMessage(msg);

        }

        public void onErrorMsg(int errorCode) {
            Message msg = new Message();
            msg.what = onErrorMsg;
            msg.obj = errorCode;
            sendMessage(msg);

        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msaveFaceListener != null) {
                switch (msg.what) {
                    case onSuccess:
                        msaveFaceListener.onSuccess((FeatureCameraModel) msg.obj);
                        break;
                    case onTimeSecondDown:
                        msaveFaceListener.onTimeSecondDown((int) msg.obj);
                        break;
                    case onErrorMsg:
                        msaveFaceListener.onErrorMsg((int) msg.obj);
                        break;
                }
            }
        }
    }
}
