package com.harvey.arcface;

import android.content.Context;
import android.support.v4.util.ArraySet;
import android.util.Log;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.harvey.arcface.moodel.FaceFindMatchModel;
import com.harvey.arcface.moodel.FaceFindModel;
import com.harvey.arcface.moodel.FaceFindPersonModel;
import com.harvey.arcface.utils.FaceUtils;
import com.harvey.db.DBHelper;
import com.harvey.db.bean.FaceRegister;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.arcsoft.face.FaceEngine.ASF_AGE;
import static com.arcsoft.face.FaceEngine.ASF_DETECT_MODE_VIDEO;
import static com.arcsoft.face.FaceEngine.ASF_FACE3DANGLE;
import static com.arcsoft.face.FaceEngine.ASF_FACE_DETECT;
import static com.arcsoft.face.FaceEngine.ASF_FACE_RECOGNITION;
import static com.arcsoft.face.FaceEngine.ASF_GENDER;
import static com.arcsoft.face.FaceEngine.ASF_IR_LIVENESS;
import static com.arcsoft.face.FaceEngine.ASF_LIVENESS;
import static com.arcsoft.face.FaceEngine.ASF_OP_0_HIGHER_EXT;
import static com.arcsoft.face.FaceEngine.CP_PAF_NV21;

/**
 * Created by harvey on 2018/1/12.
 */

public class FaceManager {

    public final static String APP_ID = "9yEHo73t5FeUpCEBxnQFRwmDtzADCntG3j5jKosXLPBq";
    public final static String SDK_KEY = "4DW218FNh9sceSWny9NiHDMkex4oF2ZngTFafMq5EPP9";
    private static final String TAG = "FaceManager";
    private static FaceManager instance;
    private final FaceEngine faceEngine;
    // 对比置信度
    private float SCORE = 0.75f;
    //检测模式
    private long DEFAULT_Mode = ASF_DETECT_MODE_VIDEO;
    //人脸检测角度
    private int DEFAULT_ORIENT_PRIORITY = ASF_OP_0_HIGHER_EXT;
    //识别的最小人脸比例
    private int DEFAULT_SCALE_VAL = 16;
    //引擎最多能检测出的人脸数
    private int DEFAULT_MAX_NUM = 25;
    //需要启用的功能组合
    private int DEFAULT_COMBINED_MASK = ASF_FACE_DETECT
            | ASF_FACE_RECOGNITION
            | ASF_AGE
            | ASF_GENDER
            | ASF_FACE3DANGLE
            | ASF_LIVENESS
            | ASF_IR_LIVENESS;
    private Set<FaceRegister> registeredFaces;
    private DBHelper dbHelper = new DBHelper();
    private boolean initSuccess = false;

    private FaceManager() {
        faceEngine = new FaceEngine();
        registeredFaces = new ArraySet<>();
    }

    public static FaceManager getInstance() {
        if (instance == null)
            instance = new FaceManager();
        return instance;
    }

    public void init(Context context) {
        long begin = System.currentTimeMillis();
        initSuccess = false;
        int code = faceEngine.activeOnline(context, APP_ID, SDK_KEY);
        if (code != ErrorInfo.MOK && code != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
            Log.i(TAG, String.format("activeOnline fail error_code:%d", code));
            return;
        }

        code = faceEngine.init(context, DEFAULT_Mode, DEFAULT_ORIENT_PRIORITY, DEFAULT_SCALE_VAL, DEFAULT_MAX_NUM, DEFAULT_COMBINED_MASK);
        if (code != ErrorInfo.MOK) {
            Log.i(TAG, String.format("init fail error_code:%d", code));
            return;
        }
        dbHelper.init(context);
        registeredFaces = new ArraySet<>(dbHelper.loadAll());
        initSuccess = true;
        Log.i(TAG, "init time：" + (System.currentTimeMillis() - begin));

    }

    public void destroy() {
        faceEngine.unInit();
    }

