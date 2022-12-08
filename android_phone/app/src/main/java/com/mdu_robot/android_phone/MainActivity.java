package com.mdu_robot.android_phone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.slider.RangeSlider;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {

    CameraBridgeViewBase cameraBridgeViewBase;
    RangeSlider hue_slider, sat_slider, val_slider, size_slider;

    Mat rgb, hsv, mask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(OpenCVLoader.initDebug()) Log.d("OPENCV:APP", "success");
        else Log.d("OPENCV:APP", "error");

        getPermission();

        hue_slider = findViewById(R.id.hue_slider);
        sat_slider = findViewById(R.id.sat_slider);
        val_slider = findViewById(R.id.val_slider);
        size_slider = findViewById(R.id.val_slider);


        cameraBridgeViewBase = findViewById(R.id.cameraView);

        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                rgb = new Mat();
                hsv = new Mat();
                mask = new Mat();

            }

            @Override
            public void onCameraViewStopped() {
                rgb.release();
                hsv.release();
                mask.release();

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                rgb = inputFrame.rgba();

                List<Float> hue_vals = hue_slider.getValues();
                List<Float> sat_vals = sat_slider.getValues();
                List<Float> val_vals = val_slider.getValues();
                List<Float> size_vals = size_slider.getValues();



                Imgproc.cvtColor(rgb, rgb, Imgproc.COLOR_RGBA2RGB);
                Imgproc.cvtColor(rgb, hsv, Imgproc.COLOR_RGB2HSV);

                Core.inRange(hsv, new Scalar(hue_vals.get(0), sat_vals.get(0), val_vals.get(0)), new Scalar(hue_vals.get(1), sat_vals.get(1), val_vals.get(1)), mask);

                rgb.setTo(new Scalar(0, 0, 255), mask);

                return rgb;
            }
        });

        cameraBridgeViewBase.enableView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    void getPermission() {
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 102);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==102 && grantResults.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                getPermission();
            }
        }
    }
}