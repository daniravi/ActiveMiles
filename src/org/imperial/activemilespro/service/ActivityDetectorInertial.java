package org.imperial.activemilespro.service;

import android.content.Context;
import android.content.res.AssetManager;

public class ActivityDetectorInertial extends ActivityDetectorBase {

    public native long initTorch(AssetManager manager, String libdir);

    public ActivityDetectorInertial(Context context, SensorDataServiceInertial sensorBase, int sensitivity) {
        super(context, sensorBase, sensitivity);
    }

    public void init(AssetManager Am, String s) {
        if (torchState == 0 && Am != null) {
            torchState = initTorch(Am, s);
        }
    }

}