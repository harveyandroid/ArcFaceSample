package com.harvey.arcface

import android.content.Context
import com.arcsoft.face.*
import com.harvey.arcface.model.*
import com.harvey.arcface.utils.DefaultLogger
import com.harvey.arcface.utils.FaceConfig
import com.harvey.arcface.utils.ILogger
import java.util.*


/**
 * Created by harvey on 2018/1/12.
 */

class AIFace private constructor(builder: Builder) {
    //人脸检测角度
    private var orientPriority: Int = 0
    private val faceEngine: FaceEngine
    @Volatile
    @get:Synchronized
    var isInit = false
        private set
    //检测模式
    private val mode: Long
    //识别的最小人脸比例
    private val scaleVal: Int
    //引擎最多能检测出的人脸数
    private val maxNum: Int
    //需要启用的功能组合
    private val combinedMask: Int
    private val mContext: Context

    init {
        mode = builder.mode
        orientPriority = builder.orientPriority
        scaleVal = builder.scaleVal
        maxNum = builder.maxNum
        combinedMask = builder.combinedMask
        mContext = builder.context
        faceEngine = FaceEngine()
        init()
    }

    @Synchronized
    fun init(): Boolean {
        if (isInit) return true
        val begin = System.currentTimeMillis()
        isInit = false
        var code = faceEngine.activeOnline(mContext, FaceConfig.APP_ID, FaceConfig.SDK_KEY)
        if (code != ErrorInfo.MOK && code != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
            logger.i(String.format("activeOnline fail error_code:%d", code))
            return false
        }
        code = faceEngine.init(mContext, mode, orientPriority, scaleVal, maxNum, combinedMask)
        if (code != ErrorInfo.MOK) {
            logger.i(String.format("init fail error_code:%d", code))
            return false
        }
        isInit = true
        logger.i("init time：" + (System.currentTimeMillis() - begin))
        return true
    }

    @Synchronized
    fun destroy() {
        isInit = false
        faceEngine.unInit()
    }


