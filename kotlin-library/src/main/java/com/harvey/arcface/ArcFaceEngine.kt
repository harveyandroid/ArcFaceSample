package com.harvey.arcface

import android.graphics.*
import android.util.Log
import com.arcsoft.ageestimation.ASAE_FSDKEngine
import com.arcsoft.ageestimation.ASAE_FSDKError
import com.arcsoft.facedetection.AFD_FSDKEngine
import com.arcsoft.facedetection.AFD_FSDKError
import com.arcsoft.facerecognition.AFR_FSDKEngine
import com.arcsoft.facerecognition.AFR_FSDKError
import com.arcsoft.facerecognition.AFR_FSDKFace
import com.arcsoft.facerecognition.AFR_FSDKMatching
import com.arcsoft.facetracking.AFT_FSDKEngine
import com.arcsoft.facetracking.AFT_FSDKError
import com.arcsoft.facetracking.AFT_FSDKFace
import com.arcsoft.genderestimation.ASGE_FSDKEngine
import com.arcsoft.genderestimation.ASGE_FSDKError
import com.harvey.arcface.model.FaceFindMatchModel
import com.harvey.arcface.model.FaceFindModel
import com.harvey.db.OwnerDBHelper
import com.harvey.db.bean.RegisteredFace
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by hanhui on 2018/6/1 0001 10:10
 */
object ArcFaceEngine {
    private const val TAG = "FaceEngine"
    const val APP_ID = "C97rEZvvFGDjx7gJVJszZGhujaKNyrHU5soLL4KBsVFg"
    const val FT_SDK_KEY = "DFkP1xTwNThNx4A3B1vo9GNXwLDMXbUXxSbctPMmBg18"
    const val FD_SDK_KEY = "DFkP1xTwNThNx4A3B1vo9GNf6jUWZ8xnQnh8pLMrbVJm"
    const val FR_SDK_KEY = "DFkP1xTwNThNx4A3B1vo9GP9kLX96vhH7zNSFuJaHBVt"
    const val SAE_SDK_KEY = "DFkP1xTwNThNx4A3B1vo9GPQ593XFB72SFtK2HiDdJd8"
    const val SGE_SDK_KEY = "DFkP1xTwNThNx4A3B1vo9GPXEYJeWF7ha1KULCQirDgC"
    // 一次性识别人脸数量[1,50]
    private const val maxFaceNum = 10
    // 最小人脸尺寸[2,32]
    private const val minFaceSize = 16
    // 对比置信度
    private const val SCORE = 0.75f

    private val FT_engine: AFT_FSDKEngine// 视频人脸跟踪
    private val SAE_engine: ASAE_FSDKEngine// 年龄检测
    private val FD_engine: AFD_FSDKEngine// 人脸检测
    private val FR_engine: AFR_FSDKEngine// 人脸对比
    private val SGE_engine: ASGE_FSDKEngine// 性别检测

    init {
        FT_engine = AFT_FSDKEngine()
        FD_engine = AFD_FSDKEngine()
        FR_engine = AFR_FSDKEngine()
        SAE_engine = ASAE_FSDKEngine()
        SGE_engine = ASGE_FSDKEngine()
        val SAE_err = SAE_engine.ASAE_FSDK_InitAgeEngine(APP_ID, SAE_SDK_KEY)
        if (SAE_err.code != ASAE_FSDKError.MOK)
            Log.e(TAG, "ASAE_FSDK_InitAgeEngine = " + SAE_err.code)

        val FD_err = FD_engine.AFD_FSDK_InitialFaceEngine(APP_ID, FD_SDK_KEY,
                AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, minFaceSize, maxFaceNum)
        if (FD_err.code != AFD_FSDKError.MOK)
            Log.d(TAG, "AFD_FSDK_InitAgeEngine = " + FD_err.code)

        val FR_error = FR_engine.AFR_FSDK_InitialEngine(APP_ID, FR_SDK_KEY)
        if (FR_error.code != AFR_FSDKError.MOK)
            Log.d(TAG, "AFR_FSDK_InitialEngine = " + FR_error.code)

        val AFT_err = FT_engine.AFT_FSDK_InitialFaceEngine(APP_ID, FT_SDK_KEY,
                AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, minFaceSize, maxFaceNum)
        if (AFT_err.code != AFT_FSDKError.MOK)
            Log.d(TAG, "AFT_FSDK_InitialFaceEngine =" + AFT_err.code)

        val ASGE_err = SGE_engine.ASGE_FSDK_InitgGenderEngine(APP_ID,
                SGE_SDK_KEY)
        if (ASGE_err.code != ASGE_FSDKError.MOK)
            Log.d(TAG, "ASGE_FSDK_InitgGenderEngine = " + ASGE_err.code)
    }

    fun destroy() {
        SAE_engine.ASAE_FSDK_UninitAgeEngine()
        FD_engine.AFD_FSDK_UninitialFaceEngine()
        FR_engine.AFR_FSDK_UninitialEngine()
        FT_engine.AFT_FSDK_UninitialFaceEngine()
        SGE_engine.ASGE_FSDK_UninitGenderEngine()
    }

