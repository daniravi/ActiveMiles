package org.imperial.activemilespro.service;

import org.imperial.activemilespro.gui.ActiveMilesGUI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent sMonitorServiceIntent;
        sMonitorServiceIntent = new Intent(context, ActiveMilesGUI.class);
        sMonitorServiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        sMonitorServiceIntent.putExtra("Type", "StartService");
        context.startActivity(sMonitorServiceIntent);
    }
}