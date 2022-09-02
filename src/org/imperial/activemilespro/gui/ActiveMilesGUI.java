package org.imperial.activemilespro.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.database.SettingTable;
import org.imperial.activemilespro.diary.DiaryActivity;
import org.imperial.activemilespro.nfc_tag.NFCActivity;
import org.imperial.activemilespro.service.BluetouthActivity;
import org.imperial.activemilespro.service.SensorDataServiceInertial;
import org.imperial.activemilespro.service.SensorDataServiceBase;
import org.imperial.activemilespro.service.SensorDataServiceBluetooth;
import org.imperial.activemilespro.service.ServiceController;
import org.imperial.activemilespro.service.StepDetector;
import org.imperial.activemilespro.service.VoiceLabeling;
import org.imperial.activemilespro.service.VoiceLabeling2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

//TODO stoppare i vari timer e tread quando si esce da una attvitity 
//TODO cercare tutti i ciao

public class ActiveMilesGUI extends FacebookManager implements OnTouchListener {
    private final long checkIfServiceisActive_Time = 5 * 1000;
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;
    private RadioButton radio1;
    private RadioButton radio2;
    private RadioButton radio3;
    private TextView textFrequence;
    private TextView sensitivityLevel;
    private final String[] freqeuncyDescription = {"Low Update", "Medium Update", "High Update", "GPS"};
    public static SensorDataServiceBase remoteService = null;                                                                                                    //
    public static final boolean debug = true;
    public static int color_hist_facebook;
    public static int DeviceType = -1;
    private static int levelOfPrecision;
    public static final String[] Activities = {"No Label", "Running", "Walking", "Cycling", "Casual Movement", "Public Transport", "No Activity", "Reading Book", "Up Stairs",
            "Down Stairs", "Jumping", "Rowing", "Eating", "Social Interaction"};
    public static final String[] ActivitiesToShow = {"No Label", "[            Running           ]", "[           Walking            ]", "[           Cycling             ]",
            "[  Casual Movement   ]", "[         Stationary          ]", "[         No Activity        ]", "[     Reading Book      ]", "[        Up Stairs           ]",
            "[       Down Stairs       ]", "[         Jumping           ]", "[           Rowing           ]", "[            Eating            ]", "[  Social Interaction  ]"};
    public static final double[] getMET = {0.0 /* No Label*/, 10.0 /* running*/, 2.7/* walking*/, 6.00 /* cycling*/, 1.5 /* Casual Movement*/, 0.9 /* public trasport*/
            , 0.9/*sleeping*/, 1.0 /* standing*/, 5/*"Up Stairs"*/, 5/*"Down Stairs"*/, 1 /*"Sitting"*/, 5 /*"Rowing"*/, 5 /*"Eating"*/,
            5														/*"Social Interaction"*/};

    public static final boolean[] ActivitiesSelected = {false, true, true, true, true, true, true, false, true, true, true, true, true, true};

    public static final int NumberOfActivities = Activities.length;
    public static int TargetMet;
    private PopupWindow pw;
    private ArrayList<String> items;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 200;

