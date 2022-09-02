package org.imperial.activemilespro.service;

import java.io.IOException;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

public class SensorDataServiceInertial extends SensorDataServiceBase implements SensorEventListener {
    private static SensorManager curr_SensorManager;

    @Override
    public void onCreate() {
        super.onCreate();
        curr_ActivityDetector = new ActivityDetectorInertial(this.getApplicationContext(), this, StepSensitivity);
        curr_SensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerDetector();
        curr_ActivityDetector.addActivityListener(this);
        curr_ActivityDetector.init(curr_ActivityDetector.c.getAssets(), curr_ActivityDetector.c.getApplicationInfo().nativeLibraryDir);

    }


    public void registerDetector() {
        Sensor mSensorA = curr_SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mSensorG = curr_SensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (mSensorG != null)
            curr_SensorManager.registerListener(this, mSensorG, SensorManager.SENSOR_DELAY_GAME);
        if (mSensorA != null)
            curr_SensorManager.registerListener(this, mSensorA, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregisterDetector() {
        curr_SensorManager.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {

            int j = (sensor != null && sensor.getType() == Sensor.TYPE_GYROSCOPE) ? 1 : 0;
            if (j == 1) {
                if (curr_ActivityDetector instanceof ActivityDetectorInertial)
                    (curr_ActivityDetector).onGyroChanged(event.values[0], event.values[1], event.values[2]);
                if (LiveViewisOn)
                    lv.addGyroData(event.values[0], event.values[1], event.values[2]);
                if (record) {
                    try {
                        writer.write("G>" + String.valueOf(event.values[0]));
                        writer.write(",");
                        writer.write(String.valueOf(event.values[1]));
                        writer.write(",");
                        writer.write(String.valueOf(event.values[2]));
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (SystemClock.elapsedRealtime()  - LastTimeStemp > sizeSegment) {

                        LastTimeStemp = SystemClock.elapsedRealtime();
                        ActivityClasify();

                    }
                }
            }

            j = (sensor != null && sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
            if (j == 1) {
                if (curr_ActivityDetector instanceof ActivityDetectorInertial)
                    (curr_ActivityDetector).onAccChanged(event.values[0], event.values[1], event.values[2]);
                if (LiveViewisOn)
                    lv.addAccData(event.values[0], event.values[1], event.values[2]);
                if (record) {
                    try {
                        writer.write("A>" + String.valueOf(event.values[0]));
                        writer.write(",");
                        writer.write(String.valueOf(event.values[1]));
                        writer.write(",");
                        writer.write(String.valueOf(event.values[2]));
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (SystemClock.elapsedRealtime() - LastTimeStemp > sizeSegment) {

                        LastTimeStemp = SystemClock.elapsedRealtime();
                        ActivityClasify();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
