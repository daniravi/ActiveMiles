package org.imperial.activemilespro.service;

import org.imperial.activemilespro.gui.ActiveMilesGUI;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceController extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!isServiceRunning(context, SensorDataServiceInertial.class) && !isServiceRunning(context, SensorDataServiceBluetooth.class)) {
            Intent startMain = new Intent(context,ActiveMilesGUI.class);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startMain);
        }
    }

    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo serviceInfo : am.getRunningServices(Integer.MAX_VALUE)) {
            String className1 = serviceInfo.service.getClassName();
            String className2 = serviceClass.getName();
            if (className1.equals(className2)) {
                return true;
            }
        }
        return false;
    }
}