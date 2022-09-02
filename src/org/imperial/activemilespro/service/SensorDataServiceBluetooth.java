package org.imperial.activemilespro.service;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.imperial.activemilespro.database.PerformanceContentProvider;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class SensorDataServiceBluetooth extends SensorDataServiceBase {

    private static final String TAG = "SensorDataServiceBlu";
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private Timer timer_checkBlu;
    private static final int checkifBluisConnected = 10 * 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        registerDetector();
        timer_checkBlu = new Timer();
        timer_checkBlu.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                checkBlu();
            }
        }, 0, checkifBluisConnected);
        curr_ActivityDetector = new ActivityDetectorBluetooth(this.getApplicationContext(), this, StepSensitivity);
        curr_ActivityDetector.addActivityListener(this);
        curr_ActivityDetector.init(curr_ActivityDetector.c.getAssets(), curr_ActivityDetector.c.getApplicationInfo().nativeLibraryDir);
    }

    public void registerDetector() {
        Intent bindIntent = new Intent(this, UartService.class);
        startService(bindIntent);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onDestroy() {
        timer_checkBlu.cancel();
        super.onDestroy();
    }

    public void unregisterDetector() {

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
            getApplicationContext().unbindService(mServiceConnection);
        } catch (IllegalArgumentException ignored) {

        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        if (mService != null) {
            mService.close();
            mService.stopSelf();
            mService = null;
        }
    }

    // UART service connected/disconnected
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
            mService.connect(deviceAddress);

        }

        public void onServiceDisconnected(ComponentName classname) {
            if (mDevice != null) {
                mService.disconnect();
            }
        }
    };

    private float[] GetData(byte[] pdata) {
        short x, y, z;
        float[] result = new float[3];
        x = (short) ((pdata[1] & 0xff) | ((pdata[2] << 8) & 0xff00));
        y = (short) ((pdata[3] & 0xff) | ((pdata[4] << 8) & 0xff00));
        z = (short) ((pdata[5] & 0xff) | ((pdata[6] << 8) & 0xff00));

        result[0] = (float) ((x / 65536.0) * 3.0);
        result[1] = (float) ((y / 65536.0) * 3.0);
        result[2] = (float) ((z / 65536.0) * 3.0);
        return result;
    }

    private float[] GetDataALL(byte[] scratchVal)
        {
            float[] result = new float[9];
            result[0] = (short)((scratchVal[1] & 0xff) | ((scratchVal[2] << 8) & 0xff00));
            result[1] = (short)((scratchVal[3] & 0xff) | ((scratchVal[4] << 8) & 0xff00));
            result[2] = (short)((scratchVal[5] & 0xff) | ((scratchVal[6] << 8) & 0xff00));
            result[3] = (short)((scratchVal[7] & 0xff) | ((scratchVal[8] << 8) & 0xff00));
            result[4] = (short)((scratchVal[9] & 0xff) | ((scratchVal[10] << 8) & 0xff00));
            result[5] = (short)((scratchVal[11] & 0xff) | ((scratchVal[12] << 8) & 0xff00));
            result[6] = (short)((scratchVal[13] & 0xff) | ((scratchVal[14] << 8) & 0xff00));
            result[7] = (short)((scratchVal[15] & 0xff) | ((scratchVal[16] << 8) & 0xff00));
            result[8] = (short)((scratchVal[17] & 0xff) | ((scratchVal[18] << 8) & 0xff00));

            result[0] = (float) ((result[0] / 65536.0) * 3.0);
            result[1] = (float) ((result[1] / 65536.0) * 3.0);
            result[2] = (float) ((result[2]/ 65536.0) * 3.0);
            result[3] = (float) ((result[3] / 65536.0) * 3.0);
            result[4] = (float) ((result[4] / 65536.0) * 3.0);
            result[5] = (float) ((result[5]/ 65536.0) * 3.0);
            result[6] = (float) ((result[6] / 65536.0) * 3.0);
            result[7] = (float) ((result[7] / 65536.0) * 3.0);
            result[8] = (float) ((result[8]/ 65536.0) * 3.0);

            return result;
        }

    private float GetDataSingle(byte[] pdata) {
        short x;
        float result;
        x = (short) ((pdata[1] & 0xff) | ((pdata[2] << 8) & 0xff00));
        result = (float) x;
        return result;
    }


    private  float tempSum, dustSum = 0;
    private  int tempCount, dustCount = 0;
    private  final int TEMP_WIDTH = 30;
    private  final int DUST_WIDTH = 100;
    private  float prev_temp=0;

    public  float extractTemp(float temp) {
        float result = prev_temp;
        if (temp > 150 && temp < 350) {
            temp = temp / 9f;
            tempCount ++;
            tempSum += temp;
        }
        if (tempCount == TEMP_WIDTH) {

            result = tempSum / tempCount;
            prev_temp=result;
            tempCount = 0;
            tempSum = 0;
        }
        return result;
    }

    public  float extractDust(float dust) {
        float result=0;

            if (dust > 5) {
                //mDustData[dustCount] = dust;
                dustCount ++;

            }else {
                dust = 0;
                dustCount++;
            }
            dustSum += dust;
            result = dustSum / dustCount;
            if (dustCount == DUST_WIDTH) {
                dustCount = 0;
                dustSum = 0;
            }
        return result;
    }


    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // *********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                Log.d(TAG, "UART_CONNECT_MSG");
            }

            // *********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {

                Log.d(TAG, "UART_DISCONNECT_MSG");
                mService.disconnect();
            }

            // *********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            // *********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                byte[] txValue = intent.getByteArrayExtra(UartService.ACC_DATA);

                if (!(txValue == null)) {
                    float[] allData=GetDataALL(txValue);
                    float[] accValue = {allData[0], allData[1],allData[2]};
                    float[] GyroValue = {allData[3], allData[4], allData[5]};
                    if (LiveViewisOn) {
                        lv.addAccData(accValue[0], accValue[1], accValue[2]);
                        lv.addGyroData(GyroValue[0], GyroValue[1], GyroValue[2]);
                    }
                    if (curr_ActivityDetector instanceof ActivityDetectorBluetooth) {
                        (curr_ActivityDetector).onGyroChanged(GyroValue[0], GyroValue[1], GyroValue[2]);
                        (curr_ActivityDetector).onAccChanged(accValue[0], accValue[1], accValue[2]);
                    }

                    if (record) {
                        {
                            try {
                                writer.write("A>" + accValue[0]);
                                writer.write(",");
                                writer.write(String.valueOf(accValue[1]));
                                writer.write(",");
                                writer.write(String.valueOf(accValue[2]));
                                writer.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (SystemClock.elapsedRealtime() - LastTimeStemp > sizeSegment) {

                                LastTimeStemp =SystemClock.elapsedRealtime();
                                ActivityClasify();

                            }
                            try {
                                writer.write("G>" + GyroValue[0]);
                                writer.write(",");
                                writer.write(String.valueOf(GyroValue[1]));
                                writer.write(",");
                                writer.write(String.valueOf(GyroValue[2]));
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



                txValue = intent.getByteArrayExtra(UartService.DAST_DATA);
                if (!(txValue == null)) {

                    float realValue = extractDust(GetDataSingle(txValue));
                    if (LiveViewisOn)
                        lv.addDastData(realValue);
                    if (record) {
                        {
                            try {
                                writer.write("D>" + realValue);
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

                txValue = intent.getByteArrayExtra(UartService.TEMP_DATA);
                if (!(txValue == null)) {

                    float realValue = extractTemp(GetDataSingle(txValue));
                    if (LiveViewisOn)
                        lv.addTempData(realValue);
                    if (record) {
                        {
                            try {
                                writer.write("T>" + realValue);
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

            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }

        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    private void checkBlu() {
        if (mService != null && mService.mConnectionState != UartService.STATE_CONNECTED) {
            Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_SETTING);
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    deviceAddress = cursor.getString(1);
                }
                cursor.close();
            }

            this.unbindService(mServiceConnection);
            Intent bindIntent = new Intent(this, UartService.class);
            stopService(bindIntent);
            startService(bindIntent);
            bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        }
    }

}