    @Override
    public void onNewIntent(Intent intent) {
        /*Uri targetUri = intent.getData();

		if (targetUri != null)
		{
			String link = targetUri.toString();
			if (link.compareTo("maps") == 0)
				recipIndex = 1;
			else if (link.compareTo("Activity") == 0)
				recipIndex = 2;
			else
				recipIndex = -1;
		}*/

    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Intent intent = new Intent(ActiveMilesGUI.this, VoiceLabeling.class);
            intent.putExtra("StartVoiceRecord", "StartVoiceRecord");
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

    public void openMap(View ignored) {
        Intent intent = new Intent(ActiveMilesGUI.this, MapActivity.class);
        intent.putExtra("data", System.currentTimeMillis());
        startActivity(intent);
    }

    public void openAlbum(View ignored) {
        Intent intent = new Intent(ActiveMilesGUI.this, DiaryActivity.class);
        intent.putExtra("data", System.currentTimeMillis());
        startActivity(intent);
    }

    public void openCamera(View ignored) {

        Intent intent = new Intent(this, CameraView.class);
        intent.putExtra("data", System.currentTimeMillis());
        startActivity(intent);
    }

    public void openMe(View ignored) {
        Intent intent = new Intent(ActiveMilesGUI.this, PersonalPerformanceActivity.class);
        intent.putExtra("data", System.currentTimeMillis());
        startActivity(intent);
    }

    public void openSocial(View ignored) {
        Intent intent = new Intent(ActiveMilesGUI.this, ComparePerformanceActivity.class);
        intent.putExtra("data", System.currentTimeMillis());
        startActivity(intent);
    }

    public void openLiveData(View ignored) {
        Intent intent = new Intent(ActiveMilesGUI.this, LiveView.class);
        startActivity(intent);
    }

    public void openNFC(View ignored) {
        Intent intent = new Intent(ActiveMilesGUI.this, NFCActivity.class);
        startActivity(intent);
    }

    public void openShowActivity(View ignored) {
        Intent intent = new Intent(ActiveMilesGUI.this, ShowActivity.class);
        startActivity(intent);
    }

    public void openQrCodeGen(View ignored) {
        Intent intent = new Intent(ActiveMilesGUI.this, QrCodeActivityGen.class);
        startActivity(intent);
    }

    public void openLabeling(View ignored) {
        Intent intent = new Intent(ActiveMilesGUI.this, VoiceLabeling.class);
        startActivity(intent);
    }

    public void openLabeling2(View ignored) {
        Intent intent = new Intent(ActiveMilesGUI.this, VoiceLabeling2.class);
        intent.putExtra("data", System.currentTimeMillis());
        startActivity(intent);
    }

    public void openSettings(View ignored) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toggle();
            }
        }, 0);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setAlpha(.5f);
            startPlay();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            v.setAlpha(1f);
        }
        return false;
    }

    private void startSensorService() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (DeviceType == 1) {
                    radio1.setChecked(true);
                    radio2.setChecked(false);
                    radio3.setChecked(false);
                    stopService(new Intent(ActiveMilesGUI.this, SensorDataServiceBluetooth.class));
                    stopService(new Intent(ActiveMilesGUI.this, SensorDataServiceInertial.class));
                    Intent bindIntent = new Intent(ActiveMilesGUI.this, SensorDataServiceInertial.class);
                    startService(bindIntent);
                    getApplicationContext().bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

                } else if (DeviceType == 2) {

                    radio1.setChecked(false);
                    radio2.setChecked(true);
                    radio3.setChecked(false);
                    stopService(new Intent(ActiveMilesGUI.this, SensorDataServiceInertial.class));
                    stopService(new Intent(ActiveMilesGUI.this, SensorDataServiceBluetooth.class));
            /*TODO verificare se il bluothout e' attivo e se il dispositivo e'  pronto*/
                    Intent bindIntent = new Intent(ActiveMilesGUI.this, SensorDataServiceBluetooth.class);
                    startService(bindIntent);
                    getApplicationContext().bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

                } else if (DeviceType == 0) {
                    radio1.setChecked(false);
                    radio2.setChecked(false);
                    radio3.setChecked(true);
                    stopService(new Intent(ActiveMilesGUI.this, SensorDataServiceInertial.class));
                    stopService(new Intent(ActiveMilesGUI.this, SensorDataServiceBluetooth.class));
                    mPendingIntent.cancel();
                    mAlarmManager.cancel(mPendingIntent);

                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length>=2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED
                        ) {
                    Toast.makeText(ActiveMilesGUI.this, "Permissions Granted", Toast.LENGTH_LONG)
                            .show();
                    startSensorService();

                } else {
                    Toast.makeText(ActiveMilesGUI.this, "Permissions Denied", Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        setBehindContentView(R.layout.conf_activemilesgui);

        Bundle extras = getIntent().getExtras();
        if ((extras != null && extras.getString("Type") != null && extras.getString("Type").compareTo("StartService") == 0))
            moveTaskToBack(true);

        SlidingMenu sm = getSlidingMenu();
        sm.setSlidingEnabled(true);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);
        initializeFacebook();

        radio1 = (RadioButton) findViewById(R.id.r1);
        radio2 = (RadioButton) findViewById(R.id.r2);
        radio3 = (RadioButton) findViewById(R.id.r3);

        SeekBar SeekBarLocationUpdate = (SeekBar) findViewById(R.id.seekLevelUpdate);
        textFrequence = (TextView) findViewById(R.id.textFrequence);
        sensitivityLevel = (TextView) findViewById(R.id.sensitivityLevel);
        SeekBar SeekBarStepSensitivity = (SeekBar) findViewById(R.id.seekStepSensitivity);

        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        ActiveMilesGUI.this.createServiceControllerPendIntent();
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, checkIfServiceisActive_Time, mPendingIntent);

        Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_SETTING);

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        levelOfPrecision = 0;
        int step_sensitivity = 2;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                DeviceType = cursor.getInt(0);
                levelOfPrecision = cursor.getInt(2);
                step_sensitivity = cursor.getInt(3);
                color_hist_facebook = cursor.getInt(6);
                TargetMet = cursor.getInt(7);

                try {
                    getApplicationContext().unbindService(mConnection);
                } catch (IllegalArgumentException ignored) {

                }
            }
            cursor.close();
        }
        int hasPermission1 = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        int hasPermission4 = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if ((hasPermission1 != PackageManager.PERMISSION_GRANTED) || (hasPermission4 != PackageManager.PERMISSION_GRANTED)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ActivityCompat.requestPermissions(ActiveMilesGUI.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }
            });

        } else {
            startSensorService();
        }
        SeekBarLocationUpdate.setProgress(levelOfPrecision);

        textFrequence.setText(freqeuncyDescription[levelOfPrecision]);
        SeekBarLocationUpdate.setMax(freqeuncyDescription.length - 1);
        SeekBarLocationUpdate.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int levelOfPrecision, boolean arg2) {

                ActiveMilesGUI.levelOfPrecision = levelOfPrecision;
                Uri uri;
                textFrequence.setText(freqeuncyDescription[levelOfPrecision]);
                ContentValues values = new ContentValues();
                values.put(SettingTable.COLUMN_TYPE_SPPED_UPDATE, levelOfPrecision);
                uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_SETTING);
                getContentResolver().update(uri, values, SettingTable.COLUMN_ID + " = ? ", new String[]{"1"});
                if (remoteService != null)
                    remoteService.getLocationTracker().setProviderPrecision(levelOfPrecision);
            }
        });

        SeekBarStepSensitivity.setProgress(step_sensitivity);
        SeekBarStepSensitivity.setMax(StepDetector.sensitivityList.length - 1);
        SeekBarStepSensitivity.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int step_sensitivity, boolean arg2) {

                Uri uri;
                ContentValues values = new ContentValues();
                values.put(SettingTable.COLUMN_SENSITIVITY_STEP, step_sensitivity);
                uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_SETTING);
                getContentResolver().update(uri, values, SettingTable.COLUMN_ID + " = ? ", new String[]{"1"});
                if (remoteService != null)
                    if (remoteService instanceof SensorDataServiceInertial)
                        (remoteService).getActivityDetector().changeSensitivity(step_sensitivity);
                // TODO add sensibility also for bluethoot
                sensitivityLevel.setText(getString(R.string.level) + step_sensitivity);
            }
        });
        sensitivityLevel.setText(getString(R.string.level) + step_sensitivity);
        TextView imageButton1 = (TextView) findViewById(R.id.settings);
        imageButton1.setOnTouchListener(this);
        TextView imageButton2 = (TextView) findViewById(R.id.me);
        imageButton2.setOnTouchListener(this);
        TextView imageButton3 = (TextView) findViewById(R.id.live);
        imageButton3.setOnTouchListener(this);
        TextView imageButton4 = (TextView) findViewById(R.id.social);
        imageButton4.setOnTouchListener(this);
        TextView imageButton5 = (TextView) findViewById(R.id.coupon);
        imageButton5.setOnTouchListener(this);
        TextView imageButton6 = (TextView) findViewById(R.id.album);
        imageButton6.setOnTouchListener(this);
        TextView imageButton7 = (TextView) findViewById(R.id.camera);
        imageButton7.setOnTouchListener(this);
        TextView imageButton8 = (TextView) findViewById(R.id.map);
        imageButton8.setOnTouchListener(this);
        TextView ShowActivityButton = (TextView) findViewById(R.id.ShowActivityButton);
        ShowActivityButton.setOnTouchListener(this);
        initializePopMenu();
    }

    public void onChangeSensorType(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        ContentValues values;
        Uri uri;
        switch (view.getId()) {
            case R.id.r1:
                if (checked)
                    radio1.setChecked(true);
                values = new ContentValues();
                DeviceType = 1;
                values.put(SettingTable.COLUMN_TYPE_SENSOR, 1);
                uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_SETTING);
                getContentResolver().update(uri, values, SettingTable.COLUMN_ID + " = ? ", new String[]{"1"});
                try {
                    getApplicationContext().unbindService(mConnection);
                } catch (IllegalArgumentException ignored) {

                }
                if (remoteService != null)
                    remoteService.stopSelf();
                Intent bindIntent = new Intent(ActiveMilesGUI.this, SensorDataServiceInertial.class);
                startService(bindIntent);
                getApplicationContext().bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
                mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, checkIfServiceisActive_Time, mPendingIntent);
                break;
            case R.id.r2:
                if (checked)
                    radio1.setChecked(false);
                startActivity(new Intent(this, BluetouthActivity.class));
                break;
            case R.id.r3:
                if (checked)
                    radio1.setChecked(false);
                values = new ContentValues();
                DeviceType = 0;
                values.put(SettingTable.COLUMN_TYPE_SENSOR, 0);
                uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_SETTING);
                getContentResolver().update(uri, values, SettingTable.COLUMN_ID + " = ? ", new String[]{"1"});
                try {
                    getApplicationContext().unbindService(mConnection);
                } catch (IllegalArgumentException ignored) {

                }
                if (remoteService != null)
                    remoteService.stopSelf();
                mPendingIntent.cancel();
                mAlarmManager.cancel(mPendingIntent);
                break;
        }
        initializePopMenu();

    }

    //TODO check the pickSpeeed it provide a wrong number
    @Override
    public void onBackPressed() {

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);

    }

    @Override
    protected void onDestroy() {

        try {
            getApplicationContext().unbindService(mConnection);
        } catch (IllegalArgumentException ignored) {

        }
        mConnection.onServiceDisconnected(null);

        super.onDestroy();
    }

    private static final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            remoteService = ((SensorDataServiceBase.LocalBinder) rawBinder).getService();
            remoteService.getLocationTracker().setProviderPrecision(levelOfPrecision);
        }

        public void onServiceDisconnected(ComponentName classname) {
        }
    };

    private void createServiceControllerPendIntent() {
        Intent intent = new Intent(ActiveMilesGUI.this, ServiceController.class);
        mPendingIntent = PendingIntent.getBroadcast(ActiveMilesGUI.this, 0, intent, 0);
    }

    private void SendDatabase(View ignored) {

        File fileDatabaseD = new File(Environment.getExternalStorageDirectory(), "performance");
        File fileDatabaseO = new File(this.getApplicationContext().getDatabasePath("performance").getAbsolutePath() + ".db");
        try {
            copyFile(fileDatabaseO, fileDatabaseD);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent email = new Intent(android.content.Intent.ACTION_SEND);
        email.setType("plain/text");
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"dani.ravi@gmail.com"});

        email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileDatabaseD));
        email.putExtra(Intent.EXTRA_SUBJECT, "Imperial ActiveMilesPro Data");
        startActivity(Intent.createChooser(email, "E-mail"));

    }

    private void startPlay() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.like_main);
        try {
            mp.prepare();
        } catch (IllegalStateException | IOException ignored) {
        }
        mp.start();
    }

    private static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            outChannel.close();
        }
    }

    private void initializePopMenu() {

        items = new ArrayList<>();
        if (DeviceType == 1)
            items.addAll(Arrays.asList(ActiveMilesGUI.ActivitiesToShow).subList(1, 7 + 1));
        if (DeviceType == 2)
            items.addAll(Arrays.asList(ActiveMilesGUI.ActivitiesToShow).subList(1, ActiveMilesGUI.NumberOfActivities));

        TextView tv = (TextView) findViewById(R.id.SelectActivityButton);

        tv.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                initiatePopUp(items);
            }
        });
        tv.setOnTouchListener(this);
    }

    private void initiatePopUp(ArrayList<String> items) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.pop_up_window, (ViewGroup) findViewById(R.id.PopUpView));

        LinearLayout layout1 = (LinearLayout) findViewById(R.id.relativeLayout1);
        pw = new PopupWindow(layout, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);

        pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pw.setTouchable(true);

        pw.setOutsideTouchable(true);
        pw.setHeight(LayoutParams.WRAP_CONTENT);

        pw.setTouchInterceptor(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    pw.dismiss();
                    return true;
                }
                return false;
            }
        });

        pw.setContentView(layout);
        pw.showAsDropDown(layout1);
        final ListView list = (ListView) layout.findViewById(R.id.DropDownListMain);
        DropDownListAdapter adapter = new DropDownListAdapter(this, items);
        list.setAdapter(adapter);
    }

}
