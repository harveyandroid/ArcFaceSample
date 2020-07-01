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
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.harvey.arcface.model.CameraModel;
import com.harvey.arcface.model.FaceAction;
import com.harvey.arcface.model.FaceCameraModel;
import com.harvey.arcface.model.FeatureCameraModel;
import com.harvey.arcface.model.FeatureModel;
import com.harvey.arcface.model.PersonCameraModel;
import com.harvey.arcface.model.PersonModel;
import com.harvey.arcface.utils.DefaultLogger;
import com.harvey.arcface.utils.ILogger;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by harvey on 2018/1/12.
 */
public class AIFace {
    static ILogger logger = new DefaultLogger();
    //人脸检测角度
    private DetectFaceOrientPriority orientPriority;
    private FaceEngine faceEngine;
    private volatile boolean initSuccess = false;
    //检测模式
    private DetectMode detectMode;
    //识别的最小人脸比例
    private int scaleVal;
    //引擎最多能检测出的人脸数
    private int maxNum;
    //需要启用的功能组合
    private int combinedMask;
    private Context mContext;
    private String appId;
    private String sdkKey;

    private AIFace(Builder builder) {
        appId = builder.appId;
        sdkKey = builder.sdkKey;
        detectMode = builder.mode;
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
        int code = FaceEngine.activeOnline(mContext, appId, sdkKey);
        logger.i("activeOnline  code is  : " + code);
        if (code != ErrorInfo.MOK && code != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
            return false;
        }
        code = faceEngine.init(mContext, detectMode, orientPriority, scaleVal, maxNum, combinedMask);
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
        int code = faceEngine.detectFaces(nv21, width, height, FaceEngine.CP_PAF_NV21, result);
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
        if (faceResult == null || faceResult.size() == 0) {
            return null;
        }
        int code = faceEngine.process(nv21, width, height, FaceEngine.CP_PAF_NV21, faceResult,
                FaceAction.FACE_PROPERTY.combinedMask);
        int faceSize = faceResult.size();
        logger.i("detectPersons faceSize：" + faceSize);
        if (code == ErrorInfo.MOK) {
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
            logger.i(String.format("detectPersons process fail error code :%d", code));
        }
        return null;
    }

    /**
     * 检测人脸 以及人脸详细人类信息（年龄、性别、三维角度、活体）
     *
     * @param model
     * @return
     */
    public List<PersonModel> detectPersons(FaceCameraModel model) {
        if (!isInit() || model == null) return null;
        List<FaceInfo> faceResult = model.getFaceInfo();
        if (faceResult == null || faceResult.size() == 0) {
            return null;
        }
        int code = faceEngine.process(model.getNv21(), model.getWidth(), model.getHeight(),
                FaceEngine.CP_PAF_NV21,
                faceResult,
                FaceAction.FACE_PROPERTY.combinedMask);
        int faceSize = faceResult.size();
        logger.i("detectPersons faceSize：" + faceSize);
        if (code == ErrorInfo.MOK) {
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
            logger.i(String.format("detectPersons process fail error code :%d", code));
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
     * 一个Camera摄像头包含所有人的信息
     *
     * @param model
     * @return
     */
    public PersonCameraModel detectPersonWithCamera(FaceCameraModel model) {
        List<PersonModel> data = detectPersons(model);
        if (data != null && data.size() > 0) {
            return new PersonCameraModel(data, model);
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
     * 摄像头一帧数据包含所有人脸特征信息数据
     *
     * @param model
     * @return
     */
    public FeatureCameraModel findFeatureWithCamera(FaceCameraModel model) {
        List<FeatureModel> data = findFaceFeature(model);
        if (data != null && data.size() > 0) {
            return new FeatureCameraModel(data, model);
        }
        return null;
    }

    /**
     * 提取摄像头所有人脸特征数据
     *
     * @param model
     * @return
     */
    public List<FeatureModel> findFaceFeature(FaceCameraModel model) {
        if (!isInit() || model == null) return null;
        long begin = System.currentTimeMillis();
        List<FaceInfo> faceInfoList = model.getFaceInfo();
        if (faceInfoList != null && faceInfoList.size() > 0) {
            List<FeatureModel> faceFeatureList = new ArrayList<>();
            for (FaceInfo faceInfo : faceInfoList) {
                FeatureModel faceFindModel = findSingleFaceFeature(model, faceInfo);
                if (faceFindModel != null) {
                    faceFeatureList.add(faceFindModel);
                }
            }
            logger.i("findFaceFeature model time：" + (System.currentTimeMillis() - begin));
            return faceFeatureList;
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
            logger.i("findFaceFeature byte[] time：" + (System.currentTimeMillis() - begin));
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
        int code = faceEngine.extractFaceFeature(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfo, result);
        if (code == ErrorInfo.MOK) {
            logger.i("findSingleFaceFeature time：" + (System.currentTimeMillis() - begin));
            return new FeatureModel(width, height, faceInfo, result);
        } else {
            logger.i(String.format("findSingleFaceFeature fail error code :%d", code));
            return null;
        }
    }

    /**
     * 提取人脸特征数据
     *
     * @param model
     * @param faceInfo
     * @return
     */
    public FeatureModel findSingleFaceFeature(CameraModel model, FaceInfo faceInfo) {
        if (!isInit() || model == null) return null;
        long begin = System.currentTimeMillis();
        FaceFeature result = new FaceFeature();
        int code = faceEngine.extractFaceFeature(model.getNv21(), model.getWidth(), model.getHeight(), FaceEngine.CP_PAF_NV21, faceInfo, result);
        if (code == ErrorInfo.MOK) {
            logger.i("findSingleFaceFeature time：" + (System.currentTimeMillis() - begin));
            return new FeatureModel(model.getWidth(), model.getHeight(), faceInfo, result);
        } else {
            logger.i(String.format("findSingleFaceFeature fail error code :%d", code));
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

    /**
     * 比对人脸特征数据获取相似度
     *
     * @param featureData1
     * @param featureData2
     * @return
     */
    public FaceSimilar compareFaceFeature(byte[] featureData1, byte[] featureData2) {
        return compareFaceFeature(new FaceFeature(featureData1), new FaceFeature(featureData2));
    }

    public static final class Builder {
        //检测模式
        DetectMode mode;
        //人脸检测角度
        DetectFaceOrientPriority orientPriority;
        //识别的最小人脸比例
        int scaleVal;
        //引擎最多能检测出的人脸数
        int maxNum;
        String appId;
        String sdkKey;
        Context context;
        //需要启用的功能组合
        int combinedMask;

        public Builder(Context context) {
            this.context = context;
            this.mode = DetectMode.ASF_DETECT_MODE_VIDEO;
            this.orientPriority = DetectFaceOrientPriority.ASF_OP_ALL_OUT;
            this.scaleVal = 16;
            this.maxNum = 25;
            this.combinedMask = FaceAction.DETECT.combinedMask;
            //企业认证的key
            this.appId = "CqqrPnuUjFhp4E8x3tK4ENZ5kwQEgPTs5oj46bsSLE7d";
            this.sdkKey = "3Whu47h1z2tPtMwGVfKxZ82TLKSKVvCS7pL2AKEAez3n";
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder sdkKey(String sdkKey) {
            this.sdkKey = sdkKey;
            return this;
        }

        public Builder mode(DetectMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder orientPriority(DetectFaceOrientPriority orientPriority) {
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
