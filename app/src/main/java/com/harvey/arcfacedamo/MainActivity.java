package com.harvey.arcfacedamo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.harvey.arcface.ArcFaceEngine;
import com.harvey.arcfacedamo.ui.FaceRegisterActivity;
import com.harvey.arcfacedamo.ui.FaceScanActivity;
import com.harvey.db.OwnerDBHelper;

/**
 * Created by hanhui on 2018/6/1 0001 14:43
 */

public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		OwnerDBHelper.getInstance().init(this);

	}

	public void doFaceScan(View view) {
		startActivity(new Intent(this, FaceScanActivity.class));
	}

	public void doFaceRegister(View view) {
		startActivity(new Intent(this, FaceRegisterActivity.class));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ArcFaceEngine.getInstance().destroy();
	}
}
