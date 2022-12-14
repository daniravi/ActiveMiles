/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.imperial.activemilespro.service;

import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class UartService extends Service {
    private final static String TAG = "UartService";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    public int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.nordicsemi.nrfUART.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.nordicsemi.nrfUART.ACTION_DATA_AVAILABLE";
    public final static String ACC_DATA = "com.nordicsemi.nrfUART.ACC_DATA";
    public final static String DAST_DATA = "com.nordicsemi.nrfUART.DAST_DATA";
    public final static String TEMP_DATA = "com.nordicsemi.nrfUART.TEMP_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART = "com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART";
    private static final UUID RX_SERVICE_UUID = UUID.fromString("47442014-0F63-5B27-9122-728099603712");
    private static final UUID DAST_CHAR_UUID = UUID.fromString("4744201D-0F63-5B27-9122-728099603712");    // DAST
    private static final UUID TEMP_CHAR_UUID = UUID.fromString("47442018-0F63-5B27-9122-728099603712");    // TEMP

    private static final UUID ACC_CHAR_UUID = UUID.fromString("47442020-0F63-5B27-9122-728099603712"); //IMU



    // Implements callback methods for GATT events that the app cares about. For
    // example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                /*synchronized (this) {
                    try {
                        this.wait(3000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }*/


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "mBluetoothGatt = " + mBluetoothGatt);

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (ACC_CHAR_UUID.equals(characteristic.getUuid())) {

            // Log.d(TAG,
            // String.format("Received TX: %d",characteristic.getValue() ));
            intent.putExtra(ACC_DATA, characteristic.getValue());
        } else if (DAST_CHAR_UUID.equals(characteristic.getUuid()))
        {
			intent.putExtra(DAST_DATA, characteristic.getValue());
		}
        else if (TEMP_CHAR_UUID.equals(characteristic.getUuid()))
        {
            intent.putExtra(TEMP_DATA, characteristic.getValue());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public UartService getService() {
            return UartService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        // mBluetoothGatt.close();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.w(TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    /*
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
	                                          boolean enabled) {
	    if (mBluetoothAdapter == null || mBluetoothGatt == null) {
	        Log.w(TAG, "BluetoothAdapter not initialized");
	        return;
	    }
	    mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

	            
	    if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
	        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
	                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
	        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        mBluetoothGatt.writeDescriptor(descriptor);
	    }
	}*/

    /**
     * Enable TXNotification
     *
     * @return
     */
    public void enableTXNotification() {
        /*
        if (mBluetoothGatt == null) {
			showMessage("mBluetoothGatt null" + mBluetoothGatt);
			broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			return;
		}
			*/
        BluetoothGattService RxService1 = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService1 == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }

        BluetoothGattCharacteristic TxChar1 = RxService1.getCharacteristic(ACC_CHAR_UUID);
        if (TxChar1 == null) {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar1, true);
        BluetoothGattDescriptor descriptor1 = TxChar1.getDescriptors().get(0);
        descriptor1.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor1);

		/*BluetoothGattCharacteristic TxChar3 = RxService1.getCharacteristic(DAST_CHAR_UUID);
		if (TxChar3 == null)
		{
			showMessage("Tx charateristic not found!");
			broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(TxChar3, true);

		BluetoothGattDescriptor descriptor3 = TxChar3.getDescriptors().get(0);
		descriptor3.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);


        BluetoothGattCharacteristic TxChar4 = RxService1.getCharacteristic(TEMP_CHAR_UUID);
        if (TxChar4 == null)
        {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar4, true);

        BluetoothGattDescriptor descriptor4 = TxChar4.getDescriptors().get(0);
        descriptor4.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);


        synchronized (this) {
			try
			{
				this.wait(700);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			mBluetoothGatt.writeDescriptor(descriptor3);
            try
            {
                this.wait(700);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            mBluetoothGatt.writeDescriptor(descriptor4);

        }*/

    }

	/*public void writeRXCharacteristic(byte[] value)
	{
	
		
		BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
		showMessage("mBluetoothGatt null"+ mBluetoothGatt);
		if (RxService == null) {
	        showMessage("Rx service not found!");
	        broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
	        return;
	    }
		BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
	    if (RxChar == null) {
	        showMessage("Rx charateristic not found!");
	        broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
	        return;
	    }
	    RxChar.setValue(value);
		boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
		
	    Log.d(TAG, "write TXchar - status=" + status);  
	}*/

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }

}
