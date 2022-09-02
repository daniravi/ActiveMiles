package org.imperial.activemilespro.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.database.SettingTable;
import org.imperial.activemilespro.interface_utility.UtilsCalendar;
import org.imperial.activemilespro.service.ActivityDetectorBase;
import org.imperial.activemilespro.tabsswipe.TabsPagerAdapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore.Images;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class PersonalPerformanceActivity extends FacebookManager implements LoaderManager.LoaderCallbacks<Cursor>, /* ActionBar.TabListener,*/OnTouchListener {

    private CalendarView myDataPicker;
    private ProgressDialog progress;
    private final String TAG = "MyPerformanceActiovity";
    private TextView energyText;
    private TextView distanceText;
    private TextView AvgSpeedText;
    private TextView PeakVelocityText;
    private TextView StepText;
    private TextView LastVelocityText;
    private long DataToGetPerformance;
    private int numActivitReceivedType1 = 0;
    private int numActivitReceivedType2 = 0;
    private int numActivitReceivedType3 = 0;
    private int totalEnergy = 0;
    private TextView giorno;
    private TextView mese;
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private PagerTabStrip tabStrip;
    private String[] tabs = {"Day", "Week", "Month", "Activities", "Elevation & Speed"};
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    private final SimpleDateFormat month_date = new SimpleDateFormat("MMM", Locale.getDefault());
    private final SimpleDateFormat day_date = new SimpleDateFormat("dd", Locale.getDefault());
    private SlidingMenu sm;
    private TextView textTargetMet;
    private static final int maxTarget = 3000;
    private static final int minTarget = 100;

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

    private void startPlay() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.like_main);
        try {
            mp.prepare();
        } catch (IllegalStateException | IOException ignored) {
        }
        mp.start();
    }

    public void yesterday(View v) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        myDataPicker.setDate(UtilsCalendar.TimeToTimestamp(year, month, day, 0, 0));

        applyData(v);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onRestart() {
        super.onRestart();
        resturtLoader();
    }

    public void today(View v) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        myDataPicker.setDate(UtilsCalendar.TimeToTimestamp(year, month, day, 0, 0));

        applyData(v);
    }

    public void applyData(View ignored) {

        DataToGetPerformance = myDataPicker.getDate();
        mese.setText(month_date.format(myDataPicker.getDate()));
        giorno.setText(day_date.format(myDataPicker.getDate()));
        final Handler h = new Handler(Looper.getMainLooper());

        final Runnable r = new Runnable() {
            public void run() {
                toggle();
            }
        };
        h.post(r);
        resturtLoader();

    }

    private void resturtLoader() {
        numActivitReceivedType1 = 0;
        numActivitReceivedType2 = 0;
        numActivitReceivedType3 = 0;
        for (int i = 0; i < 24; i++) {
            mAdapter.energyDay[i] = 0;
        }
        for (int i = 0; i < 7; i++) {
            mAdapter.energyWeek[i] = 0;
        }
        for (int i = 0; i < 31; i++) {
            mAdapter.energyMonth[i] = 0;
        }
        for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++) {
            mAdapter.cumulativeActivity[i] = 0;
        }
        for (int i = 0; i <= ActiveMilesGUI.NumberOfActivities * 3 + 2; i++)
            this.getSupportLoaderManager().restartLoader(i, null, this);

    }

    public void openSettings(View ignored) {
        sm.toggle();
    }

    public void loadedComplete() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                facebookButon.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        DataToGetPerformance = getIntent().getLongExtra("data", 0);

        setContentView(R.layout.my_performance);
        setBehindContentView(R.layout.conf_personalperformance);

        sm = getSlidingMenu();
        sm = getSlidingMenu();
        sm.setSlidingEnabled(true);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);

        initializeFacebook();
        if (ActiveMilesGUI.debug) {
            tabs = new String[6];
            tabs[0] = "Day";
            tabs[1] = "Week";
            tabs[2] = "Month";
            tabs[3] = "Activities";
            tabs[4] = "Elevation & Speed";
            tabs[5] = "Spec. Activity";
        }
        progress = new ProgressDialog(this);
        myDataPicker = (CalendarView) this.findViewById(R.id.datePicker);
        giorno = (TextView) this.findViewById(R.id.giorno);
        mese = (TextView) this.findViewById(R.id.mese);

        giorno.setText(day_date.format(System.currentTimeMillis()));
        mese.setText(month_date.format(System.currentTimeMillis()));
        myDataPicker.setOnDateChangeListener(new OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(@NonNull CalendarView arg0, int year, int month, int date) {
                myDataPicker.setDate(UtilsCalendar.TimeToTimestamp(year, month, date, 0, 0));
            }
        });

        distanceText = (TextView) findViewById(R.id.Distance);
        PeakVelocityText = (TextView) findViewById(R.id.PeakVelocity);
        AvgSpeedText = (TextView) findViewById(R.id.Speed);
        StepText = (TextView) findViewById(R.id.Step);
        LastVelocityText = (TextView) findViewById(R.id.LastVelocity);
        energyText = (TextView) findViewById(R.id.BurnEnergy);

        LinearLayout calendario = (LinearLayout) this.findViewById(R.id.calendario);
        calendario.setOnTouchListener(this);

        facebookButon.setOnTouchListener(this);
        facebookButon.setOnClickListener(new RadioButton.OnClickListener() {
            @Override
            public void onClick(View vee) {

                takeSnapshot(null);
            }
        });

        tabStrip = (PagerTabStrip) this.findViewById(R.id.tabStrip);

        viewPager = (ViewPager) this.findViewById(R.id.pager);

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                float dpi = getResources().getDisplayMetrics().density;
                mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), (int) (viewPager.getMeasuredWidth() / dpi),
                        (int) ((viewPager.getMeasuredHeight() - tabStrip.getMeasuredHeight()) / dpi), PersonalPerformanceActivity.this, tabs);
                viewPager.setAdapter(mAdapter);
                for (int i = 0; i <= ActiveMilesGUI.NumberOfActivities * 3 + 2; i++)
                    getSupportLoaderManager().initLoader(i, null, PersonalPerformanceActivity.this);
            }
        });

        SeekBar seekBarTargetMet = (SeekBar) findViewById(R.id.SeekBarTargetMet);
        textTargetMet = (TextView) findViewById(R.id.TextTargetMet);

        seekBarTargetMet.setMax(maxTarget);

        seekBarTargetMet.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int levelOfPrecision, boolean arg2) {
                if (levelOfPrecision > minTarget) {
                    Uri uri;
                    textTargetMet.setText(levelOfPrecision + getString(R.string.MetMinutes));
                    ContentValues values = new ContentValues();
                    values.put(SettingTable.COLUMN_TARGET_MET, levelOfPrecision);
                    uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_SETTING);
                    getContentResolver().update(uri, values, SettingTable.COLUMN_ID + " = ? ", new String[]{"1"});
                    ActiveMilesGUI.TargetMet = levelOfPrecision;
                } else
                    arg0.setProgress(minTarget);

            }
        });

        seekBarTargetMet.setProgress(ActiveMilesGUI.TargetMet);
    }

    private void openProgressBar(View ignored) {
        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                progress.setMessage("Upload data:");
                progress.setCancelable(false);
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setIndeterminate(true);
                progress.show();
            }
        });

        final int totalProgressTime = 100;
        Thread progresBarTrhead = new Thread() {

            @Override
            public void run() {

                int jumpTime = 0;
                while (jumpTime < totalProgressTime) {
                    try {
                        sleep(200);
                        jumpTime += 5;
                        progress.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        };
        progresBarTrhead.start();

    }

    void closeProgressBar() {

        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                progress.hide();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeProgressBar();
        progress.dismiss();
    }


    private void takeSnapshot(View v) {
        if (hasPermission()) {
            openProgressBar(v);
            viewPager.setDrawingCacheEnabled(true);
            viewPager.buildDrawingCache(true);

            Bitmap snapshotHolder = viewPager.getDrawingCache(true).copy(Config.RGB_565, false);
            viewPager.destroyDrawingCache();

            postPhoto(snapshotHolder, sharePhotoCallback);

            viewPager.setDrawingCacheEnabled(false);

        } else {
            obtainPermission();
            Toast.makeText(getApplicationContext(), "Obtain facebook permission before share the Daily Performance!", Toast.LENGTH_LONG).show();
        }
    }

    private void shareOnOtherSocial(ArrayList<String> nameApp, Bitmap inImage) {
        List<Intent> targetedShareIntents = new ArrayList<>();
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("image/jpeg");
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo) {
                Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
                targetedShare.setType("image/jpeg"); // put here your mime type

                for (int j = 0; j < nameApp.size(); j++)
                    if (info.activityInfo.packageName.toLowerCase(Locale.ENGLISH).contains(nameApp.get(j)) || info.activityInfo.name.toLowerCase(Locale.ENGLISH).contains(nameApp.get(j))) {
                        targetedShare.putExtra(Intent.EXTRA_TEXT, "My body of post/email");
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        String path = Images.Media.insertImage(this.getContentResolver(), inImage, "Title", null);
                        // targetedShare.putExtra(Intent.EXTRA_STREAM,
                        // Uri.fromFile(new File(imagePath)) );
                        targetedShare.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                        targetedShare.setPackage(info.activityInfo.packageName);
                        targetedShareIntents.add(targetedShare);
                    }
            }
            if (targetedShareIntents.size() > 0) {
                Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Select app to share");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                startActivity(chooserIntent);
            }
        }
    }

    private final FacebookCallback<Sharer.Result> sharePhotoCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            Log.d(TAG, "Canceled");

        }

        @Override
        public void onError(FacebookException error) {
            Log.d(TAG, String.format("Error: %s", error.toString()));
            String title = getString(R.string.error);
            String alertMessage = error.getMessage();
            showResult(title, alertMessage);
            closeProgressBar();
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d(TAG, "Success!");
            if (result.getPostId() != null) {
                postStory(result.getPostId());
            }
        }

        private void showResult(String title, String alertMessage) {
            new AlertDialog.Builder(PersonalPerformanceActivity.this).setTitle(title).setMessage(alertMessage)
                    .setPositiveButton(R.string.ok, null).show();
        }
    };

    private void postStory(String idImage) {
        int[] listOfArray = new int[24];
        String listOfArrayString = "";
        for (int i = 0; i < 24; i++) {
            listOfArray[i] = mAdapter.energyDay[i];
            listOfArrayString = listOfArrayString.concat(String.valueOf(listOfArray[i]) + ",");
        }

        String currTime = UtilsCalendar.getStartDay(System.currentTimeMillis());

        String timeInTheDatapicker = UtilsCalendar.getStartDay(myDataPicker.getDate());
        Calendar c = Calendar.getInstance();
        String dataDay;
        if (currTime.compareTo(timeInTheDatapicker) == 0) {

            dataDay = UtilsCalendar.TimeToTimeUTC_FB(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

        } else {
            c.setTimeInMillis(myDataPicker.getDate());
            dataDay = UtilsCalendar.TimeToTimeUTC_FB(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 23, 59);
        }

        ShareOpenGraphObject performance = new ShareOpenGraphObject.Builder().putString("og:type", "activemiles:performance").putString("og:title", "Daily Activity")
                .putString("og:description", "Can you beat me?")/*.putPhoto("og:image", sharePhoto)*/.putString("og:url", "https://www.facebook.com/photo.php?fbid=" + idImage)
                .putString("activemiles:performance_data", listOfArrayString).putInt("activemiles:total_score", this.totalEnergy).putString("activemiles:data", dataDay)
                .putString("fb:app_id", getString(R.string.app_fb_id)).build();

        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder().setActionType("activemiles:share").putObject("performance", performance).putBoolean("fb:explicitly_shared", true).build();
        ShareOpenGraphContent content1 = new ShareOpenGraphContent.Builder().setPreviewPropertyName("performance").setAction(action).build();

        // TODO use the API of facebook in case the dialog is not availeble
        ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, defaultCallBack);
        closeProgressBar();
        shareDialog.show(content1);

    }

    // TODO questo metodo contiene lo stesso codice in MapActivity.
    // Sarebbe meglio unire il codice....
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        String start;
        int id = loader.getId();
        int currActivity = id % ActiveMilesGUI.NumberOfActivities;

        if (id < ActiveMilesGUI.NumberOfActivities) {
            numActivitReceivedType1++;
            start = UtilsCalendar.getStartDay(DataToGetPerformance);
            for (int i = 0; i < 24; i++) {
                mAdapter.dataPerfromance1[currActivity][i] = 0;
            }

            if (cursor.moveToFirst())
                do {
                    int curHour = (int) (Long.parseLong(cursor.getString(0)) - Long.parseLong(start));
                    mAdapter.dataPerfromance1[currActivity][curHour] = (int) cursor.getDouble(1);
                    mAdapter.energyDay[curHour] += (int) (mAdapter.dataPerfromance1[currActivity][curHour] * ActiveMilesGUI.getMET[currActivity] / (60.0 / ActivityDetectorBase.sizeOfSegment * 1000));
                    mAdapter.cumulativeActivity[currActivity] += mAdapter.dataPerfromance1[currActivity][curHour];

                } while (cursor.moveToNext());
            if (numActivitReceivedType1 == ActiveMilesGUI.NumberOfActivities) {

                mAdapter.setPerfomanceDay(UtilsCalendar.getStartDayInTimeStemp(DataToGetPerformance));
                // mAdapter.setPerfomanceMet(UtilsCalendar.getStartDayInTimeStemp(DataToGetPerformance));
                mAdapter.setPerfomanceAct(UtilsCalendar.getStartDayInTimeStemp(DataToGetPerformance));
                totalEnergy = 0;
                for (int i = 0; i < 24; i++)
                    totalEnergy += mAdapter.energyDay[i];
                energyText.setText("" + totalEnergy);
            }

        } else if (id >= ActiveMilesGUI.NumberOfActivities && id < ActiveMilesGUI.NumberOfActivities * 2) {
            numActivitReceivedType2++;
            start = UtilsCalendar.getStartWeek(DataToGetPerformance);
            for (int i = 0; i < 7; i++) {
                mAdapter.dataPerfromance2[currActivity][i] = 0;
            }
            if (cursor.moveToFirst())
                do {
                    int curday = (int) UtilsCalendar.computeDiffDay(cursor.getString(0), start);
                    mAdapter.dataPerfromance2[currActivity][curday] = (int) cursor.getDouble(1);
                    mAdapter.energyWeek[curday] += (int) (mAdapter.dataPerfromance2[currActivity][curday] * ActiveMilesGUI.getMET[currActivity] / (60.0 / ActivityDetectorBase.sizeOfSegment * 1000));
                } while (cursor.moveToNext());

            if (numActivitReceivedType2 == ActiveMilesGUI.NumberOfActivities) {

                mAdapter.setPerfomanceWeek(UtilsCalendar.getStartWeekInTimeStemp(DataToGetPerformance));
            }
        } else if (id >= ActiveMilesGUI.NumberOfActivities * 2 && id < ActiveMilesGUI.NumberOfActivities * 3) {
            numActivitReceivedType3++;
            start = UtilsCalendar.getStartMonth(DataToGetPerformance);
            for (int i = 0; i < 31; i++) {
                mAdapter.dataPerfromance3[currActivity][i] = 0;
            }
            if (cursor.moveToFirst())
                do {
                    int curDay = (int) UtilsCalendar.computeDiffDay(cursor.getString(0), start);
                    mAdapter.dataPerfromance3[currActivity][curDay] = (int) cursor.getDouble(1);
                    mAdapter.energyMonth[curDay] += (int) (mAdapter.dataPerfromance3[currActivity][curDay] * ActiveMilesGUI.getMET[currActivity] / (60.0 / ActivityDetectorBase.sizeOfSegment * 1000));
                } while (cursor.moveToNext());
            if (numActivitReceivedType3 == ActiveMilesGUI.NumberOfActivities) {

                mAdapter.setPerfomanceMonth(UtilsCalendar.getStartMonthinTimeStemp(DataToGetPerformance));
            }
        } else if (id == ActiveMilesGUI.NumberOfActivities * 3) {
            Location newLoc = new Location("dummyprovider");
            Location prevLoc = new Location("dummyprovider");
            LatLng myLatLng;
            double distanceInMeters;
            float sumOfDistanza = 0;
            int numSpeed = 0;
            double currSpeed;
            double prevSpeed = 0;
            double sumSpeed = 0;
            double currAccuracy = 0;
            double prevAccuracy = 0;
            long currTimeStemp;
            long prevTimeStep = 0;
            long diffSecondi;
            double currError;
            double maxSpeed = 0;
            if (cursor.moveToFirst()) {

                myLatLng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
                prevLoc.setLatitude(myLatLng.latitude);
                prevLoc.setLongitude(myLatLng.longitude);
                if (cursor.moveToNext()) {
                    do {

                        myLatLng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
                        currTimeStemp = Long.parseLong(cursor.getString(3));
                        newLoc.setLatitude(myLatLng.latitude);
                        newLoc.setLongitude(myLatLng.longitude);
                        distanceInMeters = SphericalUtil.computeDistanceBetween(new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude()), myLatLng);
                        currError = (currAccuracy + prevAccuracy) / 0.8246;
                        distanceInMeters -= (currError / 5);
                        currAccuracy = cursor.getDouble(4);
                        if (prevTimeStep == 0)
                            prevTimeStep = currTimeStemp;
                        else {
                            diffSecondi = UtilsCalendar.computeDiffSec("" + currTimeStemp, "" + prevTimeStep);
                            currSpeed = distanceInMeters / diffSecondi;
                            if (((((currError < (distanceInMeters / 5)) && distanceInMeters < diffSecondi*27) && distanceInMeters > 30) || (currError < 30 && distanceInMeters > 10) /*|| currAccuracy < 20*/)
                                    && diffSecondi > 5) {
                                prevTimeStep = currTimeStemp;
                                prevAccuracy = currAccuracy;
                                prevSpeed = currSpeed;
                                if (maxSpeed < Math.abs(currSpeed))
                                    maxSpeed = Math.abs(currSpeed);
                                sumSpeed += currSpeed;
                                numSpeed++;
                                sumOfDistanza += distanceInMeters;
                                prevLoc = new Location("dummyprovider");
                                prevLoc.setLatitude(newLoc.getLatitude());
                                prevLoc.setLongitude(newLoc.getLongitude());
                            }
                        }
                    } while (cursor.moveToNext());
                }

            }
            DecimalFormat df = new DecimalFormat("0.00");
            distanceText.setText(df.format(Math.abs(sumOfDistanza)));
            AvgSpeedText.setText(df.format(numSpeed > 0 ? Math.abs(sumSpeed / numSpeed) : 0));
            LastVelocityText.setText(df.format(Math.abs(prevSpeed)));
            PeakVelocityText.setText(df.format(maxSpeed));
            PeakVelocityText.setText(df.format(maxSpeed));
        } else if (id == ActiveMilesGUI.NumberOfActivities * 3 + 1) {
            if (cursor.moveToFirst())
                StepText.setText("" + cursor.getInt(0));
        } else if (id == ActiveMilesGUI.NumberOfActivities * 3 + 2) {
            mAdapter.speed.clear();
            mAdapter.alt.clear();
            mAdapter.timeStemp.clear();
            if (cursor.moveToFirst())
                do {
                    try {
                        Date dataConverter = sdf.parse(cursor.getString(0));
                        mAdapter.speed.add(cursor.getDouble(1));
                        mAdapter.alt.add(cursor.getDouble(2) / 10);
                        mAdapter.timeStemp.add(UtilsCalendar.timeToStringDateForElevat(dataConverter.getTime()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } while (cursor.moveToNext());

            mAdapter.setPerfomanceSpeedAlt(UtilsCalendar.getStartDayInTimeStemp(DataToGetPerformance));

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

        } else if (id >= ActiveMilesGUI.NumberOfActivities && id < ActiveMilesGUI.NumberOfActivities * 2) {
            start = UtilsCalendar.getStartWeek(DataToGetPerformance);
            end = UtilsCalendar.getEndWeek(DataToGetPerformance);

            uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_DAY);
            uri = uri.buildUpon().appendQueryParameter("start", start).build();
            uri = uri.buildUpon().appendQueryParameter("end", end).build();
            uri = uri.buildUpon().appendQueryParameter("activity", "" + id % ActiveMilesGUI.NumberOfActivities).build();
            cursorLoader = new CursorLoader(this, uri, null, null, null, null);

        } else if (id >= ActiveMilesGUI.NumberOfActivities * 2 && id < ActiveMilesGUI.NumberOfActivities * 3) {
            start = UtilsCalendar.getStartMonth(DataToGetPerformance);
            end = UtilsCalendar.getEndMonth(DataToGetPerformance);

            uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_DAY);
            uri = uri.buildUpon().appendQueryParameter("start", start).build();
            uri = uri.buildUpon().appendQueryParameter("end", end).build();
            uri = uri.buildUpon().appendQueryParameter("activity", "" + id % ActiveMilesGUI.NumberOfActivities).build();
            cursorLoader = new CursorLoader(this, uri, null, null, null, null);

        } else if (id == ActiveMilesGUI.NumberOfActivities * 3) {

            start = UtilsCalendar.getStartDayInMinute(DataToGetPerformance);
            end = UtilsCalendar.getEndDayInMinute(DataToGetPerformance);

            uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_GPS);
            uri = uri.buildUpon().appendQueryParameter("start", start).build();
            uri = uri.buildUpon().appendQueryParameter("end", end).build();

            cursorLoader = new CursorLoader(this, uri, null, null, null, null);
        } else if (id == ActiveMilesGUI.NumberOfActivities * 3 + 1) {
            start = UtilsCalendar.getStartDay(DataToGetPerformance);
            end = UtilsCalendar.getEndDay(DataToGetPerformance);
            uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.SUM_STEP);
            uri = uri.buildUpon().appendQueryParameter("start", start).build();
            uri = uri.buildUpon().appendQueryParameter("end", end).build();
            cursorLoader = new CursorLoader(this, uri, null, null, null, null);
        } else if (id == ActiveMilesGUI.NumberOfActivities * 3 + 2) {

            start = UtilsCalendar.getStartDay(DataToGetPerformance);
            end = UtilsCalendar.getEndDay(DataToGetPerformance);
            uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.GET_SPEED_ALT);
            uri = uri.buildUpon().appendQueryParameter("start", start).build();
            uri = uri.buildUpon().appendQueryParameter("end", end).build();
            cursorLoader = new CursorLoader(this, uri, null, null, null, null);
        }
        return cursorLoader;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

}