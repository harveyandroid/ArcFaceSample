package com.harvey.arcface.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.harvey.arcface.moodel.FaceFindModel;
import com.harvey.arcface.R;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SurfaceViewFace extends SurfaceView implements SurfaceHolder.Callback {

	Thread thread;
	private SurfaceHolder surfaceHolder;
	private boolean surfaceRun = false;
	private boolean surfaceStop = false;
	// 旋转图片
	private Bitmap scan1;
	private Bitmap scan2;
	private Bitmap scan3;
	// SurfaceView尺寸
	private int surfaceWidth;
	private int surfaceHeight;
	// 人脸数据列表
	private List<FaceFindModel> faceFindModels;
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
						for (FaceFindModel faceFindModel : faceFindModels) {
							drawNoFindFace(canvas, faceFindModel);
						}
						drawRotate += 15;
						drawRotateFind += 5;
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
				try {
					Thread.sleep(sleepCount);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void drawNoFindFace(Canvas canvas, FaceFindModel faceFindModel) {
			Matrix matrix = new Matrix();
			Matrix matrix2 = new Matrix();

			matrix.postTranslate(-scan1.getWidth() / 2, -scan1.getHeight() / 2);// 步骤1
			matrix2.postTranslate(-scan2.getWidth() / 2, -scan2.getHeight() / 2);// 步骤1

			matrix.postRotate(drawRotate);// 步骤2
			matrix2.postRotate(360 - drawRotate * 2);// 步骤2

			// 缩放
			float scaleWidth = ((float) faceFindModel.getFaceRect().width() * (float) surfaceWidth
					/ (float) faceFindModel.getCameraWidth()) / scan1.getWidth();
			matrix.postScale(scaleWidth, scaleWidth);
			matrix2.postScale(scaleWidth, scaleWidth);
			// 中心点计算
			Rect mapRect = faceFindModel.getMappedFaceRect(surfaceHeight, surfaceWidth);

			int centerX = mapRect.centerX();
			int centerY = mapRect.centerY();

			matrix.postTranslate(centerX, centerY);// 步骤3 屏幕的中心点
			matrix2.postTranslate(centerX, centerY);// 步骤3 屏幕的中心点

			canvas.drawBitmap(scan1, matrix, null);
			canvas.drawBitmap(scan2, matrix2, null);
		}

	};

	public SurfaceViewFace(Context context, AttributeSet attrs) {
		super(context, attrs);
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		// 透明背景
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		faceFindModels = new CopyOnWriteArrayList<>();
	}

	// 更新人脸列表
	public void updateFace(List<FaceFindModel> faceFindModels) {
		surfaceStop = false;
		this.faceFindModels.clear();
		this.faceFindModels.addAll(faceFindModels);
	}

	@Override
	public void surfaceCreated(SurfaceHolder mSurfaceHolder) {
		surfaceRun = true;
		surfaceStop = true;
		scan1 = BitmapFactory.decodeResource(getResources(), R.drawable.scan1);
		scan2 = BitmapFactory.decodeResource(getResources(), R.drawable.scan2);
		scan3 = BitmapFactory.decodeResource(getResources(), R.drawable.scan3);
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
		scan3.recycle();
		while (thread.isAlive()) {
		}
	}
}