    /**
     * 获取摄像头人脸识别结果
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    public List<FaceInfo> detectFaces(byte[] data, int width, int height) {
        long begin = System.currentTimeMillis();
        List<FaceInfo> result = new ArrayList<>();
        int code = faceEngine.detectFaces(data, width, height, CP_PAF_NV21, result);
        if (code == ErrorInfo.MOK) {
            Log.i(TAG, "detectFaces time：" + (System.currentTimeMillis() - begin));
        } else {
            Log.i(TAG, String.format("detectFace fail error code :%d", code));
        }
        return result;
    }


    public List<FaceFindPersonModel> getPersonInfo(byte[] data, int width, int height) {
        List<FaceFindPersonModel> faceFindModels = new ArrayList<>();
        List<FaceInfo> faceResult = new ArrayList<>();
        List<AgeInfo> ageResult = new ArrayList<>();
        List<Face3DAngle> face3DAngleResult = new ArrayList<>();
        List<GenderInfo> genderInfoResult = new ArrayList<>();
        List<LivenessInfo> livenessInfoResult = new ArrayList<>();
        int code = faceEngine.process(data, width, height, CP_PAF_NV21, faceResult,
                ASF_AGE
                        | ASF_GENDER
                        | ASF_FACE3DANGLE
                        | ASF_LIVENESS);
        int faceSize = faceResult.size();
        if (code == ErrorInfo.MOK && faceSize > 0) {
            int ageCode = faceEngine.getAge(ageResult);
            int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleResult);
            int genderCode = faceEngine.getGender(genderInfoResult);
            int livenessCode = faceEngine.getLiveness(livenessInfoResult);
            if ((ageCode | genderCode | face3DAngleCode | livenessCode) != ErrorInfo.MOK) {
                Log.i(TAG, String.format("at lease one of age、gender、face3DAngle 、liveness detect failed! codes are:%d,%d,%d,%d "
                        , ageCode, face3DAngleCode, genderCode, livenessCode));
            } else {
                for (int i = 0; i < faceSize; i++) {
                    FaceInfo faceInfo = faceResult.get(i);
                    AgeInfo ageInfo = ageResult.get(i);
                    Face3DAngle face3DAngle = face3DAngleResult.get(i);
                    GenderInfo genderInfo = genderInfoResult.get(i);
                    LivenessInfo livenessInfo = livenessInfoResult.get(i);

                    FaceFindPersonModel personModel = new FaceFindPersonModel();
                    personModel.setRect(faceInfo.getRect());
                    personModel.setFaceId(faceInfo.getFaceId());
                    personModel.setOrient(faceInfo.getOrient());
                    personModel.setPitch(face3DAngle.getPitch());
                    personModel.setStatus(face3DAngle.getStatus());
                    personModel.setRoll(face3DAngle.getRoll());
                    personModel.setYaw(face3DAngle.getYaw());
                    personModel.setAge(ageInfo.getAge());
                    personModel.setGender(genderInfo.getGender());
                    personModel.setLiveness(livenessInfo.getLiveness());
                    faceFindModels.add(personModel);
                }
            }
        } else {
            Log.i(TAG, String.format("process fail error code :%d", code));
        }
        return faceFindModels;
    }

    /**
     * 提取摄像头所有人脸特征数据
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    public List<FaceFindModel> extractAllFaceFeature(byte[] data, int width, int height) {
        long begin = System.currentTimeMillis();
        List<FaceFindModel> faceFeatureList = new ArrayList<>();
        List<FaceInfo> faceInfoList = detectFaces(data, width, height);

        for (FaceInfo faceInfo : faceInfoList) {
            FaceFindModel faceFindModel = extractFaceFeature(data, width, height, faceInfo);
            if (faceFindModel != null) {
                faceFeatureList.add(faceFindModel);
            }
        }
        Log.i(TAG, "extractAllFaceFeature time：" + (System.currentTimeMillis() - begin));
        return faceFeatureList;
    }

    /**
     * 提取人脸特征数据
     *
     * @param data
     * @param width
     * @param height
     * @param faceInfo
     * @return
     */
    public FaceFindModel extractFaceFeature(byte[] data, int width, int height, FaceInfo faceInfo) {
        long begin = System.currentTimeMillis();
        FaceFeature result = new FaceFeature();
        int code = faceEngine.extractFaceFeature(data, width, height, CP_PAF_NV21, faceInfo, result);
        if (code == ErrorInfo.MOK) {
            Log.i(TAG, "extractFaceFeature time：" + (System.currentTimeMillis() - begin));
            return new FaceFindModel(width, height, faceInfo, result);
        } else {
            Log.i(TAG, String.format("extractFaceFeature fail error code :%d", code));
            return null;
        }
    }


    /**
     * 比对人脸特征数据获取相似度
     *
     * @param feature1
     * @param feature2
     * @return
     */
    private FaceSimilar compareFaceFeature(FaceFeature feature1, FaceFeature feature2) {
        FaceSimilar result = new FaceSimilar();
        int code = faceEngine.compareFaceFeature(feature1, feature2, result);
        if (code == ErrorInfo.MOK) {
            return result;
        } else {
            Log.i(TAG, String.format("compareFaceFeature fail error code :%d", code));
            return null;
        }
    }

