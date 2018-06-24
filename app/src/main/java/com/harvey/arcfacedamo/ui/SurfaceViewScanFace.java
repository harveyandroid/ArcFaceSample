package com.harvey.arcfacedamo.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.harvey.arcfacedamo.R;
import com.harvey.arcface.moodel.FaceFindCameraModel;
import com.harvey.arcface.moodel.FaceFindModel;
import com.harvey.arcfacedamo.utils.DialogUtil;
import com.harvey.arcfacedamo.utils.DisplayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by harvey on 2018/5/2 0002 09:31
 */

public class SurfaceViewScanFace extends SurfaceView implements SurfaceHolder.Callback {
	public static final int ERROR_NO = 0;
	public static final int ERROR_MOREFACE = 1;
	public static final int ERROR_NOFACE = 2;
	public static final int ERROR_OUTFACE = 3;

	final float STARTANGLE = 110;
	final float SUMANGLE = 320;
	final float TEXT_BG_ANGLE = 120;
	final int MIN_FACE_WIDTH = 200;
	String name = "";
	int age = 0;
	String sex = "";
	private SurfaceHolder surfaceHolder;
	private Thread thread;
	private int surfaceWidth;
	private int surfaceHeight;
	private boolean surfaceRun = false;
	private boolean surfaceStop = false;
	private Paint mPaint;
	private Paint mTextPaint;
	private float roundWidth = 0f;
	private int roundColor = Color.GRAY;
	private int roundProgressColor = Color.BLUE;
	private int progress = 0;
	private Rect faceOut;
	private SaveFaceHandler saveFaceHandler;
	private FaceFindCameraModel faceModel;
	private Runnable drawRunnable = new Runnable() {
		int sleepCount = 100;

		@Override
		public void run() {
			while (surfaceRun) {
				if (!surfaceStop) {
					Canvas canvas = surfaceHolder.lockCanvas();
					if (canvas != null) {
						String remindText = "";
						canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
						if (faceModel == null || faceModel.getFaceFindModels() == null
								|| faceModel.getFaceFindModels().size() == 0) {
							remindText = "没有检测到人脸";
							progress = 0;
						} else if (faceModel.getFaceFindModels().size() != 1) {
							progress = 30;
							remindText = "请保持一个人脸";
						} else {
							Rect face = faceModel.getFaceFindModels().get(0).getMappedFaceRect(surfaceWidth,
									surfaceHeight);
							// 在矩形框外侧
							if (!faceOut.contains(face)) {
								progress = 60;
								remindText = "请保持屏幕居中";
							} else if (face.width() < MIN_FACE_WIDTH) {
								progress = 90;
								remindText = "请靠近一点";
							} else {
								progress = 100;
								surfaceStop = true;
								saveFaceHandler.onSuccess(faceModel);
							}
						}
						drawScanView(remindText, canvas);
					}
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
				try {
					Thread.sleep(sleepCount);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		void drawScanView(String remindTex, Canvas canvas) {
			if (progress == 100) {
				drawFaceImg(canvas, faceModel.getFaceFindModels().get(0), faceModel.getCameraData());
				return;
			}
			canvas.drawColor(Color.WHITE);
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			float centerX = surfaceWidth / 2;
			float centerY = surfaceHeight / 3;
			float circleRadius = surfaceWidth / 4;
			float loopRadius = circleRadius + roundWidth;

			// 设置透明圆
			mPaint.reset();
			mPaint.setAntiAlias(true);
			mPaint.setFilterBitmap(true);
			mPaint.setColor(Color.TRANSPARENT);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			mPaint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(centerX, centerY, circleRadius, mPaint);

			// 设置字体半透明背景
			mPaint.reset();
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setColor(Color.parseColor("#66000000"));
			RectF textBgRect = new RectF(centerX - circleRadius, centerY - circleRadius, centerX + circleRadius,
					centerY + circleRadius);
			canvas.drawArc(textBgRect, 210, TEXT_BG_ANGLE, false, mPaint);

			// 设置字体
			Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
			// 计算字体中心偏移圆点距离
			float textOffCircle = (float) (circleRadius / 2 * (1 + Math.cos(TEXT_BG_ANGLE / 2 * Math.PI / 180)));
			float top = Math.abs(fontMetrics.top);// 为基线到字体上边框的距离
			float bottom = Math.abs(fontMetrics.bottom);// 为基线到字体下边框的距离
			float baseLineY = centerY - (textOffCircle - top / 2 - bottom / 2);// 基线中间点的y轴计算公式
			canvas.drawText(remindTex, centerX, baseLineY, mTextPaint);

			mPaint.reset();
			mPaint.setStrokeWidth(roundWidth); // 设置圆环的宽度
			RectF oval = new RectF(centerX - loopRadius, centerY - loopRadius, centerX + loopRadius,
					centerY + loopRadius);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setColor(roundColor);
			canvas.drawArc(oval, STARTANGLE, SUMANGLE, false, mPaint);
			mPaint.setColor(roundProgressColor);
			canvas.drawArc(oval, STARTANGLE, getEndAngle(progress), false, mPaint);
		}

		float getEndAngle(int progress) {
			float endAngle = progress * 1.0f / 100 * SUMANGLE;
			return endAngle;
		}

	};
	public SurfaceViewScanFace(Context context, AttributeSet attrs) {
		super(context, attrs);
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		mPaint = new Paint();
		roundWidth = DisplayUtils.dp2px(context, 5);
		mTextPaint = new Paint();
		mTextPaint.setTextSize(DisplayUtils.dp2px(getContext(), 16));
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		saveFaceHandler = new SaveFaceHandler();
	}

	// 更新人脸列表
	public void updateFace(FaceFindCameraModel faceModel) {
		if (!surfaceStop) {
			this.faceModel = faceModel;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceRun = true;
		surfaceStop = false;
		surfaceWidth = getWidth();
		surfaceHeight = getHeight();
		faceOut = new Rect(surfaceWidth / 5, surfaceHeight / 6, surfaceWidth * 4 / 5, surfaceHeight * 5 / 6);
		thread = new Thread(drawRunnable);
		thread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		setSaveFaceListener(null);
		surfaceRun = false;
		surfaceStop = true;
	}

	public void showEditDialog(final FaceFindModel faceFindModel, final byte[] data) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_face_info, null);
		final EditText etName = view.findViewById(R.id.et_name);
		RadioGroup rgSex = view.findViewById(R.id.rg_sex);
		final EditText etAge = view.findViewById(R.id.et_age);

		rgSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.rb_man) {
					sex = "男";

				} else if (checkedId == R.id.rb_women) {
					sex = "女";
				}
			}
		});
		builder.setTitle("请输入人脸信息").setView(view).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		}).setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				name = etName.getText().toString();
				age = TextUtils.isEmpty(etAge.getText().toString()) ? 0 : Integer.parseInt(etAge.getText().toString());
				// ArcFaceEngine.getInstance().saveFace(data, faceFindModel,
				// name, age, sex, FileUtil.getFaceDir());
				surfaceStop = false;
				// 继续识别人脸
			}
		});
	}

	// 绘制人脸
	private void drawFaceImg(Canvas canvas, FaceFindModel faceFindModel, byte[] data) {
		YuvImage yuv = new YuvImage(data, ImageFormat.NV21, faceFindModel.getCameraWidth(),
				faceFindModel.getCameraHeight(), null);
		ByteArrayOutputStream ops = new ByteArrayOutputStream();
		yuv.compressToJpeg(faceFindModel.getFaceMoreRect(), 100, ops);
		byte[] tmp = ops.toByteArray();
		Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
		canvas.drawBitmap(bmp, surfaceWidth / 5, surfaceHeight / 6, null);
		try {
			ops.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setSaveFaceListener(SaveFaceListener saveFaceListener) {
		saveFaceHandler.setListener(saveFaceListener);
	}
	public interface SaveFaceListener {
		void onSuccess(FaceFindCameraModel faceModel);
		void onTimeSecondDown(int TimeSecond);
		void onErrorMsg(int errorCode);
	}

	class SaveFaceHandler extends Handler {
		private final int onSuccess = 1;
		private final int onTimeSecondDown = 2;
		private final int onErrorMsg = 3;
		private SaveFaceListener msaveFaceListener;

		public void setListener(SaveFaceListener saveFaceListener) {
			this.msaveFaceListener = saveFaceListener;
		}

		public void onSuccess(FaceFindCameraModel faceModel) {
			Message msg = new Message();
			msg.what = onSuccess;
			msg.obj = faceModel;
			sendMessage(msg);
		}

		@Override
		public void handleMessage(final Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case onSuccess :
					final FaceFindCameraModel findCameraModel = (FaceFindCameraModel) msg.obj;
					DialogUtil.showDialog(getContext(), "提示", "是否保存当前人脸?", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							showEditDialog(findCameraModel.getFaceFindModels().get(0), findCameraModel.getCameraData());
						}
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							surfaceStop = false;
						}
					});
					break;
			}
		}
	}
}
