package com.harvey.arcfacedamo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.harvey.arcface.ArcFaceEngine
import com.harvey.arcfacedamo.ui.FaceRegisterActivity
import com.harvey.arcfacedamo.ui.FaceScanActivity
import com.harvey.db.OwnerDBHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OwnerDBHelper.getInstance().init(this)
    }

    fun doFaceScan(v: View) {
        startActivity(Intent(this, FaceScanActivity().javaClass))
    }

    fun doFaceRegister(v: View) {
        startActivity(Intent(this, FaceRegisterActivity().javaClass))
    }

    override fun onDestroy() {
        super.onDestroy()
        ArcFaceEngine.getInstance().destroy()
    }
}
