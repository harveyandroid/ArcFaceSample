package com.harvey.arcface;

import android.content.Context;

import androidx.collection.ArraySet;

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
import com.harvey.arcface.template.ILogger;
import com.harvey.arcface.utils.FaceUtils;
import com.harvey.db.DBHelper;
import com.harvey.db.bean.FaceRegister;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Created by harvey on 2018/1/12.
 */

public class AIFace {
    static ILogger logger = new DefaultLogger();
    //人脸检测角度
    int orientPriority;
    private FaceEngine faceEngine;
    private Set<FaceRegister> registeredFaces;
    private DBHelper dbHelper = new DBHelper();
    private volatile boolean initSuccess = false;
    // 对比置信度
    private float score;
    //检测模式
    private long mode;
    //识别的最小人脸比例
    private int scaleVal;
    //引擎最多能检测出的人脸数
    private int maxNum;
    //需要启用的功能组合
    private int combinedMask;
    private Context mContext;

    private AIFace(Builder builder) {
        score = builder.score;
        mode = builder.mode;
        orientPriority = builder.orientPriority;
        scaleVal = builder.scaleVal;
        maxNum = builder.maxNum;
        combinedMask = builder.combinedMask;
        mContext = builder.context;
        faceEngine = new FaceEngine();
        registeredFaces = new ArraySet<>();
        init();
    }

    public static void showLog(boolean isShowLog) {
        logger.showLog(isShowLog);
    }

    public static void showStackTrace(boolean isShowStackTrace) {
        logger.showStackTrace(isShowStackTrace);
    }

    public synchronized boolean isInit() {
        return initSuccess;
    }

    public synchronized boolean init() {
        if (initSuccess) return true;
        long begin = System.currentTimeMillis();
        initSuccess = false;
        int code = faceEngine.activeOnline(mContext, FaceConfig.APP_ID, FaceConfig.SDK_KEY);
        if (code != ErrorInfo.MOK && code != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
            logger.i(String.format("activeOnline fail error_code:%d", code));
            return false;
        }
        code = faceEngine.init(mContext, mode, orientPriority, scaleVal, maxNum, combinedMask);
        if (code != ErrorInfo.MOK) {
            logger.i(String.format("init fail error_code:%d", code));
            return false;
        }
        dbHelper.init(mContext);
        registeredFaces = new ArraySet<>(dbHelper.loadAll());
        initSuccess = true;
        logger.i("init time：" + (System.currentTimeMillis() - begin));
        return true;
    }


    public synchronized void destroy() {
        initSuccess = false;
        faceEngine.unInit();
        faceEngine = null;
        registeredFaces.clear();

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
        if (!initSuccess) return null;
        long begin = System.currentTimeMillis();
        List<FaceInfo> result = new ArrayList<>();
        int code = faceEngine.detectFaces(data, width, height, FaceConfig.CP_PAF_NV21, result);
        if (code == ErrorInfo.MOK) {
            if (result.size() > 0)
                logger.i(String.format("detectFaces %d, time：%d", result.size(), (System.currentTimeMillis() - begin)));
        } else {
            logger.i(String.format("detectFace fail error code :%d", code));
        }
        return result;
    }

