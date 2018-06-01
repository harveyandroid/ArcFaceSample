package com.harvey.arcfacedemo.ui

import android.app.AlertDialog
import android.content.Context
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import com.guo.android_extend.widget.ExtImageView
import com.harvey.arcface.ArcFaceEngine
import com.harvey.arcface.DetectFaceAction
import com.harvey.arcface.model.FaceFindCameraModel
import com.harvey.arcface.model.FaceFindModel
import com.harvey.arcface.view.SurfaceViewCamera
import com.harvey.arcface.view.SurfaceViewSaveFace
import com.harvey.arcfacedemo.R

/**
 * Created by hanhui on 2018/6/1 0001 13:53
 */
class FaceRegisterActivity : AppCompatActivity(), Camera.PreviewCallback, DetectFaceAction.OnFaceDetectListener {
    private val mDetectFaceAction: DetectFaceAction by lazy {
        DetectFaceAction()
    }
    private lateinit var surfaceViewSaveFace: SurfaceViewSaveFace
    private lateinit var surfaceViewCamera: SurfaceViewCamera
    private lateinit var dialogLayout: View
    private lateinit var dialogExtImageView: ExtImageView
    private lateinit var dialogName: EditText
    private lateinit var dialogAge: EditText
    private lateinit var dialogSex: RadioGroup
    private var faceName: String? = null
    private var faceAge: String? = null
    private var faceSex: String? = null
    private var registerDialog: AlertDialog? = null

    private val contentViewId: Int
        get() = R.layout.activity_register_face

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentViewId)
        initHolder()
        initListener()
        initData()
    }

    private fun initHolder() {
        surfaceViewCamera = findViewById(R.id.surfaceView_Camera)
        surfaceViewSaveFace = findViewById(R.id.surfaceViewSaveFace)
        dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_save_face, null)
        dialogExtImageView = dialogLayout.findViewById(R.id.extimageview)
        dialogName = dialogLayout.findViewById(R.id.et_name)
        dialogAge = dialogLayout.findViewById(R.id.et_age)
        dialogSex = dialogLayout.findViewById(R.id.rg_sex)

    }

    private fun initListener() {
        dialogSex.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_man) {
                faceSex = "男"

            }
            if (checkedId == R.id.rb_women) {
                faceSex = "女"
            }
        }
    }

    private fun initData() {
        mDetectFaceAction.setOnFaceDetectListener(this)
        surfaceViewCamera.setCameraCallBack(this)
        surfaceViewSaveFace.uploadTimeSecondDown(1)
        surfaceViewSaveFace.setSaveFaceListener(object : SurfaceViewSaveFace.SaveFaceListener {

            override fun onSuccess(faceModel: FaceFindCameraModel?) {
                if (faceModel != null && faceModel.faceFindModels.isNotEmpty()) {
                    showSaveFaceDialog(faceModel.faceFindModels[0], faceModel.cameraData)
                }
            }

            override fun onTimeSecondDown(TimeSecond: Int) {
                Log.e("harvey", "onTimeSecondDown---->" + TimeSecond)
            }

            override fun onErrorMsg(errorCode: Int) {
                Log.e("harvey", "onErrorMsg---->" + errorCode)
            }
        })
    }

    fun showSaveFaceDialog(faceModel: FaceFindModel, data: ByteArray) {
        if (registerDialog != null && registerDialog!!.isShowing) {
            registerDialog!!.dismiss()
        }
        val parent = dialogLayout.parent
        if (parent != null) {
            (parent as ViewGroup).removeAllViews()
        }
        val faceBitmap = ArcFaceEngine.getFaceBitmap(faceModel, data)
        dialogExtImageView.setImageBitmap(faceBitmap)
        registerDialog = AlertDialog.Builder(this).setTitle("是否注册该图片?").setIcon(android.R.drawable.ic_dialog_info)
                .setView(dialogLayout).setPositiveButton("确定", null)
                .setNegativeButton("取消") { dialog, which ->
                    if (!faceBitmap.isRecycled)
                        faceBitmap.recycle()
                    surfaceViewSaveFace.reset()
                    dialog.dismiss()
                }.create()
        registerDialog!!.show()
        registerDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            faceName = dialogName.text.toString().trim { it <= ' ' }
            faceAge = dialogAge.text.toString().trim { it <= ' ' }
            when {
                TextUtils.isEmpty(faceName) -> showToast(this, "请输入姓名！")
                TextUtils.isEmpty(faceAge) -> showToast(this, "请输入年龄！")
                TextUtils.isEmpty(faceSex) -> showToast(this, "请选择性别！")
                else -> {
                    val result = ArcFaceEngine.saveFace(data, faceModel, faceName!!,
                            Integer.valueOf(faceAge)!!, faceSex!!, application.externalCacheDir!!.path)
                    if (result)
                        showToast(this, "注册人脸成功！")
                    else
                        showToast(this, "注册人脸失败！")
                    if (!faceBitmap.isRecycled)
                        faceBitmap.recycle()
                    surfaceViewSaveFace.reset()
                    registerDialog!!.dismiss()
                }
            }
        }
        registerDialog!!.setCanceledOnTouchOutside(false)
    }

    fun showToast(context: Context, msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()

    }

    fun switchCamera(view: View) {
        surfaceViewCamera.switchCamera()
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        val size = camera.parameters.previewSize
        mDetectFaceAction.detectFace(data, size.width, size.height)
    }

    override fun onFaceDetect(faceFindModels: List<FaceFindModel>, frameBytes: ByteArray) {
        surfaceViewSaveFace.uploadFace(faceFindModels, frameBytes)
    }

    override fun onDestroy() {
        super.onDestroy()
        mDetectFaceAction.destroy()
    }
}
