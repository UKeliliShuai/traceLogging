package com.baidu.sener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @project: BaiduTrace_AndroidSDK_v3_1_7_Sample
 * @author: UKelili
 * @date: 2020/12/23 19
 */
public class SenserDetect implements SensorEventListener {
    float[] Acc_data;
    float[] gravity;
    float[] linear_acceleration;
    final float alpha = (float) 0.8;
    @Override
    public void onSensorChanged(SensorEvent event) {
//        加速度计
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            gravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Acc_data = event.values;
            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public float[] getCurrentAcce() {
        return linear_acceleration;
    }
}
