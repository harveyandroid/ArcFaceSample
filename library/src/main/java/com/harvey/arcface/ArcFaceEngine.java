package com.harvey.arcface;

import android.util.Log;

import com.arcsoft.ageestimation.ASAE_FSDKEngine;
import com.arcsoft.ageestimation.ASAE_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.genderestimation.ASGE_FSDKEngine;
import com.arcsoft.genderestimation.ASGE_FSDKError;
import com.harvey.arcface.moodel.FaceFindModel;
import com.harvey.arcface.moodel.FaceFindMatchModel;
import com.harvey.arcface.utils.FaceUtils;
import com.harvey.db.OwnerDBHelper;
import com.harvey.db.bean.RegisteredFace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanhui on 2018/1/12.
 */

public class ArcFaceEngine {

    private static final String TAG = "FaceEngine";
    private static ArcFaceEngine instance;
    final AFT_FSDKEngine FT_engine;// 视频人脸跟踪
    final ASAE_FSDKEngine SAE_engine;// 年龄检测
    final AFD_FSDKEngine FD_engine;// 人脸检测
    final AFR_FSDKEngine FR_engine;// 人脸对比
    final ASGE_FSDKEngine SGE_engine;// 性别检测
    // 一次性识别人脸数量[1,50]
    private int maxFaceNum = 10;
    // 最小人脸尺寸[2,32]
    private int minFaceSize = 16;
    // 对比置信度
    private float SCORE = 0.75f;

    private ArcFaceEngine() {
        FT_engine = new AFT_FSDKEngine();
        FD_engine = new AFD_FSDKEngine();
        FR_engine = new AFR_FSDKEngine();
        SAE_engine = new ASAE_FSDKEngine();
        SGE_engine = new ASGE_FSDKEngine();
        init();
    }

    public static ArcFaceEngine getInstance() {
        if (instance == null)
            instance = new ArcFaceEngine();
        return instance;
    }

    private void init() {
        ASAE_FSDKError SAE_err = SAE_engine.ASAE_FSDK_InitAgeEngine(ArcFaceConfig.APP_ID, ArcFaceConfig.SAE_SDK_KEY);
        if (SAE_err.getCode() != ASAE_FSDKError.MOK)
            Log.e(TAG, "ASAE_FSDK_InitAgeEngine = " + SAE_err.getCode());

        AFD_FSDKError FD_err = FD_engine.AFD_FSDK_InitialFaceEngine(ArcFaceConfig.APP_ID, ArcFaceConfig.FD_SDK_KEY,
                AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, minFaceSize, maxFaceNum);
        if (FD_err.getCode() != AFD_FSDKError.MOK)
            Log.d(TAG, "AFD_FSDK_InitAgeEngine = " + FD_err.getCode());

        AFR_FSDKError FR_error = FR_engine.AFR_FSDK_InitialEngine(ArcFaceConfig.APP_ID, ArcFaceConfig.FR_SDK_KEY);
        if (FR_error.getCode() != AFR_FSDKError.MOK)
            Log.d(TAG, "AFR_FSDK_InitialEngine = " + FR_error.getCode());

        AFT_FSDKError AFT_err = FT_engine.AFT_FSDK_InitialFaceEngine(ArcFaceConfig.APP_ID, ArcFaceConfig.FT_SDK_KEY,
                AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, minFaceSize, maxFaceNum);
        if (AFT_err.getCode() != AFT_FSDKError.MOK)
            Log.d(TAG, "AFT_FSDK_InitialFaceEngine =" + AFT_err.getCode());

        ASGE_FSDKError ASGE_err = SGE_engine.ASGE_FSDK_InitgGenderEngine(ArcFaceConfig.APP_ID,
                ArcFaceConfig.SGE_SDK_KEY);
        if (ASGE_err.getCode() != ASGE_FSDKError.MOK)
            Log.d(TAG, "ASGE_FSDK_InitgGenderEngine = " + ASGE_err.getCode());
    }


    public void destroy() {
        SAE_engine.ASAE_FSDK_UninitAgeEngine();
        FD_engine.AFD_FSDK_UninitialFaceEngine();
        FR_engine.AFR_FSDK_UninitialEngine();
        FT_engine.AFT_FSDK_UninitialFaceEngine();
        SGE_engine.ASGE_FSDK_UninitGenderEngine();
    }

