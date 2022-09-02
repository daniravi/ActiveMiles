package org.imperial.activemilespro.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.database.RecordTable;
import org.imperial.activemilespro.gui.FacebookManager;
import org.imperial.activemilespro.interface_utility.UtilsCalendar;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class VoiceLabeling2 extends FacebookManager implements LoaderManager.LoaderCallbacks<Cursor> {

    private TextView txtSpeechInput;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private long dataToAnalize;
    private ListView listView;
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy  |  HH:mm:ss  |  ", Locale.getDefault());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.labeling2);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        ImageButton btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        listView = (ListView) findViewById(R.id.listView);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dataToAnalize = extras.getLong("data");
        }
        setBehindContentView(R.layout.conf_diary);
        SlidingMenu sm = getSlidingMenu();
        sm.setSlidingEnabled(true);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);
        initializeFacebook();
        getSupportLoaderManager().initLoader(0, null, this);

    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));

		/*SpeechRecognizer recognizer = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
        recognizer.setRecognitionListener(listener);
		recognizer.startListening(intent);*/
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Receiving speech input
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    long name = System.currentTimeMillis();
                    ContentValues values = new ContentValues();
                    values.put(RecordTable.COLUMN_LABEL, result.get(0));
                    values.put(RecordTable.COLUMN_DATA, UtilsCalendar.timeToStringDate(name).substring(0, 8));
                    values.put(RecordTable.COLUMN_TIME_STAMP_LONG, String.valueOf(name));
                    Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.GET_RECROD);
                    getContentResolver().insert(uri, values);
                }
                break;
            }

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri;

        uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.GET_RECROD);
        uri = uri.buildUpon().appendQueryParameter("time_stamp", UtilsCalendar.timeToStringDate(dataToAnalize).substring(0, 8)).build();

        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<String> labelArray = new ArrayList<>();
        Date date;
        String timeSpampFormatted;
        if (data.moveToFirst()) {
            do {
                date = new Date(Long.parseLong(data.getString(1)));
                timeSpampFormatted = formatter.format(date);
                labelArray.add(timeSpampFormatted.concat(data.getString(0)));
            } while (data.moveToNext());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labelArray);
        listView.setAdapter(arrayAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


}
