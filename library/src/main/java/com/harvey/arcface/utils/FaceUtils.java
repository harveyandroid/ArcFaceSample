package com.harvey.arcface.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.util.Log;

import com.harvey.arcface.moodel.FaceFindModel;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by harvey on 2018/2/6 0006 11:30
 */

public class FaceUtils {

    public static Bitmap getFaceBitmap(FaceFindModel faceFindModel, byte[] data) {
        YuvImage yuv = new YuvImage(data, ImageFormat.NV21, faceFindModel.getCameraWidth(),
                faceFindModel.getCameraHeight(), null);
        ByteArrayOutputStream ops = new ByteArrayOutputStream();
        yuv.compressToJpeg(faceFindModel.getFaceMoreRect(), 100, ops);
        byte[] tmp = ops.toByteArray();
        Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
        try {
            ops.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Matrix m = new Matrix();
        m.setRotate(faceFindModel.getOrientation());
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
    }


    public static void saveFaceImage(String filepath, FaceFindModel faceFindModel, byte[] data) throws IOException {
        FileOutputStream stream = new FileOutputStream(filepath);
        ByteArrayOutputStream ops = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(data, ImageFormat.NV21, faceFindModel.getCameraWidth(),
                faceFindModel.getCameraHeight(), null);
        yuv.compressToJpeg(faceFindModel.getFaceMoreRect(), 100, ops);
        byte[] tmp = ops.toByteArray();
        Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
        Matrix m = new Matrix();
        m.setRotate(faceFindModel.getOrientation());
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        stream.flush();
        ops.close();
        stream.close();
        bmp.recycle();
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

}
