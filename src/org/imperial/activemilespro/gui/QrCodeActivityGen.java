package org.imperial.activemilespro.gui;

import java.util.regex.Pattern;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.interface_utility.UtilsCalendar;
import org.imperial.activemilespro.service.ActivityDetectorBase;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QrCodeActivityGen extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private int numActivitReceivedType1 = 0;
    private final int[][] dataPerfromance1 = new int[ActiveMilesGUI.NumberOfActivities][24];
    private final int[] energyDay = new int[24];
    private long DataToGetPerformance;
    private String possibleEmail;
    private ImageView qrcode;
    private TextView textForVoucher;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.qr_code_gen);
        DataToGetPerformance = System.currentTimeMillis();

        possibleEmail = "";
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.GET_ACCOUNTS},
                    REQUEST_CODE_ASK_PERMISSIONS);

        } else {
            Account[] accounts = AccountManager.get(this).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    possibleEmail = account.name;

                }
            }
        }

        qrcode = ((ImageView) findViewById(R.id.qrCode));
        for (int i = 0; i <= ActiveMilesGUI.NumberOfActivities * 3; i++)
            getLoaderManager().initLoader(i, null, this);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearlayout1);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        textForVoucher = (TextView) findViewById(R.id.TextForVoucher);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(QrCodeActivityGen.this, "Permissions Granted", Toast.LENGTH_LONG)
                            .show();
                    finish();

                } else {
                    Toast.makeText(QrCodeActivityGen.this, "Permissions Denied", Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        String start;
        int id = loader.getId();
        int currActivity = id % ActiveMilesGUI.NumberOfActivities;

        if (id < ActiveMilesGUI.NumberOfActivities) {
            numActivitReceivedType1++;
            start = UtilsCalendar.getStartDay(DataToGetPerformance);
            for (int i = 0; i < 24; i++) {
                dataPerfromance1[currActivity][i] = 0;
            }

            if (cursor.moveToFirst())
                do {
                    int curHour = (int) (Long.parseLong(cursor.getString(0)) - Long.parseLong(start));
                    dataPerfromance1[currActivity][curHour] = (int) cursor.getDouble(1);
                    energyDay[curHour] += (int) (dataPerfromance1[currActivity][curHour] * ActiveMilesGUI.getMET[currActivity] / (60.0 / ActivityDetectorBase.sizeOfSegment * 1000));

                } while (cursor.moveToNext());
            if (numActivitReceivedType1 == ActiveMilesGUI.NumberOfActivities) {

                int totalEnergy = 0;
                for (int i = 0; i < 24; i++)
                    totalEnergy += energyDay[i];
                try {
                    QRCodeWriter writer = new QRCodeWriter();
                    BitMatrix bitMatrix = writer.encode("Id:" + possibleEmail + " Energ:" + totalEnergy + " Date: " + UtilsCalendar.timeToStringForView(DataToGetPerformance), BarcodeFormat.QR_CODE, 512, 512);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }
                    qrcode.setImageBitmap(bmp);
                    textForVoucher.setText(textForVoucher.getText().toString() + " " + totalEnergy + getString(R.string.MetMinutes));
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader cursorLoader = null;
        String start;
        String end;
        Uri uri;
        if (id < ActiveMilesGUI.NumberOfActivities) {
            start = UtilsCalendar.getStartDay(DataToGetPerformance);
            end = UtilsCalendar.getEndDay(DataToGetPerformance);

            uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_HOUR);
            uri = uri.buildUpon().appendQueryParameter("start", start).build();
            uri = uri.buildUpon().appendQueryParameter("end", end).build();
            uri = uri.buildUpon().appendQueryParameter("activity", "" + id % ActiveMilesGUI.NumberOfActivities).build();
            cursorLoader = new CursorLoader(this, uri, null, null, null, null);

        }
        return cursorLoader;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }
}