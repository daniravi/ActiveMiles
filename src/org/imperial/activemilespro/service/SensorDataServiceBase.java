package org.imperial.activemilespro.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.database.GPSTable;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.database.PerformanceDayTable;
import org.imperial.activemilespro.database.PerformanceHourTable;
import org.imperial.activemilespro.database.StepTable;
import org.imperial.activemilespro.gui.ActiveMilesGUI;
import org.imperial.activemilespro.gui.LiveView;
import org.imperial.activemilespro.interface_utility.IntActivityListener;
import org.imperial.activemilespro.interface_utility.IntOnLocationChange;
import org.imperial.activemilespro.interface_utility.UtilsCalendar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public abstract class SensorDataServiceBase extends Service implements IntActivityListener, IntOnLocationChange {

    private LocationTracker curr_location_Tracker;
    private String currentHour;
    private int[] curr_performanceActivities;
    private int numberOfStep;
    private static final long UpdateTableDayFreq = 1000 * 60 * 60 * 12;
    private Timer timer_ifToSaveDay;
    private final static int myID = 19811981;
    int StepSensitivity = 2;
    String deviceAddress;
    static ActivityDetectorBase curr_ActivityDetector;
    LiveView lv;
    boolean LiveViewisOn;
    private PowerManager.WakeLock wl;

    public abstract void registerDetector();

    protected abstract void unregisterDetector();

    public class LocalBinder extends Binder {
        public SensorDataServiceBase getService() {
            return SensorDataServiceBase.this;
        }
    }

    public void showOnLiveView(LiveView lv) {
        this.lv = lv;
        LiveViewisOn = true;
    }

    public void removeLiveView() {
        LiveViewisOn = false;
        this.lv = null;
    }

    public ActivityDetectorBase getActivityDetector() {

        return curr_ActivityDetector;
    }

    public LocationTracker getLocationTracker() {

        return curr_location_Tracker;
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_SETTING);

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                deviceAddress = cursor.getString(1);
                StepSensitivity = cursor.getInt(3);
                record = cursor.getInt(4) > 0;
                label = cursor.getString(5);
                for (int i = 0; i < ActiveMilesGUI.Activities.length; i++)
                    if (label.compareToIgnoreCase(ActiveMilesGUI.Activities[i]) == 0)
                        this.idLabel = i;

            }
            cursor.close();
        }

        /*uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.GET_LAST_TIMESTEMP);

        cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst())
                currentHour = cursor.getString(0);
            else
                currentHour = UtilsCalendar.currTimeToStringHour();
            cursor.close();
        } else
            currentHour = UtilsCalendar.currTimeToStringHour();
            */
        currentHour = UtilsCalendar.currTimeToStringHour();
        curr_performanceActivities = new int[ActiveMilesGUI.NumberOfActivities];
        for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++) {
            curr_performanceActivities[i] = loadPerformance(i);
            if (curr_performanceActivities[i] == -1) {
                curr_performanceActivities[i] = 0;
                insertPerformanceInDb(i);
            }
        }
        numberOfStep = loadStep();
        if (numberOfStep == -1) {
            numberOfStep = 0;
            insertStepInDb(0);
        }

        timer_ifToSaveDay = new Timer();
        timer_ifToSaveDay.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                updateTableDay();
            }
        }, 0, UpdateTableDayFreq);

        curr_location_Tracker = new LocationTracker(this, this);
        PowerManager pm = (PowerManager) getSystemService(getApplicationContext().POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        wl.acquire();


    }

    private void updateTableDay() {
        String currentDay;
        String start;
        String end;
        Uri uri;
        Cursor cursor;
        Cursor cursor2;
        long dataToAnalize;
        ContentValues values;
        for (int daysToAdd = 1; daysToAdd < 5; daysToAdd++) {
            dataToAnalize = UtilsCalendar.addDay(System.currentTimeMillis(), -daysToAdd);
            currentDay = UtilsCalendar.timeToStringDate(dataToAnalize);
            uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_DAY);
            uri = uri.buildUpon().appendQueryParameter("start", currentDay).build();
            uri = uri.buildUpon().appendQueryParameter("end", currentDay).build();
            uri = uri.buildUpon().appendQueryParameter("activity", "" + 0).build();
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() == 0) {
                    start = UtilsCalendar.getStartDay(dataToAnalize);
                    end = UtilsCalendar.getEndDay(dataToAnalize);
                    for (int activty = 0; activty < ActiveMilesGUI.NumberOfActivities; activty++) {
                        uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.SUM_PERFORMANCE);
                        uri = uri.buildUpon().appendQueryParameter("start", start).build();
                        uri = uri.buildUpon().appendQueryParameter("end", end).build();
                        uri = uri.buildUpon().appendQueryParameter("activity", "" + activty).build();
                        cursor2 = getContentResolver().query(uri, null, null, null, null);
                        int PerformInDay = 0;
                        if (cursor2 != null) {
                            if (cursor2.moveToFirst()) {
                                PerformInDay = cursor2.getInt(0);
                            }
                            cursor2.close();
                        }
                        uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_DAY);
                        values = new ContentValues();
                        values.put(PerformanceDayTable.COLUMN_PERFORMANCE, PerformInDay);
                        values.put(PerformanceDayTable.COLUMN_DAY, currentDay);
                        values.put(PerformanceDayTable.COLUMN_ACTIVITY, activty);
                        getContentResolver().insert(uri, values);
                    }
                }
                cursor.close();
            }
        }

    }

    @Override
    public void onActivityDetected(int activity, int performance, int step) {


        curr_performanceActivities[activity] += performance;
        numberOfStep += step;
        updatePerformanceInDb(activity);
        updateStepInDb(numberOfStep);
        checkIfToSave();


    }

    @Override
    public void onDestroy() {
        curr_ActivityDetector.closeTorch();
        unregisterDetector();
        curr_location_Tracker.stopUsingLocation();
        if (timer_ifToSaveDay != null)
            timer_ifToSaveDay.cancel();
        this.stopSelf();
        closeFile();
        super.onDestroy();
    }

    private void insertPerformanceInDb(int activity) {
        ContentValues values = new ContentValues();
        values.put(PerformanceHourTable.COLUMN_PERFORMANCE, curr_performanceActivities[activity]);
        values.put(PerformanceHourTable.COLUMN_HOUR, currentHour);
        values.put(PerformanceHourTable.COLUMN_ACTIVITY, activity);
        Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_HOUR);
        getContentResolver().insert(uri, values);

    }

    private void insertStepInDb(int step) {
        ContentValues values = new ContentValues();
        values.put(StepTable.COLUMN_STEP, step);
        values.put(StepTable.COLUMN_HOUR, currentHour);
        Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_STEP);
        getContentResolver().insert(uri, values);
    }

    private void updatePerformanceInDb(int activity) {
        ContentValues values = new ContentValues();
        values.put(PerformanceHourTable.COLUMN_PERFORMANCE, curr_performanceActivities[activity]);
        values.put(PerformanceHourTable.COLUMN_HOUR, currentHour);
        values.put(PerformanceHourTable.COLUMN_ACTIVITY, activity);
        Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_HOUR);
        getContentResolver().update(uri, values, PerformanceHourTable.COLUMN_HOUR + " = ? AND " + PerformanceHourTable.COLUMN_ACTIVITY + " = ?", new String[]{currentHour, "" + activity});
    }

    private void updateStepInDb(int step) {
        ContentValues values = new ContentValues();
        values.put(StepTable.COLUMN_STEP, step);
        values.put(StepTable.COLUMN_HOUR, currentHour);
        Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_STEP);
        getContentResolver().update(uri, values, StepTable.COLUMN_HOUR + " = ?", new String[]{currentHour});
    }

    private int loadPerformance(int activity) {
        String start;
        Uri uri;
        start = UtilsCalendar.currTimeToStringHour();

        uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_HOUR);
        uri = uri.buildUpon().appendQueryParameter("start", start).build();
        uri = uri.buildUpon().appendQueryParameter("end", start).build();
        uri = uri.buildUpon().appendQueryParameter("activity", String.valueOf(activity)).build();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        int performInHour = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                performInHour = cursor.getInt(1);
            }
            cursor.close();
        }
        return performInHour;
    }

    private int loadStep() {
        Uri uri;
        String start = UtilsCalendar.currTimeToStringHour();
        uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_STEP);
        uri = uri.buildUpon().appendQueryParameter("start", start).build();
        uri = uri.buildUpon().appendQueryParameter("end", start).build();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        int numbeOfStep = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                numbeOfStep = cursor.getInt(0);
            }
            cursor.close();
        }
        return numbeOfStep;

    }

    private void checkIfToSave() {
        if (!currentHour.equals(UtilsCalendar.currTimeToStringHour())) {
            currentHour = UtilsCalendar.currTimeToStringHour();
            for (int activty = 0; activty < ActiveMilesGUI.NumberOfActivities; activty++) {
                curr_performanceActivities[activty] = 0;
                insertPerformanceInDb(activty);
            }
            numberOfStep = 0;
            insertStepInDb(0);
            insertNewLocationInDb(this.curr_location_Tracker.getCurrentLocation());
        }
    }

    private void insertNewLocationInDb(Location currLocation) {
        if (currLocation != null && currLocation.getAccuracy() <= GPSTable.PRECISION_TRESHOLD) {
            String timeStemp = UtilsCalendar.currTimeToStringSec();
            ContentValues values = new ContentValues();
            values.put(GPSTable.COLUMN_ALT, currLocation.getAltitude());
            values.put(GPSTable.COLUMN_LAT, currLocation.getLatitude());
            values.put(GPSTable.COLUMN_LNG, currLocation.getLongitude());
            values.put(GPSTable.PRECISION, currLocation.getAccuracy());
            values.put(GPSTable.COLUMN_TIME_STEMP, timeStemp);
            values.put(GPSTable.TYPE, currLocation.getProvider());
            if (currLocation.hasSpeed()) {
                values.put(GPSTable.SPEED, currLocation.getSpeed());
            } else
                values.put(GPSTable.SPEED, -1);
            Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_GPS);
            getContentResolver().insert(uri, values);
        }

    }

    @Override
    public void onLocationChange(Location currLocation) {
        insertNewLocationInDb(currLocation);
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        wl.release();
        /*if (RESTART==1) {
            System.out.println("service in onTaskRemoved");
            long ct = System.currentTimeMillis(); //get current time
            Intent restartService = new Intent(getApplicationContext(),
                    SensorDataServiceInertial.class);
            PendingIntent restartServicePI = PendingIntent.getService(
                    getApplicationContext(), 0, restartService,
                    0);

            AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            mgr.setRepeating(AlarmManager.RTC_WAKEUP, ct, 1 * 1000, restartServicePI);
        }*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent MainIntent = new Intent(this, ActiveMilesGUI.class);
        MainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, MainIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.footicon);
        Notification notification = new NotificationCompat.Builder(this).setContentTitle("Active Miles Service").setTicker("Starting Active Miles Service").setContentText("My Activity Tracker")
                .setSmallIcon(R.drawable.notification).setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false)).setContentIntent(pendIntent).setOngoing(true).build();
        startForeground(myID, notification);
        return START_STICKY;
    }

    // /FOR LABELING
    long LastTimeStemp = 0;
    public boolean record = false;
    public String label = "";
    public int idLabel = 0;
    BufferedWriter writer;
    final int sizeSegment = 5000;
    public static final String filePath = Environment.getExternalStorageDirectory().getPath() + "/mysdfile.txt";
    public boolean dbNeedtoBeremoveed = false;

    public SensorDataServiceBase() {
        LastTimeStemp = SystemClock.elapsedRealtime();
        try {
            writer = new BufferedWriter(new FileWriter(filePath, true));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void ActivityClasify() {

        try {
            curr_ActivityDetector.setWriter(writer);
            Toast.makeText(getApplicationContext(), "Recording", Toast.LENGTH_SHORT).show();
             if (curr_location_Tracker!=null && curr_location_Tracker.getCurrentLocation() != null) {
                 writer.write("GPS>" + curr_location_Tracker.getCurrentLocation().getAltitude() + "," + curr_location_Tracker.getCurrentLocation().getLatitude() + "," + curr_location_Tracker.getCurrentLocation().getLongitude());
                 writer.newLine();
             }
            writer.write("L>" + label);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void NewSection() {
        try {
            writer.write("N>New Section");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void RemoveSection() {
        try {
            writer.write("R>Remove Section");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void RemoveAll() {
        try {
            writer.flush();
            writer.close();
            File file = new File(filePath);
            file.delete();
            writer = new BufferedWriter(new FileWriter(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeFile() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
