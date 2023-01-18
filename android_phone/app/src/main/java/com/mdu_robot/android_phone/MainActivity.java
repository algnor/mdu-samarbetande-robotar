package com.mdu_robot.android_phone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class MainActivity extends CameraActivity {

    CameraBridgeViewBase cameraBridgeViewBase;
    RangeSlider hue_slider, sat_slider, val_slider, size_slider;
    Slider erode_slider, dilate_slider;

    Mat rgb, hsv, mask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Log.d("USBSERIAL", "No driver found");
            return;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            Log.d("USBSERIAL", "No connection");
            return;
        }
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_EVEN);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int i = 0;
        while (i > 0) {
            try {
                i++;
                port.write("Hello world".getBytes(), 10);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(OpenCVLoader.initDebug()) Log.d("OPENCV:APP", "success");
        else Log.d("OPENCV:APP", "error");

        getPermission();

        hue_slider = findViewById(R.id.hue_slider);
        sat_slider = findViewById(R.id.sat_slider);
        val_slider = findViewById(R.id.val_slider);
        size_slider = findViewById(R.id.size_slider);
        erode_slider = findViewById(R.id.erode_slider);
        dilate_slider = findViewById(R.id.dilate_slider);


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
                int erode_val = (int) erode_slider.getValue();
                int dilate_val = (int) dilate_slider.getValue();


                Imgproc.cvtColor(rgb, rgb, Imgproc.COLOR_RGBA2RGB);
                Imgproc.cvtColor(rgb, hsv, Imgproc.COLOR_RGB2HSV);

                Core.inRange(hsv, new Scalar(hue_vals.get(0), sat_vals.get(0), val_vals.get(0)), new Scalar(hue_vals.get(1), sat_vals.get(1), val_vals.get(1)), mask);

                if (erode_val > 0) {
                    Mat erode_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erode_val, erode_val));
                    Imgproc.erode(mask, mask, erode_kernel);
                }

                if (dilate_val > 0) {
                    Mat dilate_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilate_val, dilate_val));
                    Imgproc.dilate(mask, mask, dilate_kernel);
                }

                List<MatOfPoint> contours = new ArrayList<>();

                Imgproc.findContours(mask, contours, new Mat(), 2, Imgproc.RETR_TREE);

                Scalar color = new Scalar(0, 255, 0);

                rgb.setTo(new Scalar(0, 0, 255), mask);


                Point[] centers = new Point[contours.size()];
                float[][] radiuses = new float[contours.size()][1];
                MatOfPoint2f[] contoursPoly = new MatOfPoint2f[contours.size()];
                Scalar selection_color = new Scalar(255, 200, 100);
                Scalar primary_selection_color = new Scalar(255, 50, 200);
                List<MatOfPoint> filtered_contours = new ArrayList<>();

                int largest = 0;

                if (contours.size() == 0) { return rgb;}

                for (int i = 0; i < contours.size(); i++) {

                    contoursPoly[i] = new MatOfPoint2f();
                    Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), contoursPoly[i], 3, true);
                    centers[i] = new Point();
                    Imgproc.minEnclosingCircle(contoursPoly[i], centers[i], radiuses[i]);
                    if (radiuses[i][0] > radiuses[largest][0]) {
                        largest = i;
                    }
                    Log.d("OPENCV", String.valueOf(size_vals.get(0)));
                    Log.d("OPENCV", String.valueOf(radiuses[i][0]));
                    if ((radiuses[i][0] > size_vals.get(0)) && (radiuses[i][0] < size_vals.get(1))) {
                        Imgproc.circle(rgb, centers[i], (int) radiuses[i][0], selection_color, 2);
                        filtered_contours.add(contours.get(i));
                    }
                }
                Imgproc.drawContours(rgb, filtered_contours, -1, color, Imgproc.LINE_8);
                Imgproc.circle(rgb, centers[largest], (int) radiuses[largest][0], primary_selection_color, 10);

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