package com.harvey.arcface;

import android.content.Context;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.harvey.arcface.model.FaceAction;
import com.harvey.arcface.model.FaceCameraModel;
import com.harvey.arcface.model.FeatureCameraModel;
import com.harvey.arcface.model.FeatureModel;
import com.harvey.arcface.model.PersonCameraModel;
import com.harvey.arcface.model.PersonModel;
import com.harvey.arcface.utils.DefaultLogger;
import com.harvey.arcface.utils.FaceConfig;
import com.harvey.arcface.utils.ILogger;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by harvey on 2018/1/12.
 */

public class AIFace {
    static ILogger logger = new DefaultLogger();
    //人脸检测角度
    int orientPriority;
    private FaceEngine faceEngine;
    private volatile boolean initSuccess = false;
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
        mode = builder.mode;
        orientPriority = builder.orientPriority;
        scaleVal = builder.scaleVal;
        maxNum = builder.maxNum;
        combinedMask = builder.combinedMask;
        mContext = builder.context;
        faceEngine = new FaceEngine();
        init();
    }

    public static void showLog(boolean isShowLog) {
        logger.showLog(isShowLog);
    }

    public static void showStackTrace(boolean isShowStackTrace) {
        logger.showStackTrace(isShowStackTrace);
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
        initSuccess = true;
        logger.i("init time：" + (System.currentTimeMillis() - begin));
        return true;
    }

    public synchronized boolean isInit() {
        return initSuccess;
    }

    public synchronized void destroy() {
        initSuccess = false;
        faceEngine.unInit();
        faceEngine = null;
    }


    /**
     * 获取摄像头人脸识别结果
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    public List<FaceInfo> detectFaces(byte[] nv21, int width, int height) {
        if (!isInit()) return null;
        long begin = System.currentTimeMillis();
        List<FaceInfo> result = new ArrayList<>();
        int code = faceEngine.detectFaces(nv21, width, height, FaceConfig.CP_PAF_NV21, result);
        if (code == ErrorInfo.MOK && result.size() > 0) {
            logger.i(String.format("detectFaces %d, time：%d", result.size(), (System.currentTimeMillis() - begin)));
            return result;
        } else {
            logger.i(String.format("detectFace fail error code :%d", code));
        }
        return null;
    }

    /**
     * 一个Camera摄像头包含的所有人脸信息
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    public FaceCameraModel detectFaceWithCamera(byte[] nv21, int width, int height) {
        List<FaceInfo> data = detectFaces(nv21, width, height);
        if (data != null && data.size() > 0) {
            return new FaceCameraModel(data, nv21, width, height);
        }
        return null;
    }


    /**
     * 检测人脸 以及人脸详细人类信息（年龄、性别、三维角度、活体）
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    public List<PersonModel> detectPersons(byte[] nv21, int width, int height) {
        if (!isInit()) return null;
        List<FaceInfo> faceResult = detectFaces(nv21, width, height);
        if (faceResult == null) {
            return null;
        }
        int code = faceEngine.process(nv21, width, height, FaceConfig.CP_PAF_NV21, faceResult,
                FaceConfig.ASF_AGE
                        | FaceConfig.ASF_GENDER
                        | FaceConfig.ASF_FACE3DANGLE
                        | FaceConfig.ASF_LIVENESS);
        int faceSize = faceResult.size();
        logger.i("getPersonInfo faceSize：" + faceSize);
        if (code == ErrorInfo.MOK && faceSize > 0) {
            List<PersonModel> faceFindModels = new ArrayList<>();
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
                    PersonModel personModel = new PersonModel();
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
     * 一个Camera摄像头包含所有人的信息
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    public PersonCameraModel detectPersonWithCamera(byte[] nv21, int width, int height) {
        List<PersonModel> data = detectPersons(nv21, width, height);
        if (data != null && data.size() > 0) {
            return new PersonCameraModel(data, nv21, width, height);
        }
        return null;
    }

    /**
     * 摄像头一帧数据包含所有人脸特征信息数据
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    public FeatureCameraModel findFeatureWithCamera(byte[] nv21, int width, int height) {
        List<FeatureModel> data = findFaceFeature(nv21, width, height);
        if (data != null && data.size() > 0) {
            return new FeatureCameraModel(data, nv21, width, height);
        }
        return null;
    }

    /**
     * 提取摄像头所有人脸特征数据
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    public List<FeatureModel> findFaceFeature(byte[] nv21, int width, int height) {
        if (!isInit()) return null;
        long begin = System.currentTimeMillis();
        List<FaceInfo> faceInfoList = detectFaces(nv21, width, height);
        if (faceInfoList != null && faceInfoList.size() > 0) {
            List<FeatureModel> faceFeatureList = new ArrayList<>();
            for (FaceInfo faceInfo : faceInfoList) {
                FeatureModel faceFindModel = findSingleFaceFeature(nv21, width, height, faceInfo);
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
     * @param nv21
     * @param width
     * @param height
     * @param faceInfo
     * @return
     */
    public FeatureModel findSingleFaceFeature(byte[] nv21, int width, int height, FaceInfo faceInfo) {
        if (!isInit()) return null;
        long begin = System.currentTimeMillis();
        FaceFeature result = new FaceFeature();
        int code = faceEngine.extractFaceFeature(nv21, width, height, FaceConfig.CP_PAF_NV21, faceInfo, result);
        if (code == ErrorInfo.MOK) {
            logger.i("findSingleFaceFeature time：" + (System.currentTimeMillis() - begin));
            return new FeatureModel(width, height, faceInfo, result);
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
    public FaceSimilar compareFaceFeature(FaceFeature feature1, FaceFeature feature2) {
        if (!isInit()) return null;
        FaceSimilar result = new FaceSimilar();
        int code = faceEngine.compareFaceFeature(feature1, feature2, result);
        if (code == ErrorInfo.MOK) {
            return result;
        } else {
            logger.i(String.format("compareFaceFeature fail error code :%d", code));
            return null;
        }
    }


    public static final class Builder {
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
            mode = FaceConfig.ASF_DETECT_MODE_VIDEO;
            orientPriority = FaceConfig.ASF_OP_0_HIGHER_EXT;
            scaleVal = 16;
            maxNum = 25;
            combinedMask = FaceAction.DETECT.combinedMask;
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

        public Builder combinedMask(FaceAction action) {
            this.combinedMask = action.combinedMask;
            return this;
        }

        public AIFace build() {
            return new AIFace(this);
        }

    }

}