    /**
     * 单张人脸与库中人脸对比
     */
    public FaceFindMatchModel matchFace(byte[] data, int width, int height, FaceInfo faceInfo) {
        long begin = System.currentTimeMillis();
        FaceFindModel faceFindModel = extractFaceFeature(data, width, height, faceInfo);
        if (faceFindModel == null) {
            return null;
        }
        FaceFindMatchModel findMatchingModel = new FaceFindMatchModel();
        for (FaceRegister registeredFace : registeredFaces) {
            FaceSimilar similar = compareFaceFeature(faceFindModel.getFaceFeature(), new FaceFeature(registeredFace.getFeatureData()));
            if (similar != null) {
                Log.e(TAG, "对比分数：" + similar.getScore());
                if (similar.getScore() > SCORE) {
                    // 没有对比成功的记录
                    if (!findMatchingModel.isMatching()) {
                        findMatchingModel.setMatching(true);
                        findMatchingModel.setScore(similar.getScore());
                        findMatchingModel.setImagePath(registeredFace.getImagePath());
                        findMatchingModel.setPersonId(registeredFace.getPerson_id());
                        findMatchingModel.setName(registeredFace.getName());
                        findMatchingModel.setGender(registeredFace.getGender());
                    }
                    // 有记录，但新记录更接近
                    else {
                        if (similar.getScore() > findMatchingModel.getScore()) {
                            findMatchingModel.setMatching(true);
                            findMatchingModel.setScore(similar.getScore());
                            findMatchingModel.setPersonId(registeredFace.getPerson_id());
                            findMatchingModel.setName(registeredFace.getName());
                            findMatchingModel.setGender(registeredFace.getGender());
                        }
                    }
                }
            }
        }
        Log.i(TAG, "人脸对比耗费时间：" + (System.currentTimeMillis() - begin));
        return findMatchingModel;
    }


    /**
     * 单张人脸与库中人脸对比
     *
     * @param faceFeature1
     * @return
     */
    public FaceFindMatchModel matchFace(FaceFeature faceFeature1) {
        long begin = System.currentTimeMillis();
        FaceFindMatchModel findMatchingModel = new FaceFindMatchModel();
        for (FaceRegister registeredFace : registeredFaces) {
            FaceSimilar similar = compareFaceFeature(faceFeature1, new FaceFeature(registeredFace.getFeatureData()));
            if (similar != null) {
                Log.e(TAG, "对比分数：" + similar.getScore());
                if (similar.getScore() > SCORE) {
                    // 没有对比成功的记录
                    if (!findMatchingModel.isMatching()) {
                        findMatchingModel.setMatching(true);
                        findMatchingModel.setScore(similar.getScore());
                        findMatchingModel.setImagePath(registeredFace.getImagePath());
                        findMatchingModel.setPersonId(registeredFace.getPerson_id());
                        findMatchingModel.setName(registeredFace.getName());
                        findMatchingModel.setGender(registeredFace.getGender());
                    }
                    // 有记录，但新记录更接近
                    else {
                        if (similar.getScore() > findMatchingModel.getScore()) {
                            findMatchingModel.setMatching(true);
                            findMatchingModel.setScore(similar.getScore());
                            findMatchingModel.setPersonId(registeredFace.getPerson_id());
                            findMatchingModel.setName(registeredFace.getName());
                            findMatchingModel.setGender(registeredFace.getGender());
                        }
                    }
                }
            }
        }
        Log.e(TAG, "人脸对比耗费时间：" + (System.currentTimeMillis() - begin));
        return findMatchingModel;
    }

    // 获取人脸特征码并存储人脸图片到本地
    public boolean registerNv21(byte[] nv21, FaceFindModel model, String name, int age, String sex, String dir) {
        try {
            byte[] featureData = model.getFeatureData();
            if (featureData == null || featureData.length != FaceFeature.FEATURE_SIZE) {
                FaceFindModel faceFindModel = extractFaceFeature(nv21, model.getCameraWidth(), model.getCameraHeight(), model.getFaceInfo());
                if (faceFindModel == null) {
                    return false;
                } else {
                    featureData = faceFindModel.getFeatureData();
                }
            }
            String userName = name == null ? String.valueOf(System.currentTimeMillis()) : name;
            String imgFile = dir + File.separator + userName + ".jpg";
            FaceUtils.saveFaceImage(imgFile, model, nv21);
            FaceRegister registeredFace = new FaceRegister();
            registeredFace.setAge(age);
            registeredFace.setGender(sex);
            registeredFace.setFeatureData(featureData);
            registeredFace.setName(name);
            registeredFace.setImagePath(imgFile);
            registeredFace.setFaceTime(System.currentTimeMillis());
            Log.d(TAG, "人脸信息保存到本地数据库：" + registeredFace.toString());
            dbHelper.save(registeredFace);
            registeredFaces.remove(registeredFace);
            registeredFaces.add(registeredFace);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


}
