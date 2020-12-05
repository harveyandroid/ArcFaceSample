package com.harvey.arcface;

import android.content.Context;

import com.arcsoft.face.ActiveFileInfo;
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
import com.harvey.arcface.model.OneFaceCameraModel;
import com.harvey.arcface.model.OneFeatureCameraModel;
import com.harvey.arcface.model.PersonCameraModel;
import com.harvey.arcface.model.PersonModel;
import com.harvey.arcface.model.ProcessState;
import com.harvey.arcface.utils.DefaultLogger;
import com.harvey.arcface.utils.ILogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Created by harvey on 2018/1/12.
 */
public class AIFace {
    static ILogger logger = new DefaultLogger();
    private FaceEngine faceEngine;
    private volatile boolean initSuccess = false;
    private DetectFaceOrientPriority orientPriority;
    private DetectMode detectMode;
    private int scaleVal;
    private int maxNum;
    private int combinedMask;
    private Context mContext;
    private String appId;
    private String sdkKey;
    /**
     * 当前一帧人脸数据处理状态
     */
    private ConcurrentMap<Integer, Integer> currentFrameFace;

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
        currentFrameFace = new ConcurrentHashMap<>();
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
        initSuccess = false;
        long begin = System.currentTimeMillis();
        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
        int code = FaceEngine.getActiveFileInfo(mContext, activeFileInfo);
        if (code == ErrorInfo.MOK) {
            logger.i("activeFileInfo is : " + activeFileInfo);
        } else {
            logger.i("getActiveFileInfo failed, code is  : " + code);
        }
        code = FaceEngine.activeOnline(mContext, appId, sdkKey);
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

    public Map<Integer, Integer> getCurrentFrameFace() {
        return currentFrameFace;
    }

    public int getFaceProcessState(FaceInfo faceInfo) {
        if (faceInfo != null) {
            return getFaceProcessState(faceInfo.getFaceId());
        } else {
            return ProcessState.NONE;
        }
    }

    public int getFaceProcessState(int faceId) {
        Integer state = currentFrameFace.get(faceId);
        if (state == null) {
            return ProcessState.NONE;
        } else {
            return state;
        }
    }

    public void replaceFaceProcessState(int faceId, int processState) {
        currentFrameFace.replace(faceId, processState);
    }

    /**
     * 判断该人脸是否是人脸检测状态
     *
     * @param faceInfo
     * @return
     */
    public boolean isFaceDetectState(FaceInfo faceInfo) {
        if (faceInfo == null) {
            return false;
        }
        Integer state = currentFrameFace.get(faceInfo.getFaceId());
        if (state == null) {
            return false;
        } else {
            return state == ProcessState.FD;
        }
    }

    /**
     * 判断该人脸是否是人脸特征值提取等待中
     *
     * @param faceInfo
     * @return
     */
    public boolean isFRWaitingState(FaceInfo faceInfo) {
        if (faceInfo == null) {
            return false;
        }
        Integer state = currentFrameFace.get(faceInfo.getFaceId());
        if (state == null) {
            return false;
        } else {
            return state == ProcessState.FR_WAITING;
        }
    }

    public synchronized boolean isInit() {
        return initSuccess;
    }

    public synchronized void destroy() {
        currentFrameFace.clear();
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
        if (code == ErrorInfo.MOK) {
            if (result.size() > 0) {
                logger.i(String.format("detectFaces %d, time：%d", result.size(), (System.currentTimeMillis() - begin)));
            }
            //判断是否与前一帧检测到的人脸结果属于同一个人
            // 如果某个人脸框首次出现，或者如果某个trackid对应的人脸框仍未识别成功，都需要做FR操作。
            Map<Integer, Integer> map = new HashMap();
            for (FaceInfo faceInfo : result) {
                //和上一帧人脸数据判断
                Integer state = currentFrameFace.get(faceInfo.getFaceId());
                if (state != null) {
                    //人脸识别结束重置状态
                    if (state > ProcessState.FR_PROCESSING) {
                        map.put(faceInfo.getFaceId(), ProcessState.FD);
                    } else {
                        map.put(faceInfo.getFaceId(), state);
                    }
                } else {
                    map.put(faceInfo.getFaceId(), ProcessState.FD);
                }
                currentFrameFace.clear();
                currentFrameFace.putAll(map);
            }
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
                    PersonModel personModel = new PersonModel(faceResult.get(i),
                            ageResult.get(i),
                            face3DAngleResult.get(i),
                            genderInfoResult.get(i),
                            livenessInfoResult.get(i));
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
                    PersonModel personModel = new PersonModel(faceResult.get(i),
                            ageResult.get(i),
                            face3DAngleResult.get(i),
                            genderInfoResult.get(i),
                            livenessInfoResult.get(i));
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
        currentFrameFace.replace(faceInfo.getFaceId(), ProcessState.FR_PROCESSING);
        int code = faceEngine.extractFaceFeature(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfo, result);
        if (code == ErrorInfo.MOK) {
            currentFrameFace.replace(faceInfo.getFaceId(), ProcessState.FR_PROCESSING, ProcessState.FR_SUCCESS);
            logger.i("findSingleFaceFeature time：" + (System.currentTimeMillis() - begin));
            return new FeatureModel(faceInfo, result);
        } else {
            currentFrameFace.replace(faceInfo.getFaceId(), ProcessState.FR_PROCESSING, ProcessState.FR_FAILED);
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
    public OneFeatureCameraModel findSingleFaceFeatureCamera(CameraModel model, FaceInfo faceInfo) {
        if (!isInit() || model == null || faceInfo == null) return null;
        FeatureModel featureModel = findSingleFaceFeature(model.getNv21(), model.getWidth(), model.getHeight(), faceInfo);
        if (featureModel != null) {
            return new OneFeatureCameraModel(featureModel, model);
        } else {
            return null;
        }
    }

    public OneFeatureCameraModel findSingleFaceFeatureCamera(OneFaceCameraModel model) {
        if (!isInit() || model == null) return null;
        return findSingleFaceFeatureCamera(model, model.getFaceInfo());
    }

    /**
     * 提取人脸特征数据
     *
     * @param model
     * @param faceInfo
     * @return
     */
    public FeatureModel findSingleFaceFeature(CameraModel model, FaceInfo faceInfo) {
        if (!isInit() || model == null || faceInfo == null) return null;
        return findSingleFaceFeature(model.getNv21(), model.getWidth(), model.getHeight(), faceInfo);
    }

    public FeatureModel findSingleFaceFeature(OneFaceCameraModel model) {
        if (!isInit() || model == null) return null;
        return findSingleFaceFeature(model, model.getFaceInfo());
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

        public Builder orientPriority(int orientation) {
            switch (orientation) {
                case 0:
                    this.orientPriority = DetectFaceOrientPriority.ASF_OP_0_ONLY;
                    break;
                case 90:
                    this.orientPriority = DetectFaceOrientPriority.ASF_OP_90_ONLY;
                    break;
                case 180:
                    this.orientPriority = DetectFaceOrientPriority.ASF_OP_180_ONLY;
                    break;
                case 270:
                    this.orientPriority = DetectFaceOrientPriority.ASF_OP_270_ONLY;
                    break;
                default:
                    this.orientPriority = DetectFaceOrientPriority.ASF_OP_ALL_OUT;
                    break;
            }
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