    fun detectFace(data: ByteArray, width: Int, height: Int): List<FaceFindModel> {
        var result = mutableListOf<AFT_FSDKFace>()
        var faceFindModels = mutableListOf<FaceFindModel>()
        val err = FT_engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine.CP_PAF_NV21,
                result)
        if (err.code == AFT_FSDKError.MOK) {
            result.forEach { fsdkFace ->
                val model = FaceFindModel(width, height, fsdkFace.rect, fsdkFace.degree)
                faceFindModels.add(model)
            }
        } else {
            Log.e(TAG, "AFT_FSDK_FaceFeatureDetect fail! error code :" + err.code)
        }
        return faceFindModels
    }

    fun saveFace(data: ByteArray, model: FaceFindModel, name: String, age: Int, sex: String, path: String): Boolean {
        val result = AFR_FSDKFace()
        var isSuccess = false
        val error = FR_engine.AFR_FSDK_ExtractFRFeature(data, model.cameraWidth,
                model.cameraHeight, AFR_FSDKEngine.CP_PAF_NV21, model.faceRect, model.degree, result)
        if (error.code == AFR_FSDKError.MOK) {
            try {
                val imgFile = path + File.separator + name + ".jpg"
                saveFaceImage(imgFile, model, data)
                val registeredFace = RegisteredFace()
                registeredFace.age = age
                registeredFace.gender = sex
                registeredFace.featureData = result.featureData
                registeredFace.name = name
                registeredFace.imagePath = imgFile
                registeredFace.setFaceTime(System.currentTimeMillis())
                Log.d(TAG, "人脸信息保存到本地数据库：" + registeredFace.toString())
                OwnerDBHelper.saveRegisteredFace(registeredFace)
                isSuccess = true
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
            Log.d(TAG, "人脸特征生成出错：" + error.code)
        }
        return isSuccess
    }

    /**
     * 单张人脸与库中人脸对比
     * 人脸对比很耗资源，所以用完就释放
     *
     * @param data            人脸图片数据
     * @param findModel       人脸标定数据
     * @param registeredFaces 本地人脸库
     * @return
     */
    fun matchFace(data: ByteArray, findModel: FaceFindModel, registeredFaces: List<RegisteredFace>): FaceFindMatchModel {
        val begin = System.currentTimeMillis()
        val findMatchingModel = FaceFindMatchModel()
        val FR_engine = AFR_FSDKEngine()
        val afr_fsdkError = FR_engine.AFR_FSDK_InitialEngine(APP_ID, FR_SDK_KEY)
        if (afr_fsdkError.code != AFR_FSDKError.MOK) {
            Log.e(TAG, "AFR_FSDK_InitialEngine fail! error code :" + afr_fsdkError.code)
        }
        val result = AFR_FSDKFace()
        val error = FR_engine.AFR_FSDK_ExtractFRFeature(data, findModel.cameraWidth,
                findModel.cameraHeight, AFR_FSDKEngine.CP_PAF_NV21, findModel.faceRect,
                findModel.degree, result)
        if (error.code == AFR_FSDKError.MOK) {
            // 记录对比开始时间
            val matchingBegin = System.currentTimeMillis()
            // 本地存储数据依次比对
            Log.e(TAG, "本地存储数据依次比对：" + registeredFaces.size)
            for (registeredFace in registeredFaces) {
                // 进行人脸近似度对比
                val input = AFR_FSDKFace()
                input.featureData = registeredFace.featureData
                val matching = AFR_FSDKMatching()
                val err = FR_engine.AFR_FSDK_FacePairMatching(result, input, matching)
                if (err.code == AFR_FSDKError.MOK) {
                    Log.e(TAG, "对比分数：" + matching.score)
                    if (matching.score > SCORE) {
                        // 没有对比成功的记录
                        if (!findMatchingModel.isMatching) {
                            findMatchingModel.isMatching = false
                            findMatchingModel.score = matching.score
                            findMatchingModel.imagePath = registeredFace.imagePath
                            findMatchingModel.student_id = registeredFace.person_id!!
                            findMatchingModel.name = registeredFace.name
                            findMatchingModel.gender = registeredFace.gender
                        } else {
                            if (matching.score > findMatchingModel.score) {
                                findMatchingModel.isMatching = false
                                findMatchingModel.score = matching.score
                                findMatchingModel.imagePath = registeredFace.imagePath
                                findMatchingModel.student_id = registeredFace.person_id!!
                                findMatchingModel.name = registeredFace.name
                                findMatchingModel.gender = registeredFace.gender
                            }
                        }
                    }
                }
            }
            val MatingbeginTime = System.currentTimeMillis() - matchingBegin
            Log.e(TAG, "人脸对比耗费时间：" + MatingbeginTime)
        } else {
            Log.e(TAG, "人脸特征生成出错：" + error.code)
        }
        val beginTime = System.currentTimeMillis() - begin
        Log.e(TAG, "总对比耗费时间：" + beginTime)
        FR_engine.AFR_FSDK_UninitialEngine()
        return findMatchingModel
    }

    fun getFaceBitmap(faceFindModel: FaceFindModel, data: ByteArray): Bitmap {
        val yuv = YuvImage(data, ImageFormat.NV21, faceFindModel.cameraWidth,
                faceFindModel.cameraHeight, null)
        val ops = ByteArrayOutputStream()
        yuv.compressToJpeg(faceFindModel.getFaceMoreRect(), 100, ops)
        val tmp = ops.toByteArray()
        val bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
        try {
            ops.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val m = Matrix()
        m.setRotate(faceFindModel.getOrientation().toFloat())
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }


    @Throws(IOException::class)
    fun saveFaceImage(filepath: String, faceFindModel: FaceFindModel, data: ByteArray) {
        val stream = FileOutputStream(filepath)
        val ops = ByteArrayOutputStream()
        val yuv = YuvImage(data, ImageFormat.NV21, faceFindModel.cameraWidth,
                faceFindModel.cameraHeight, null)
        yuv.compressToJpeg(faceFindModel.getFaceMoreRect(), 100, ops)
        val tmp = ops.toByteArray()
        var bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
        val m = Matrix()
        m.setRotate(faceFindModel.getOrientation().toFloat())
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        ops.close()
        stream.close()
        bmp.recycle()
    }

}