    // 获取摄像头人脸识别结果
    public List<FaceFindModel> detectFace(byte[] data, int width, int height) {
        List<AFT_FSDKFace> result = new ArrayList<>();
        List<FaceFindModel> faceFindModels = new ArrayList<>();
        AFT_FSDKError err = FT_engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine.CP_PAF_NV21,
                result);
        if (err.getCode() == AFT_FSDKError.MOK) {
            for (AFT_FSDKFace fsdkFace : result) {
                FaceFindModel model = new FaceFindModel();
                model.setCameraHeight(height);
                model.setCameraWidth(width);
                model.setFaceRect(fsdkFace.getRect());
                model.setDegree(fsdkFace.getDegree());
                faceFindModels.add(model);
            }
        } else {
            Log.e(TAG, "AFT_FSDK_FaceFeatureDetect fail! error code :" + err.getCode());
        }
        result.clear();
        return faceFindModels;
    }

    // 获取人脸特征码并存储人脸图片到本地
    public boolean saveFace(byte[] data, FaceFindModel model, String name, int age, String sex, String path) {
        AFR_FSDKFace result = new AFR_FSDKFace();
        boolean isSuccess = false;
        AFR_FSDKError error = FR_engine.AFR_FSDK_ExtractFRFeature(data, model.getCameraWidth(),
                model.getCameraHeight(), AFR_FSDKEngine.CP_PAF_NV21, model.getFaceRect(), model.getDegree(), result);
        if (error.getCode() == AFR_FSDKError.MOK) {
            try {
                String imgFile = path + File.separator + name + ".jpg";
                FaceUtils.saveFaceImage(imgFile, model, data);
                RegisteredFace registeredFace = new RegisteredFace();
                registeredFace.setAge(age);
                registeredFace.setGender(sex);
                registeredFace.setFeatureData(result.getFeatureData());
                registeredFace.setName(name);
                registeredFace.setImagePath(imgFile);
                registeredFace.setFaceTime(System.currentTimeMillis());
                Log.d(TAG, "人脸信息保存到本地数据库：" + registeredFace.toString());
                OwnerDBHelper.getInstance().saveRegisteredFace(registeredFace);
                isSuccess = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "人脸特征生成出错：" + error.getCode());
        }
        return isSuccess;
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
    public FaceFindMatchModel matchFace(byte[] data, FaceFindModel findModel, List<RegisteredFace> registeredFaces) {
        long begin = System.currentTimeMillis();
        FaceFindMatchModel findMatchingModel = new FaceFindMatchModel();
        AFR_FSDKEngine FR_engine = new AFR_FSDKEngine();
        AFR_FSDKError afr_fsdkError = FR_engine.AFR_FSDK_InitialEngine(ArcFaceConfig.APP_ID, ArcFaceConfig.FR_SDK_KEY);
        if (afr_fsdkError.getCode() != AFR_FSDKError.MOK) {
            Log.e(TAG, "AFR_FSDK_InitialEngine fail! error code :" + afr_fsdkError.getCode());
        }
        AFR_FSDKFace result = new AFR_FSDKFace();
        AFR_FSDKError error = FR_engine.AFR_FSDK_ExtractFRFeature(data, findModel.getCameraWidth(),
                findModel.getCameraHeight(), AFR_FSDKEngine.CP_PAF_NV21, findModel.getFaceRect(),
                findModel.getDegree(), result);
        if (error.getCode() == AFR_FSDKError.MOK) {
            // 记录对比开始时间
            long matchingBegin = System.currentTimeMillis();
            // 本地存储数据依次比对
            Log.e(TAG, "本地存储数据依次比对：" + registeredFaces.size());
            for (RegisteredFace registeredFace : registeredFaces) {
                // 进行人脸近似度对比
                AFR_FSDKFace input = new AFR_FSDKFace();
                input.setFeatureData(registeredFace.getFeatureData());
                AFR_FSDKMatching matching = new AFR_FSDKMatching();
                AFR_FSDKError err = FR_engine.AFR_FSDK_FacePairMatching(result, input, matching);
                if (err.getCode() == AFR_FSDKError.MOK) {
                    Log.e(TAG, "对比分数：" + matching.getScore());
                    if (matching.getScore() > SCORE) {
                        // 没有对比成功的记录
                        if (!findMatchingModel.isMatching()) {
                            findMatchingModel.setMatching(true);
                            findMatchingModel.setScore(matching.getScore());
                            findMatchingModel.setImagePath(registeredFace.getImagePath());
                            findMatchingModel.setStudent_id(registeredFace.getPerson_id());
                            findMatchingModel.setName(registeredFace.getName());
                            findMatchingModel.setGender(registeredFace.getGender());
                        }
                        // 有记录，但新记录更接近
                        else {
                            if (matching.getScore() > findMatchingModel.getScore()) {
                                findMatchingModel.setMatching(true);
                                findMatchingModel.setScore(matching.getScore());
                                findMatchingModel.setStudent_id(registeredFace.getPerson_id());
                                findMatchingModel.setName(registeredFace.getName());
                                findMatchingModel.setGender(registeredFace.getGender());
                            }
                        }
                    }
                }
            }
            long MatingbeginTime = System.currentTimeMillis() - matchingBegin;
            Log.e(TAG, "人脸对比耗费时间：" + MatingbeginTime);
        } else {
            Log.e(TAG, "人脸特征生成出错：" + error.getCode());
        }
        long beginTime = System.currentTimeMillis() - begin;
        Log.e(TAG, "总对比耗费时间：" + beginTime);
        FR_engine.AFR_FSDK_UninitialEngine();
        return findMatchingModel;
    }

}