    /**
     * 获取摄像头人脸识别结果
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    fun detectFaces(nv21: ByteArray, width: Int, height: Int): MutableList<FaceInfo>? {
        if (!isInit) return null
        val begin = System.currentTimeMillis()
        val result = ArrayList<FaceInfo>()
        val code = faceEngine.detectFaces(nv21, width, height, FaceConfig.CP_PAF_NV21, result)
        if (code == ErrorInfo.MOK && result.isNotEmpty()) {
            logger.i(String.format("detectFaces %d, time：%d", result.size, System.currentTimeMillis() - begin))
            return result
        } else {
            logger.i(String.format("detectFace fail error code :%d", code))
        }
        return null
    }

    /**
     * 一个Camera摄像头包含的所有人脸信息
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    fun detectFaceWithCamera(nv21: ByteArray, width: Int, height: Int): FaceCameraModel? {
        val data = detectFaces(nv21, width, height)
        return if (data.isNullOrEmpty()) null else FaceCameraModel(data, nv21, width, height)
    }


    /**
     * 检测人脸 以及人脸详细人类信息（年龄、性别、三维角度、活体）
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    fun detectPersons(nv21: ByteArray, width: Int, height: Int): MutableList<PersonModel>? {
        if (!isInit) return null
        val faceResult = detectFaces(nv21, width, height)
        if (faceResult.isNullOrEmpty()) {
            return null
        }
        val code = faceEngine.process(nv21, width, height, FaceConfig.CP_PAF_NV21, faceResult,
                FaceConfig.ASF_AGE
                        or FaceConfig.ASF_GENDER
                        or FaceConfig.ASF_FACE3DANGLE
                        or FaceConfig.ASF_LIVENESS)
        val faceSize = faceResult.size
        logger.i("detectPersons faceSize：$faceSize")
        if (code == ErrorInfo.MOK) {
            val faceFindModels = ArrayList<PersonModel>()
            val ageResult = ArrayList<AgeInfo>()
            val face3DAngleResult = ArrayList<Face3DAngle>()
            val genderInfoResult = ArrayList<GenderInfo>()
            val livenessInfoResult = ArrayList<LivenessInfo>()
            val ageCode = faceEngine.getAge(ageResult)
            val face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleResult)
            val genderCode = faceEngine.getGender(genderInfoResult)
            val livenessCode = faceEngine.getLiveness(livenessInfoResult)
            if ((ageCode or genderCode or face3DAngleCode or livenessCode) != ErrorInfo.MOK) {
                logger.i(String.format("at lease one of age、gender、face3DAngle 、liveness detect failed! codes are:%d,%d,%d,%d ", ageCode, face3DAngleCode, genderCode, livenessCode))
            } else {
                for (i in 0 until faceSize) {
                    val faceInfo = faceResult[i]
                    val ageInfo = ageResult[i]
                    val face3DAngle = face3DAngleResult[i]
                    val genderInfo = genderInfoResult[i]
                    val livenessInfo = livenessInfoResult[i]
                    val personModel = PersonModel(
                            faceInfo.rect,
                            faceInfo.orient,
                            faceInfo.faceId,
                            ageInfo.age,
                            genderInfo.gender,
                            face3DAngle.yaw,
                            face3DAngle.roll,
                            face3DAngle.pitch,
                            face3DAngle.status,
                            livenessInfo.liveness)
                    faceFindModels.add(personModel)
                }
            }
            return faceFindModels
        } else {
            logger.i(String.format("detectPersons process fail error code :%d", code))
        }
        return null
    }

    /**
     * 检测人脸 以及人脸详细人类信息（年龄、性别、三维角度、活体）
     *
     * @param model
     * @return
     */
    fun detectPersons(model: FaceCameraModel?): MutableList<PersonModel>? {
        if (!isInit || model == null) return null
        val faceResult = model.faceInfo
        if (faceResult.isNullOrEmpty()) {
            return null
        }
        val code = faceEngine!!.process(model.nv21, model.width, model.height,
                FaceConfig.CP_PAF_NV21,
                faceResult,
                (FaceConfig.ASF_AGE
                        or FaceConfig.ASF_GENDER
                        or FaceConfig.ASF_FACE3DANGLE
                        or FaceConfig.ASF_LIVENESS))
        val faceSize = faceResult.size
        logger.i("detectPersons faceSize：$faceSize")
        if (code == ErrorInfo.MOK) {
            val faceFindModels = ArrayList<PersonModel>()
            val ageResult = ArrayList<AgeInfo>()
            val face3DAngleResult = ArrayList<Face3DAngle>()
            val genderInfoResult = ArrayList<GenderInfo>()
            val livenessInfoResult = ArrayList<LivenessInfo>()
            val ageCode = faceEngine.getAge(ageResult)
            val face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleResult)
            val genderCode = faceEngine.getGender(genderInfoResult)
            val livenessCode = faceEngine.getLiveness(livenessInfoResult)
            if ((ageCode or genderCode or face3DAngleCode or livenessCode) != ErrorInfo.MOK) {
                logger.i(String.format("at lease one of age、gender、face3DAngle 、liveness detect failed! codes are:%d,%d,%d,%d ", ageCode, face3DAngleCode, genderCode, livenessCode))
            } else {
                for (i in 0 until faceSize) {
                    val faceInfo = faceResult[i]
                    val ageInfo = ageResult[i]
                    val face3DAngle = face3DAngleResult[i]
                    val genderInfo = genderInfoResult[i]
                    val livenessInfo = livenessInfoResult[i]
                    val personModel = PersonModel(
                            faceInfo.rect,
                            faceInfo.orient,
                            faceInfo.faceId,
                            ageInfo.age,
                            genderInfo.gender,
                            face3DAngle.yaw,
                            face3DAngle.roll,
                            face3DAngle.pitch,
                            face3DAngle.status,
                            livenessInfo.liveness)
                    faceFindModels.add(personModel)
                }
            }
            return faceFindModels
        } else {
            logger.i(String.format("detectPersons process fail error code :%d", code))
        }
        return null
    }


    /**
     * 一个Camera摄像头包含所有人的信息
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    fun detectPersonWithCamera(nv21: ByteArray, width: Int, height: Int): PersonCameraModel? {
        val data = detectPersons(nv21, width, height)
        return if (data.isNullOrEmpty()) null else PersonCameraModel(data, nv21, width, height)
    }

    /**
     * 一个Camera摄像头包含所有人的信息
     *
     * @param model
     * @return
     */
    fun detectPersonWithCamera(model: FaceCameraModel): PersonCameraModel? {
        val data = detectPersons(model)
        return if (data.isNullOrEmpty()) null else PersonCameraModel(data, model)
    }


    /**
     * 摄像头一帧数据包含所有人脸特征信息数据
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    fun findFeatureWithCamera(nv21: ByteArray, width: Int, height: Int): FeatureCameraModel? {
        val data = findFaceFeature(nv21, width, height)
        return if (data.isNullOrEmpty()) null else FeatureCameraModel(data, nv21, width, height)
    }

    /**
     * 摄像头一帧数据包含所有人脸特征信息数据
     *
     * @param model
     * @return
     */
    fun findFeatureWithCamera(model: FaceCameraModel): FeatureCameraModel? {
        val data = findFaceFeature(model)
        return if (data.isNullOrEmpty()) null else FeatureCameraModel(data, model)
    }

    /**
     * 提取摄像头所有人脸特征数据
     *
     * @param model
     * @return
     */
    fun findFaceFeature(model: FaceCameraModel?): MutableList<FeatureModel>? {
        if (!isInit || model == null) return null
        val begin = System.currentTimeMillis()
        val faceInfoList = model.faceInfo
        if (faceInfoList != null && faceInfoList.size > 0) {
            val faceFeatureList = mutableListOf<FeatureModel>()
            for (faceInfo in faceInfoList) {
                val faceFindModel = findSingleFaceFeature(model, faceInfo)
                if (faceFindModel != null) {
                    faceFeatureList.add(faceFindModel)
                }
            }
            logger.i("findFaceFeature model time：" + (System.currentTimeMillis() - begin))
            return faceFeatureList
        }
        return null
    }


    /**
     * 提取摄像头所有人脸特征数据
     *
     * @param nv21
     * @param width
     * @param height
     * @return
     */
    fun findFaceFeature(nv21: ByteArray, width: Int, height: Int): MutableList<FeatureModel>? {
        if (!isInit) return null
        val begin = System.currentTimeMillis()
        val faceInfoList = detectFaces(nv21, width, height)
        if (faceInfoList != null && faceInfoList.isNotEmpty()) {
            val faceFeatureList = ArrayList<FeatureModel>()
            for (faceInfo in faceInfoList) {
                val faceFindModel = findSingleFaceFeature(nv21, width, height, faceInfo)
                if (faceFindModel != null) {
                    faceFeatureList.add(faceFindModel)
                }
            }
            logger.i("findFaceFeature byte[] time：" + (System.currentTimeMillis() - begin))
            return faceFeatureList
        }
        return null
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
    fun findSingleFaceFeature(nv21: ByteArray, width: Int, height: Int, faceInfo: FaceInfo): FeatureModel? {
        if (!isInit) return null
        val begin = System.currentTimeMillis()
        val result = FaceFeature()
        val code = faceEngine.extractFaceFeature(nv21, width, height, FaceConfig.CP_PAF_NV21, faceInfo, result)
        return if (code == ErrorInfo.MOK) {
            logger.i("findSingleFaceFeature time：" + (System.currentTimeMillis() - begin))
            FeatureModel(width, height, faceInfo, result)
        } else {
            logger.i(String.format("findSingleFaceFeature fail error code :%d", code))
            null
        }
    }

    /**
     * 提取人脸特征数据
     *
     * @param model
     * @param faceInfo
     * @return
     */
    fun findSingleFaceFeature(model: CameraModel?, faceInfo: FaceInfo): FeatureModel? {
        if (!isInit || model == null) return null
        val begin = System.currentTimeMillis()
        val result = FaceFeature()
        val code = faceEngine.extractFaceFeature(model.nv21, model.width,
                model.height, FaceConfig.CP_PAF_NV21, faceInfo, result)
        return if (code == ErrorInfo.MOK) {
            logger.i("findSingleFaceFeature time：" + (System.currentTimeMillis() - begin))
            FeatureModel(model.width, model.height, faceInfo, result)
        } else {
            logger.i(String.format("findSingleFaceFeature fail error code :%d", code))
            null
        }
    }


    /**
     * 比对人脸特征数据获取相似度
     *
     * @param feature1
     * @param feature2
     * @return
     */
    fun compareFaceFeature(feature1: FaceFeature, feature2: FaceFeature): FaceSimilar? {
        if (!isInit) return null
        val result = FaceSimilar()
        val code = faceEngine.compareFaceFeature(feature1, feature2, result)
        return if (code == ErrorInfo.MOK) {
            result
        } else {
            logger.i(String.format("compareFaceFeature fail error code :%d", code))
            null
        }
    }

    /**
     * 比对人脸特征数据获取相似度
     *
     * @param featureData1
     * @param featureData2
     * @return
     */
    fun compareFaceFeature(featureData1: ByteArray, featureData2: ByteArray): FaceSimilar? {
        return compareFaceFeature(FaceFeature(featureData1), FaceFeature(featureData2))
    }

    class Builder(val context: Context) {
        //检测模式
        internal var mode: Long = 0
        //人脸检测角度
        internal var orientPriority: Int = 0
        //识别的最小人脸比例
        internal var scaleVal: Int = 0
        //引擎最多能检测出的人脸数
        internal var maxNum: Int = 0
        //需要启用的功能组合
        internal var combinedMask: Int = 0

        init {
            mode = FaceConfig.ASF_DETECT_MODE_VIDEO
            orientPriority = FaceConfig.ASF_OP_0_HIGHER_EXT
            scaleVal = 16
            maxNum = 25
            combinedMask = FaceAction.DETECT.combinedMask
        }

        fun mode(mode: Long): Builder {
            this.mode = mode
            return this
        }

        fun orientPriority(orientPriority: Int): Builder {
            this.orientPriority = orientPriority
            return this
        }

        fun scaleVal(scaleVal: Int): Builder {
            this.scaleVal = scaleVal
            return this
        }

        fun maxNum(maxNum: Int): Builder {
            this.maxNum = maxNum
            return this
        }

        fun combinedMask(combinedMask: Int): Builder {
            this.combinedMask = combinedMask
            return this
        }

        fun combinedMask(action: FaceAction): Builder {
            this.combinedMask = action.combinedMask
            return this
        }

        fun build(): AIFace {
            return AIFace(this)
        }

    }

    companion object {
        internal var logger: ILogger = DefaultLogger()

        fun showLog(isShowLog: Boolean) {
            logger.showLog(isShowLog)
        }

        fun showStackTrace(isShowStackTrace: Boolean) {
            logger.showStackTrace(isShowStackTrace)
        }
    }

}
