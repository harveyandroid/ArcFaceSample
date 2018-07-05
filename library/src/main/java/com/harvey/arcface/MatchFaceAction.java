package com.harvey.arcface;

import android.os.HandlerThread;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.util.Log;

import com.harvey.arcface.moodel.FaceFindModel;
import com.harvey.arcface.moodel.FaceFindMatchModel;
import com.harvey.arcface.utils.MainHandler;
import com.harvey.arcface.utils.ThreadManager;
import com.harvey.db.OwnerDBHelper;
import com.harvey.db.bean.RegisteredFace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by harvey on 2018/2/7 0007 09:45
 */

public class MatchFaceAction implements Runnable {
    final String TAG = "MatchFace";
    final List<FaceFindModel> faceFindModels = new CopyOnWriteArrayList<>();
    byte[] frameBytes;
    OnFaceMatchListener matchListener;
    volatile boolean setToStop = false;
    volatile boolean isMatchingFace = false;
    List<RegisteredFace> registeredFaces = new ArrayList<>();

    public MatchFaceAction() {
        ThreadManager.getFile().submit(new Runnable() {
            @Override
            public void run() {
                registeredFaces = OwnerDBHelper.getInstance().getRegisteredFaces();
                Log.d(TAG, "本地注册人脸数据:" + registeredFaces.toString());
            }
        });
    }

    public void destroy() {
        setToStop = true;
        faceFindModels.clear();
        frameBytes = null;
        matchListener = null;
        isMatchingFace = false;
    }

    public void setFrameBytes(byte[] frameBytes) {
        this.frameBytes = frameBytes;
    }

    public void matchFace(List<FaceFindModel> data) {
        this.faceFindModels.clear();
        this.faceFindModels.addAll(data);
    }

    public void setOnFaceMatchListener(OnFaceMatchListener l) {
        this.matchListener = l;
    }

    @Override
    public void run() {
        while (!setToStop) {
            if (!isMatchingFace && frameBytes != null) {
                isMatchingFace = true;
                for (FaceFindModel findModel : faceFindModels) {
                    final FaceFindMatchModel matchModel = ArcFaceEngine.getInstance().matchFace(frameBytes, findModel,
                            registeredFaces);
                    callOnFaceMatch(matchModel);
                }
                isMatchingFace = false;
            }
        }

    }

    void callOnFaceMatch(final FaceFindMatchModel face) {
        Log.d(TAG, "人脸匹配结果-->" + face.toString());
        MainHandler.run(new Runnable() {
            @Override
            public void run() {
                if (matchListener != null && !TextUtils.isEmpty(face.getName())) {
                    matchListener.onFaceMatch(face);
                }
            }
        });
    }

    public interface OnFaceMatchListener {

        @MainThread
        void onFaceMatch(FaceFindMatchModel face);

    }
}
