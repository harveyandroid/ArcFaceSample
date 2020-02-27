package com.harvey.arcface.utils

import android.graphics.*
import android.media.ExifInterface
import android.util.Log
import com.arcsoft.face.util.ImageUtils
import com.harvey.arcface.model.FeatureModel
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by harvey on 2018/2/6 0006 11:30
 */

object FaceUtils {

    fun getFaceBitmap(faceFindModel: FeatureModel, data: ByteArray): Bitmap {
        val yuv = YuvImage(data, ImageFormat.NV21, faceFindModel.cameraWidth,
                faceFindModel.cameraHeight, null)
        val ops = ByteArrayOutputStream()
        yuv.compressToJpeg(faceFindModel.faceMoreRect, 100, ops)
        val tmp = ops.toByteArray()
        val bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
        try {
            ops.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val m = Matrix()
        m.setRotate(faceFindModel.orientation.toFloat())
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }


    @Throws(IOException::class)
    fun saveFaceImage(filepath: String, faceFindModel: FeatureModel, data: ByteArray) {
        var fileOutputStream: FileOutputStream? = null
        var byteArrayOutputStream: ByteArrayOutputStream? = null
        var bitmap: Bitmap? = null
        try {
            fileOutputStream = FileOutputStream(filepath)
            byteArrayOutputStream = ByteArrayOutputStream()
            val yuv = YuvImage(data, ImageFormat.NV21, faceFindModel.cameraWidth,
                    faceFindModel.cameraHeight, null)
            yuv.compressToJpeg(faceFindModel.faceMoreRect, 100, byteArrayOutputStream)
            val tmp = byteArrayOutputStream.toByteArray()
            bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
            bitmap = ImageUtils.rotateBitmap(bitmap, faceFindModel.orientation.toFloat())
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
        } finally {
            fileOutputStream?.close()
            byteArrayOutputStream?.close()
            bitmap?.recycle()
        }
    }

    /**
     * @param path
     * @return
     */
    fun decodeImage(path: String): Bitmap? {
        val res: Bitmap
        try {
            val exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            val op = BitmapFactory.Options()
            op.inSampleSize = 1
            op.inJustDecodeBounds = false
            // op.inMutable = true;
            res = BitmapFactory.decodeFile(path, op)
            // rotate and scale.
            val matrix = Matrix()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            val temp = Bitmap.createBitmap(res, 0, 0, res.width, res.height, matrix, true)
            Log.d("com.arcsoft", "check target Image:" + temp.width + "X" + temp.height)

            if (temp != res) {
                res.recycle()
            }
            return temp
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

}