    public List<FaceFindPersonModel> detectPersons(byte[] data, int width, int height) {
        if (!initSuccess) {
            return null;
        }
        List<FaceInfo> faceResult = detectFaces(data, width, height);
        if (faceResult == null || faceResult.size() == 0) {
            return null;
        }
        int code = faceEngine.process(data, width, height, FaceConfig.CP_PAF_NV21, faceResult,
                FaceConfig.ASF_AGE
                        | FaceConfig.ASF_GENDER
                        | FaceConfig.ASF_FACE3DANGLE
                        | FaceConfig.ASF_LIVENESS);
        int faceSize = faceResult.size();
        logger.i("getPersonInfo faceSize：" + faceSize);
        if (code == ErrorInfo.MOK && faceSize > 0) {
            List<FaceFindPersonModel> faceFindModels = new ArrayList<>();
            List<AgeInfo> ageResult = new ArrayList<>();
            List<Face3DAngle> face3DAngleResult = new ArrayList<>();
            List<GenderInfo> genderInfoResult = new ArrayList<>();
            List<LivenessInfo> livenessInfoResult = new ArrayList<>();

            int ageCode = faceEngine.getAge(ageResult);
            int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleResult);
            int genderCode = faceEngine.getGender(genderInfoResult);
            int livenessCode = faceEngine.getLiveness(livenessInfoResult);
            if ((ageCode | genderCode | face3DAngleCode | livenessCode) != ErrorInfo.MOK) {
                logger.i(String.format("at lease one of age、gender、face3DAngle 、liveness detect failed! codes are:%d,%d,%d,%d "
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
            return faceFindModels;
        } else {
            logger.i(String.format("process fail error code :%d", code));
        }
        return null;
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
        if (!initSuccess) return null;
        long begin = System.currentTimeMillis();
        List<FaceInfo> faceInfoList = detectFaces(data, width, height);
        if (faceInfoList != null && faceInfoList.size() > 0) {
            List<FaceFindModel> faceFeatureList = new ArrayList<>();
            for (FaceInfo faceInfo : faceInfoList) {
                FaceFindModel faceFindModel = extractFaceFeature(data, width, height, faceInfo);
                if (faceFindModel != null) {
                    faceFeatureList.add(faceFindModel);
                }
            }
            logger.i("extractAllFaceFeature time：" + (System.currentTimeMillis() - begin));
            return faceFeatureList;
        }
        return null;
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
        if (!initSuccess) return null;
        long begin = System.currentTimeMillis();
        FaceFeature result = new FaceFeature();
        int code = faceEngine.extractFaceFeature(data, width, height, FaceConfig.CP_PAF_NV21, faceInfo, result);
        if (code == ErrorInfo.MOK) {
            logger.i("extractFaceFeature time：" + (System.currentTimeMillis() - begin));
            return new FaceFindModel(width, height, faceInfo, result);
        } else {
            logger.i(String.format("extractFaceFeature fail error code :%d", code));
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
        if (!initSuccess) return null;
        FaceSimilar result = new FaceSimilar();
        int code = faceEngine.compareFaceFeature(feature1, feature2, result);
        if (code == ErrorInfo.MOK) {
            return result;
        } else {
            logger.i(String.format("compareFaceFeature fail error code :%d", code));
            return null;
        }
    }

    /**
     * 单张人脸与库中人脸对比
     */
    public FaceFindMatchModel matchFace(byte[] data, int width, int height, FaceInfo faceInfo) {
        if (!initSuccess) {
            return null;
        }
        long begin = System.currentTimeMillis();
        FaceFindModel faceFindModel = extractFaceFeature(data, width, height, faceInfo);
        if (faceFindModel == null) {
            return null;
        }
        if (registeredFaces.size() == 0) {
            return null;
        }
        FaceFindMatchModel findMatchingModel = new FaceFindMatchModel();
        for (FaceRegister registeredFace : registeredFaces) {
            FaceSimilar similar = compareFaceFeature(faceFindModel.getFaceFeature(), new FaceFeature(registeredFace.getFeatureData()));
            if (similar != null) {
                logger.e("对比分数：" + similar.getScore());
                if (similar.getScore() > score) {
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
        logger.i("人脸对比耗费时间：" + (System.currentTimeMillis() - begin));
        return findMatchingModel;
    }

    /**
     * 单张人脸与库中人脸对比
     *
     * @param faceFeature1
     * @return
     */
    public FaceFindMatchModel matchFace(FaceFeature faceFeature1) {
        if (!initSuccess) {
            return null;
        }
        long begin = System.currentTimeMillis();
        FaceFindMatchModel findMatchingModel = new FaceFindMatchModel();
        if (registeredFaces.size() == 0) {
            return null;
        }
        for (FaceRegister registeredFace : registeredFaces) {
            FaceSimilar similar = compareFaceFeature(faceFeature1, new FaceFeature(registeredFace.getFeatureData()));
            if (similar != null) {
                logger.e("对比分数：" + similar.getScore());
                if (similar.getScore() > score) {
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
        logger.e("人脸对比耗费时间：" + (System.currentTimeMillis() - begin));
        return findMatchingModel;
    }

    // 获取人脸特征码并存储人脸图片到本地
    public boolean registerNv21(byte[] nv21, FaceFindModel model, String name, int age, String sex, String dir) {
        if (!initSuccess) return false;
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
            logger.d("人脸信息保存到本地数据库：" + registeredFace.toString());
            dbHelper.save(registeredFace);
            registeredFaces.remove(registeredFace);
            registeredFaces.add(registeredFace);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static final class Builder {
        // 对比置信度
        float score;
        //检测模式
        long mode;
        //人脸检测角度
        int orientPriority;
        //识别的最小人脸比例
        int scaleVal;
        //引擎最多能检测出的人脸数
        int maxNum;
        Context context;
        //需要启用的功能组合
        int combinedMask;

        public Builder() {
            score = 0.75f;
            mode = FaceConfig.ASF_DETECT_MODE_VIDEO;
            orientPriority = FaceConfig.ASF_OP_0_HIGHER_EXT;
            scaleVal = 16;
            maxNum = 25;
            combinedMask = FaceConfig.ASF_FACE_DETECT
                    | FaceConfig.ASF_FACE_RECOGNITION
                    | FaceConfig.ASF_AGE
                    | FaceConfig.ASF_GENDER
                    | FaceConfig.ASF_FACE3DANGLE
                    | FaceConfig.ASF_LIVENESS;
        }

        public Builder score(float score) {
            this.score = score;
            return this;
        }

        public Builder mode(long mode) {
            this.mode = mode;
            return this;
        }

        public Builder orientPriority(int orientPriority) {
            this.orientPriority = orientPriority;
            return this;
        }

        public Builder scaleVal(int scaleVal) {
            this.scaleVal = scaleVal;
            return this;
        }

        public Builder maxNum(int maxNum) {
            this.maxNum = maxNum;
            return this;
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder combinedMask(int combinedMask) {
            this.combinedMask = combinedMask;
            return this;
        }

        public AIFace build() {
            return new AIFace(this);
        }
    }

}
