package com.harvey.arcface;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.util.Log;

import com.harvey.arcface.moodel.FaceFindModel;
import com.harvey.arcface.moodel.FaceFindMatchModel;
import com.harvey.arcface.utils.MainHandler;
import com.harvey.db.OwnerDBHelper;
import com.harvey.db.bean.RegisteredFace;

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
    volatile boolean isMatchingFace = false;
    Handler mMatchFaceHandler;
    Looper mMatchFaceLooper;
    volatile boolean isInit = false;
    List<RegisteredFace> registeredFaces;

    public void init() {
        registeredFaces = OwnerDBHelper.getInstance().getRegisteredFaces();
        Log.d(TAG, "本地注册人脸数据:" + registeredFaces.toString());
        // 人脸匹配线程
        HandlerThread matchFaceThread = new HandlerThread("MatchFace");
        matchFaceThread.start();
        mMatchFaceLooper = matchFaceThread.getLooper();
        mMatchFaceHandler = new Handler(mMatchFaceLooper);
        isInit = true;
    }

    public void destroy() {
        if (isInit) {
            mMatchFaceLooper.quit();
        }
        faceFindModels.clear();
        matchListener = null;
        frameBytes = null;
        isMatchingFace = false;
        isInit = false;
    }

    public void setFrameBytes(byte[] frameBytes) {
        this.frameBytes = frameBytes;
    }

    public void matchFace(List<FaceFindModel> data) {
        if (!isInit)
            throw new NullPointerException("没有初始化人脸匹配!");
        this.faceFindModels.clear();
        this.faceFindModels.addAll(data);
        mMatchFaceHandler.post(this);
    }

    public void setOnFaceMatchListener(OnFaceMatchListener l) {
        this.matchListener = l;
    }

    @Override
    public void run() {
        if (isMatchingFace) {
            return;
        }
        if (frameBytes == null) {
            return;
        }
        if (faceFindModels.isEmpty()) {
            return;
        }
        if (registeredFaces.isEmpty()) {
            return;
        }
        isMatchingFace = true;
        for (FaceFindModel findModel : faceFindModels) {
            final FaceFindMatchModel matchModel = ArcFaceEngine.getInstance().matchFace(frameBytes, findModel,
                    registeredFaces);
            callOnFaceMatch(matchModel);
        }
        faceFindModels.clear();
        frameBytes = null;
        isMatchingFace = false;
    }

    void callOnFaceMatch(final FaceFindMatchModel face) {
        Log.d(TAG, "人脸匹配结果-->" + face.toString());
        if (matchListener != null && !TextUtils.isEmpty(face.getName())) {
            MainHandler.run(new Runnable() {
                @Override
                public void run() {
                    matchListener.onFaceMatch(face);
                }
            });
        }
    }

    public interface OnFaceMatchListener {

        @MainThread
        void onFaceMatch(FaceFindMatchModel face);

    }
}
