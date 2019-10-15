package com.harvey.arcfacedamo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.harvey.arcfacedamo.ui.FaceRegisterActivity;
import com.harvey.arcfacedamo.ui.FaceScanActivity;
import com.harvey.arcfacedamo.ui.TestActivity;

/**
 * Created by harvey on 2018/6/1 0001 14:43
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void doFaceScan(View view) {
        startActivity(new Intent(this, FaceScanActivity.class));
    }

    public void doFaceRegister(View view) {
        startActivity(new Intent(this, FaceRegisterActivity.class));
    }

    public void doTest(View view) {
        startActivity(new Intent(this, TestActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
