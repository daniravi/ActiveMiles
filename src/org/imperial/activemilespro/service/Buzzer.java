package org.imperial.activemilespro.service;

import android.content.Context;
import android.os.Vibrator;

public class Buzzer {

    private final Vibrator mVibrator;

    public Buzzer(Context context) {
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void buzz() {
        mVibrator.vibrate(10);
    }

}
