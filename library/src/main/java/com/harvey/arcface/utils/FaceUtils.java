package com.harvey.arcface.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.util.Log;

import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.util.ImageUtils;
import com.harvey.arcface.model.CameraModel;
import com.harvey.arcface.model.OneFaceCameraModel;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by harvey on 2018/2/6 0006 11:30
 */

public class FaceUtils {

    public static Bitmap getFaceBitmap(OneFaceCameraModel model) {
        return getFaceBitmap(model.getFaceInfo(), model.getWidth(), model.getHeight(), model.getNv21());
    }

    public static Bitmap getFaceBitmap(FaceInfo faceInfo, CameraModel model) {
        return getFaceBitmap(faceInfo, model.getWidth(), model.getHeight(), model.getNv21());
    }

    public static Bitmap getFaceBitmap(FaceInfo faceInfo, int cameraWidth,
                                       int cameraHeight, byte[] data) {
        YuvImage yuv = new YuvImage(data, ImageFormat.NV21, cameraWidth,
                cameraHeight, null);
        ByteArrayOutputStream ops = new ByteArrayOutputStream();
        yuv.compressToJpeg(getFaceMoreRect(faceInfo.getRect(), cameraHeight, cameraWidth), 100, ops);
        byte[] tmp = ops.toByteArray();
        Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
        try {
            ops.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Matrix m = new Matrix();
        m.setRotate(getOrientation(faceInfo.getOrient()));
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
    }

    public static void saveFaceImage(String filepath, FaceInfo faceInfo, CameraModel model) throws IOException {
        saveFaceImage(filepath, faceInfo, model.getWidth(), model.getHeight(), model.getNv21());
    }

    public static void saveFaceImage(String filepath, FaceInfo faceInfo, int cameraWidth,
                                     int cameraHeight, byte[] data) throws IOException {
        FileOutputStream fileOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        Bitmap bitmap = null;
        try {
            fileOutputStream = new FileOutputStream(filepath);
            byteArrayOutputStream = new ByteArrayOutputStream();
            YuvImage yuv = new YuvImage(data, ImageFormat.NV21, cameraWidth, cameraHeight, null);
            yuv.compressToJpeg(getFaceMoreRect(faceInfo.getRect(), cameraWidth, cameraHeight), 100, byteArrayOutputStream);
            byte[] tmp = byteArrayOutputStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
            bitmap = ImageUtils.rotateBitmap(bitmap, getOrientation(faceInfo.getOrient()));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    /**
     * @param path
     * @return
     */
    public static Bitmap decodeImage(String path) {
        Bitmap res;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = 1;
            op.inJustDecodeBounds = false;
            // op.inMutable = true;
            res = BitmapFactory.decodeFile(path, op);
            // rotate and scale.
            Matrix matrix = new Matrix();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }

            Bitmap temp = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
            Log.d("com.arcsoft", "check target Image:" + temp.getWidth() + "X" + temp.getHeight());

            if (!temp.equals(res)) {
                res.recycle();
            }
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将图像中需要截取的Rect向外扩张一倍，若扩张一倍会溢出，则扩张到边界，若Rect已溢出，则收缩到边界
     *
     * @param rect         人脸矩阵
     * @param cameraWidth  摄像头宽
     * @param cameraHeight 摄像头高
     * @return
     */
    public static Rect getFaceMoreRect(Rect rect,
                                       int cameraWidth,
                                       int cameraHeight
    ) {
        //1.原rect边界已溢出宽高的情况
        int maxOverFlow = 0;
        int tempOverFlow = 0;
        if (rect.left < 0) {
            maxOverFlow = -rect.left;
        }
        if (rect.top < 0) {
            tempOverFlow = -rect.top;
            if (tempOverFlow > maxOverFlow) {
                maxOverFlow = tempOverFlow;
            }
        }
        if (rect.right > cameraWidth) {
            tempOverFlow = rect.right - cameraWidth;
            if (tempOverFlow > maxOverFlow) {
                maxOverFlow = tempOverFlow;
            }
        }
        if (rect.bottom > cameraHeight) {
            tempOverFlow = rect.bottom - cameraHeight;
            if (tempOverFlow > maxOverFlow) {
                maxOverFlow = tempOverFlow;
            }
        }
        if (maxOverFlow != 0) {
            return new Rect(rect.left + maxOverFlow,
                    rect.top + maxOverFlow,
                    rect.right - maxOverFlow,
                    rect.bottom - maxOverFlow);
        }
        //2.原rect边界未溢出宽高的情况
        int padding = rect.height() / 2;
        //若以此padding扩张rect会溢出，取最大padding为四个边距的最小值
        if (!(rect.left - padding > 0
                && rect.right + padding < cameraWidth && rect.top - padding > 0
                && rect.bottom + padding < cameraHeight)) {
            padding = Math.min(Math.min(Math.min(rect.left, cameraWidth - rect.right), cameraHeight - rect.bottom), rect.top);
        }
        return new Rect(rect.left - padding,
                rect.top - padding,
                rect.right + padding,
                rect.bottom + padding);
    }

    public static int getOrientation(int orient) {
        switch (orient) {
            case 2:
                return 90;
            case 3:
                return 270;
            case 4:
                return 180;
            case 5:
                return 30;
            case 6:
                return 60;
            case 7:
                return 120;
            case 8:
                return 150;
            case 9:
                return 210;
            case 10:
                return 240;
            case 11:
                return 300;
            case 12:
                return 330;
            default:
                return 0;
        }
    }

    /**
     * 调整人脸框用来绘制(针对手机)
     *
     * @param rect               人脸矩阵
     * @param cameraWidth        摄像头宽
     * @param cameraHeight       摄像头高
     * @param displayOrientation 显示的角度
     * @param frontCamera        是否前置
     * @param canvasWidth
     * @param canvasHeight
     * @return 调整后的需要被绘制到View上的rect
     */
    public static Rect adjustRect(Rect rect,
                                  int cameraWidth,
                                  int cameraHeight,
                                  int displayOrientation,
                                  boolean frontCamera,
                                  int canvasWidth,
                                  int canvasHeight) {
        Rect target = new Rect(rect);
        float horizontalRatio;
        float verticalRatio;
        if (displayOrientation % 180 == 0) {
            horizontalRatio = (float) canvasWidth / (float) cameraWidth;
            verticalRatio = (float) canvasHeight / (float) cameraHeight;
        } else {
            horizontalRatio = (float) canvasHeight / (float) cameraWidth;
            verticalRatio = (float) canvasWidth / (float) cameraHeight;
        }
        target.left *= horizontalRatio;
        target.right *= horizontalRatio;
        target.top *= verticalRatio;
        target.bottom *= verticalRatio;

        Rect newRect = new Rect();
        switch (displayOrientation) {
            case 0:
                if (frontCamera) {
                    newRect.left = canvasWidth - target.right;
                    newRect.right = canvasWidth - target.left;
                } else {
                    newRect.left = target.left;
                    newRect.right = target.right;
                }
                newRect.top = target.top;
                newRect.bottom = target.bottom;
                break;
            case 90:
                newRect.right = canvasWidth - target.top;
                newRect.left = canvasWidth - target.bottom;
                if (frontCamera) {
                    newRect.top = canvasHeight - target.right;
                    newRect.bottom = canvasHeight - target.left;
                } else {
                    newRect.top = target.left;
                    newRect.bottom = target.right;
                }
                break;
            case 180:
                newRect.top = canvasHeight - target.bottom;
                newRect.bottom = canvasHeight - target.top;
                if (frontCamera) {
                    newRect.left = target.left;
                    newRect.right = target.right;
                } else {
                    newRect.left = canvasWidth - target.right;
                    newRect.right = canvasWidth - target.left;
                }
                break;
            case 270:
                newRect.left = target.top;
                newRect.right = target.bottom;
                if (frontCamera) {
                    newRect.top = target.left;
                    newRect.bottom = target.right;
                } else {
                    newRect.top = canvasHeight - target.right;
                    newRect.bottom = canvasHeight - target.left;
                }
                break;
            default:
                break;
        }
        return newRect;
    }

    /**
     * 实际展示 宽高相反
     *
     * @param rect         人脸矩阵
     * @param cameraWidth  摄像头宽
     * @param cameraHeight 摄像头高
     * @param mappedWidth
     * @param mappedHeight
     * @return
     */
    public static Rect getMappedFaceRect(Rect rect,
                                         int cameraWidth,
                                         int cameraHeight,
                                         int mappedWidth,
                                         int mappedHeight) {
        int left = rect.right * mappedWidth / cameraWidth;
        int top = rect.top * mappedHeight / cameraHeight;
        int right = rect.left * mappedWidth / cameraWidth;
        int bottom = rect.bottom * mappedHeight / cameraHeight;
        return new Rect(left, top, right, bottom);
    }
}
