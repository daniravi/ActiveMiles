/*
 * Copyright (C) 2008 ZXing authors
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

package org.imperial.activemilespro.gui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import org.imperial.activemilespro.R;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;


/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements OnTouchListener, DecoratedBarcodeView.TorchListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    private String scanMode;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setAlpha(.5f);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            v.setAlpha(1f);
        }
        return false;
    }

    public void OpenQrCoder(View ignored2) {

        if (scanMode.compareTo("QR_CODE_TYPES") != 0) {
            try {
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setCaptureActivity(CaptureActivity.class);
                integrator.setOrientationLocked(true);
                integrator.setPrompt("Scan a barcode");
                integrator.setCameraId(0);  // Use a specific camera of the device
                integrator.setBeepEnabled(true);
                integrator.addExtra("SCAN_MODE", "QR_CODE_TYPES");
                integrator.initiateScan();
                finish();
            } catch (ActivityNotFoundException ignored) {

            }
        }
    }

    public void OpenBarCoder(View ignored2) {
        if (scanMode.compareTo("ONE_D_CODE_TYPES") != 0) {
            try {
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                integrator.setCaptureActivity(CaptureActivity.class);
                integrator.setOrientationLocked(true);
                integrator.setPrompt("Scan a barcode");
                integrator.setCameraId(0);  // Use a specific camera of the device
                integrator.setBeepEnabled(true);
                integrator.addExtra("SCAN_MODE", "ONE_D_CODE_TYPES");
                integrator.initiateScan();
                finish();
            } catch (ActivityNotFoundException ignored) {

            }
        }
    }

    public void OpenPhoto(View ignored) {

        finish();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.capture);

        ImageView rb1 = (ImageView) this.findViewById(R.id.Button_BarCode);
        ImageView rb2 = (ImageView) this.findViewById(R.id.Button_QRCode);
        ImageView rb3 = (ImageView) this.findViewById(R.id.Button_Photo);
        scanMode = this.getIntent().getStringExtra("SCAN_MODE");

        rb1.setOnTouchListener(this);
        rb2.setOnTouchListener(this);
        rb3.setOnTouchListener(this);


        barcodeScannerView = (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.setTorchListener(this);
        if (scanMode.compareTo("QR_CODE_TYPES") != 0) {
            rb1.setBackgroundResource(R.drawable.red_border);
        }
        if (scanMode.compareTo("ONE_D_CODE_TYPES") != 0) {
            rb2.setBackgroundResource(R.drawable.red_border);
        }

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;


    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }


    @Override
    public void onTorchOn() {

    }

    @Override
    public void onTorchOff() {

    }

}
