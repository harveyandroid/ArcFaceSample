package com.harvey.arcfacedamo.utils;

import android.content.Context;

import androidx.collection.ArraySet;

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.harvey.arcface.AIFace;
import com.harvey.arcface.model.FeatureModel;
import com.harvey.arcface.utils.DefaultLogger;
import com.harvey.arcface.utils.FaceUtils;
import com.harvey.arcface.utils.ILogger;
import com.harvey.arcfacedamo.db.DBHelper;
import com.harvey.arcfacedamo.db.bean.FaceRegister;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Created by hanhui on 2019/12/12 0012 15:55
 */
public class FaceMatchHelper {
    final ILogger logger;
    final private DBHelper dbHelper;
    private Set<FaceRegister> registeredFaces;
    private AIFace mAiFace;
    private float score = 0.75f;

    public FaceMatchHelper(Context context, AIFace aiFace) {
        dbHelper = new DBHelper(context);
        mAiFace = aiFace;
        registeredFaces = new ArraySet<>(dbHelper.loadAll());
        logger = new DefaultLogger("FaceRegisterHelper");
    }

    /**
     * 单张人脸与库中人脸对比
     */
    public FaceFindMatchModel matchFace(byte[] data, int width, int height, FaceInfo faceInfo) {
        long begin = System.currentTimeMillis();
        FeatureModel faceFindModel = mAiFace.findSingleFaceFeature(data, width, height, faceInfo);
        if (faceFindModel == null) {
            return null;
        }
        if (registeredFaces.size() == 0) {
            return null;
        }
        FaceFindMatchModel findMatchingModel = new FaceFindMatchModel();
        for (FaceRegister registeredFace : registeredFaces) {
            FaceSimilar similar = mAiFace.compareFaceFeature(faceFindModel.getFaceFeature(), new FaceFeature(registeredFace.getFeatureData()));
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
        long begin = System.currentTimeMillis();
        FaceFindMatchModel findMatchingModel = new FaceFindMatchModel();
        if (registeredFaces.size() == 0) {
            return null;
        }
        for (FaceRegister registeredFace : registeredFaces) {
            FaceSimilar similar = mAiFace.compareFaceFeature(faceFeature1, new FaceFeature(registeredFace.getFeatureData()));
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
    public boolean registerNv21(byte[] nv21, FeatureModel model, String name, int age, String sex, String dir) {
        try {
            byte[] featureData = model.getFeatureData();
            if (featureData == null || featureData.length != FaceFeature.FEATURE_SIZE) {
                FeatureModel faceFindModel = mAiFace.findSingleFaceFeature(nv21, model.getCameraWidth(), model.getCameraHeight(), model.getFaceInfo());
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

}
