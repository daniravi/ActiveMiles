package org.imperial.activemilespro.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.database.SettingTable;
import org.imperial.activemilespro.gui.ActiveMilesGUI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class VoiceLabeling extends Activity {

    private TextView txtSpeechInput;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private boolean serviceIsNotRunnning = false;
    private RadioButton recordLed;
    private Button startButtom;
    private boolean ledBlink = false;
    private Spinner LabelText;
    private SensorDataServiceBase remoteService = null;
    private Timer timer_checkBlu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getIntent().getStringExtra("StartVoiceRecord") != null) {
            promptSpeechInput();
        }
        setContentView(R.layout.labeling);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        ImageButton btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        // hide the action bar
        getActionBar().hide();

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        recordLed = (RadioButton) findViewById(R.id.led);
        startButtom = (Button) findViewById(R.id.Record);
        LabelText = (Spinner) findViewById(R.id.label);
        recordLed.setClickable(false);
        timer_checkBlu = new Timer();


        if (ActiveMilesGUI.DeviceType == 1) {

            getApplicationContext().bindService(new Intent(VoiceLabeling.this, SensorDataServiceInertial.class), mConnection, Context.BIND_AUTO_CREATE);

        } else if (ActiveMilesGUI.DeviceType == 2) {
            getApplicationContext().bindService(new Intent(VoiceLabeling.this, SensorDataServiceBluetooth.class), mConnection, Context.BIND_AUTO_CREATE);

        } else {
            Toast.makeText(this.getBaseContext(), "No Device is Availeble", Toast.LENGTH_LONG).show();
            serviceIsNotRunnning = true;
        }

        if (!serviceIsNotRunnning) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ActiveMilesGUI.Activities);
            LabelText.setAdapter(adapter);

            LabelText.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    remoteService.label = LabelText.getSelectedItem().toString();
                    remoteService.idLabel = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }

            });
        }

    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (serviceIsNotRunnning)
            finish();
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    for (int i = 0; i < ActiveMilesGUI.Activities.length; i++)
                        if (result.get(0).equalsIgnoreCase(ActiveMilesGUI.Activities[i])) {
                            LabelText.setSelection(i);
                            if (remoteService.record)
                                startRecord(null);
                            startRecord(null);
                        }
                    if (result.get(0).equalsIgnoreCase("Stop")) {
                        if (remoteService.record)
                            startRecord(null);
                    }
                }
                break;
            }

        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {

            remoteService = ((SensorDataServiceBase.LocalBinder) rawBinder).getService();
            changeRecordingState(remoteService.record);
            LabelText.setSelection(remoteService.idLabel);
        }

        public void onServiceDisconnected(ComponentName classname) {

        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            Intent intent = new Intent(VoiceLabeling.this, VoiceLabeling.class);
            intent.putExtra("StartVoiceRecord", "StartVoiceRecord");
            startActivity(intent);
            finish();
            // event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void changeRecordingState(final boolean state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recordLed.setChecked(state);
                if (state) {
                    startButtom.setText(getString(R.string.StopRecording));
                    timer_checkBlu = new Timer();
                    timer_checkBlu.scheduleAtFixedRate(new TimerTask() {
                        public void run() {
                            ledBlink();
                        }
                    }, 0, 3000);
                } else {
                    if (timer_checkBlu != null) {
                        timer_checkBlu.cancel();
                    }
                    startButtom.setText(getString(R.string.StartRecording));
                    recordLed.setChecked(false);
                    recordLed.setPressed(false);
                }
            }
        });

    }

    private void ledBlink() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ledBlink = !ledBlink;
                recordLed.invalidate();
                if (ledBlink) {
                    recordLed.setChecked(true);
                    recordLed.setPressed(true);
                } else {

                    recordLed.setChecked(false);
                    recordLed.setPressed(false);
                }
            }
        });

    }

    @SuppressWarnings("resource")
    public static void copyFile(File src, File dst) throws IOException {
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

    @Override
    protected void onDestroy() {
        timer_checkBlu.cancel();
        if (!serviceIsNotRunnning) {
            getApplicationContext().unbindService(mConnection);
            mConnection.onServiceDisconnected(null);
        }
        super.onDestroy();
    }

    public void sendDatabase(View ignored) {
        File fileDatabaseD = new File(SensorDataServiceBase.filePath);
        Intent email = new Intent(android.content.Intent.ACTION_SEND);
        email.setType("plain/text");
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"dani.ravi@gmail.com"});
        email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileDatabaseD));
        email.putExtra(Intent.EXTRA_SUBJECT, "Imperial ActiveMilesPro Data");
        startActivity(Intent.createChooser(email, "E-mail"));
        remoteService.dbNeedtoBeremoveed = true;

    }

    public void startRecord(View ignored) {
        if (remoteService.dbNeedtoBeremoveed)
            remoteService.RemoveAll();
        remoteService.dbNeedtoBeremoveed = false;
        if (!remoteService.record)
            remoteService.NewSection();
        remoteService.label = LabelText.getSelectedItem().toString();
        remoteService.record = !remoteService.record;
        changeRecordingState(remoteService.record);

        Uri uri;
        ContentValues values = new ContentValues();
        values.put(SettingTable.COLUMN_CURR_LABEL, LabelText.getSelectedItem().toString());
        values.put(SettingTable.COLUMN_IS_RECORDING, remoteService.record);
        uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_SETTING);
        getContentResolver().update(uri, values, SettingTable.COLUMN_ID + " = ? ", new String[]{"1"});
    }

    public void clearDatabase(View ignored) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Are you sure you want delete all the records?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                remoteService.RemoveAll();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.show();

    }

    public void removeSection(View ignored) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Are you sure you want delete this?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (startButtom.getText().toString().compareTo("Stop Record") == 0) {
                    startRecord(null);
                }
                remoteService.RemoveSection();
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.show();

    }

}
