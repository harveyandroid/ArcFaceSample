package com.harvey.arcfacedemo.ui

import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.harvey.arcface.DetectFaceAction
import com.harvey.arcface.MatchFaceAction
import com.harvey.arcface.model.FaceFindMatchModel
import com.harvey.arcface.model.FaceFindModel
import com.harvey.arcface.view.SurfaceViewCamera
import com.harvey.arcface.view.SurfaceViewFace
import com.harvey.arcfacedemo.R
import com.harvey.arcfacedemo.adapter.MatchFaceAdapter
import java.util.concurrent.TimeUnit

/**
 * Created by hanhui on 2018/6/1 0001 14:32
 */
class FaceScanActivity : AppCompatActivity(), Camera.PreviewCallback, DetectFaceAction.OnFaceDetectListener, MatchFaceAction.OnFaceMatchListener {
    private val FINISH_SHOW_WHAT = 1
    private val mDetectFaceAction: DetectFaceAction by lazy {
        DetectFaceAction()
    }
    private val matchFaceAction: MatchFaceAction by lazy {
        MatchFaceAction()
    }
    private lateinit var surfaceViewFace: SurfaceViewFace
    private lateinit var surfaceViewCamera: SurfaceViewCamera
    private lateinit var faceList: RecyclerView
    private lateinit var matchFaceAdapter: MatchFaceAdapter

    private val mHandler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            FINISH_SHOW_WHAT -> matchFaceAdapter.setNewData(null)
        }
        false
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentViewId)
        initHolder()
        initData()
    }

    private val contentViewId: Int
        get() = R.layout.activity_scan_face

    private fun initHolder() {
        surfaceViewCamera = findViewById(R.id.surfaceView_Camera)
        surfaceViewFace = findViewById(R.id.surfaceViewFace)
        faceList = findViewById(R.id.face_list)
    }

    private fun initData() {
        mDetectFaceAction.setOnFaceDetectListener(this)
        matchFaceAction.setOnFaceMatchListener(this)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.isSmoothScrollbarEnabled = true
        layoutManager.isAutoMeasureEnabled = true
        faceList.layoutManager = layoutManager
        matchFaceAdapter = MatchFaceAdapter()
        faceList.adapter = matchFaceAdapter
        faceList.setHasFixedSize(true)
        faceList.isNestedScrollingEnabled = false
        surfaceViewCamera.setCameraCallBack(this)
    }

    fun switchCamera(view: View) {
        surfaceViewCamera.switchCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        matchFaceAction.destroy()
        mDetectFaceAction.destroy()
        mHandler.removeMessages(FINISH_SHOW_WHAT)
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        val size = camera.parameters.previewSize
        mDetectFaceAction.detectFace(data, size.width, size.height)
        matchFaceAction.setFrameBytes(data)
    }

    override fun onFaceDetect(faceFindModels: List<FaceFindModel>, frameBytes: ByteArray) {
        surfaceViewFace.updateFace(faceFindModels)
        matchFaceAction.matchFace(faceFindModels)
    }

    override fun onFaceMatch(face: FaceFindMatchModel) {
        var isExist = false
        var existPosition = 0
        val models = matchFaceAdapter.data
        for (i in models.indices) {
            if (models[i].name == face.name) {
                existPosition = i
                isExist = true
                break
            }
        }
        if (!isExist) {
            matchFaceAdapter.addData(face)
        } else {
            matchFaceAdapter.setData(existPosition, face)
        }
        mHandler.removeMessages(FINISH_SHOW_WHAT)
        mHandler.sendEmptyMessageDelayed(FINISH_SHOW_WHAT, TimeUnit.SECONDS.toMillis(5))
    }
}
