package com.harvey.arcfacedamo.ui;

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
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.harvey.arcface.R;

public class TestSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
    private boolean surfaceRun = false;
    // 旋转图片
    private Bitmap scan1;
    private Bitmap scan2;
    // SurfaceView尺寸
    private int surfaceWidth;
    private int surfaceHeight;
    private Paint rectPaint;
    private Runnable drawRunnable = new Runnable() {
        // 旋转计数器
        int drawRotate = 0;
        int drawRotateFind = 0;
        int sleepCount = 100;// 毫秒

        @Override
        public void run() {
            while (surfaceRun) {
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    drawFindFace(canvas);
                    drawRotate += 15;
                    drawRotateFind += 5;
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
                try {
                    Thread.sleep(sleepCount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void drawFindFace(Canvas canvas) {
            Matrix matrix = new Matrix();
            Matrix matrix2 = new Matrix();

            matrix.postTranslate(-scan1.getWidth() / 2, -scan1.getHeight() / 2);// 步骤1
            matrix2.postTranslate(-scan2.getWidth() / 2, -scan2.getHeight() / 2);// 步骤1

            matrix.postRotate(drawRotate);// 步骤2
            matrix2.postRotate(360 - drawRotate * 2);// 步骤2

            int centerX = surfaceWidth / 2;
            int centerY = surfaceHeight / 2;
            matrix.postTranslate(centerX, centerY);// 步骤3 屏幕的中心点
            matrix2.postTranslate(centerX, centerY);// 步骤3 屏幕的中心点

            canvas.drawBitmap(scan1, matrix, new Paint());
            canvas.drawBitmap(scan2, matrix2, new Paint());
        }

    };

    public TestSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        // 透明背景
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        rectPaint = new Paint();
        rectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(5);
    }

    @Override
    public void surfaceCreated(SurfaceHolder mSurfaceHolder) {
        surfaceRun = true;
        scan1 = BitmapFactory.decodeResource(getResources(), R.drawable.scan1);
        scan2 = BitmapFactory.decodeResource(getResources(), R.drawable.scan2);
        surfaceWidth = getWidth();
        surfaceHeight = getHeight();
        Thread thread = new Thread(drawRunnable);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        surfaceRun = false;
        scan1.recycle();
        scan2.recycle();
    }
}
