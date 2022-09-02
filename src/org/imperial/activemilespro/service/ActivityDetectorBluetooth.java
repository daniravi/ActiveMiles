package org.imperial.activemilespro.service;

import android.content.Context;
import android.content.res.AssetManager;

public class ActivityDetectorBluetooth extends ActivityDetectorBase {

    public native long initTorch(AssetManager manager, String libdir);


    public void init(AssetManager Am, String s) {
        if (torchState == 0 && Am != null) {
            torchState = initTorch(Am, s);
        }
    }

    public ActivityDetectorBluetooth(Context context, SensorDataServiceBluetooth sensorBase, int sensitivity) {
        super(context, sensorBase, sensitivity);
    }

}